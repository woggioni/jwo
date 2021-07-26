package net.woggioni.jwo.benchmark;

import lombok.SneakyThrows;
import net.woggioni.jwo.Chronometer;
import net.woggioni.jwo.CollectionUtils;
import net.woggioni.jwo.JWO;
import net.woggioni.jwo.CircularInputStream;
import net.woggioni.jwo.Tuple2;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Main {

    @SneakyThrows
    private static void withBenchmarkReader(Consumer<Reader> fn) {
        Reader reader = new InputStreamReader(JWO.class.getResourceAsStream("/render_template_test.txt"));
        InputStream is = new CircularInputStream(JWO.readAll(reader).getBytes(), 10000);
        try {
            fn.accept(new InputStreamReader(is));
        } finally {
            if (reader != null) reader.close();
        }
    }

    public static void main(String... args)  {
        Map<String, Object> valuesMap = Stream.of(
                new Tuple2<>("author", "John Doe"),
                new Tuple2<>("date", "2020-03-25 16:22"),
                new Tuple2<>("adjective", "simple"))
            .collect(CollectionUtils.toUnmodifiableTreeMap(it -> it._1, it -> it._2));
        withBenchmarkReader(
                reader -> {
                Chronometer chronometer = new Chronometer();
                String result = JWO.renderTemplate(reader, valuesMap);
                System.out.printf("Elapsed time: %.3f\n", chronometer.elapsed(Chronometer.UnitOfMeasure.SECONDS));
        });

        withBenchmarkReader(
                reader -> {
                Chronometer chronometer = new Chronometer();
                String result = NaiveTemplateRenderer.renderTemplateNaive(JWO.readAll(reader), valuesMap);
                System.out.printf("Elapsed time: %.3f\n", chronometer.elapsed(Chronometer.UnitOfMeasure.SECONDS));
        });
    }
}
