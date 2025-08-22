package com.example.jmsstub.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stub")
public class AppProperties {
	private List<Broker> brokers;
	private List<Route> routes;

	public List<Broker> getBrokers() {
		return brokers;
	}

	public void setBrokers(List<Broker> brokers) {
		this.brokers = brokers;
	}

	public List<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public static class Broker {
		private String id;
		private String host;
		private int port = 1414;
		private String queueManager;
		private String channel;
		private String username;
		private String password;
		private Tls tls;

		public String getId() { return id; }
		public void setId(String id) { this.id = id; }
		public String getHost() { return host; }
		public void setHost(String host) { this.host = host; }
		public int getPort() { return port; }
		public void setPort(int port) { this.port = port; }
		public String getQueueManager() { return queueManager; }
		public void setQueueManager(String queueManager) { this.queueManager = queueManager; }
		public String getChannel() { return channel; }
		public void setChannel(String channel) { this.channel = channel; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
		public Tls getTls() { return tls; }
		public void setTls(Tls tls) { this.tls = tls; }
	}

	public static class Tls {
		private boolean enabled;
		private String cipherSuite;
		private String truststorePath;
		private String truststorePassword;
		private String truststoreType = "JKS";
		private String keystorePath;
		private String keystorePassword;
		private String keystoreType = "JKS";

		public boolean isEnabled() { return enabled; }
		public void setEnabled(boolean enabled) { this.enabled = enabled; }
		public String getCipherSuite() { return cipherSuite; }
		public void setCipherSuite(String cipherSuite) { this.cipherSuite = cipherSuite; }
		public String getTruststorePath() { return truststorePath; }
		public void setTruststorePath(String truststorePath) { this.truststorePath = truststorePath; }
		public String getTruststorePassword() { return truststorePassword; }
		public void setTruststorePassword(String truststorePassword) { this.truststorePassword = truststorePassword; }
		public String getTruststoreType() { return truststoreType; }
		public void setTruststoreType(String truststoreType) { this.truststoreType = truststoreType; }
		public String getKeystorePath() { return keystorePath; }
		public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }
		public String getKeystorePassword() { return keystorePassword; }
		public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
		public String getKeystoreType() { return keystoreType; }
		public void setKeystoreType(String keystoreType) { this.keystoreType = keystoreType; }
	}

	public static class Route {
		private String listenBrokerId;
		private String listenQueue;
		private String replyBrokerId;
		private String replyQueue;
		private String staticResponse;

		public String getListenBrokerId() { return listenBrokerId; }
		public void setListenBrokerId(String listenBrokerId) { this.listenBrokerId = listenBrokerId; }
		public String getListenQueue() { return listenQueue; }
		public void setListenQueue(String listenQueue) { this.listenQueue = listenQueue; }
		public String getReplyBrokerId() { return replyBrokerId; }
		public void setReplyBrokerId(String replyBrokerId) { this.replyBrokerId = replyBrokerId; }
		public String getReplyQueue() { return replyQueue; }
		public void setReplyQueue(String replyQueue) { this.replyQueue = replyQueue; }
		public String getStaticResponse() { return staticResponse; }
		public void setStaticResponse(String staticResponse) { this.staticResponse = staticResponse; }
	}
}