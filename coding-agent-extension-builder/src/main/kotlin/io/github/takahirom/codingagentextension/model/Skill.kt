package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Skill(
    val name: String,
    val description: String,
    val body: String,
    val license: String? = null,
    val allowedTools: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
) {
    class Builder(
        private val name: String,
        private val description: String,
        private val body: String
    ) {
        private var license: String? = null
        private var allowedTools: List<String> = emptyList()
        private var metadata: MutableMap<String, String> = mutableMapOf()

        fun license(license: String) = apply { this.license = license }

        fun allowedTools(vararg tools: String) = apply { this.allowedTools = tools.toList() }

        fun addMetadata(key: String, value: String) = apply { this.metadata[key] = value }

        fun build(): Skill {
            Validators.validateName(name).throwIfInvalid()
            Validators.validateDescription(description).throwIfInvalid()

            return Skill(
                name = name,
                description = description,
                body = body,
                license = license,
                allowedTools = allowedTools,
                metadata = metadata.toMap()
            )
        }
    }
}
