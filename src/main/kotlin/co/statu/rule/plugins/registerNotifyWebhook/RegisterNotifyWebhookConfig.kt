package co.statu.rule.plugins.registerNotifyWebhook

import co.statu.parsek.api.config.PluginConfig

data class RegisterNotifyWebhookConfig(
    val discordWebhookConfig: DiscordWebhookConfig = DiscordWebhookConfig()
) : PluginConfig() {
    companion object {
        data class DiscordWebhookConfig(
            val enabled: Boolean = false,
            val url: String = "",
            val username: String = "Parsek",
            val avatarUrl: String = "",
            val flags: Long = 4096,
            val titleText: String = "New Registration",
            val titleColor: Long = 1691988,
            val nameText: String = "Name",
            val surnameText: String = "Surname",
            val emailText: String = "E-Mail",
        )
    }
}