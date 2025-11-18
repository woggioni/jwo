package net.woggioni.jwo;

import lombok.SneakyThrows;

public class Chronometer {

    public enum UnitOfMeasure {
        NANOSECONDS(1),
        MICROSECONDS(NANOSECONDS.nanoseconds * 1000),
        MILLISECONDS(MICROSECONDS.nanoseconds * 1000),
        SECONDS(MILLISECONDS.nanoseconds * 1000),
        MINUTES(SECONDS.nanoseconds * 60),
        HOURS(MINUTES.nanoseconds * 60),
        DAYS(HOURS.nanoseconds * 24),
        WEEKS(DAYS.nanoseconds * 7),
        MONTHS(DAYS.nanoseconds * 30),
        YEARS(DAYS.nanoseconds * 365);

        public final long nanoseconds;

        UnitOfMeasure(final long nanoseconds) {
            this.nanoseconds = nanoseconds;
        }
    }

    private long start;

    @SneakyThrows
    public Chronometer() {
        start = System.nanoTime();
    }

    public void reset() {
        start = System.nanoTime();
    }

    public long elapsed() {
        return System.nanoTime() - start;
    }

    public double elapsed(final UnitOfMeasure unitOfMeasure) {
        return ((double) (System.nanoTime() - start)) / unitOfMeasure.nanoseconds;
    }
}
