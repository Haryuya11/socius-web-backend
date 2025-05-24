package org.socius.sociuswebbackend.config;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class ChatRabbitMQConfig {

    final private ConfigService configService;

    @Bean
    public TopicExchange chatExchange() {
        return ExchangeBuilder.topicExchange(RabbitMQKeyBuilder.getChatExchange())
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(RabbitMQKeyBuilder.getDeadLetterExchange())
                .durable(true)
                .build();
    }

    @Bean
    public Queue privateMessageQueue() {
        return QueueBuilder
                .durable(RabbitMQKeyBuilder.getPrivateMessageQueue())
                .withArgument("x-dead-letter-exchange", RabbitMQKeyBuilder.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", RabbitMQKeyBuilder.getDeadLetterRoutingKey())
                .withArgument("x-max-retries", configService.getInt("rabbitmq.max.retries", 3))
                .build();
    }

    @Bean
    public Queue groupMessageQueue() {
        return QueueBuilder
                .durable(RabbitMQKeyBuilder.getGroupMessageQueue())
                .withArgument("x-dead-letter-exchange", RabbitMQKeyBuilder.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", RabbitMQKeyBuilder.getDeadLetterRoutingKey())
                .withArgument("x-max-retries", configService.getInt("rabbitmq.max.retries", 3))
                .build();
    }

    @Bean
    public Queue readReceiptQueue() {
        return QueueBuilder
                .durable(RabbitMQKeyBuilder.getReadReceiptQueue())
                .withArgument("x-dead-letter-exchange", RabbitMQKeyBuilder.getDeadLetterExchange())
                .withArgument("x-dead-letter-routing-key", RabbitMQKeyBuilder.getDeadLetterRoutingKey())
                .withArgument("x-max-retries", configService.getInt("rabbitmq.max.retries", 3))
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMQKeyBuilder.getDeadLetterQueue())
                .withArgument("x-message-ttl", configService.getInt("rabbitmq.dlx.message.ttl", 86400000))
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMQKeyBuilder.getDeadLetterRoutingKey());
    }

    @Bean
    public Binding privateMessageBinding() {
        return BindingBuilder
                .bind(privateMessageQueue())
                .to(chatExchange())
                .with(RabbitMQKeyBuilder.getPrivateRoutingKey());
    }

    @Bean
    public Binding groupMessageBinding() {
        return BindingBuilder
                .bind(groupMessageQueue())
                .to(chatExchange())
                .with(RabbitMQKeyBuilder.getGroupRoutingKey());
    }

    @Bean
    public Binding readReceiptBinding() {
        return BindingBuilder
                .bind(readReceiptQueue())
                .to(chatExchange())
                .with(RabbitMQKeyBuilder.getReadReceiptRoutingKey());
    }


    @Bean
    @Profile("!test")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);

        // Cấu hình prefetch - chỉ lấy số lượng message nhất định, tránh overload consumer
        factory.setPrefetchCount(configService.getInt("rabbitmq.prefetch.count", 10));

        // Số lượng consumer đồng thời xử lý tin nhắn
        factory.setConcurrentConsumers(configService.getInt("rabbitmq.concurrent.consumers", 3));
        factory.setMaxConcurrentConsumers(configService.getInt("rabbitmq.max.concurrent.consumers", 10));

        // Không báo lỗi khi queue không tồn tại
        factory.setMissingQueuesFatal(false);

        // Cấu hình timeout cho consumer
        factory.setReceiveTimeout((long) configService.getInt("rabbitmq.receive.timeout", 500));

        // Cấu hình retry khi nhận message
        factory.setDefaultRequeueRejected(false); // Không đưa tin nhắn trở lại queue khi xử lý thất bại
        factory.setAdviceChain(RetryInterceptorBuilder
                .stateless()
                .maxAttempts(configService.getInt("rabbitmq.retry.max.attempts", 3))
                .backOffOptions(
                        configService.getInt("rabbitmq.retry.initial.interval", 1000),
                        configService.getDouble("rabbitmq.retry.multiplier", 2.0),
                        configService.getInt("rabbitmq.retry.max.interval", 10000))
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build());

        return factory;
    }
}
