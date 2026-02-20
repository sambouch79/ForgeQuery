package io.github.sambouch79.queryforge.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * QueryForge CLI - Dynamic SQL Query Generator
 *
 * Usage:
 *   java -jar queryforge-cli.jar generate --file mapping.json
 *   java -jar queryforge-cli.jar validate --file mapping.json
 *   java -jar queryforge-cli.jar validate --dir ./mappings/
 */
@Command(
        name = "queryforge",
        description = "Dynamic SQL Query Generator from JSON configuration",
        version = "QueryForge 1.0.0",
        mixinStandardHelpOptions = true,
        subcommands = {
                GenerateCommand.class,
                ValidateCommand.class
        }
)
public class QueryForgeCLI implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryForgeCLI()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        // Affiché si aucune sous-commande n'est fournie
        CommandLine.usage(this, System.out);
    }
}
