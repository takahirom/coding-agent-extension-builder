package io.github.takahirom.codingagentextension.model

data class Author(
    val name: String,
    val email: String? = null
) {
    class Builder(private val name: String) {
        private var email: String? = null

        fun email(email: String) = apply { this.email = email }

        fun build(): Author = Author(name = name, email = email)
    }
}
