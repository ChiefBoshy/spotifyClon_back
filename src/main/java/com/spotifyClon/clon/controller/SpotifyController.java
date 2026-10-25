package com.spotifyClon.clon.controller;

import com.spotifyClon.clon.service.SpotifyService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@RestController
@RequestMapping("/api/spotify")
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search")
    public Map<?, ?> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "track,artist,album")
            String type) {

        if (q.isBlank()) {
            throw new IllegalArgumentException(
                    "El término de búsqueda no puede estar vacío"
            );
        }

        Set<String> allowedTypes = Set.of(
                "track", "artist", "album", "playlist"
        );

        for (String item : type.split(",")) {
            if (!allowedTypes.contains(item.trim())) {
                throw new IllegalArgumentException(
                        "Tipo de búsqueda no válido: " + item
                );
            }
        }

        return spotifyService.search(q, type);
    }


    @GetMapping("/home")
    public Map<String, Object> home() {
        Map<String, Object> result = new HashMap<>();

        result.put("rock", spotifyService.search("rock", "track"));
        result.put("pop", spotifyService.search("pop", "track"));
        result.put("indie", spotifyService.search("indie", "track"));

        return result;
    }
}