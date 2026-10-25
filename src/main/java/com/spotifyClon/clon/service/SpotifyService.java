
package com.spotifyClon.clon.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final RestClient restClient;

    private String accessToken;
    private Instant expiresAt = Instant.MIN;

    public SpotifyService(RestClient restClient) {
        this.restClient = restClient;
    }

    private synchronized String getAccessToken() {

        if (accessToken != null &&
                Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            return accessToken;
        }

        String credentials = clientId + ":" + clientSecret;

        String encodedCredentials = Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "client_credentials");

        Map<?, ?> response = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        "Authorization",
                        "Basic " + encodedCredentials
                )
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null ||
                response.get("access_token") == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el token de Spotify"
            );
        }

        accessToken = response.get("access_token").toString();

        Number expiresIn = (Number) response.get("expires_in");

        expiresAt = Instant.now()
                .plusSeconds(expiresIn.longValue());

        return accessToken;
    }

    public Map<?, ?> search(String query, String type) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.spotify.com")
                        .path("/v1/search")
                        .queryParam("q", query)
                        .queryParam("type", type)
                        .queryParam("limit", 10)
                        .queryParam("market", "MX")
                        .build())
                .header(
                        "Authorization",
                        "Bearer " + getAccessToken()
                )
                .retrieve()
                .body(Map.class);
    }
}
