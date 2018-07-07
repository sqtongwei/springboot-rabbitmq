package com.rabbitmq.direct;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.rabbitmq.client.Channel;
import com.rabbitmq.config.AmqpConfig;

/**
 * 监听的业务类，实现接口MessageListener
 */

// @RabbitListener(containerFactory = "rabbitListenerContainer", queues =
// AmqpConfig.QUEUE_NAME)
public class Receiver implements ChannelAwareMessageListener {
	public Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RabbitTemplate template;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {

		try {
			String msg = new String(message.getBody());
			System.out.println("已消费消息:" + msg);

			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);// 手动消息应答
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);// 对于处理不了的异常消息
			// 发送到失败队列
			template.convertAndSend(AmqpConfig.EXCHANGE, AmqpConfig.ROUTINGKEY_FAIL, new String(message.getBody()));
		}
	}

}