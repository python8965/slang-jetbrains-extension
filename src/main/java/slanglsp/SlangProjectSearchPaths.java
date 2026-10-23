package slanglsp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Reproduces the Slang module roots used by game/bevy_slang for IDE analysis.
 * Explicit paths take priority. No filesystem mutations or Rust project changes.
 */
final class SlangProjectSearchPaths {
    private SlangProjectSearchPaths() {}

    static List<String> discover(Path projectRoot, List<String> configured) {
        Set<String> paths = new LinkedHashSet<>();
        Path root = projectRoot == null ? null : projectRoot.toAbsolutePath().normalize();
        if (configured != null) {
            for (String value : configured) {
                if (value == null || value.isBlank()) continue;
                try {
                    Path path = Paths.get(value.trim());
                    if (!path.isAbsolute() && root != null) path = root.resolve(path);
                    addIfDirectory(paths, path);
                } catch (InvalidPathException ignored) {
                    // An invalid user path must not prevent the entire LSP configuration.
                }
            }
        }
        if (root != null) {
            addIfDirectory(paths, root.resolve("assets/shaders"));
            collectShaderRoots(paths, root.resolve("crates"));
            collectShaderRoots(paths, root.resolve("benches"));
        }
        return new ArrayList<>(paths);
    }

    private static void collectShaderRoots(Set<String> paths, Path parent) {
        if (!Files.isDirectory(parent)) return;
        try (Stream<Path> children = Files.list(parent)) {
            children.filter(Files::isDirectory)
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .forEach(dir -> addIfDirectory(paths, dir.resolve("assets/shaders")));
        } catch (IOException ignored) {
            // Permission errors in unrelated project folders are nonfatal.
        }
    }

    private static void addIfDirectory(Set<String> paths, Path path) {
        if (!Files.isDirectory(path)) return;
        try {
            paths.add(path.toRealPath().toString());
        } catch (IOException ignored) {
            paths.add(path.toAbsolutePath().normalize().toString());
        }
    }
}
