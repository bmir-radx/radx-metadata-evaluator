package bmir.radx.metadata.evaluator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

@SpringBootApplication(scanBasePackages = "bmir.radx.metadata.evaluator")
public class Application implements CommandLineRunner {
	private final CommandLine.IFactory iFactory;
	@Autowired
	private ApplicationContext applicationContext;
	private int exitCode;

	public Application(CommandLine.IFactory iFactory) {
		this.iFactory = iFactory;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		var command = applicationContext.getBean(EvaluateCommand.class);
		exitCode = new CommandLine(command, iFactory).execute(args);
	}
}
