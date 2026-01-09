package io.github.takahirom.codingagentextension.model

/**
 * Hook events supported by Claude Code.
 */
enum class HookEvent {
    PreToolUse,
    PostToolUse,
    PermissionRequest,
    Notification,
    UserPromptSubmit,
    Stop,
    SubagentStop,
    PreCompact,
    SessionStart,
    SessionEnd
}

/**
 * A single hook command or prompt configuration.
 */
sealed class HookCommand {
    abstract val timeout: Int?

    data class Command(
        val command: String,
        override val timeout: Int? = null
    ) : HookCommand()

    data class Prompt(
        val prompt: String,
        override val timeout: Int? = null
    ) : HookCommand()
}

/**
 * A hook matcher configuration that groups hooks by pattern.
 */
data class HookMatcher(
    val event: HookEvent,
    val matcher: String? = null,
    val hooks: List<HookCommand>
) {
    class Builder(
        private val event: HookEvent,
        hook: HookCommand
    ) {
        private var matcher: String? = null
        private val hooks = mutableListOf(hook)

        /**
         * Set the matcher pattern for tool-based events (PreToolUse, PostToolUse, PermissionRequest).
         * Supports exact match ("Write") or regex ("Write|Edit", "Bash.*").
         * Use "*" or omit to match all tools.
         */
        fun matcher(pattern: String) = apply { this.matcher = pattern }

        /**
         * Add a command hook.
         * @param command The bash command to execute. Use ${CLAUDE_PLUGIN_ROOT} for plugin-relative paths.
         */
        fun addCommand(command: HookCommand.Command) = apply {
            hooks.add(command)
        }

        /**
         * Add a prompt hook (LLM-based evaluation).
         * @param prompt The prompt to send to the LLM. Use $ARGUMENTS for hook input.
         */
        fun addPrompt(prompt: HookCommand.Prompt) = apply {
            hooks.add(prompt)
        }

        fun build(): HookMatcher {
            require(hooks.isNotEmpty()) { "At least one hook command or prompt is required" }
            return HookMatcher(
                event = event,
                matcher = matcher,
                hooks = hooks.toList()
            )
        }
    }
}
