package io.github.takahirom.codingagentextension.claudecode

import io.github.takahirom.codingagentextension.model.*
import kotlinx.serialization.json.*
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

private val json = Json {
    prettyPrint = true
    encodeDefaults = false
}

// Extension functions for Skill
fun Skill.toClaudeCodeString(): String = buildString {
    appendLine("---")
    appendLine("name: $name")
    if (description.contains("\n")) {
        appendLine("description: |")
        description.lines().forEach { appendLine("  $it") }
    } else {
        appendLine("description: $description")
    }
    license?.let { appendLine("license: $it") }
    if (allowedTools.isNotEmpty()) {
        appendLine("allowed-tools: ${allowedTools.joinToString(", ")}")
    }
    if (metadata.isNotEmpty()) {
        appendLine("metadata:")
        metadata.forEach { (key, value) ->
            appendLine("  $key: $value")
        }
    }
    appendLine("---")
    appendLine()
    append(body)
}

fun Skill.writeClaudeCodeExtension(outputDir: Path) {
    val skillDir = outputDir.resolve(name)
    skillDir.createDirectories()
    skillDir.resolve("SKILL.md").writeText(toClaudeCodeString())
}

// Extension functions for Command
fun Command.toClaudeCodeString(): String = buildString {
    appendLine("---")
    appendLine("description: $description")
    appendLine("---")
    appendLine()
    append(body)
}

// Extension functions for Subagent
fun Subagent.toClaudeCodeString(): String = buildString {
    appendLine("---")
    appendLine("name: $name")
    appendLine("description: $description")
    if (tools.isNotEmpty()) {
        appendLine("tools: ${tools.joinToString(", ")}")
    }
    model?.let { appendLine("model: $it") }
    permissionMode?.let { appendLine("permissionMode: $it") }
    if (skills.isNotEmpty()) {
        appendLine("skills: ${skills.joinToString(", ")}")
    }
    appendLine("---")
    appendLine()
    append(body)
}

// Extension functions for Hooks
fun List<HookMatcher>.toClaudeCodeHooksJson(): String {
    val hooksData = buildJsonObject {
        // Group hooks by event
        val groupedByEvent = this@toClaudeCodeHooksJson.groupBy { it.event }
        put("hooks", buildJsonObject {
            groupedByEvent.forEach { (event, matchers) ->
                put(event.name, buildJsonArray {
                    matchers.forEach { matcher ->
                        add(buildJsonObject {
                            matcher.matcher?.let { put("matcher", it) }
                            put("hooks", buildJsonArray {
                                matcher.hooks.forEach { hook ->
                                    add(buildJsonObject {
                                        when (hook) {
                                            is HookCommand.Command -> {
                                                put("type", "command")
                                                put("command", hook.command)
                                            }
                                            is HookCommand.Prompt -> {
                                                put("type", "prompt")
                                                put("prompt", hook.prompt)
                                            }
                                        }
                                        hook.timeout?.let { put("timeout", it) }
                                    })
                                }
                            })
                        })
                    }
                })
            }
        })
    }
    return json.encodeToString(JsonObject.serializer(), hooksData)
}

// Extension functions for Plugin
fun Plugin.toClaudeCodePluginJson(): String {
    val pluginData = buildJsonObject {
        put("name", name)
        put("description", description)
        put("version", version)
        author?.let {
            put("author", buildJsonObject {
                put("name", it.name)
                it.email?.let { email -> put("email", email) }
            })
        }
    }
    return json.encodeToString(JsonObject.serializer(), pluginData)
}

fun Plugin.writeClaudeCodeExtension(outputDir: Path) {
    val pluginDir = outputDir.resolve(name)

    // Create .claude-plugin/plugin.json
    val claudePluginDir = pluginDir.resolve(".claude-plugin")
    claudePluginDir.createDirectories()
    claudePluginDir.resolve("plugin.json").writeText(toClaudeCodePluginJson())

    // Create commands/*.md
    if (commands.isNotEmpty()) {
        val commandsDir = pluginDir.resolve("commands")
        commandsDir.createDirectories()
        commands.forEach { command ->
            commandsDir.resolve("${command.name}.md").writeText(command.toClaudeCodeString())
        }
    }

    // Create skills/*/SKILL.md
    if (skills.isNotEmpty()) {
        val skillsDir = pluginDir.resolve("skills")
        skills.forEach { skill ->
            skill.writeClaudeCodeExtension(skillsDir)
        }
    }

    // Create agents/*.md
    if (subagents.isNotEmpty()) {
        val agentsDir = pluginDir.resolve("agents")
        agentsDir.createDirectories()
        subagents.forEach { subagent ->
            agentsDir.resolve("${subagent.name}.md").writeText(subagent.toClaudeCodeString())
        }
    }

    // Create hooks/hooks.json
    if (hooks.isNotEmpty()) {
        val hooksDir = pluginDir.resolve("hooks")
        hooksDir.createDirectories()
        hooksDir.resolve("hooks.json").writeText(hooks.toClaudeCodeHooksJson())
    }
}

// Extension functions for Marketplace
fun Marketplace.toClaudeCodeMarketplaceJson(): String {
    val marketplaceData = buildJsonObject {
        put("name", name)
        put("owner", buildJsonObject {
            put("name", owner.name)
            owner.email?.let { put("email", it) }
        })
        put("plugins", buildJsonArray {
            plugins.forEach { plugin ->
                add(buildJsonObject {
                    put("name", plugin.name)
                    when (val source = plugin.source) {
                        is PluginSource.RelativePath -> put("source", source.path)
                        is PluginSource.GitHub -> put("source", buildJsonObject {
                            put("source", "github")
                            put("repo", source.repo)
                        })
                        is PluginSource.Git -> put("source", buildJsonObject {
                            put("source", "url")
                            put("url", source.url)
                        })
                    }
                    plugin.description?.let { put("description", it) }
                    plugin.version?.let { put("version", it) }
                    plugin.author?.let {
                        put("author", buildJsonObject {
                            put("name", it.name)
                            it.email?.let { email -> put("email", email) }
                        })
                    }
                })
            }
        })
        metadata?.let { meta ->
            if (meta.description != null || meta.version != null || meta.pluginRoot != null) {
                put("metadata", buildJsonObject {
                    meta.description?.let { put("description", it) }
                    meta.version?.let { put("version", it) }
                    meta.pluginRoot?.let { put("pluginRoot", it) }
                })
            }
        }
    }
    return json.encodeToString(JsonObject.serializer(), marketplaceData)
}

fun Marketplace.writeClaudeCodeExtension(outputDir: Path) {
    val marketplaceDir = outputDir.resolve(name)
    val claudePluginDir = marketplaceDir.resolve(".claude-plugin")
    claudePluginDir.createDirectories()
    claudePluginDir.resolve("marketplace.json").writeText(toClaudeCodeMarketplaceJson())

    // Write embedded plugins
    embeddedPlugins.forEach { embedded ->
        // relativePath is like "./plugins/my-plugin", we need to resolve it from marketplaceDir
        val pluginPath = embedded.relativePath.removePrefix("./")
        val pluginParentDir = marketplaceDir.resolve(pluginPath).parent ?: marketplaceDir
        embedded.plugin.writeClaudeCodeExtension(pluginParentDir)
    }
}
