package net.woggioni.jwo.benchmark

import java.io.{InputStream, InputStreamReader, Reader}

import net.woggioni.jwo.io.CircularInputStream
import net.woggioni.jwo.{Chronometer, JWO}

import scala.collection.JavaConverters._

object Main {

    def withBenchmarkReader(fn : Reader => Unit) = {
        val reader = new InputStreamReader(classOf[JWO].getResourceAsStream("/render_template_test.txt"))
        val is : InputStream = new CircularInputStream(JWO.readAll(reader).getBytes, 10000)
        try {
            fn(new InputStreamReader(is))
        } finally {
            if (reader != null) reader.close()
        }
    }

    def renderTemplateBenchmark() {
        val valuesMap = Map[String, Object]("author" -> "John Doe",
                            "date" -> "2020-03-25 16:22",
                            "adjective" -> "simple")
        withBenchmarkReader(
            reader => {
                val chronometer = new Chronometer
                val result = JWO.renderTemplate(reader, valuesMap.asJava)
                printf("Elapsed time: %.3f\n", chronometer.elapsed(Chronometer.UnitOfMeasure.SECONDS))
            }
        )

        withBenchmarkReader(
            reader => {
                val chronometer = new Chronometer
                val result = NaiveTemplateRenderer.renderTemplateNaive(JWO.readAll(reader), valuesMap.asJava)
                printf("Elapsed time: %.3f\n", chronometer.elapsed(Chronometer.UnitOfMeasure.SECONDS))
            }
        )
    }

    def main(argv : Array[String]) = {
        renderTemplateBenchmark()
    }

}
