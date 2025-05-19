package org.socius.sociuswebbackend.config;

import java.util.List;

import org.mockito.Mockito;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public ConfigService configService() {
        ConfigService mockConfigService = mock(ConfigService.class);

        when(mockConfigService.getString("cookie.same.site", "Lax")).thenReturn("Lax");
        when(mockConfigService.getString("cookie.name", "SOCIUS_SESSION")).thenReturn("SOCIUS_SESSION");
        when(mockConfigService.getString("cookie.path", "/")).thenReturn("/");

        // Cấu hình mock cho các phương thức getInt
        when(mockConfigService.getInt("session.duration.minutes", 30)).thenReturn(30);
        when(mockConfigService.getInt("session_timeout", 30)).thenReturn(30);
        when(mockConfigService.getInt("session_extension_threshold", 2)).thenReturn(2);

        // Cấu hình mock cho các phương thức getList
        when(mockConfigService.getList("cors.allowed.origins"))
                .thenReturn(List.of("http://localhost:3000"));

        // Cấu hình mock cho RBAC settings
        when(mockConfigService.getString("rbac.key.prefix", "rbac:")).thenReturn("rbac:");
        when(mockConfigService.getString("rbac.role.users.prefix", "role:users:")).thenReturn("role:users:");

        // Cấu hình cho các phương thức mới
        when(mockConfigService.getProperty(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(mockConfigService.getSetting(anyString(), anyString()))
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
        when(queue.getName()).thenReturn(RabbitMQKeyBuilder.getSessionInvalidationQueue());
        return queue;
    }

    @Bean("testSessionManagementExchange")
    @Primary
    public TopicExchange sessionManagementExchange() {
        TopicExchange exchange = Mockito.mock(TopicExchange.class);
        when(exchange.getName()).thenReturn(RabbitMQKeyBuilder.getSessionManagementExchange());
        return exchange;
    }

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return Mockito.mock(ConnectionFactory.class);
    }

    @Bean
    @Primary
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        return factory;
    }
}