package org.socius.sociuswebbackend.config;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatRabbitMQConfig {

    final private ConfigService configService;

    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String PRIVATE_QUEUE = "chat.private.queue";
    public static final String GROUP_QUEUE = "chat.group.queue";
    public static final String READ_RECEIPT_QUEUE = "chat.receipt.queue";
    public static final String PRIVATE_ROUTING_KEY = "chat.message.private.*";
    public static final String GROUP_ROUTING_KEY = "chat.message.group.*";
    public static final String READ_RECEIPT_ROUTING_KEY = "chat.receipt.*";
    public static final String CHAT_DLX_EXCHANGE = "chat.dlx";
    public static final String CHAT_DLX_QUEUE = "chat.dlq";
    public static final String CHAT_DLX_ROUTING_KEY = "chat.message.failed";

    @Bean
    public TopicExchange chatExchange() {
        return ExchangeBuilder.topicExchange(configService.getString("rabbitmq.exchange.chat", CHAT_EXCHANGE))
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(configService.getString("rabbitmq.exchange.dlx", CHAT_DLX_EXCHANGE))
                .durable(true)
                .build();
    }

    @Bean
    public Queue privateMessageQueue() {
        return QueueBuilder
                .durable(configService.getString("rabbitmq.queue.private", PRIVATE_QUEUE))
                .withArgument("x-dead-letter-exchange", configService.getString("rabbitmq.exchange.dlx", CHAT_DLX_EXCHANGE))
                .withArgument("x-dead-letter-routing-key", configService.getString("rabbitmq.routing.dlq", CHAT_DLX_ROUTING_KEY))
                .build();
    }

    @Bean
    public Queue groupMessageQueue() {
        return QueueBuilder
                .durable(configService.getString("rabbitmq.queue.group", GROUP_QUEUE))
                .withArgument("x-dead-letter-exchange", configService.getString("rabbitmq.exchange.dlx", CHAT_DLX_EXCHANGE))
                .withArgument("x-dead-letter-routing-key", configService.getString("rabbitmq.routing.dlq", CHAT_DLX_ROUTING_KEY))
                .build();
    }

    @Bean
    public Queue readReceiptQueue() {
        return QueueBuilder
                .durable(configService.getString("rabbitmq.queue.receipt", READ_RECEIPT_QUEUE))
                .withArgument("x-dead-letter-exchange", configService.getString("rabbitmq.exchange.dlx", CHAT_DLX_EXCHANGE))
                .withArgument("x-dead-letter-routing-key", configService.getString("rabbitmq.routing.dlq", CHAT_DLX_ROUTING_KEY))
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(configService.getString("rabbitmq.queue.dlq", CHAT_DLX_QUEUE))
                .withArgument("x-message-ttl", configService.getInt("rabbitmq.dlx.message.ttl", 86400000)) // Mặc định 24h
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(configService.getString("rabbitmq.routing.dlq", CHAT_DLX_ROUTING_KEY));
    }

    @Bean
    public Binding privateMessageBinding() {
        return BindingBuilder
                .bind(privateMessageQueue())
                .to(chatExchange())
                .with(configService.getString("rabbitmq.routing.private", PRIVATE_ROUTING_KEY));
    }

    @Bean
    public Binding groupMessageBinding() {
        return BindingBuilder
                .bind(groupMessageQueue())
                .to(chatExchange())
                .with(configService.getString("rabbitmq.routing.group", GROUP_ROUTING_KEY));
    }

    @Bean
    public Binding readReceiptBinding() {
        return BindingBuilder
                .bind(readReceiptQueue())
                .to(chatExchange())
                .with(configService.getString("rabbitmq.routing.receipt", READ_RECEIPT_ROUTING_KEY));
    }


    @Bean
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
