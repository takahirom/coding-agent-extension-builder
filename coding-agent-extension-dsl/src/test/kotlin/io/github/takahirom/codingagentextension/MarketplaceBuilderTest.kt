package io.github.takahirom.codingagentextension

import io.github.takahirom.codingagentextension.claudecode.toClaudeCodeMarketplaceJson
import io.github.takahirom.codingagentextension.claudecode.writeToClaudeCode
import io.github.takahirom.codingagentextension.model.Marketplace
import io.github.takahirom.codingagentextension.model.Owner
import io.github.takahirom.codingagentextension.model.gitHubSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarketplaceBuilderTest {

    @Test
    fun `marketplace with required fields`() {
        val mp = Marketplace.Builder(
            name = "my-marketplace",
            owner = Owner(name = "Test Owner")
        ).build()

        assertEquals("my-marketplace", mp.name)
        assertEquals("Test Owner", mp.owner.name)
    }

    @Test
    fun `marketplace with plugins`() {
        val mp = Marketplace.Builder(
            name = "my-marketplace",
            owner = Owner(name = "Owner", email = "owner@example.com")
        )
            .addPlugin(
                name = "formatter",
                source = "./plugins/formatter",
                description = "Code formatter"
            )
            .addPlugin(
                name = "linter",
                source = gitHubSource("company/linter"),
                description = "Code linter"
            )
            .build()

        assertEquals(2, mp.plugins.size)
        assertEquals("formatter", mp.plugins[0].name)
        assertEquals("linter", mp.plugins[1].name)
    }

    @Test
    fun `marketplace generates valid JSON`() {
        val mp = Marketplace.Builder(
            name = "test-mp",
            owner = Owner(name = "Owner")
        )
            .addPlugin(
                name = "test-plugin",
                source = "./test",
                description = "Test"
            )
            .build()

        val json = mp.toClaudeCodeMarketplaceJson()

        assertTrue(json.contains("\"name\": \"test-mp\""))
        assertTrue(json.contains("\"owner\""))
        assertTrue(json.contains("\"plugins\""))
    }

    @Test
    fun `marketplace writes correct directory structure`(@TempDir tempDir: Path) {
        val mp = Marketplace.Builder(
            name = "my-marketplace",
            owner = Owner(name = "Owner")
        )
            .addPlugin(name = "plugin1", source = "./p1")
            .build()

        mp.writeToClaudeCode(tempDir)

        assertTrue(tempDir.resolve("my-marketplace/.claude-plugin/marketplace.json").toFile().exists())
    }
}
