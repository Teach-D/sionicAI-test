package com.sionic.app.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class OpenAiConfig {

    @Bean
    fun openAiRestClient(
        builder: RestClient.Builder,
        @Value("\${app.openai.api-key}") apiKey: String,
        @Value("\${app.openai.base-url}") baseUrl: String
    ): RestClient = builder
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer $apiKey")
        .build()
}
