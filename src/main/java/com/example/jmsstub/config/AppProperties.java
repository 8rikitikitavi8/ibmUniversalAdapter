package com.example.jmsstub.config;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stub")
@Data
public class AppProperties {
	private List<Broker> brokers;
	private List<Route> routes;

	@Data
	public static class Broker {
		private String id;
		private String host;
		private int port = 1414;
		private String queueManager;
		private String channel;
		private String username;
		private String password;
		private Tls tls;
	}

	@Data
	public static class Tls {
		private boolean enabled;
		private String cipherSuite;
		private String truststorePath;
		private String truststorePassword;
		private String truststoreType = "JKS";
		private String keystorePath;
		private String keystorePassword;
		private String keystoreType = "JKS";
	}

	@Data
	public static class Route {
		private String listenBrokerId;
		private String listenQueue;
		private String replyBrokerId;
		private String replyQueue;
		private String staticResponse;
	}
}