package org.socius.sociuswebbackend.config;

import java.util.List;

import org.mockito.Mockito;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ConfigService configService() {
        ConfigService mockConfigService = Mockito.mock(ConfigService.class);

        Mockito.when(mockConfigService.getString("cookie.same.site", "Lax")).thenReturn("Lax");
        Mockito.when(mockConfigService.getString("cookie.name", "SOCIUS_SESSION")).thenReturn("SOCIUS_SESSION");
        Mockito.when(mockConfigService.getString("cookie.path", "/")).thenReturn("/");

        // Cấu hình mock cho các phương thức getInt
        Mockito.when(mockConfigService.getInt("session.duration.minutes", 30)).thenReturn(30);
        Mockito.when(mockConfigService.getInt("session_timeout", 30)).thenReturn(30);
        Mockito.when(mockConfigService.getInt("session_extension_threshold", 2)).thenReturn(2);

        // Cấu hình mock cho các phương thức getList
        Mockito.when(mockConfigService.getList("cors.allowed.origins"))
                .thenReturn(List.of("http://localhost:3000"));

        // Cấu hình mock cho RabbitMQ settings
        Mockito.when(mockConfigService.getString("rabbitmq.exchange.session", RabbitMQConfig.SESSION_MANAGEMENT_EXCHANGE))
                .thenReturn("test.session.management");
        Mockito.when(mockConfigService.getString("rabbitmq.queue.invalidation", RabbitMQConfig.SESSION_INVALIDATION_QUEUE))
                .thenReturn("test.session.invalidation");
        Mockito.when(mockConfigService.getString("rabbitmq.routing.invalidate", RabbitMQConfig.INVALIDATE_SESSION_ROUTING_KEY))
                .thenReturn("test.invalidate.session");

        // Cấu hình mock cho RBAC settings
        Mockito.when(mockConfigService.getString("rbac.key.prefix", "rbac:")).thenReturn("rbac:");
        Mockito.when(mockConfigService.getString("rbac.role.users.prefix", "role:users:")).thenReturn("role:users:");

        // Cấu hình cho các phương thức mới
        Mockito.when(mockConfigService.getProperty(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        Mockito.when(mockConfigService.getSetting(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        return mockConfigService;
    }


    @Bean("testRabbitTemplate")
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }

    @Bean("testRabbitMQConfig")
    @Primary
    public RabbitMQConfig rabbitMQConfig() {
        return Mockito.mock(RabbitMQConfig.class);
    }

    @Bean("testSessionInvalidationQueue")
    @Primary
    public Queue sessionInvalidationQueue() {
        Queue queue = Mockito.mock(Queue.class);
        Mockito.when(queue.getName()).thenReturn("test.session.invalidation");
        return queue;
    }

    @Bean("testSessionManagementExchange")
    @Primary
    public TopicExchange sessionManagementExchange() {
        TopicExchange exchange = Mockito.mock(TopicExchange.class);
        Mockito.when(exchange.getName()).thenReturn("test.session.management");
        return exchange;
    }

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return Mockito.mock(ConnectionFactory.class);
    }
}