package io.github.takahirom.codingagentextension

import io.github.takahirom.codingagentextension.claudecode.writeClaudeCodeExtension
import io.github.takahirom.codingagentextension.model.Command
import io.github.takahirom.codingagentextension.model.Plugin
import io.github.takahirom.codingagentextension.model.Skill
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.test.assertTrue

/**
 * Integration test that generates skills to temp/generated-skills for validation
 * with the agentskills validator (skills-ref).
 */
class GenerationTest {

    private val outputDir: Path = Path.of(System.getProperty("user.dir"))
        .parent
        .resolve("temp/generated-skills")

    @Test
    fun `generate sample skill for validation`() {
        outputDir.createDirectories()

        val skill = Skill.Builder(
            name = "pdf-processor",
            description = "Extract text and tables from PDF files, fill forms, merge documents. Use when working with PDF documents.",
            body = """
                # PDF Processing

                ## Quick Start
                Use pdfplumber for text extraction:
                ```python
                import pdfplumber
                with pdfplumber.open("file.pdf") as pdf:
                    text = pdf.pages[0].extract_text()
                ```

                ## Capabilities
                - Extract text from PDF pages
                - Extract tables as structured data
                - Fill PDF forms
                - Merge multiple PDFs
            """.trimIndent()
        )
            .license("MIT")
            .allowedTools("Read", "Bash(python:*)")
            .addMetadata("author", "test-org")
            .addMetadata("version", "1.0.0")
            .build()

        skill.writeClaudeCodeExtension(outputDir)

        assertTrue(
            outputDir.resolve("pdf-processor/SKILL.md").exists(),
            "SKILL.md should be generated"
        )
    }

    @Test
    fun `generate plugin with skills for validation`() {
        outputDir.createDirectories()

        val plugin = Plugin.Builder(
            name = "code-quality",
            description = "Code quality tools for linting and formatting",
            version = "1.0.0"
        )
            .author(name = "Test Author") {
                email("test@example.com")
            }
            .addCommand(
                Command.Builder(
                    "lint",
                    "Run linter on the codebase",
                    "# Lint Command\nRun the appropriate linter for the project."
                ).build()
            )
            .addSkill(
                Skill.Builder(
                    "style-guide",
                    "Code style guidelines for consistent formatting across the project.",
                    """
                        # Style Guide

                        ## General Rules
                        - Use 4 spaces for indentation
                        - Max line length: 120 characters
                        - Use descriptive variable names
                    """.trimIndent()
                ).build()
            )
            .build()

        plugin.writeClaudeCodeExtension(outputDir)

        assertTrue(
            outputDir.resolve("code-quality/.claude-plugin/plugin.json").exists(),
            "plugin.json should be generated"
        )
        assertTrue(
            outputDir.resolve("code-quality/skills/style-guide/SKILL.md").exists(),
            "Skill SKILL.md should be generated"
        )
    }
}
