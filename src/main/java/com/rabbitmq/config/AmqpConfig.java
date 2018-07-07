package com.rabbitmq.config;


import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.rabbitmq.direct.Receiver;

@EnableRabbit
@Configuration
public class AmqpConfig {
    public static final String EXCHANGE = "spring.boot.direct";
    public static final String ROUTINGKEY_FAIL = "spring.boot.routingKey.failure";
    public static final String ROUTINGKEY = "spring.boot.routingKey";
    public static final String QUEUE_NAME = "spring.demo";
    public static final String QUEUE_NAME_FAIL = "spring.demo.failure";

    //RabbitMQ的配置信息
    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${spring.rabbitmq.port}")
    private Integer port;
    @Value("${spring.rabbitmq.username}")
    private String username;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${spring.rabbitmq.virtual-host}")
    private String virtualHost;


    //建立一个连接容器，类型数据库的连接池
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        //发布确认，template要求CachingConnectionFactory的publisherConfirms属性设置为true
        connectionFactory.setPublisherConfirms(true);// 发送确认机制
        return connectionFactory;
    }

    // RabbitMQ的使用入口
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(this.connectionFactory());
        template.setMessageConverter(this.jsonMessageConverter());
        template.setMandatory(true);
        return template;
    }

    /**
     * 交换机
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * 队列
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true); //队列持久

    }

    @Bean
    public Queue queueFail() {
        return new Queue(QUEUE_NAME_FAIL, true); //队列持久

    }
    /**
     * 绑定
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue()).to(exchange()).with(AmqpConfig.ROUTINGKEY);
    }

    @Bean
    public Binding bindingFail(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queueFail()).to(exchange()).with(AmqpConfig.ROUTINGKEY_FAIL);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    Receiver receiver(){
        return new Receiver();
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "onMessage");
    }

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.setQueueNames(AmqpConfig.QUEUE_NAME);
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(listenerAdapter);
        return container;
    }



    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

}
