package org.socius.sociuswebbackend.config;


import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Tạo TopicExchange cho việc quản lý phiên
    @Bean
    public TopicExchange sessionManagementExchange() {
        return new TopicExchange(RabbitMQKeyBuilder.getSessionManagementExchange());
    }

    // Tạo queue cho việc hủy phiên
    @Bean
    public Queue sessionInvalidationQueue() {
        return new Queue(RabbitMQKeyBuilder.getSessionInvalidationQueue(), true);
    }

    // Tạo binding giữa exchange và queue với routing key
    @Bean
    public Binding sessionInvalidationBinding() {
        return BindingBuilder.bind(sessionInvalidationQueue())
                .to(sessionManagementExchange())
                .with(RabbitMQKeyBuilder.getInvalidateSessionRoutingKey());
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
