package ibm.mock.models;

import lombok.Data;

@Data
public class DestinationWithProperties {
    String requestQueue;
    String responseQueue;
    String templateName;
    Long sleep;

//    public void setRequestQueue(String requestQueue) {
//        this.requestQueue = requestQueue;
//    }
//
//    public void setResponseQueue(String responseQueue) {
//        this.responseQueue = responseQueue;
//    }
//
//    public void setTemplateName(String templateName) {
//        this.templateName = templateName;
//    }
//
//    public void setSleep(Long sleep) {
//        this.sleep = sleep;
//    }
//
//    public String getRequestQueue() {
//        return requestQueue;
//    }
//
//    public String getResponseQueue() {
//        return responseQueue;
//    }
//
//    public String getTemplateName() {
//        return templateName;
//    }
//
//    public Long getSleep() {
//        return sleep;
//    }
}
