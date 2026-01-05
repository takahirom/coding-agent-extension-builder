package io.github.takahirom.codingagentextension

import io.github.takahirom.codingagentextension.claudecode.toClaudeCodeString
import io.github.takahirom.codingagentextension.claudecode.writeClaudeCodeExtension
import io.github.takahirom.codingagentextension.model.Skill
import io.github.takahirom.codingagentextension.validation.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkillBuilderTest {

    @Test
    fun `skill with required fields only`() {
        val skill = Skill.Builder(
            name = "pdf-processor",
            description = "Extract text from PDFs",
            body = "# PDF Processing"
        ).build()

        assertEquals("pdf-processor", skill.name)
        assertEquals("Extract text from PDFs", skill.description)
        assertEquals("# PDF Processing", skill.body)
    }

    @Test
    fun `skill with all optional fields`() {
        val skill = Skill.Builder(
            name = "pdf-processor",
            description = "Extract text from PDFs",
            body = "# PDF Processing"
        )
            .license("MIT")
            .allowedTools("Read", "Grep", "Glob")
            .addMetadata("version", "1.0.0")
            .build()

        assertEquals("MIT", skill.license)
        assertEquals(listOf("Read", "Grep", "Glob"), skill.allowedTools)
        assertEquals(mapOf("version" to "1.0.0"), skill.metadata)
    }

    @Test
    fun `skill generates valid SKILL md`() {
        val skill = Skill.Builder(
            name = "test-skill",
            description = "A test skill",
            body = "# Test\n\nThis is a test."
        )
            .license("MIT")
            .allowedTools("Read")
            .build()

        val output = skill.toClaudeCodeString()

        assertTrue(output.contains("---"))
        assertTrue(output.contains("name: test-skill"))
        assertTrue(output.contains("description: A test skill"))
        assertTrue(output.contains("license: MIT"))
        assertTrue(output.contains("allowed-tools: Read"))
        assertTrue(output.contains("# Test"))
    }

    @Test
    fun `skill writes to correct directory structure`(@TempDir tempDir: Path) {
        val skill = Skill.Builder(
            name = "my-skill",
            description = "Test skill",
            body = "# Content"
        ).build()

        skill.writeClaudeCodeExtension(tempDir)

        val skillFile = tempDir.resolve("my-skill/SKILL.md")
        assertTrue(skillFile.toFile().exists())
    }

    @Test
    fun `invalid name throws ValidationException`() {
        assertThrows<ValidationException> {
            Skill.Builder(
                name = "Invalid Name",
                description = "Test",
                body = "# Test"
            ).build()
        }
    }

    @Test
    fun `name with consecutive hyphens throws ValidationException`() {
        assertThrows<ValidationException> {
            Skill.Builder(
                name = "invalid--name",
                description = "Test",
                body = "# Test"
            ).build()
        }
    }

    @Test
    fun `description with angle brackets throws ValidationException`() {
        assertThrows<ValidationException> {
            Skill.Builder(
                name = "valid-name",
                description = "Test <script>",
                body = "# Test"
            ).build()
        }
    }
}
