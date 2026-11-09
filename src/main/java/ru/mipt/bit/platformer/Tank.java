package ru.mipt.bit.platformer;

import java.util.Objects;

public final class Tank implements CellOccupant, Updatable, Damageable, HealthStatus {
    private final float moveDurationSeconds;
    private final CombatSettings combat;
    private int health;
    private float reloadRemaining;
    private static final float PROGRESS_TOLERANCE = 0.000001f;

    private int x;
    private int y;
    private int movementStartX;
    private int movementStartY;
    private Direction direction = Direction.RIGHT;
    private float movementProgress = 1f;

    public Tank(int x, int y, float moveDurationSeconds) {
        this(x, y, moveDurationSeconds, CombatSettings.DEFAULT);
    }

    public Tank(int x, int y, float moveDurationSeconds, CombatSettings combat) {
        this.combat = Objects.requireNonNull(combat);
        health = combat.getHealth();
        if (!Float.isFinite(moveDurationSeconds) || moveDurationSeconds <= 0f) {
            throw new IllegalArgumentException("Move duration must be finite and positive");
        }
        this.moveDurationSeconds = moveDurationSeconds;
        this.x = x;
        this.y = y;
        movementStartX = x;
        movementStartY = y;
    }

    public boolean tryMove(Direction direction, GameField field) {
        if (!isAlive() || isMoving() || !field.contains(this)) {
            return false;
        }
        this.direction = Objects.requireNonNull(direction);
        int targetX = x + direction.getDeltaX();
        int targetY = y + direction.getDeltaY();

        if (!field.isInside(targetX, targetY) || field.tileOccupied(targetX, targetY)) {
            return false;
        }
        movementStartX = x;
        movementStartY = y;
        x = targetX;
        y = targetY;
        movementProgress = 0f;
        return true;
    }

    public boolean tryShoot(GameWorld world) {
        Objects.requireNonNull(world);
        if (!isAlive() || isMoving() || reloadRemaining > 0f || !world.containsObject(this)) { return false; }
        reloadRemaining = combat.getReloadSeconds();
        Bullet bullet = new Bullet(x + direction.getDeltaX(), y + direction.getDeltaY(),
                direction, combat.getBulletSpeed(), combat.getDamage(), this, world);
        world.addObject(bullet);
        // This also resolves adjacent targets and shots directed outside the map.
        bullet.checkCollision();
        return true;
    }

    @Override public void takeDamage(int damage) {
        if (damage <= 0) { throw new IllegalArgumentException("Damage must be positive"); }
        health = Math.max(0, health - damage);
    }

    @Override public boolean isAlive() { return health > 0; }
    @Override public int getHealth() { return health; }
    @Override public int getMaxHealth() { return combat.getHealth(); }
    public float getReloadRemaining() { return reloadRemaining; }

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
        if (!isAlive()) { return; }
        reloadRemaining = Math.max(0f, reloadRemaining - deltaTime);
        if (reloadRemaining <= PROGRESS_TOLERANCE) { reloadRemaining = 0f; }
        movementProgress = Math.max(0f, Math.min(1f,
                movementProgress + deltaTime / moveDurationSeconds));
        if (Math.abs(1f - movementProgress) <= PROGRESS_TOLERANCE) {
            movementProgress = 1f;
            movementStartX = x;
            movementStartY = y;
        }
    }

    public boolean isMoving() {
        return movementProgress < 1f;
    }

    @Override
    public boolean tileOccupied(int x, int y) {
        // The source stays occupied until the physical step has completed.
        return isAlive() && ((this.x == x && this.y == y)
                || (isMoving() && movementStartX == x && movementStartY == y));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMovementStartX() {
        return movementStartX;
    }

    public int getMovementStartY() {
        return movementStartY;
    }

    public Direction getDirection() {
        return direction;
    }

    public float getMovementProgress() {
        return movementProgress;
    }
}
