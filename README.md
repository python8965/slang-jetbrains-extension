### This is an unofficial LSP for Slang to use with jetbrain IDEs.

---

### Example
![Example](./images/example.gif)

---

### Jetbrains plugin distribution page
https://plugins.jetbrains.com/plugin/26239-slang-unofficial-

---

### To Build
1. install Intellij IDEA 2024.x.x
2. fetch code with `git clone --recursive https://github.com/16-Bit-Dog/slang-intellj-extenson.git`
3. build using predefined project tasks using IDEA 2024.x.x
4. as a note, the "build & run" task may fail (jetbrains bug?). Re-run the task if this happens.

---

### If highlighting does not work, try the following:
(1) delete the Slang extension
(2) reinstall the Slang extension (latest version)

---

### To Run Tests
The project contains native Java tests to verify Lexer and Syntax Highlighter correctness. To run the tests, use the included Gradle wrapper (requires JDK 21):

**On Windows:**
```cmd
./gradlew test
```

**On macOS/Linux:**
```bash
./gradlew test
```



### Game workspace import resolution

The plugin automatically adds Slang search roots from the open project:
`assets/shaders`, every existing `crates/*/assets/shaders` directory,
and `benches/*/assets/shaders`. Manual include paths in
**Settings → Tools → Slang** take precedence. The server receives typed
`slang.additionalSearchPaths` and `slang.predefinedMacros` settings.
Unlike the Rust shader compiler, `slangd` does not receive per-shader
command-line defines; use the project macros setting where needed.

The game generates `assets/shaders/Generated/*.slang` via Rust
`#[shader_export]` macros. Run `cargo check -p game` in the game workspace
at least once if imports under `Generated.*` are missing.

The LSP mapping includes both `.slang` and `.slangh`. Definition, hover,
references/Find Usages and rename requests use the installed LSP4IJ and
`slangd`; these features depend on the language server's advertised
capabilities and will not silently replace project symbols with a text search.
