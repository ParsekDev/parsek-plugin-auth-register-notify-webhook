package co.statu.rule.plugins.registerNotifyWebhook.event

import co.statu.parsek.api.annotation.EventListener
import co.statu.parsek.api.config.PluginConfigManager
import co.statu.parsek.api.event.CoreEventListener
import co.statu.parsek.config.ConfigManager
import co.statu.rule.plugins.registerNotifyWebhook.RegisterNotifyWebhookConfig
import co.statu.rule.plugins.registerNotifyWebhook.RegisterNotifyWebhookPlugin
import org.slf4j.Logger

@EventListener
class ParsekEventHandler(
    private val registerNotifyWebhookPlugin: RegisterNotifyWebhookPlugin,
    private val logger: Logger
) : CoreEventListener {
    override suspend fun onConfigManagerReady(configManager: ConfigManager) {
        val pluginConfigManager = PluginConfigManager(
            configManager,
            registerNotifyWebhookPlugin,
            RegisterNotifyWebhookConfig::class.java,
            listOf(),
            listOf("register-notify-webhook")
        )

        registerNotifyWebhookPlugin.pluginBeanContext.beanFactory.registerSingleton(
            pluginConfigManager.javaClass.name,
            pluginConfigManager
        )

        logger.info("Initialized plugin config")
    }
}