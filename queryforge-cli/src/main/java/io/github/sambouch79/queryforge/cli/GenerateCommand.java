package io.github.sambouch79.queryforge.cli;


import io.github.sambouch79.queryforge.generator.SQLGenerator;
import io.github.sambouch79.queryforge.loader.MappingLoader;
import io.github.sambouch79.queryforge.model.Mapping;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ExitCode;

import java.nio.file.Files;
import java.nio.file.Path;

@Command(
        name = "generate",
        description = "Generate SQL from a JSON mapping file",
        mixinStandardHelpOptions = true
)
public class GenerateCommand implements Runnable {

    @Option(
            names = {"-f", "--file"},
            description = "Path to the JSON mapping file",
            required = true
    )
    private Path file;

    @Option(
            names = {"-o", "--output"},
            description = "Output file path (default: stdout)"
    )
    private Path output;

    @Option(
            names = {"--no-validate"},
            description = "Skip validation before generation",
            defaultValue = "false"
    )
    private boolean skipValidation;

    @Override
    public void run() {
        try {
            // 1. Vérifier que le fichier existe
            if (!Files.exists(file)) {
                System.err.println("[KO] File not found: " + file);
                System.exit(ExitCode.USAGE);
            }

            // 2. Valider si demandé (par défaut oui)
            if (!skipValidation) {
                MappingLoader loader = new MappingLoader();
                var validationResult = loader.validate(file);
                if (!validationResult.isValid()) {
                    System.err.println("[KO] Validation failed:");
                    System.err.println(validationResult.getErrorMessage());
                    System.exit(ExitCode.SOFTWARE);
                }
            }

            // 3. Charger et générer
            MappingLoader loader = new MappingLoader();
            Mapping mapping = loader.loadFromFile(file);

            SQLGenerator generator = new SQLGenerator();
            String sql = generator.generate(mapping);

            // 4. Output
            if (output != null) {
                Files.writeString(output, sql);
                System.out.println("[OK] SQL written to: " + output);
            } else {
                System.out.println(sql);
            }

        } catch (Exception e) {
            System.err.println("[KO] Error: " + e.getMessage());
            System.exit(ExitCode.SOFTWARE);
        }
    }
}