package net.woggioni.jwo;

public record TestCase<T, U>(T input, U expectedOutput, Class<? extends Throwable> error) {
}