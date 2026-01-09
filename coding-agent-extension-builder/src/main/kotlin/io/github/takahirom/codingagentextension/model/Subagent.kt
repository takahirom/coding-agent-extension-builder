package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Subagent(
    val name: String,
    val description: String,
    val body: String,
    val tools: List<String> = emptyList(),
    val model: String? = null,
    val permissionMode: String? = null,
    val skills: List<String> = emptyList()
) {
    class Builder(
        private val name: String,
        private val description: String,
        private val body: String
    ) {
        private var tools: List<String> = emptyList()
        private var model: String? = null
        private var permissionMode: String? = null
        private var skills: List<String> = emptyList()

        fun tools(vararg tools: String) = apply { this.tools = tools.toList() }

        fun model(model: String) = apply { this.model = model }

        fun permissionMode(permissionMode: String) = apply { this.permissionMode = permissionMode }

        fun skills(vararg skills: String) = apply { this.skills = skills.toList() }

        fun build(): Subagent {
            Validators.validateName(name).throwIfInvalid()
            Validators.validateDescription(description).throwIfInvalid()

            return Subagent(
                name = name,
                description = description,
                body = body,
                tools = tools,
                model = model,
                permissionMode = permissionMode,
                skills = skills
            )
        }
    }
}
