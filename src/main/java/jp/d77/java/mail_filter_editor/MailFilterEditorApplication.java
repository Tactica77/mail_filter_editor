package jp.d77.java.mail_filter_editor;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MailFilterEditorApplication {
	private static String[] staticArgs;

	public static void main(String[] args) {
		staticArgs = args;
		SpringApplication.run(MailFilterEditorApplication.class, args);
	}

	public static Optional<String> getFilePath(){
		if ( MailFilterEditorApplication.staticArgs == null ) return Optional.empty();
		if ( MailFilterEditorApplication.staticArgs.length <= 0 ) return Optional.empty();
		return Optional.ofNullable( MailFilterEditorApplication.staticArgs[0] );
	}
}
