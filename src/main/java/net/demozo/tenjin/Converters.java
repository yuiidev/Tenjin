package net.demozo.tenjin;

import net.demozo.tenjin.converter.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class Converters extends HashMap<Class<?>, Converter<?>> {

    public Converters() {
        this.put(Integer.class, new IntegerConverter());
        this.put(int.class, new IntegerConverter());

        this.put(Float.class, new FloatConverter());
        this.put(float.class, new FloatConverter());

        this.put(Boolean.class, new BooleanConverter());
        this.put(boolean.class, new BooleanConverter());

        this.put(String.class, new StringConverter());
        this.put(UUID.class, new UUIDConverter());
        this.put(Instant.class, new InstantConverter());
        this.put(Object.class, new ObjectConverter());
    }

    @Override
    public Converter<?> get(Object key) {
        var converter = super.get(key);

        if(converter == null) {
            Tenjin.info("Retrieved converter for class {} is null", ((Class<?>) key).getName());
        }

        return converter;
    }
}
