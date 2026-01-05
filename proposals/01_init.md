# Proposal: coding-agent-extension-builder

## Problem

Creating Claude Code plugins and skills is error-prone:
- Directory structure must be exact (`.claude-plugin/`, `commands/`, `skills/`)
- File naming conventions (`SKILL.md`, `plugin.json`)
- Field validation rules (name: hyphen-case, max 64 chars; description: max 1024 chars, no `<>`)

## Solution

A Kotlin library using Builder pattern that generates correct plugin/skill structures with compile-time safety.

## Why Builder over DSL?

Based on [Coil's experience](https://colinwhite.me/prefer-builders-over-dsls):

1. **Clear scope**: DSLs mix receiver scope with outer scope, causing slower IDE autocomplete and accidental global function resolution
2. **Factory pattern**: Builders support `newBuilder()` for creating variations
3. **Java compatibility**: Builders work well with Java consumers
4. **Industry standard**: Used by OkHttp, Retrofit, Coil, etc.

## API Design

```kotlin
// Required fields as constructor args (compile-time enforcement)
// Optional fields as builder methods
val skill = Skill.Builder(
    name = "pdf-processor",
    description = "Extract text from PDFs. Use when working with PDF files.",
    body = "# PDF Processing\n\nExtract text with pdfplumber..."
)
    .license("MIT")                       // single value: no prefix
    .allowedTools("Read", "Grep", "Glob") // single value: no prefix
    .addMetadata("version", "1.0.0")      // collection: add prefix
    .build()

// Output to specific platform
skill.writeClaudeCodeExtension(Path("output"))  // generates: output/pdf-processor/SKILL.md
skill.toClaudeCodeString()               // returns SKILL.md content as string

// Future: other platforms
// skill.writeToGeminiCli(Path("output"))

val plugin = Plugin.Builder(
    name = "my-formatter",
    description = "Code formatting utilities",
    version = "1.0.0"
)
    .author(name = "Your Name") { email("you@example.com") }
    .addCommand(name = "format", description = "Format code", body = "# Format\n...")
    .addSkill(name = "style", description = "Style guide", body = "# Style\n...")
    .build()

plugin.writeClaudeCodeExtension(Path("output"))
// generates:
// output/my-formatter/
// ├── .claude-plugin/plugin.json
// ├── commands/format.md
// └── skills/style/SKILL.md

// Marketplace generation
val marketplace = Marketplace.Builder(
    name = "my-team-tools",
    owner = Owner(name = "DevTools Team", email = "dev@example.com")
)
    .addPlugin(
        name = "formatter",
        source = "./plugins/formatter",
        description = "Code formatter"
    )
    .addPlugin(
        name = "linter",
        source = gitHubSource("company/linter-plugin")
    )
    .build()

marketplace.writeClaudeCodeExtension(Path("output"))
// generates:
// output/my-team-tools/
// └── .claude-plugin/marketplace.json
```

## Scope

| Component | Required (Constructor) | Optional (Methods) |
|-----------|------------------------|-------------------|
| Skill.Builder | name, description, body | license(), allowedTools(), addMetadata() |
| Plugin.Builder | name, description, version | author(), addCommand(), addSkill() |
| Command.Builder | name, description, body | - |
| Marketplace.Builder | name, owner | addPlugin(), metadata() |

## Module Structure

```
coding-agent-extension-builder/src/main/kotlin/io/github/takahirom/codingagentextension/
├── model/           # Data classes with nested Builder (Skill, Plugin, Command, Marketplace)
├── validation/      # Name/description validators
└── claudecode/      # Claude Code specific generator (extension functions)
    └── ClaudeCodeGenerator.kt
# Future:
# └── geminicli/     # Gemini CLI generator
```

## Build Structure

Uses Gradle composite build with build-logic:

```
coding-agent-extension-builder/
├── build-logic/
│   └── convention/          # Shared build conventions
├── coding-agent-extension-builder/
│   └── build.gradle.kts     # Module build
├── gradle/
│   └── libs.versions.toml   # Version catalog
└── settings.gradle.kts
```

## Out of Scope (v1)

- Agents, hooks, MCP servers
- Gradle plugin integration
