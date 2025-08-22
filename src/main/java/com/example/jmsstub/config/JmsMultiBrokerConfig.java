package com.example.jmsstub.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
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
		JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
		JmsConnectionFactory cf = ff.createConnectionFactory();
		cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, broker.getHost());
		cf.setIntProperty(WMQConstants.WMQ_PORT, broker.getPort());
		cf.setStringProperty(WMQConstants.WMQ_CHANNEL, broker.getChannel());
		cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, broker.getQueueManager());
		cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);

		if (broker.getUsername() != null && !broker.getUsername().isEmpty()) {
			cf.setStringProperty(WMQConstants.USERID, broker.getUsername());
			if (broker.getPassword() != null) {
				cf.setStringProperty(WMQConstants.PASSWORD, broker.getPassword());
			}
			cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
		}

		AppProperties.Tls tls = broker.getTls();
		if (tls != null && tls.isEnabled()) {
			if (tls.getCipherSuite() != null) {
				cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, tls.getCipherSuite());
			}
			SSLSocketFactory socketFactory = buildSslSocketFactory(tls);
			if (socketFactory != null) {
				cf.setObjectProperty(WMQConstants.WMQ_SSL_SOCKET_FACTORY, socketFactory);
			}
		}
		return cf;
	}

	private SSLSocketFactory buildSslSocketFactory(AppProperties.Tls tls) throws Exception {
		KeyManagerFactory kmf = null;
		TrustManagerFactory tmf = null;

		if (tls.getKeystorePath() != null) {
			KeyStore ks = loadKeyStore(tls.getKeystorePath(), tls.getKeystorePassword(), tls.getKeystoreType());
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, tls.getKeystorePassword() != null ? tls.getKeystorePassword().toCharArray() : new char[0]);
		}
		if (tls.getTruststorePath() != null) {
			KeyStore ts = loadKeyStore(tls.getTruststorePath(), tls.getTruststorePassword(), tls.getTruststoreType());
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ts);
		}
		if (kmf == null && tmf == null) return null;
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(kmf != null ? kmf.getKeyManagers() : null, tmf != null ? tmf.getTrustManagers() : null, null);
		return ctx.getSocketFactory();
	}

	private KeyStore loadKeyStore(String location, String password, String type) throws Exception {
		String path = resolveToFilePath(location);
		KeyStore ks = KeyStore.getInstance(type != null ? type : "JKS");
		try (InputStream in = new FileInputStream(new File(path))) {
			ks.load(in, password != null ? password.toCharArray() : null);
		}
		return ks;
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