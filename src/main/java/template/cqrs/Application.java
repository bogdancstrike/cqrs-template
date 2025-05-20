package template.cqrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Required for the scheduled batching in query side
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}