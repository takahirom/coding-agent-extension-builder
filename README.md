# coding-agent-extension-dsl

Kotlin library for generating coding agent (Claude Code, etc.) plugins and skills using Builder pattern.

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
- `proposals/` - Design documents
