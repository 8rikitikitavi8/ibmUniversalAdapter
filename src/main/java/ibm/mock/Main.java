package ibm.mock;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Main {
    public static InfluxDB influxDB = InfluxDBFactory.connect("http://vs2645:8086");

    public static void main(String[] args) {
        influxDB.setDatabase("MOCKS");
        influxDB.enableBatch(2000, 1000, TimeUnit.MILLISECONDS);
        SpringApplication.run(Main.class, args);
    }

    public static void writeToInflux(String operation, Long time) {
        influxDB.write(Point.measurement("MCSP_ibm_mock")
                .time(System.currentTimeMillis(),TimeUnit.MILLISECONDS)
                .tag("name",operation)
                .addField("ResponseTime",time)
                .build());
    }

}
