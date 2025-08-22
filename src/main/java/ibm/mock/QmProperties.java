package ibm.mock;

import com.ibm.mq.spring.boot.MQConfigurationProperties;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import ibm.mock.models.DestinationWithProperties;
import ibm.mock.models.Mq;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ibm.msg.client.jakarta.wmq.common.CommonConstants.WMQ_SSL_CIPHER_SUITE;

@Data
@ConfigurationProperties("ibm.mq")
@Configuration
@Component
public class QmProperties {
    Logger logger = LoggerFactory.getLogger(QueueConsumer.class);
    private List<Mq> mqs;

    Set<String> requestQueuesFromConfigFile = new HashSet<>();

    @PostConstruct
    public void testProps() {
        for (Mq mq : mqs) {
            logger.info(mq.getQueueManager());
//            broker.getDwp().stream().forEach(e->System.out.println(e.getRequestQueue()));
        }
        for (Mq mq : mqs) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                logger.info(mq.getDestinationWithProperties().get(i).getRequestQueue());
            }
        }
        for (Mq mq : mqs) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                logger.info(mq.getDestinationWithProperties().get(i).getRequestQueue());
            }
        }

    }

    public List<MQConfigurationProperties> getListMQConfigurationProperties() {
        List<MQConfigurationProperties> list = new ArrayList<>();
        for (Mq mq : mqs) {
            MQConfigurationProperties mqConfigurationProperties = new MQConfigurationProperties();
            mqConfigurationProperties.setChannel(mq.getChannel());
            mqConfigurationProperties.setConnName(mq.getConnName());
            mqConfigurationProperties.setQueueManager(mq.getQueueManager());
            mqConfigurationProperties.setUser(mq.getUser());
            mqConfigurationProperties.setPassword(mq.getPassword());
            if (mq.getSslConnect() != null) {
                mqConfigurationProperties.setSslCipherSuite("*TLS12");
                System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false"); // for Oracle Java
                System.setProperty("javax.net.ssl.trustStore", mq.getSslTrustStore());
                System.setProperty("javax.net.ssl.trustStorePassword", mq.getSslTrustStorePassword());
            }
            list.add(mqConfigurationProperties);
        }
        return list;
    }

    public List<String> getDest(String queueManager) {
        List<String> dests = new ArrayList<>();
        for (Mq mq : mqs) {
            if (mq.getQueueManager().equals(queueManager)) {
                for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                    dests.add(mq.getDestinationWithProperties().get(i).getRequestQueue());
                }
            }
        }
        return dests;
    }

    @PostConstruct
    private void getSetDest() {
        for (Mq mq : mqs) {
            for (DestinationWithProperties dwp : mq.getDestinationWithProperties()) {
                requestQueuesFromConfigFile.add(dwp.getRequestQueue());
            }
        }
    }

    public String getQueueManagerFromDestination(String dest) {
        for (Mq mq : mqs) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                if (dest.equals(mq.getDestinationWithProperties().get(i).getResponseQueue())) {
                    return mq.getQueueManager();
                }
            }
        }
        return null;
    }

    public Long getSleepFromDestination(String dest) {
        for (Mq mq : mqs) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                if (dest.equals(mq.getDestinationWithProperties().get(i).getResponseQueue())) {
                    return mq.getDestinationWithProperties().get(i).getSleep() * 1000;
                }
            }
        }
        return null;
    }

    public String databasePathFromQueueManager(String queueManager) {
        for (Mq mq : mqs) {
            for (int i = 0; i < mq.getDestinationWithProperties().size(); i++) {
                if (queueManager.equals(mq.getQueueManager())) {
                    return mq.getDatabasePath();
                }
            }
        }
        return null;
    }

}
