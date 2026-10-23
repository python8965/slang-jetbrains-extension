package slanglsp;

import java.util.*;

import com.intellij.util.xmlb.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;

import javax.print.DocFlavor;
import java.io.*;


@State(
    name="SlangPersistentStateComponentConfig",
    storages = {
        @Storage("SlangPluginSettings.xml")
    }
)
class SlangPersistentStateConfig implements PersistentStateComponent<SlangPersistentStateConfig.State>
{
    static class State
    {
        // TODO: make a cached state which transforms this State into an efficent to compare state (assumes rare to change settings)
        public List<String> additionalIncludePaths = new ArrayList<>();
        public List<String> predefinedMacros = new ArrayList<>();

        public String explicitSlangdLocation = "";
//        public String traceServer = "off"; // handled by LSP4IJ's (runtime) debug tool
        public String enableCommitCharactersInAutoCompletion = "membersOnly";

        public Boolean enableInlayHintsForDeducedTypes = true;
        public Boolean enableInlayHintsForParameterNames = true;
        public Boolean enableSearchingSubDirectoriesOfWorkspace = true;

        public void copyValues(State otherState)
        {
            additionalIncludePaths = otherState.additionalIncludePaths;
            predefinedMacros = otherState.predefinedMacros;
            explicitSlangdLocation = otherState.explicitSlangdLocation;
            enableCommitCharactersInAutoCompletion = otherState.enableCommitCharactersInAutoCompletion;
            enableInlayHintsForDeducedTypes = otherState.enableInlayHintsForDeducedTypes;
            enableInlayHintsForParameterNames = otherState.enableInlayHintsForParameterNames;
            enableSearchingSubDirectoriesOfWorkspace = otherState.enableSearchingSubDirectoriesOfWorkspace;
        }
        public boolean equals(State other)
        {
            return true
                && additionalIncludePaths.equals(other.additionalIncludePaths)
                && predefinedMacros.equals(other.predefinedMacros)
                && explicitSlangdLocation.equals(other.explicitSlangdLocation)
                && enableCommitCharactersInAutoCompletion.equals(other.enableCommitCharactersInAutoCompletion)
                && enableInlayHintsForDeducedTypes.equals(other.enableInlayHintsForDeducedTypes)
                && enableInlayHintsForParameterNames.equals(other.enableInlayHintsForParameterNames)
                && enableSearchingSubDirectoriesOfWorkspace.equals(other.enableSearchingSubDirectoriesOfWorkspace)
                ;
        }

        // LSP4IJ expects a JSON object with the original JSON value types.
        JsonObject createJSONFromObject(java.nio.file.Path projectRoot)
        {
            JsonObject settings = new JsonObject();
            JsonArray searchPaths = new JsonArray();
            for (String path : SlangProjectSearchPaths.discover(projectRoot, additionalIncludePaths))
                searchPaths.add(path);
            settings.add("slang.additionalSearchPaths", searchPaths);

            JsonArray macros = new JsonArray();
            for (String macro : predefinedMacros)
                if (macro != null && !macro.isBlank()) macros.add(macro.trim());
            settings.add("slang.predefinedMacros", macros);

            settings.addProperty("slang.enableCommitCharactersInAutoCompletion",
                enableCommitCharactersInAutoCompletion);
            settings.addProperty("slang.inlayHints.deducedTypes",
                Boolean.TRUE.equals(enableInlayHintsForDeducedTypes));
            settings.addProperty("slang.inlayHints.parameterNames",
                Boolean.TRUE.equals(enableInlayHintsForParameterNames));
            settings.addProperty("slang.searchInAllWorkspaceDirectories",
                Boolean.TRUE.equals(enableSearchingSubDirectoriesOfWorkspace));
            return settings;
        }
    }

    @NotNull
    private State state = new State();

    JsonObject createJSONFromObject(Project project)
    {
        String basePath = project.getBasePath();
        return state.createJSONFromObject(basePath == null ? null : java.nio.file.Paths.get(basePath));
    }

    String getExplicitSlangdLocation()
    {
        return state.explicitSlangdLocation;
    }

    void setState(State otherState)
    {
        state.copyValues(otherState);
    }

    @NotNull
    @Override
    public State getState()
    {
        return state;
    }

    @Override
    public void loadState(@NotNull State config)
    {
        state = config;
    }


    @Nullable
    public static SlangPersistentStateConfig getInstance(Project project)
    {
        return project.getService(SlangPersistentStateConfig.class);
    }
}
