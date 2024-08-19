package de.afrouper.api.clients.tibber;

import de.afrouper.api.clients.tibber.dto.LiveMeasurement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class TibberClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TibberClientTest.class);

    private TibberClient tibberClient;

    private boolean gotLiveMeasurement = false;
    private boolean finished = false;
    private boolean errorFound = false;

    @BeforeEach
    public void initTibberClient() throws Exception {
        tibberClient = new TibberClient(getApiUri(), getApiKey(), getHomeId(), getConnectTimeout(), getRequestTimeout());
    }

    @AfterEach
    public void destroy() throws Exception {
        tibberClient.stopRealTimeConsumption();
    }

    @Test
    public void priceRequest() throws Exception {
        tibberClient.getPrices();
    }

    @Test
    public void webSocketConnection() throws Exception {
        tibberClient.initialize();
        tibberClient.startRealTimeConsumption(new TibberHandler() {
            @Override
            public void onLiveMeasurement(LiveMeasurement liveMeasurement) {
                LOGGER.info("Current power consumption: {} watts", liveMeasurement.getPower());
                gotLiveMeasurement = true;
            }

            @Override
            public void finished() {
                LOGGER.info("Tibber websocket connection finished");
                finished = true;
            }

            @Override
            public void error(String error) {
                LOGGER.error("Tibber websocket error: {}", error);
                errorFound = true;
            }
        });

        Thread.sleep(1000 * 20);
        assertTrue(gotLiveMeasurement);
        assertFalse(finished);
        assertFalse(errorFound);
    }

    private static String getApiKey(){
        return System.getenv("TIBBER_API_KEY");
    }

    private static String getHomeId() {
        return System.getenv("TIBBER_HOME_ID");
    }

    private static URI getApiUri(){
        try {
            return new URI("https://api.tibber.com/v1-beta/gql");
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Tibber URI has wrong format in configuration class.", e);
        }
    }

    private static Duration getConnectTimeout() {
        return Duration.ofSeconds(10);
    }

    private static Duration getRequestTimeout() {
        return Duration.ofSeconds(30);
    }
}