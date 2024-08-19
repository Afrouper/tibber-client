package de.afrouper.api.clients.tibber;

import de.afrouper.api.clients.tibber.dto.LiveMeasurement;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalTime;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Start...");

        TibberClient tibberClient = new TibberClient(getApiUri(), getApiKey(), getHomeId(), getConnectTimeout(), getRequestTimeout());
        tibberClient.initialize();
        tibberClient.startRealTimeConsumption(new TibberHandler() {
            @Override
            public void onLiveMeasurement(LiveMeasurement liveMeasurement) {
                System.out.println(LocalTime.now() + " - Power: " + liveMeasurement.getPower());
            }

            @Override
            public void finished() {
                System.out.println(LocalTime.now() + " - Finished!");
            }

            @Override
            public void error(String error) {
                System.out.println(LocalTime.now() + " - Error occurred: " + error);
            }
        });

        System.out.println("Sleep for 48 hours");
        try {
            Thread.sleep(Duration.ofHours(48).toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished main method");
    }

    private static String getApiKey(){
        return System.getProperty("TIBBER_API_KEY");
    }

    private static String getHomeId() {
        return System.getProperty("TIBBER_HOME_ID");
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
