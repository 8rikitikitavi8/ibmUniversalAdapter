package com.example.jmsstub.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.jmsstub.config.AppProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicListenerRegistrar {
	private final AppProperties appProperties;
	@Qualifier("connectionFactoriesByBrokerId")
	private final Map<String, jakarta.jms.ConnectionFactory> connectionFactoriesByBrokerId;
	@Qualifier("jmsTemplatesByBrokerId")
	private final Map<String, JmsTemplate> jmsTemplatesByBrokerId;
	private final List<DefaultMessageListenerContainer> containers = new ArrayList<>();

	@EventListener(ApplicationReadyEvent.class)
	public void onReady() {
		if (appProperties.getRoutes() == null || appProperties.getRoutes().isEmpty()) {
			log.warn("No routes configured under 'stub.routes' - nothing to listen to");
			return;
		}
		for (AppProperties.Route route : appProperties.getRoutes()) {
			jakarta.jms.ConnectionFactory listenCf = connectionFactoriesByBrokerId.get(route.getListenBrokerId());
			JmsTemplate replyTemplate = jmsTemplatesByBrokerId.get(route.getReplyBrokerId());
			if (listenCf == null) {
				log.error("Unknown listen broker id: {} - skipping route {}", route.getListenBrokerId(), route);
				continue;
			}
			if (replyTemplate == null) {
				log.error("Unknown reply broker id: {} - skipping route {}", route.getReplyBrokerId(), route);
				continue;
			}

			DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
			container.setConnectionFactory(listenCf);
			container.setDestinationName(route.getListenQueue());
			container.setSessionTransacted(false);
			container.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONSUMER);
			container.setConcurrentConsumers(1);
			container.setMessageListener(new StubMessageListener(route, replyTemplate));
			container.afterPropertiesSet();
			container.start();
			containers.add(container);
			log.info("Started listener for queue '{}' on broker '{}' -> replies to '{}' on broker '{}'", route.getListenQueue(), route.getListenBrokerId(), route.getReplyQueue(), route.getReplyBrokerId());
		}
	}

	@PreDestroy
	public void shutdown() {
		for (DefaultMessageListenerContainer container : containers) {
			try {
				container.stop();
			} catch (Exception e) {
				log.warn("Error stopping container: {}", e.getMessage());
			}
		}
	}
}