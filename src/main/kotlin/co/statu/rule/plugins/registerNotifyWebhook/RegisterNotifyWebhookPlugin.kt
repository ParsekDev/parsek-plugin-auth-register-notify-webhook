package co.statu.rule.plugins.registerNotifyWebhook

import co.statu.parsek.api.ParsekPlugin
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions

class RegisterNotifyWebhookPlugin : ParsekPlugin() {
    override suspend fun onCreate() {
        val webClient = WebClient.create(vertx, WebClientOptions())

        pluginBeanContext.beanFactory.registerSingleton(webClient.javaClass.name, webClient)
    }
}

