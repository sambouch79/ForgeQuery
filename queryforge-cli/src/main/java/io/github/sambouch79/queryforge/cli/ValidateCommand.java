package io.github.sambouch79.queryforge.cli;


import io.github.sambouch79.queryforge.loader.MappingLoader;
import io.github.sambouch79.queryforge.validator.ValidationResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ExitCode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Command(
        name = "validate",
        description = "Validate one or multiple JSON mapping files",
        mixinStandardHelpOptions = true
)
public class ValidateCommand implements Runnable {

    @Option(
            names = {"-f", "--file"},
            description = "Path to a single JSON mapping file"
    )
    private Path file;

    @Option(
            names = {"-d", "--dir"},
            description = "Path to a directory containing JSON mapping files"
    )
    private Path dir;

    @Override
    public void run() {
        if (file == null && dir == null) {
            System.err.println("[KO]  Please specify --file or --dir");
            System.exit(ExitCode.USAGE);
        }

        if (file != null) {
            validateSingle(file);
        } else {
            validateDirectory(dir);
        }
    }

    private void validateSingle(Path path) {
        if (!Files.exists(path)) {
            System.err.println("[KO] File not found: " + path);
            System.exit(ExitCode.USAGE);
        }

        MappingLoader loader = new MappingLoader();
        ValidationResult result = loader.validate(path);

        if (result.isValid()) {
            System.out.println("[OK] " + path.getFileName() + " — VALID");
        } else {
            System.err.println("[KO] " + path.getFileName() + " — INVALID");
            System.err.println(result.getErrorMessage());
            System.exit(ExitCode.SOFTWARE);
        }
    }

    private void validateDirectory(Path directory) {
        if (!Files.isDirectory(directory)) {
            System.err.println("[KO] Not a directory: " + directory);
            System.exit(ExitCode.USAGE);
        }

        MappingLoader loader = new MappingLoader();
        int total = 0, valid = 0, invalid = 0;

        try (Stream<Path> files = Files.list(directory)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted()) {

            List<Path> jsonFiles = files.toList();

            if (jsonFiles.isEmpty()) {
                System.out.println(" No JSON files found in: " + directory);
                return;
            }

            System.out.println(" Validating " + jsonFiles.size() + " file(s) in: " + directory);
            System.out.println();

            for (Path jsonFile : jsonFiles) {
                total++;
                ValidationResult result = loader.validate(jsonFile);
                if (result.isValid()) {
                    System.out.println("  [OK]  " + jsonFile.getFileName());
                    valid++;
                } else {
                    System.err.println("  [KO] " + jsonFile.getFileName());
                    System.err.println("     " + result.getErrorMessage());
                    invalid++;
                }
            }

        } catch (Exception e) {
            System.err.println("[KO]  Error reading directory: " + e.getMessage());
            System.exit(ExitCode.SOFTWARE);
        }

        // Rapport final
        System.out.println();
        System.out.println("─".repeat(40));
        System.out.printf("  Total: %d | [OK]  Valid: %d | [KO]  Invalid: %d%n", total, valid, invalid);

        if (invalid > 0) {
            System.exit(ExitCode.SOFTWARE);
        }
    }
}
