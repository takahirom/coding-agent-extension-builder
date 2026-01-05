package io.github.takahirom.codingagentextension

import io.github.takahirom.codingagentextension.claudecode.toClaudeCodePluginJson
import io.github.takahirom.codingagentextension.claudecode.writeClaudeCodeExtension
import io.github.takahirom.codingagentextension.model.Agent
import io.github.takahirom.codingagentextension.model.Command
import io.github.takahirom.codingagentextension.model.Plugin
import io.github.takahirom.codingagentextension.model.Skill
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginBuilderTest {

    @Test
    fun `plugin with required fields only`() {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "A test plugin",
            version = "1.0.0"
        ).build()

        assertEquals("my-plugin", plugin.name)
        assertEquals("A test plugin", plugin.description)
        assertEquals("1.0.0", plugin.version)
    }

    @Test
    fun `plugin with author`() {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "A test plugin",
            version = "1.0.0"
        )
            .author(name = "Test Author") {
                email("test@example.com")
            }
            .build()

        assertEquals("Test Author", plugin.author?.name)
        assertEquals("test@example.com", plugin.author?.email)
    }

    @Test
    fun `plugin with commands and skills`() {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "A test plugin",
            version = "1.0.0"
        )
            .addCommand(
                Command.Builder("format", "Format code", "# Format Command").build()
            )
            .addSkill(
                Skill.Builder("style-guide", "Style guidelines", "# Style Guide").build()
            )
            .addAgent(
                Agent.Builder("code-reviewer", "Review code changes", "Review code changes.").build()
            )
            .build()

        assertEquals(1, plugin.commands.size)
        assertEquals("format", plugin.commands[0].name)
        assertEquals(1, plugin.skills.size)
        assertEquals("style-guide", plugin.skills[0].name)
        assertEquals(1, plugin.agents.size)
        assertEquals("code-reviewer", plugin.agents[0].name)
    }

    @Test
    fun `plugin generates valid JSON`() {
        val plugin = Plugin.Builder(
            name = "test-plugin",
            description = "Test",
            version = "1.0.0"
        )
            .author(name = "Author")
            .build()

        val json = plugin.toClaudeCodePluginJson()

        assertTrue(json.contains("\"name\": \"test-plugin\""))
        assertTrue(json.contains("\"version\": \"1.0.0\""))
        assertTrue(json.contains("\"author\""))
    }

    @Test
    fun `plugin writes correct directory structure`(@TempDir tempDir: Path) {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "Test plugin",
            version = "1.0.0"
        )
            .addCommand(
                Command.Builder("hello", "Say hello", "# Hello").build()
            )
            .addSkill(
                Skill.Builder("greeting", "Greeting skill", "# Greeting").build()
            )
            .addAgent(
                Agent.Builder("code-reviewer", "Review code changes", "Review code changes.").build()
            )
            .build()

        plugin.writeClaudeCodeExtension(tempDir)

        assertTrue(tempDir.resolve("my-plugin/.claude-plugin/plugin.json").toFile().exists())
        assertTrue(tempDir.resolve("my-plugin/commands/hello.md").toFile().exists())
        assertTrue(tempDir.resolve("my-plugin/skills/greeting/SKILL.md").toFile().exists())
        assertTrue(tempDir.resolve("my-plugin/agents/code-reviewer.md").toFile().exists())
    }
}
