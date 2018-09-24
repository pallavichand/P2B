package infrrd.p2b;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class P2BApplication {

	public static final String ROOT = "upload-dir";
	public static final String DOCUMENTS_DIRECTORY = "upload-dir" + File.separator + "documents";

	public static void main(String[] args) {
		SpringApplication.run(P2BApplication.class, args);

	}

	@Bean
	CommandLineRunner init() {
		return (String[] args) -> {
			new File(P2BApplication.ROOT).mkdir();
			new File(P2BApplication.DOCUMENTS_DIRECTORY).mkdir();
		};
	}

}
