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
    appendLine("description: $description")
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

fun Skill.writeToClaudeCode(outputDir: Path) {
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

fun Plugin.writeToClaudeCode(outputDir: Path) {
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
            skill.writeToClaudeCode(skillsDir)
        }
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

fun Marketplace.writeToClaudeCode(outputDir: Path) {
    val marketplaceDir = outputDir.resolve(name)
    val claudePluginDir = marketplaceDir.resolve(".claude-plugin")
    claudePluginDir.createDirectories()
    claudePluginDir.resolve("marketplace.json").writeText(toClaudeCodeMarketplaceJson())

    // Write embedded plugins
    embeddedPlugins.forEach { embedded ->
        // relativePath is like "./plugins/my-plugin", we need to resolve it from marketplaceDir
        val pluginPath = embedded.relativePath.removePrefix("./")
        val pluginParentDir = marketplaceDir.resolve(pluginPath).parent ?: marketplaceDir
        embedded.plugin.writeToClaudeCode(pluginParentDir)
    }
}
