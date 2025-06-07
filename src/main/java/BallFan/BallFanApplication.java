package BallFan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "BallFan.repository") // JPA용
public class BallFanApplication {

	public static void main(String[] args) {
		SpringApplication.run(BallFanApplication.class, args);
	}

}
