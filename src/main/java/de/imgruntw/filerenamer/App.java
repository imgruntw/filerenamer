package de.imgruntw.filerenamer;

import java.io.File;
import java.time.Clock;

public final class App {
    private static final String PATTERN_CMD = "-pattern";
    private static final String TIMESTAMP_CMD = "-timestamp";
    private static final String SOURCE_CMD = "-source";

    private final FileRenamer fileRenamer;

    private App() {
        fileRenamer = new FileRenamer();
    }

    public static void main(String[] args) {
        final ArgHolder argHolder = ArgHolder.of(args);

        if (argHolder.getCmdValue(SOURCE_CMD).isPresent()) {
            final String source = argHolder.getCmdValue(SOURCE_CMD).get();

            if (new File(source).exists()) {
                if (argHolder.getCmdValue(PATTERN_CMD).isPresent()) {
                    new App().renameWithPattern(source);
                } else if (argHolder.getCmdValue(TIMESTAMP_CMD).isPresent()) {
                    new App().renameWithTimestamp(source);
                } else {
                    System.err.printf("missing command statements: %s or %s\n", PATTERN_CMD, TIMESTAMP_CMD);
                }
            } else {
                System.err.printf("%s source directory does not exists\n", source);
            }
        } else {
            System.err.printf("missing %s source directory statement\n", SOURCE_CMD);
        }
    }

    private void renameWithPattern(String dir) {
        System.out.println("starting renaming ...");

        executeWithMeter(() -> fileRenamer.renameWithPattern(dir));

        System.out.println("finished.");
    }

    private void renameWithTimestamp(String dir) {
        System.out.println("starting renaming ...");

        executeWithMeter(() -> fileRenamer.renameWithTimestamp(dir));

        System.out.println("finished.");
    }

    private void executeWithMeter(Runnable action) {
        final long start = Clock.systemUTC().millis();

        action.run();

        final long end = Clock.systemUTC().millis();
        System.out.printf("action took %d ms.\n", end - start);
    }
}
