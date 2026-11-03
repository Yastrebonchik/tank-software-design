package ru.mipt.bit.platformer;

/** Immutable combat parameters, shared by tanks with the same weapon. */
public final class CombatSettings {
    public static final CombatSettings DEFAULT = new CombatSettings(100, 25, 0.5f, 5f);
    private final int health;
    private final int damage;
    private final float reloadSeconds;
    private final float bulletSpeed;

    public CombatSettings(int health, int damage, float reloadSeconds, float bulletSpeed) {
        if (health <= 0 || damage <= 0 || !Float.isFinite(reloadSeconds) || reloadSeconds <= 0f
                || !Float.isFinite(bulletSpeed) || bulletSpeed <= 0f) {
            throw new IllegalArgumentException("Health, damage, reload time and bullet speed must be positive and finite");
        }
        this.health = health;
        this.damage = damage;
        this.reloadSeconds = reloadSeconds;
        this.bulletSpeed = bulletSpeed;
    }

    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public float getReloadSeconds() { return reloadSeconds; }
    public float getBulletSpeed() { return bulletSpeed; }
}
