package org.example.aiassistantservice.context;

import java.util.UUID;

public final class UserContext {

    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(UUID userId) {
        CURRENT_USER.set(userId);
    }

    public static UUID getUserId() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
