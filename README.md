# coding-agent-extension-dsl

Kotlin library for generating coding agent (Claude Code, etc.) plugins and skills using Builder pattern.

## Motivation

Writing coding agent extensions (plugins, skills, marketplaces) by hand is error-prone:

- Need to remember `marketplace.json` format
- Need to create correct folder structure (`.claude-plugin/plugin.json`, `skills/*/SKILL.md`, etc.)
- Easy to misspell YAML front matter field names (`allowed-tools`? `allowedTools`?)
- **Mistakes are hard to catch because agents still work reasonably well with invalid config**

This library provides:

- **Compile-time validation** by making required fields constructor arguments
- Automatic folder structure and file name generation
- No more typos in field names

## Usage

```kotlin
val skill = Skill.Builder(
    name = "pdf-processor",
    description = "Extract text from PDFs. Use when working with PDF files.",
    body = "# PDF Processing\n\nExtract text with pdfplumber..."
)
    .license("MIT")
    .allowedTools("Read", "Grep", "Glob")
    .build()

skill.writeToClaudeCode(Path("output"))
```

See [proposals/01_init.md](proposals/01_init.md) for full API design.

## Build

```bash
./gradlew build
./gradlew check
```

## Project Structure

- `build-logic/` - Gradle convention plugins (composite build)
- `coding-agent-extension-dsl/` - Main library module
- `workspace/` - Playground for trying out the library
- `proposals/` - Design documents

## Workspace

The `workspace/` module is a playground for trying out the library locally.

```bash
# Copy the template
cp workspace/src/main/kotlin/Main.kt.template workspace/src/main/kotlin/Main.kt

# Edit Main.kt as needed, then run
./gradlew :workspace:run
```

- `Main.kt` is gitignored - modify freely
- `Main.kt.template` is the starting point
- Output goes to `workspace/output/` (also gitignored)
