package org.example.vgtuaventory.service;

import org.example.vgtuaventory.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final ConcurrentHashMap<String, Integer> activeTokens = new ConcurrentHashMap<>();

    public String generateToken(User user){
        String token = UUID.randomUUID().toString();
        activeTokens.put(token, user.getId());
        return token;
    }

    public boolean isTokenValid(String token) {
        return token != null && activeTokens.containsKey(token);
    }

    public void revokeToken(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }
}
