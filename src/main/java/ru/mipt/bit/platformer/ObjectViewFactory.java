package ru.mipt.bit.platformer;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Model-to-view bindings are configured when assembling the scene. */
public final class ObjectViewFactory {
    private final Map<Class<?>, Function<CellOccupant, ObjectView>> factories = new HashMap<>();

    public <T extends CellOccupant> void register(Class<T> type, Function<T, ObjectView> factory) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(factory);
        if (factories.containsKey(type)) { throw new IllegalArgumentException("View factory already registered for " + type); }
        factories.put(type, object -> factory.apply(type.cast(object)));
    }

    public ObjectView create(CellOccupant object) {
        Objects.requireNonNull(object);
        Function<CellOccupant, ObjectView> factory = factories.get(object.getClass());
        if (factory == null) { throw new IllegalArgumentException("No view registered for " + object.getClass()); }
        return Objects.requireNonNull(factory.apply(object));
    }
}
