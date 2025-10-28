package oneclass.oneclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class OneclassApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneclassApplication.class, args);
    }

}
