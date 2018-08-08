package de.imgruntw.filerenamer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public final class ArgHolder {
    private final Map<String, String> parsedArgs;

    private ArgHolder(Map<String, String> parsedArgs) {
        this.parsedArgs = parsedArgs;
    }

    public static ArgHolder of(String[] args) {
        requireNonNull(args, "args must not be null");

        return new ArgHolder(parseArgs(args));
    }

    public Optional<String> getCmdValue(String cmd) {
        return ofNullable(parsedArgs.get(cmd));
    }

    private static Map<String, String> parseArgs(String[] args) {
        final Map<String, String> parsedArgs = new LinkedHashMap<>();

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            if (arg.startsWith("-")) {
                final int successorIndex = i + 1;

                String argSuccessor = "";

                if (successorIndex < args.length && !args[successorIndex].startsWith("-")) {
                    argSuccessor = args[successorIndex];
                }

                parsedArgs.put(arg, argSuccessor);
            }
        }

        return parsedArgs;
    }
}
