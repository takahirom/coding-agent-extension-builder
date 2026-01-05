package io.github.takahirom.codingagentextension.validation

/**
 * Validation rules based on Agent Skills specification.
 * https://agentskills.org/specification
 */
object Validators {
    private val NAME_PATTERN = Regex("^[a-z0-9-]+$")
    private const val NAME_MAX_LENGTH = 64
    private const val DESCRIPTION_MAX_LENGTH = 1024

    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult.Invalid("Name cannot be blank")
        }
        if (!NAME_PATTERN.matches(name)) {
            return ValidationResult.Invalid(
                "Name '$name' should be hyphen-case (lowercase letters, digits, and hyphens only)"
            )
        }
        if (name.startsWith("-") || name.endsWith("-")) {
            return ValidationResult.Invalid(
                "Name '$name' cannot start or end with hyphen"
            )
        }
        if ("--" in name) {
            return ValidationResult.Invalid(
                "Name '$name' cannot contain consecutive hyphens"
            )
        }
        if (name.length > NAME_MAX_LENGTH) {
            return ValidationResult.Invalid(
                "Name is too long (${name.length} characters). Maximum is $NAME_MAX_LENGTH characters."
            )
        }
        return ValidationResult.Valid
    }

    fun validateDescription(description: String): ValidationResult {
        if (description.isBlank()) {
            return ValidationResult.Invalid("Description cannot be blank")
        }
        if ('<' in description || '>' in description) {
            return ValidationResult.Invalid(
                "Description cannot contain angle brackets (< or >)"
            )
        }
        if (description.length > DESCRIPTION_MAX_LENGTH) {
            return ValidationResult.Invalid(
                "Description is too long (${description.length} characters). Maximum is $DESCRIPTION_MAX_LENGTH characters."
            )
        }
        return ValidationResult.Valid
    }
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult

    fun isValid(): Boolean = this is Valid

    fun throwIfInvalid() {
        if (this is Invalid) {
            throw ValidationException(message)
        }
    }
}

class ValidationException(message: String) : IllegalArgumentException(message)
