package io.brieflyz.ai_service.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import javax.net.ssl.SSLException

@Configuration
class WebClientConfig(
    private val props: AiServiceProperties,
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val HTTP_CONNECT_TIMEOUT_MILLIS = 30000
        const val HTTP_READ_TIMEOUT_SECONDS = 30
        const val HTTP_WRITE_TIMEOUT_SECONDS = 30
        const val HTTP_MAX_CONNECTIONS = 100
        const val HTTP_PENDING_ACQUIRE_TIMEOUT_SECONDS = 30L
        const val HTTP_MAX_IDLE_MINUTES = 1L
        const val SSL_HANDSHAKE_TIMEOUT_MILLIS = 30000L
    }

    @Bean
    fun imageAiWebClient(): WebClient = webClient().mutate()
        .baseUrl(props.image.url)
        .defaultHeaders {
            it.contentType = MediaType.APPLICATION_JSON
            it.accept = listOf(MediaType.APPLICATION_JSON)
            it.set("x-freepik-api-key", props.image.secret)
        }
        .build()

    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create(connectionProvider())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, HTTP_CONNECT_TIMEOUT_MILLIS)
            .secure {
                try {
                    it.sslContext(
                        SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE) // SSL 인증서 확인 비활성화
                            .build()
                    ).handshakeTimeoutMillis(SSL_HANDSHAKE_TIMEOUT_MILLIS)
                } catch (e: SSLException) {
                    throw RuntimeException(e.localizedMessage, e)
                }
            }
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(HTTP_READ_TIMEOUT_SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(HTTP_WRITE_TIMEOUT_SECONDS))
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .exchangeStrategies(exchangeStrategies())
            .build()
    }

    @Bean
    fun connectionProvider(): ConnectionProvider = ConnectionProvider.builder("")
        .maxConnections(HTTP_MAX_CONNECTIONS)
        .pendingAcquireTimeout(Duration.ofSeconds(HTTP_PENDING_ACQUIRE_TIMEOUT_SECONDS))
        .pendingAcquireMaxCount(-1)
        .maxIdleTime(Duration.ofMinutes(HTTP_MAX_IDLE_MINUTES))
        .build()

    @Bean
    fun exchangeStrategies(): ExchangeStrategies = ExchangeStrategies.builder()
        .codecs {
            it.defaultCodecs().jackson2JsonEncoder(
                Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
            )
            it.defaultCodecs().jackson2JsonDecoder(
                Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)
            )
            it.defaultCodecs().maxInMemorySize(1024 * 1024)
        }
        .build()
}