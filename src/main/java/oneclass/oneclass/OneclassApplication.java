package oneclass.oneclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "oneclass.oneclass")
@EnableAsync(proxyTargetClass = true)
@EnableScheduling
public class OneclassApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneclassApplication.class, args);
    }

}
