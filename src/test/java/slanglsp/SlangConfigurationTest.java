package slanglsp;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SlangConfigurationTest {
    @TempDir Path workspace;

    @Test
    void detectsGameShaderRootsInCompilerOrder() throws Exception {
        Path shared = Files.createDirectories(workspace.resolve("assets/shaders"));
        Path a = Files.createDirectories(workspace.resolve("crates/a/assets/shaders"));
        Path z = Files.createDirectories(workspace.resolve("crates/z/assets/shaders"));
        Path bench = Files.createDirectories(workspace.resolve("benches/example/assets/shaders"));
        Path manual = Files.createDirectories(workspace.resolve("custom-shaders"));

        List<String> paths = SlangProjectSearchPaths.discover(workspace, List.of("custom-shaders", "assets/shaders"));
        assertEquals(List.of(
            manual.toRealPath().toString(), shared.toRealPath().toString(),
            a.toRealPath().toString(), z.toRealPath().toString(),
            bench.toRealPath().toString()
        ), paths);
    }

    @Test
    void producesTypedSlangdConfiguration() throws Exception {
        Files.createDirectories(workspace.resolve("assets/shaders"));
        SlangPersistentStateConfig.State state = new SlangPersistentStateConfig.State();
        state.additionalIncludePaths = List.of("assets/shaders");
        state.predefinedMacros = List.of("EDITOR", "VOXEL_HW_RAY_QUERY=1");
        state.enableSearchingSubDirectoriesOfWorkspace = false;
        JsonObject settings = state.createJSONFromObject(workspace);

        assertEquals(workspace.resolve("assets/shaders").toRealPath().toString(),
            settings.getAsJsonArray("slang.additionalSearchPaths").get(0).getAsString());
        assertEquals(2, settings.getAsJsonArray("slang.predefinedMacros").size());
        assertEquals("EDITOR", settings.getAsJsonArray("slang.predefinedMacros").get(0).getAsString());
        assertFalse(settings.get("slang.searchInAllWorkspaceDirectories").getAsBoolean());
        assertTrue(settings.get("slang.inlayHints.deducedTypes").getAsBoolean());
        assertTrue(settings.get("slang.additionalSearchPaths").isJsonArray());
    }

    @Test
    void handlesAbsentShaderDirectoriesWithoutCrashing() {
        assertTrue(SlangProjectSearchPaths.discover(workspace, List.of("missing")).isEmpty());
        assertTrue(SlangProjectSearchPaths.discover(null, null).isEmpty());
    }
}
