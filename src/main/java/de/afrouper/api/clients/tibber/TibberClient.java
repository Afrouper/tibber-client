package de.afrouper.api.clients.tibber;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class TibberClient {

    static final Logger LOGGER = LoggerFactory.getLogger(TibberClient.class.getPackageName());

    private final HttpClient httpClient;

    private CompletableFuture<WebSocket> webSocket;
    private WebSocketListener webSocketListener;

    private boolean realTimeConsumptionEnabled;
    private final String homeId;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final URI uri;
    private final String apiKey;

    private boolean initialized;

    public TibberClient(URI uri, String apiKey, String homeId, Duration connectTimeout, Duration requestTimeout) {
        Objects.requireNonNull(uri, "URI cannot be null");
        Objects.requireNonNull(apiKey, "API key cannot be null");
        Objects.requireNonNull(connectTimeout, "Connect timeout cannot be null");
        Objects.requireNonNull(requestTimeout, "Request timeout cannot be null");

        this.uri = uri;
        this.apiKey = apiKey;
        this.homeId = homeId;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;

        httpClient = HttpClient.newBuilder()
                .connectTimeout(this.connectTimeout)
                .build();

        LOGGER.debug("Tibber Client created");
    }

    String getApiKey() {
        return apiKey;
    }

    String getHomeId() {
        return homeId;
    }

    public void initialize() throws IOException {
        LOGGER.info("Initialize Tibber Client");

        String connectJson = Configuration.getGraphQlAsJson("connect", homeId);
        LOGGER.debug(connectJson);

        HttpRequest initRequest = HttpRequest.newBuilder()
                .timeout(requestTimeout)
                .uri(uri)
                .headers(getDefaultHeaders())
                .POST(HttpRequest.BodyPublishers.ofString(connectJson))
                .build();

        try {
            HttpResponse<String> send = httpClient.send(initRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            LOGGER.debug("Init request returned with HTTP status '{}'", send.statusCode());

            if (send.statusCode() == 200) {
                handleInitResponse(JsonParser.parseString(send.body()));
                LOGGER.info("Tibber Client initialized state: {}, realTimeConsumptionEnabled: {}", initialized, realTimeConsumptionEnabled);
            }
            else {
                throw new IOException("Cannot initialize Tibber Client. Status code was '" + send.statusCode() +"', Server reply: " + send.body());
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while initializing Tibber Client", e);
        }
    }

    private void handleInitResponse(JsonElement initResponse) {
        if(initResponse.isJsonObject()) {
            JsonObject jsonObject = initResponse.getAsJsonObject();
            if(jsonObject.get("data") != null && jsonObject.get("data").isJsonObject()) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                if(data.get("viewer") != null && data.get("viewer").isJsonObject()) {
                    initialized = true;
                    JsonObject viewer = data.get("viewer").getAsJsonObject();
                    if(viewer.get("home") != null && viewer.get("home").isJsonObject()) {
                        JsonObject home = viewer.get("home").getAsJsonObject();
                        String homeIdFromJson = home.get("home_id") != null ? home.get("home_id").getAsString() : null;
                        if(!this.homeId.equals(homeIdFromJson)) {
                            LOGGER.warn("Configured HomeId {} did not match HomeId from Init-Reply {}", this.homeId, homeIdFromJson);
                        }
                        if(home.get("features") != null && home.get("features").isJsonObject()) {
                            JsonObject features = home.get("features").getAsJsonObject();
                            realTimeConsumptionEnabled = features.get("realTimeConsumptionEnabled") != null && features.get("realTimeConsumptionEnabled").getAsBoolean();
                        }
                    }
                }
            }
        }
    }

    public void startRealTimeConsumption(TibberHandler tibberHandler) throws IOException {
        if(webSocket != null) {
            throw new IllegalStateException("WebSocket connection already active");
        }
        if(!initialized) {
            throw new IllegalStateException("Tibber Client is not initialized");
        }
        if(!realTimeConsumptionEnabled) {
            throw new IllegalStateException("Tibber real time consumption is not enabled");
        }
        String webSocketUrl = getWebSocketUrl();

        try {
            LOGGER.info("Start WebSocket connection to: {}", webSocketUrl);
            webSocketListener = new WebSocketListener(this, tibberHandler);
            webSocket = httpClient.newWebSocketBuilder()
                    .connectTimeout(connectTimeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("User-Agent", Configuration.getUserAgent())
                    .subprotocols("graphql-transport-ws")
                    .buildAsync(new URI(webSocketUrl), webSocketListener);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopRealTimeConsumption() {
        LOGGER.info("Stop real time consumption");
        try {
            if(webSocket != null) {
                if(webSocket.isDone()) {
                    WebSocket ws = webSocket.get();
                    if(webSocketListener != null) {
                        LOGGER.info("Send Stop");
                        webSocketListener.stopSubscription(ws);
                    }
                    ws.sendClose(1000, "done");
                    ws.abort();
                } else {
                    webSocket.cancel(true);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Exception while stopping Tibber WebSocket", e);
        }
    }

    //TODO: Reply content 😉
    public void getPrices() throws IOException {
        String requestBodyJson = Configuration.getGraphQlAsJson("prices", homeId);

        HttpRequest priceRequest = HttpRequest.newBuilder()
                .timeout(requestTimeout)
                .uri(uri)
                .headers(getDefaultHeaders())
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        try {
            HttpResponse<String> send = httpClient.send(priceRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            LOGGER.debug("Prices Query returned with HTTP status '{}'", send.statusCode());

            if (send.statusCode() == 200) {
                JsonElement jsonElement = JsonParser.parseString(send.body());
                LOGGER.info("JSON: {}", jsonElement);
            }
            else {
                throw new IOException("Cannot send prices request. Status code was '" + send.statusCode() +"', Server reply: " + send.body());
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while sending price request", e);
        }
    }

    void webSocketConnectionClosed(int statusCode, String reason) {
        LOGGER.info("WebSocket onClose with statusCode={}, reason={}", statusCode, reason);
    }

    private String getWebSocketUrl() throws IOException {
        String webSocketUrl = Configuration.getGraphQlAsJson("webSocketUri", homeId);
        LOGGER.debug(webSocketUrl);

        HttpRequest initRequest = HttpRequest.newBuilder()
                .timeout(requestTimeout)
                .uri(uri)
                .headers(getDefaultHeaders())
                .POST(HttpRequest.BodyPublishers.ofString(webSocketUrl))
                .build();

        try {
            HttpResponse<String> send = httpClient.send(initRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            LOGGER.debug("WebSocket Query returned with HTTP status '{}'", send.statusCode());

            if (send.statusCode() == 200) {
                return readWebSocketUrl(JsonParser.parseString(send.body()));
            }
            else {
                throw new IOException("Cannot initialize Tibber Client. Status code was '" + send.statusCode() +"', Server reply: " + send.body());
            }
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while initializing Tibber Client", e);
        }
    }

    private String readWebSocketUrl(JsonElement jsonElement) {
        if(jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.get("data") != null && jsonObject.get("data").isJsonObject()) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                if(data.get("viewer") != null && data.get("viewer").isJsonObject()) {
                    initialized = true;
                    JsonObject viewer = data.get("viewer").getAsJsonObject();
                    return viewer.get("websocketSubscriptionUrl") != null ? viewer.get("websocketSubscriptionUrl").getAsString() : null;
                }
            }
        }
        LOGGER.error("Cannot read websocketSubscriptionUrl from JSON '{}'", jsonElement);
        return null;
    }

    private String[] getDefaultHeaders() {
        return new String[]{
                "Authorization", "Bearer " + apiKey,
                "cache-control", "no-cache",
                "Content-Type", "application/json",
                "User-Agent", Configuration.getUserAgent()};
    }
}
