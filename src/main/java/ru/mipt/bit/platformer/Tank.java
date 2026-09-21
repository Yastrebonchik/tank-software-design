package ru.mipt.bit.platformer;

import java.util.Objects;

public final class Tank implements CellOccupant {
    public enum MoveDirection {
        UP, DOWN, LEFT, RIGHT
    }

    private static final float MOVE_DURATION_SECONDS = 0.4f;
    private static final float PROGRESS_TOLERANCE = 0.000001f;

    private int x;
    private int y;
    private int movementStartX;
    private int movementStartY;
    private MoveDirection direction = MoveDirection.RIGHT;
    private float movementProgress = 1f;

    public Tank(int x, int y) {
        this.x = x;
        this.y = y;
        movementStartX = x;
        movementStartY = y;
    }

    public boolean tryMove(MoveDirection direction, GameField field) {
        if (isMoving()) {
            return false;
        }
        this.direction = Objects.requireNonNull(direction);
        int targetX = x;
        int targetY = y;
        switch (direction) {
            case UP:
                targetY++;
                break;
            case DOWN:
                targetY--;
                break;
            case LEFT:
                targetX--;
                break;
            case RIGHT:
                targetX++;
                break;
        }

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

    public void update(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0f) {
            throw new IllegalArgumentException("deltaTime must be finite and non-negative");
        }
        movementProgress = Math.max(0f, Math.min(1f,
                movementProgress + deltaTime / MOVE_DURATION_SECONDS));
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
        return this.x == x && this.y == y;
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

    public MoveDirection getDirection() {
        return direction;
    }

    public float getMovementProgress() {
        return movementProgress;
    }
}
