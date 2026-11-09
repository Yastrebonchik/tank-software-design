package ru.mipt.bit.platformer;

/** Read-only health information. Maximum health must be positive. */
public interface HealthStatus {
    int getHealth();
    int getMaxHealth();
}
