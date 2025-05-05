package org.socius.sociuswebbackend.config;


import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Autowired
    private ConfigService configService;

    // Tên exchange mặc định
    public static final String SESSION_MANAGEMENT_EXCHANGE = "session.management";

    // Tên queue mặc định
    public static final String SESSION_INVALIDATION_QUEUE = "session.invalidation";

    // Routing key mặc định
    public static final String INVALIDATE_SESSION_ROUTING_KEY = "invalidate.session";


    // Tạo TopicExchange cho việc quản lý phiên
    @Bean
    public TopicExchange sessionManagementExchange() {
        return new TopicExchange(
                configService.getString("rabbitmq.exchange.session", SESSION_MANAGEMENT_EXCHANGE)
        );
    }

    // Tạo queue cho việc hủy phiên
    @Bean
    public Queue sessionInvalidationQueue() {
        String queueName = configService.getString("rabbitmq.queue.invalidation", SESSION_INVALIDATION_QUEUE);

        return new Queue(queueName, true);
    }

    // Tạo binding giữa exchange và queue với routing key
    @Bean
    public Binding sessionInvalidationBinding() {
        return BindingBuilder.bind(sessionInvalidationQueue())
                .to(sessionManagementExchange())
                .with(configService.getString("rabbitmq.routing.invalidate", INVALIDATE_SESSION_ROUTING_KEY));
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

}
