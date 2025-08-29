package ibm.mock.models;

import lombok.Data;

@Data
public class DestinationWithProperties {
    String requestQueue;
    String responseQueue;
    String templateName;
    Long sleep;

}
