package com.example.jmsstub.runtime;

import com.example.jmsstub.config.AppProperties;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;

@RequiredArgsConstructor
@Slf4j
public class StubMessageListener implements MessageListener {
	private final AppProperties.Route route;
	private final JmsTemplate replyTemplate;

	@Override
	public void onMessage(Message message) {
		String payload = extractPayload(message);
		String response = route.getStaticResponse() != null ? route.getStaticResponse() : defaultResponse(payload);

		try {
			String correlationId = message.getJMSCorrelationID();
			if (correlationId == null) {
				correlationId = message.getJMSMessageID();
			}
			final String finalCorrelationId = correlationId;
			replyTemplate.convertAndSend(route.getReplyQueue(), response, m -> {
				try {
					m.setJMSCorrelationID(finalCorrelationId);
				} catch (JMSException e) {
					log.warn("Failed to set correlation ID: {}", e.getMessage());
				}
				return m;
			});
			log.info("Processed message from '{}', sent reply to '{}' with correlationId='{}'", route.getListenQueue(), route.getReplyQueue(), correlationId);
		} catch (Exception ex) {
			log.error("Failed to process message for route {}: {}", route.getListenQueue(), ex.getMessage(), ex);
		}
	}

	private String extractPayload(Message message) {
		try {
			if (message instanceof TextMessage text) {
				return text.getText();
			}
			if (message instanceof BytesMessage bytes) {
				long len = bytes.getBodyLength();
				byte[] data = new byte[(int) len];
				bytes.readBytes(data);
				return new String(data);
			}
			return message.toString();
		} catch (Exception e) {
			return "";
		}
	}

	private String defaultResponse(String payload) {
		return "{\"status\":\"ok\",\"echo\":" + quote(payload) + "}";
	}

	private String quote(String s) {
		if (s == null) return "\"\"";
		return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}