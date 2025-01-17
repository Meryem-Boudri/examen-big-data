package ma.enset.exercice2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class Exercice2Application {

    public static void main(String[] args) {
        SpringApplication.run(Exercice2Application.class, args);
    }

}
