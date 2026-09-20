package dev.noe.loomcrete.domain;

import java.time.Instant;
import java.util.UUID;

public record Tenant(
    String id,
    String name,
    String config
) {
    public Tenant {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Tenant id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be null or blank");
        }
    }

    public static Tenant create(String name, String config) {
        return new Tenant(UUID.randomUUID().toString(), name, config != null ? config : "");
    }
}
