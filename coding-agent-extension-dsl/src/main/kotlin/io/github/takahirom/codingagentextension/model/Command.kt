package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Command(
    val name: String,
    val description: String,
    val body: String
) {
    class Builder(
        private val name: String,
        private val description: String,
        private val body: String
    ) {
        fun build(): Command {
            Validators.validateName(name).throwIfInvalid()
            Validators.validateDescription(description).throwIfInvalid()

            return Command(name = name, description = description, body = body)
        }
    }
}
