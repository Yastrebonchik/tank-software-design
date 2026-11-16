package ru.mipt.bit.platformer;

/**
 * Read-only domain contract consumed by presentation; not a separate external-system port.
 * <p>Read-only health information. Maximum health must be positive.
 */
public interface HealthStatus {
    int getHealth();
    int getMaxHealth();
}
