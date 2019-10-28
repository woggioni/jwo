package net.woggioni.jwo;

import lombok.SneakyThrows;

public class Chronometer {

    public enum UnitOfMeasure {
        NANOSECONDS(1),
        MICROSECONDS(NANOSECONDS.nanoseconds_size * 1000),
        MILLISECONDS(MICROSECONDS.nanoseconds_size * 1000),
        SECONDS(MILLISECONDS.nanoseconds_size * 1000),
        MINUTES(SECONDS.nanoseconds_size * 60),
        HOURS(MINUTES.nanoseconds_size * 60),
        DAYS(HOURS.nanoseconds_size * 24),
        WEEKS(DAYS.nanoseconds_size * 7),
        MONTHS(DAYS.nanoseconds_size * 30),
        YEARS(DAYS.nanoseconds_size * 365);

        public long nanoseconds_size;

        UnitOfMeasure(long nanoseconds_size) {
            this.nanoseconds_size = nanoseconds_size;
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

    public double elapsed(UnitOfMeasure unitOfMeasure) {
        return ((double) (System.nanoTime() - start)) / unitOfMeasure.nanoseconds_size;
    }
}
