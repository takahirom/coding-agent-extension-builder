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
 * Type of hook execution.
 */
enum class HookType {
    Command,
    Prompt
}

/**
 * A single hook command or prompt configuration.
 */
data class HookCommand(
    val type: HookType,
    val command: String? = null,
    val prompt: String? = null,
    val timeout: Int? = null
)

/**
 * A hook matcher configuration that groups hooks by pattern.
 */
data class HookMatcher(
    val event: HookEvent,
    val matcher: String? = null,
    val hooks: List<HookCommand>
) {
    class Builder(private val event: HookEvent) {
        private var matcher: String? = null
        private val hooks = mutableListOf<HookCommand>()

        /**
         * Set the matcher pattern for tool-based events (PreToolUse, PostToolUse, PermissionRequest).
         * Supports exact match ("Write") or regex ("Write|Edit", "Bash.*").
         * Use "*" or omit to match all tools.
         */
        fun matcher(pattern: String) = apply { this.matcher = pattern }

        /**
         * Add a command hook.
         * @param command The bash command to execute. Use ${CLAUDE_PLUGIN_ROOT} for plugin-relative paths.
         * @param timeout Optional timeout in seconds.
         */
        fun command(command: String, timeout: Int? = null) = apply {
            hooks.add(HookCommand(type = HookType.Command, command = command, timeout = timeout))
        }

        /**
         * Add a prompt hook (LLM-based evaluation).
         * @param prompt The prompt to send to the LLM. Use $ARGUMENTS for hook input.
         * @param timeout Optional timeout in seconds.
         */
        fun prompt(prompt: String, timeout: Int? = null) = apply {
            hooks.add(HookCommand(type = HookType.Prompt, prompt = prompt, timeout = timeout))
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
