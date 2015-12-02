package com.olastore.listing.clustering.reporting;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.customer.HeartbeatRequest;
import com.ifountain.opsgenie.client.model.customer.HeartbeatResponse;
import com.olastore.listing.clustering.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

/**
 * Created by gurramvinay on 12/3/15.
 */
public class OpsGenieHelper {

  public static OpsGenieClient opsGenieClient;
  public static final Logger logger = LoggerFactory.getLogger(OpsGenieHelper.class);
  public String heartbeatName;
  public String apiKey;

  public OpsGenieHelper() throws FileNotFoundException {
    createOpsGenieClient();
    setHeartBeatParams();
  }

  private void createOpsGenieClient() {
    opsGenieClient = new OpsGenieClient();
  }

  private void setHeartBeatParams() throws FileNotFoundException {
    ConfigReader configReader = new ConfigReader("config/opsgenie.yaml");
    this.heartbeatName = (String) configReader.readValue("opsgenie_heartbeat_name");
    this.apiKey = (String) configReader.readValue("opsgenie_api_key");
  }

  public void sendHeartBeat() throws ParseException, OpsGenieClientException, IOException {
    HeartbeatRequest heartbeatRequest = new HeartbeatRequest();
    heartbeatRequest.setName(heartbeatName);
    heartbeatRequest.setApiKey(apiKey);
    HeartbeatResponse response = opsGenieClient.heartbeat(heartbeatRequest);
    long heartbeatInMillis = response.getHeartbeat();
    logger.info("Heartbeat info "+heartbeatInMillis);
  }

}
