package com.rabbitmq.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rabbitmq.config.AmqpConfig;
import com.rabbitmq.domain.User;

@RestController
public class AmqpController {

	@Autowired
    RabbitTemplate rabbitTemplate;
	
	@RequestMapping("/greeting")
	public String hello(String message) {
	/*	User user = new User();
		user.setName(message);*/
		rabbitTemplate.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTINGKEY, message);
		return message;
	}
}
