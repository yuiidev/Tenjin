package net.demozo.tenjin;

import java.time.Instant;
import java.time.LocalDateTime;

public interface HasTimestamps {
    Instant getCreatedAt();
    Instant getUpdatedAt();
    void create();
    void update();
}
