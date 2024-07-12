package co.statu.rule.plugins.registerNotifyWebhook.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.rule.auth.db.model.User
import co.statu.rule.auth.event.AuthEventListener
import co.statu.rule.plugins.registerNotifyWebhook.RegisterNotifyWebhookConfig
import co.statu.rule.plugins.registerNotifyWebhook.RegisterNotifyWebhookPlugin
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@EventListener
class AuthEventHandler(private val registerNotifyWebhookPlugin: RegisterNotifyWebhookPlugin) : AuthEventListener {
    private val webClient by lazy {
        registerNotifyWebhookPlugin.pluginBeanContext.getBean(WebClient::class.java)
    }

    private val pluginConfigManager by lazy {
        registerNotifyWebhookPlugin.pluginBeanContext.getBean(PluginConfigManager::class.java) as PluginConfigManager<RegisterNotifyWebhookConfig>
    }

    private val config by lazy {
        pluginConfigManager.config
    }

    private val discordWebhookConfig by lazy {
        config.discordWebhookConfig
    }

    override suspend fun onRegistrationComplete(user: User) {
        if (!discordWebhookConfig.enabled) {
            return
        }

        val instant = Instant.ofEpochMilli(user.registerDate)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        val requestBody = JsonObject()

        requestBody.put("content", null)
        requestBody.put("username", discordWebhookConfig.username)
        requestBody.put("avatar_url", discordWebhookConfig.avatarUrl)
        requestBody.put("attachments", emptyList<Any?>())
        requestBody.put("flags", discordWebhookConfig.flags)

        val embeds = JsonObject()

        embeds.put("title", discordWebhookConfig.titleText)
        embeds.put("color", discordWebhookConfig.titleColor)
        embeds.put(
            "fields", listOf(
                JsonObject()
                    .put("name", discordWebhookConfig.nameText)
                    .put("value", user.additionalFields.getString("name"))
                    .put("inline", true),
                JsonObject()
                    .put("name", discordWebhookConfig.surnameText)
                    .put("value", user.additionalFields.getString("surname"))
                    .put("inline", true),
                JsonObject()
                    .put("name", discordWebhookConfig.emailText)
                    .put("value", "```${user.email}```")
            )
        )
        embeds.put("footer", JsonObject().put("text", discordWebhookConfig.username))
        embeds.put("timestamp", dateTime.toString())

        requestBody.put("embeds", listOf(embeds))

        webClient
            .postAbs(discordWebhookConfig.url)
            .sendJsonObject(requestBody)
    }
}