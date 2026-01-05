package io.github.takahirom.codingagentextension.model

import io.github.takahirom.codingagentextension.validation.Validators

data class Marketplace(
    val name: String,
    val owner: Owner,
    val plugins: List<MarketplacePlugin> = emptyList(),
    val metadata: MarketplaceMetadata? = null,
    val embeddedPlugins: List<EmbeddedPlugin> = emptyList()
) {
    class Builder(
        private val name: String,
        private val owner: Owner
    ) {
        private val plugins = mutableListOf<MarketplacePlugin>()
        private val embeddedPlugins = mutableListOf<EmbeddedPlugin>()
        private var metadata: MarketplaceMetadata? = null

        /**
         * Add a Plugin object directly. The plugin will be written to ./plugins/<name>/
         * when writeClaudeCodeExtension() is called.
         */
        fun addPlugin(plugin: Plugin) = apply {
            val relativePath = "./plugins/${plugin.name}"
            plugins.add(
                MarketplacePlugin(
                    name = plugin.name,
                    source = PluginSource.RelativePath(relativePath),
                    description = plugin.description,
                    version = plugin.version,
                    author = plugin.author
                )
            )
            embeddedPlugins.add(EmbeddedPlugin(relativePath, plugin))
        }

        /**
         * Add a reference to an external plugin by path (not embedded).
         */
        fun addPlugin(
            name: String,
            source: String,
            description: String? = null,
            version: String? = null
        ) = apply {
            plugins.add(
                MarketplacePlugin(
                    name = name,
                    source = PluginSource.RelativePath(source),
                    description = description,
                    version = version
                )
            )
        }

        /**
         * Add a reference to an external plugin (GitHub, Git URL, etc.).
         */
        fun addPlugin(
            name: String,
            source: PluginSource,
            description: String? = null,
            version: String? = null
        ) = apply {
            plugins.add(
                MarketplacePlugin(
                    name = name,
                    source = source,
                    description = description,
                    version = version
                )
            )
        }

        fun metadata(
            description: String? = null,
            version: String? = null,
            pluginRoot: String? = null
        ) = apply {
            this.metadata = MarketplaceMetadata(
                description = description,
                version = version,
                pluginRoot = pluginRoot
            )
        }

        fun build(): Marketplace {
            Validators.validateName(name).throwIfInvalid()

            return Marketplace(
                name = name,
                owner = owner,
                plugins = plugins.toList(),
                metadata = metadata,
                embeddedPlugins = embeddedPlugins.toList()
            )
        }
    }
}

data class EmbeddedPlugin(
    val relativePath: String,
    val plugin: Plugin
)

data class Owner(
    val name: String,
    val email: String? = null
)

data class MarketplaceMetadata(
    val description: String? = null,
    val version: String? = null,
    val pluginRoot: String? = null
)

data class MarketplacePlugin(
    val name: String,
    val source: PluginSource,
    val description: String? = null,
    val version: String? = null,
    val author: Author? = null
)

sealed interface PluginSource {
    data class RelativePath(val path: String) : PluginSource
    data class GitHub(val repo: String) : PluginSource
    data class Git(val url: String) : PluginSource
}

// Helper functions for PluginSource
fun gitHubSource(repo: String): PluginSource = PluginSource.GitHub(repo)
fun gitSource(url: String): PluginSource = PluginSource.Git(url)
