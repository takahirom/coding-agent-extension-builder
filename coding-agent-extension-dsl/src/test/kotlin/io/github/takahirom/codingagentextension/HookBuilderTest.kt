package io.github.takahirom.codingagentextension

import io.github.takahirom.codingagentextension.claudecode.toClaudeCodeHooksJson
import io.github.takahirom.codingagentextension.claudecode.writeClaudeCodeExtension
import io.github.takahirom.codingagentextension.model.HookEvent
import io.github.takahirom.codingagentextension.model.HookMatcher
import io.github.takahirom.codingagentextension.model.Plugin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HookBuilderTest {

    @Test
    fun `hook with command`() {
        val hook = HookMatcher.Builder(HookEvent.PostToolUse)
            .matcher("Write|Edit")
            .command("\${CLAUDE_PLUGIN_ROOT}/scripts/format.sh", timeout = 30)
            .build()

        assertEquals(HookEvent.PostToolUse, hook.event)
        assertEquals("Write|Edit", hook.matcher)
        assertEquals(1, hook.hooks.size)
        assertEquals("\${CLAUDE_PLUGIN_ROOT}/scripts/format.sh", hook.hooks[0].command)
        assertEquals(30, hook.hooks[0].timeout)
    }

    @Test
    fun `hook with prompt`() {
        val hook = HookMatcher.Builder(HookEvent.Stop)
            .prompt("Check if all tasks are complete. Context: \$ARGUMENTS")
            .build()

        assertEquals(HookEvent.Stop, hook.event)
        assertEquals(null, hook.matcher)
        assertEquals(1, hook.hooks.size)
        assertEquals("Check if all tasks are complete. Context: \$ARGUMENTS", hook.hooks[0].prompt)
    }

    @Test
    fun `hook with multiple commands`() {
        val hook = HookMatcher.Builder(HookEvent.PreToolUse)
            .matcher("Bash")
            .command("\${CLAUDE_PLUGIN_ROOT}/scripts/validate.sh")
            .command("\${CLAUDE_PLUGIN_ROOT}/scripts/log.sh", timeout = 10)
            .build()

        assertEquals(2, hook.hooks.size)
    }

    @Test
    fun `hooks generate valid JSON`() {
        val hooks = listOf(
            HookMatcher.Builder(HookEvent.PostToolUse)
                .matcher("Write|Edit")
                .command("\${CLAUDE_PLUGIN_ROOT}/scripts/format.sh", timeout = 30)
                .build()
        )

        val json = hooks.toClaudeCodeHooksJson()

        assertTrue(json.contains("\"hooks\""))
        assertTrue(json.contains("\"PostToolUse\""))
        assertTrue(json.contains("\"matcher\": \"Write|Edit\""))
        assertTrue(json.contains("\"type\": \"command\""))
        assertTrue(json.contains("\"timeout\": 30"))
    }

    @Test
    fun `plugin with hooks writes hooks json`(@TempDir tempDir: Path) {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "Test plugin",
            version = "1.0.0"
        )
            .addHook(
                HookMatcher.Builder(HookEvent.PostToolUse)
                    .matcher("Write|Edit")
                    .command("\${CLAUDE_PLUGIN_ROOT}/scripts/format.sh")
                    .build()
            )
            .build()

        plugin.writeClaudeCodeExtension(tempDir)

        assertTrue(tempDir.resolve("my-plugin/hooks/hooks.json").toFile().exists())
    }

    @Test
    fun `plugin without hooks does not create hooks directory`(@TempDir tempDir: Path) {
        val plugin = Plugin.Builder(
            name = "my-plugin",
            description = "Test plugin",
            version = "1.0.0"
        ).build()

        plugin.writeClaudeCodeExtension(tempDir)

        assertTrue(!tempDir.resolve("my-plugin/hooks").toFile().exists())
    }

    @Test
    fun `multiple hooks for same event are grouped`() {
        val hooks = listOf(
            HookMatcher.Builder(HookEvent.PostToolUse)
                .matcher("Write")
                .command("cmd1")
                .build(),
            HookMatcher.Builder(HookEvent.PostToolUse)
                .matcher("Edit")
                .command("cmd2")
                .build()
        )

        val json = hooks.toClaudeCodeHooksJson()

        // Both should be under PostToolUse array
        assertTrue(json.contains("\"PostToolUse\""))
        assertTrue(json.contains("\"Write\""))
        assertTrue(json.contains("\"Edit\""))
    }
}
