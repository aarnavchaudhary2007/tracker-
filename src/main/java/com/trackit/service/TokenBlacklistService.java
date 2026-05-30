package com.trackit.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Thread-safe set to store blacklisted JWT tokens
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null && !token.isBlank()) {
            blacklist.add(token.trim());
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return blacklist.contains(token.trim());
    }
}
