package com.sionic.app.domain.chat

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sionic.app.exception.OpenAiException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Component
class OpenAiClient(private val openAiRestClient: RestClient) {

    private val mapper = jacksonObjectMapper()

    fun complete(model: String, messages: List<OpenAiMessage>): String {
        return try {
            val response = openAiRestClient.post()
                .uri("/chat/completions")
                .body(OpenAiChatRequest(model = model, messages = messages))
                .retrieve()
                .body(OpenAiChatResponse::class.java)
                ?: throw OpenAiException("OpenAI로부터 빈 응답을 받았습니다.")
            response.choices.firstOrNull()?.message?.content
                ?: throw OpenAiException("OpenAI 응답에 content가 없습니다.")
        } catch (e: OpenAiException) {
            throw e
        } catch (e: Exception) {
            throw OpenAiException("OpenAI API 호출 실패: ${e.message}")
        }
    }

    fun stream(
        model: String,
        messages: List<OpenAiMessage>,
        emitter: SseEmitter,
        isCancelled: () -> Boolean,
        onComplete: (String) -> Unit
    ) {
        try {
            openAiRestClient.post()
                .uri("/chat/completions")
                .body(OpenAiChatRequest(model = model, messages = messages, stream = true))
                .exchange { _, response ->
                    if (response.statusCode.isError) {
                        throw OpenAiException("OpenAI API 오류: ${response.statusCode.value()}")
                    }
                    val sb = StringBuilder()
                    response.body.bufferedReader().use { reader ->
                        // forEachLine 대신 lineSequence + break로 취소 시 즉시 루프 이탈 → use가 reader를 close하여 upstream 연결 해제
                        for (line in reader.lineSequence()) {
                            if (isCancelled()) return@exchange
                            if (!line.startsWith("data: ")) continue
                            val data = line.removePrefix("data: ").trim()
                            if (data == "[DONE]") {
                                onComplete(sb.toString())
                                runCatching { emitter.send(SseEmitter.event().data("[DONE]")) }
                                runCatching { emitter.complete() }
                                return@exchange
                            }
                            val content = runCatching {
                                mapper.readTree(data)?.at("/choices/0/delta/content")
                                    ?.asText()?.takeIf { it.isNotEmpty() }
                            }.getOrNull() ?: continue
                            sb.append(content)
                            runCatching { emitter.send(SseEmitter.event().data(content)) }
                        }
                    }
                    // [DONE] 없이 스트림 종료
                    runCatching { emitter.complete() }
                }
        } catch (e: Exception) {
            if (isCancelled()) return
            runCatching {
                emitter.send(SseEmitter.event().data("[ERROR]"))
                emitter.complete()
            }
            if (e is OpenAiException) throw e
            throw OpenAiException("OpenAI 스트리밍 실패: ${e.message}")
        }
    }
}

data class OpenAiMessage(val role: String, val content: String)

data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val stream: Boolean = false
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChatResponse(val choices: List<OpenAiChoice>)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChoice(
    val message: OpenAiChoiceMessage?,
    @JsonProperty("finish_reason") val finishReason: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChoiceMessage(val role: String, val content: String)
