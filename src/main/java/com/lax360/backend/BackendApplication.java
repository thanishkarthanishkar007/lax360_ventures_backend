package com.lax360.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Loads a local ".env" file (project root, next to pom.xml) into System
     * properties — but ONLY for keys that aren't already set as a real OS
     * environment variable or System property.
     * <p>
     * This is what makes `mvn spring-boot:run` pick up MONGODB_URI,
     * RESEND_API_KEY, etc. automatically on Windows (PowerShell/cmd),
     * macOS, and Linux, with no manual "export"/"$env:" step. In
     * production (Render), the real environment variables set in the
     * dashboard are already present, so this method finds nothing left to
     * do and is effectively a no-op there.
     */
    private static void loadDotEnvIfPresent() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq < 0) {
                    continue;
                }
                String key = trimmed.substring(0, eq).trim();
                String value = trimmed.substring(eq + 1).trim();
                value = stripSurroundingQuotes(value);

                boolean alreadySet = System.getenv(key) != null || System.getProperty(key) != null;
                if (!alreadySet && !key.isEmpty()) {
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read .env file (" + e.getMessage() + "). "
                    + "Falling back to real environment variables only.");
        }
    }

    private static String stripSurroundingQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
