package de.imgruntw.filerenamer;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public final class FileRenamer {
    private static final String TEMP_DIR;
    private static final DateTimeFormatter OLD_FORMATTER;
    private static final DateTimeFormatter NEW_FORMATTER;
    private static final DirectoryStream.Filter<Path> HIDDEN_FILE_FILTER;

    static {
        TEMP_DIR = "tmp/";
        OLD_FORMATTER = DateTimeFormatter.ofPattern("uuuu.MM.dd HH.mm.ss");
        NEW_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss:SSS");
        HIDDEN_FILE_FILTER = p -> !p.getFileName().toString().startsWith(".");
    }

    public void renameWithPattern(String dir) {
        final Path dirPath = Paths.get(requireNonNull(dir, "dir must not be null"));

        if (Files.isDirectory(dirPath)) {
            final Set<Path> filePaths = loadFilePaths(dirPath);
            final Optional<Path> tempPath = createTempPath(dir);

            if (tempPath.isPresent()) {
                for (Path filePath : filePaths) {
                    final String fileName = correctOldFileName(filePath);

                    try {
                        final LocalDateTime localDateTime = LocalDateTime.parse(fileName, OLD_FORMATTER);
                        final String newFilename = localDateTime.format(NEW_FORMATTER);
                        renameWithTimestamp(filePath, newFilename, tempPath.get());
                    } catch (DateTimeParseException | IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException(String.format("couldn't create %s directory", TEMP_DIR));
            }
        } else {
            throw new IllegalArgumentException(String.format("%s is no directory", dir));
        }
    }

    public void renameWithTimestamp(String dir) {
        final Path dirPath = Paths.get(requireNonNull(dir, "dir must not be null"));

        if (Files.isDirectory(dirPath)) {
            final Set<Path> filePaths = loadFilePaths(dirPath);
            final Optional<Path> tempPath = createTempPath(dir);

            if (tempPath.isPresent()) {
                final Set<Instant> processedTimestamps = new HashSet<>();

                for (Path filePath : filePaths) {
                    try {
                        final Instant timestamp = readTimestamp(filePath);
                        final String newFilename = convertToFileName(processedTimestamps, timestamp);
                        renameWithTimestamp(filePath, newFilename, tempPath.get());
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } else {
                throw new IllegalArgumentException(String.format("couldn't create %s directory", TEMP_DIR));
            }
        } else {
            throw new IllegalArgumentException(String.format("%s is no directory", dir));
        }
    }

    private String correctOldFileName(Path filePath) {
        String fileName = filePath.getFileName().toString();
        fileName = fileName.replaceAll("\\.(\\d)\\.", ".0$1.");
        fileName = fileName.replaceAll("\\.(\\d)\\s", ".0$1 ");
        fileName = fileName.replaceAll("\\s(\\d)\\.", " 0$1.");
        fileName = fileName.replaceAll("\\.(\\d)\\.", ".0$1.");
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        return fileName;
    }

    private Optional<Path> createTempPath(String dir) {
        Optional<Path> tempPath = Optional.of(Paths.get(dir, TEMP_DIR));

        if (!Files.exists(tempPath.get())) {
            try {
                Files.createDirectory(tempPath.get());
            } catch (IOException e) {
                tempPath = Optional.empty();
                e.printStackTrace();
            }
        }

        return tempPath;
    }

    private Set<Path> loadFilePaths(Path dirPath) {
        final Set<Path> filePaths = new HashSet<>();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dirPath, HIDDEN_FILE_FILTER)) {
            dirStream.forEach(filePaths::add);
        } catch (IOException e) {
            e.printStackTrace();
            filePaths.clear();
        }

        return filePaths;
    }

    private Instant readTimestamp(Path filePath) throws IOException {
        final BasicFileAttributes attributes = Files.getFileAttributeView(filePath, BasicFileAttributeView.class)
                .readAttributes();

        return attributes.lastModifiedTime().toInstant();
    }

    private String convertToFileName(Set<Instant> processedTimestamps, Instant timestamp) {
        Instant processedTimestamp = timestamp;

        while (processedTimestamps.contains(processedTimestamp)) {
            processedTimestamp = processedTimestamp.plusMillis(1);
        }

        processedTimestamps.add(processedTimestamp);

        final LocalDateTime localDateTime = LocalDateTime.ofInstant(processedTimestamp, ZoneId.systemDefault());

        return localDateTime.format(NEW_FORMATTER);
    }

    private void renameWithTimestamp(Path fromFilePath, String toFileName, Path tempPath) throws IOException {
        final Optional<String> fileExtension = getFileExtension(fromFilePath);

        if (fileExtension.isPresent()) {
            final Path toFilePath = tempPath.resolve(toFileName + fileExtension.get());
            Files.move(fromFilePath, toFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Optional<String> getFileExtension(Path filePath) {
        final String fileName = filePath.getFileName().toString();
        final int dotIndex = fileName.lastIndexOf(".");

        return dotIndex != -1 ? Optional.of(fileName.substring(dotIndex).toLowerCase()) : Optional.empty();
    }
}
