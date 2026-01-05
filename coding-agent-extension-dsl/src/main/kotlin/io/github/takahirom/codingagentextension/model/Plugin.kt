package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Plugin(
    val name: String,
    val description: String,
    val version: String,
    val author: Author? = null,
    val commands: List<Command> = emptyList(),
    val skills: List<Skill> = emptyList()
) {
    class Builder(
        private val name: String,
        private val description: String,
        private val version: String
    ) {
        private var author: Author? = null
        private val commands = mutableListOf<Command>()
        private val skills = mutableListOf<Skill>()

        fun author(name: String, block: Author.Builder.() -> Unit = {}) = apply {
            this.author = Author.Builder(name).apply(block).build()
        }

        fun addCommand(name: String, description: String, body: String) = apply {
            commands.add(Command.Builder(name, description, body).build())
        }

        fun addSkill(name: String, description: String, body: String, block: Skill.Builder.() -> Unit = {}) = apply {
            skills.add(Skill.Builder(name, description, body).apply(block).build())
        }

        fun build(): Plugin {
            Validators.validateName(name).throwIfInvalid()
            Validators.validateDescription(description).throwIfInvalid()

            return Plugin(
                name = name,
                description = description,
                version = version,
                author = author,
                commands = commands.toList(),
                skills = skills.toList()
            )
        }
    }
}
