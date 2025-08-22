package com.example.jmsstub.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSslConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class JmsMultiBrokerConfig {
	@Bean
	@Qualifier("connectionFactoriesByBrokerId")
	public Map<String, jakarta.jms.ConnectionFactory> connectionFactoriesByBrokerId(AppProperties properties) throws Exception {
		Map<String, jakarta.jms.ConnectionFactory> map = new HashMap<>();
		for (AppProperties.Broker broker : properties.getBrokers()) {
			jakarta.jms.ConnectionFactory cf = createConnectionFactory(broker);
			map.put(broker.getId(), cf);
		}
		return map;
	}

	@Bean
	@Qualifier("jmsTemplatesByBrokerId")
	public Map<String, JmsTemplate> jmsTemplatesByBrokerId(@Qualifier("connectionFactoriesByBrokerId") Map<String, jakarta.jms.ConnectionFactory> factories) {
		Map<String, JmsTemplate> map = new HashMap<>();
		for (Map.Entry<String, jakarta.jms.ConnectionFactory> e : factories.entrySet()) {
			JmsTemplate template = new JmsTemplate(e.getValue());
			template.setDeliveryPersistent(true);
			map.put(e.getKey(), template);
		}
		return map;
	}

	private jakarta.jms.ConnectionFactory createConnectionFactory(AppProperties.Broker broker) throws Exception {
		AppProperties.Tls tls = broker.getTls();
		if (tls != null && tls.isEnabled()) {
			ActiveMQSslConnectionFactory cf = new ActiveMQSslConnectionFactory(broker.getUrl());
			if (broker.getUsername() != null) cf.setUserName(broker.getUsername());
			if (broker.getPassword() != null) cf.setPassword(broker.getPassword());
			if (tls.getTruststorePath() != null) {
				String trustPath = resolveToFilePath(tls.getTruststorePath());
				cf.setTrustStore(trustPath);
				if (tls.getTruststorePassword() != null) cf.setTrustStorePassword(tls.getTruststorePassword());
				if (tls.getTruststoreType() != null) cf.setTrustStoreType(tls.getTruststoreType());
			}
			if (tls.getKeystorePath() != null) {
				String keyPath = resolveToFilePath(tls.getKeystorePath());
				cf.setKeyStore(keyPath);
				if (tls.getKeystorePassword() != null) cf.setKeyStorePassword(tls.getKeystorePassword());
				if (tls.getKeystoreType() != null) cf.setKeyStoreType(tls.getKeystoreType());
			}
			return cf;
		} else {
			ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(broker.getUrl());
			if (broker.getUsername() != null) cf.setUserName(broker.getUsername());
			if (broker.getPassword() != null) cf.setPassword(broker.getPassword());
			return cf;
		}
	}

	private String resolveToFilePath(String value) throws IOException {
		if (value == null) return null;
		if (value.startsWith("classpath:")) {
			String path = value.substring("classpath:".length());
			Resource resource = new DefaultResourceLoader().getResource("classpath:" + path);
			try (InputStream in = resource.getInputStream()) {
				File temp = File.createTempFile("jmsstub-store-", ".jks");
				temp.deleteOnExit();
				try (FileOutputStream out = new FileOutputStream(temp)) {
					in.transferTo(out);
				}
				return temp.getAbsolutePath();
			}
		}
		return value;
	}
}