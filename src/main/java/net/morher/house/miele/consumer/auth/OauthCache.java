package net.morher.house.miele.consumer.auth;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OauthCache {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new JavaTimeModule());
    private final File cacheFile;

    public OauthCache(String cacheFile) {
        this.cacheFile = new File(cacheFile);
    }

    private CacheFile loadCache() {
        if (cacheFile.exists()) {

            try {
                CacheFile cache = OBJECT_MAPPER.readValue(cacheFile, CacheFile.class);
                if (cache != null && cache.getTokenCache() != null) {
                    return cache;
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load cache file " + cacheFile, e);
            }
        }
        return new CacheFile();
    }

    public OauthToken getToken(String user) {
        OauthToken token = loadCache().getTokenCache().get(user);
        if (token != null) {
            log.debug("Found token for {} in cache.", user);
        }
        return token;
    }

    public void storeToken(String user, OauthToken token) {
        CacheFile cache = loadCache();
        cache.getTokenCache().put(user, token);
        try {
            OBJECT_MAPPER.writeValue(cacheFile, cache);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write cache file", e);
        }
    }

    @Data
    @JsonInclude(Include.NON_EMPTY)
    public static class CacheFile {
        private final Map<String, OauthToken> tokenCache = new HashMap<>();
    }
}
