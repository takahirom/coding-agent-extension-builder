---
name: coding-agent-extension-dsl
description: |
  Generate Claude Code plugins, skills, and marketplaces using Kotlin DSL.
  Use when the user wants to:
  - Create a new plugin with commands and skills
  - Create a marketplace to distribute plugins
  - Generate correct folder structure (.claude-plugin/, skills/, commands/)
  - Avoid mistakes in JSON format and YAML front matter field names
license: MIT
allowed-tools: Read, Write, Edit, Bash, Glob, Grep
---

# coding-agent-extension-dsl

Kotlin library for generating coding agent extensions with compile-time safety.

## Why Use This Library

Writing extensions by hand is error-prone:
- Need to remember `marketplace.json` format
- Need correct folder structure (`.claude-plugin/plugin.json`, `skills/*/SKILL.md`)
- Easy to misspell field names (`allowed-tools`? `allowedTools`?)
- Mistakes are hard to catch because agents still work with invalid config

## Quick Start

```kotlin
// 1. Create a plugin
val plugin = Plugin.Builder(
    name = "my-plugin",
    description = "My plugin description",
    version = "1.0.0"
)
    .author(name = "Your Name")
    .addCommand(
        Command.Builder(
            name = "my-command",
            description = "What this command does",
            body = "# Command\n\nInstructions..."
        ).build()
    )
    .addSkill(
        Skill.Builder(
            name = "my-skill",
            description = "When to use this skill",
            body = "# Skill\n\nGuidance..."
        ).build()
    )
    .build()

// 2. Create marketplace (optional)
val marketplace = Marketplace.Builder(
    name = "my-marketplace",
    owner = Owner(name = "Your Name")
)
    .addPlugin(plugin)
    .build()

// 3. Write to disk
marketplace.writeClaudeCodeExtension(Path("output"))
// Or just the plugin: plugin.writeClaudeCodeExtension(Path("output"))
```

## Generated Structure

```
output/my-marketplace/
├── .claude-plugin/
│   └── marketplace.json
└── plugins/
    └── my-plugin/
        ├── .claude-plugin/
        │   └── plugin.json
        ├── commands/
        │   └── my-command.md
        └── skills/
            └── my-skill/
                └── SKILL.md
```

## API Reference

### Skill.Builder(name, description, body)
- `.license(license: String)` - Set license
- `.allowedTools(vararg tools: String)` - Restrict tools
- `.addMetadata(key, value)` - Add custom metadata

### Command.Builder(name, description, body)
- No optional fields currently

### Plugin.Builder(name, description, version)
- `.author(name: String)` - Set author
- `.addCommand(command: Command)` - Add command
- `.addSkill(skill: Skill)` - Add skill
- `.addHook(hook: HookMatcher)` - Add hook

### HookMatcher.Builder(event: HookEvent)
- `.matcher(pattern: String)` - Tool pattern (e.g., "Write|Edit")
- `.command(command: String, timeout: Int?)` - Add command hook
- `.prompt(prompt: String, timeout: Int?)` - Add prompt hook (LLM-based)

HookEvent: PreToolUse, PostToolUse, PermissionRequest, Notification, UserPromptSubmit, Stop, SubagentStop, PreCompact, SessionStart, SessionEnd

### Marketplace.Builder(name, owner)
- `.addPlugin(plugin: Plugin)` - Add embedded plugin
- `.addPlugin(name, source, ...)` - Add external plugin reference
- `.metadata(description, version, pluginRoot)` - Set metadata

## Run in Workspace

```bash
cd workspace
cp src/main/kotlin/Main.kt.template src/main/kotlin/Main.kt
# Edit Main.kt
./gradlew :workspace:run
```