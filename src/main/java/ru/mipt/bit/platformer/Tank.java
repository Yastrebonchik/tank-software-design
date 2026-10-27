package ru.mipt.bit.platformer;

import java.util.Objects;

public final class Tank implements CellOccupant, Updatable {
    private final float moveDurationSeconds;
    private static final float PROGRESS_TOLERANCE = 0.000001f;

    private int x;
    private int y;
    private int movementStartX;
    private int movementStartY;
    private Direction direction = Direction.RIGHT;
    private float movementProgress = 1f;

    public Tank(int x, int y, float moveDurationSeconds) {
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
        if (isMoving()) {
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

    @Override
    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
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
        return (this.x == x && this.y == y)
                || (isMoving() && movementStartX == x && movementStartY == y);
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
