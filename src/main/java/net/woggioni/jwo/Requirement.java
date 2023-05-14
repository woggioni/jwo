package net.woggioni.jwo;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.function.Supplier;

import static net.woggioni.jwo.JWO.newThrowable;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Requirement {
    private final Supplier<Boolean> booleanSupplier;

    public static Requirement require(Supplier<Boolean> booleanSupplier) {
        return new Requirement(booleanSupplier);
    }

    @SneakyThrows
    public <T extends Throwable> void otherwise(Class<T> cls, String format, Object... args) {
        if(!booleanSupplier.get()) {
            throw newThrowable(cls, format, args);
        }
    }
}
