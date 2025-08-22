package ibm.mock.models;

import lombok.Data;

import java.util.List;

@Data
public class Mq {
    String queueManager;
    String channel;
    String connName;
    String user;
    String password;
    String host;
    int port;
    String databasePath;
    String charset;
    Boolean sslConnect;
    String sslTrustStore;
    String sslTrustStorePassword;
    List <DestinationWithProperties> destinationWithProperties;

}
