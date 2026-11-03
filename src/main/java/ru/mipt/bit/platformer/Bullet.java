package ru.mipt.bit.platformer;

import java.util.Objects;

/** A projectile traverses every crossed cell, even during a long frame. */
public final class Bullet implements CellOccupant, Updatable, Damageable {
    private int x;
    private int y;
    private double progress;
    private final Direction direction;
    private final float speed;
    private final int damage;
    private final CellOccupant owner;
    private final GameWorld world;
    private boolean alive = true;

    public Bullet(int x, int y, Direction direction, float speed, int damage,
                  CellOccupant owner, GameWorld world) {
        if (!Float.isFinite(speed) || speed <= 0f || damage <= 0) {
            throw new IllegalArgumentException("Bullet speed and damage must be positive and finite");
        }
        this.x = x;
        this.y = y;
        this.direction = Objects.requireNonNull(direction);
        this.speed = speed;
        this.damage = damage;
        this.owner = Objects.requireNonNull(owner);
        this.world = Objects.requireNonNull(world);
    }

    @Override public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
        if (!alive || !world.containsObject(this)) { return; }
        checkCollision();
        if (!alive) { return; }
        progress += (double) deltaTime * speed;
        while (progress >= 1.0 && alive) {
            progress -= 1.0;
            x += direction.getDeltaX();
            y += direction.getDeltaY();
            checkCollision();
        }
    }

    void checkCollision() {
        if (!alive) { return; }
        if (!world.getField().isInside(x, y)) { destroy(); return; }
        for (CellOccupant target : world.getField().occupantsAt(x, y)) {
            if (target == this || target == owner) { continue; }
            if (target instanceof Damageable) {
                Damageable damageable = (Damageable) target;
                damageable.takeDamage(damage);
                if (!damageable.isAlive()) { world.removeObject(target); }
            }
            destroy();
            return;
        }
    }

    private void destroy() { alive = false; world.removeObject(this); }

    @Override public void takeDamage(int damage) {
        if (damage <= 0) { throw new IllegalArgumentException("Damage must be positive"); }
        alive = false;
    }

    @Override public boolean isAlive() { return alive; }
    @Override public boolean blocksMovement() { return false; }
    @Override public boolean tileOccupied(int x, int y) { return alive && this.x == x && this.y == y; }
    public int getX() { return x; }
    public int getY() { return y; }
    public float getDisplayX() { return x + direction.getDeltaX() * (float) progress; }
    public float getDisplayY() { return y + direction.getDeltaY() * (float) progress; }
    public Direction getDirection() { return direction; }
    public int getDamage() { return damage; }
}
