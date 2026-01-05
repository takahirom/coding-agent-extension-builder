# coding-agent-extension-builder

Kotlin library for generating coding agent (Claude Code, etc.) plugins and skills using Builder pattern.

## Motivation

Writing coding agent extensions (plugins, skills, marketplaces) is error-prone:

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
// Create a plugin with command and skill
val plugin = Plugin.Builder(
    name = "my-plugin",
    description = "My awesome plugin",
    version = "1.0.0"
)
    .author(name = "My Team")
    .addCommand(
        Command.Builder(
            name = "greet",
            description = "Say hello",
            body = "# Greet\n..."
        ).build()
    )
    .addSkill(
        Skill.Builder(
            name = "coding-style",
            description = "Style guide",
            body = "# Style\n..."
        ).build()
    )
    .addHook(
        HookMatcher.Builder(HookEvent.PostToolUse)
            .matcher("Write|Edit")
            .command("\${CLAUDE_PLUGIN_ROOT}/scripts/format.sh", timeout = 30)
            .build()
    )
    .build()

// Create marketplace with the plugin
val marketplace = Marketplace.Builder(
    name = "my-marketplace",
    owner = Owner(name = "My Team")
)
    .addPlugin(plugin)
    .build()

// Write everything with a single call
marketplace.writeClaudeCodeExtension(Path("output"))
```

This generates:

```
output/my-marketplace/
├── .claude-plugin/
│   └── marketplace.json
└── plugins/
    └── my-plugin/
        ├── .claude-plugin/
        │   └── plugin.json
        ├── commands/
        │   └── greet.md
        ├── hooks/
        │   └── hooks.json
        ├── scripts/
        │   └── format.sh  ← You provide this
        └── skills/
            └── coding-style/
                └── SKILL.md
```

Writing this structure by hand requires knowing the exact folder names, file names, JSON format, and YAML front matter fields. This library handles all of that.

## Build

```bash
./gradlew build
./gradlew check
```

## Project Structure

- `build-logic/` - Gradle convention plugins (composite build)
- `coding-agent-extension-builder/` - Main library module
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

## Distributing Plugins via GitHub

1. **Clone this repository** and edit `workspace/src/main/kotlin/Main.kt`:

```bash
git clone https://github.com/takahirom/coding-agent-extension-builder.git
cd coding-agent-extension-builder
cp workspace/src/main/kotlin/Main.kt.template workspace/src/main/kotlin/Main.kt
# Edit Main.kt with your plugin definition
```

2. **Generate directly to your plugin repository**:

```bash
# Pass output path as argument
./gradlew :workspace:run --args="/path/to/my-plugin-repo"
```

3. **Commit and push**:

```bash
cd /path/to/my-plugin-repo
git add -A && git commit -m "Add plugins"
git push
```

4. **Install from GitHub** in Claude Code:

```
/plugin marketplace add yourname/my-plugin-repo
```

Your team can now install your plugins directly from GitHub!
