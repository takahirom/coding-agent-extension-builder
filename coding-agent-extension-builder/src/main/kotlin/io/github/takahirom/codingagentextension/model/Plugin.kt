package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Plugin(
    val name: String,
    val description: String,
    val version: String,
    val author: Author? = null,
    val commands: List<Command> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val subagents: List<Subagent> = emptyList(),
    val hooks: List<HookMatcher> = emptyList()
) {
    class Builder(
        private val name: String,
        private val description: String,
        private val version: String
    ) {
        private var author: Author? = null
        private val commands = mutableListOf<Command>()
        private val skills = mutableListOf<Skill>()
        private val subagents = mutableListOf<Subagent>()
        private val hooks = mutableListOf<HookMatcher>()

        fun author(name: String, block: Author.Builder.() -> Unit = {}) = apply {
            this.author = Author.Builder(name).apply(block).build()
        }

        fun addCommand(command: Command) = apply {
            commands.add(command)
        }

        fun addSkill(skill: Skill) = apply {
            skills.add(skill)
        }

        fun addSubagent(subagent: Subagent) = apply {
            subagents.add(subagent)
        }

        fun addHook(hook: HookMatcher) = apply {
            hooks.add(hook)
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
                skills = skills.toList(),
                subagents = subagents.toList(),
                hooks = hooks.toList()
            )
        }
    }
}
