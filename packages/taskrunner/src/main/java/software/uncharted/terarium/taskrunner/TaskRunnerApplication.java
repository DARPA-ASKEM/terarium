package software.uncharted.terarium.taskrunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class TaskRunnerApplication {

	public static void main(final String[] args) {
		SpringApplication.run(TaskRunnerApplication.class, args);
	}
}
