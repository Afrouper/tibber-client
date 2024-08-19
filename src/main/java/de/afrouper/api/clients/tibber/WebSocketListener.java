package de.afrouper.api.clients.tibber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.afrouper.api.clients.tibber.dto.*;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;

class WebSocketListener implements Listener {

    private final UUID uuid;
    private final Gson gson;
    private final TibberClient tibberClient;
    private final TibberHandler tibberHandler;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private StringBuilder stringBuilder = new StringBuilder();
    private ScheduledFuture<?> scheduledFuture;

    WebSocketListener(TibberClient tibberClient, TibberHandler tibberHandler) {
        this.tibberClient = tibberClient;
        this.tibberHandler = tibberHandler;
        uuid = UUID.randomUUID();
        gson = new GsonBuilder()
                .create();
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        TibberClient.LOGGER.info("WebSocket onOpen called");

        ConnectionInitMessage initMessage = new ConnectionInitMessage();
        initMessage.addStringPayload("token", tibberClient.getApiKey());

        String json = gson.toJson(initMessage);

        TibberClient.LOGGER.info("Send WebSocket subscription: {}", json);
        webSocket.sendText(json, true);
        webSocket.request(1);
        startPing(webSocket);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        TibberClient.LOGGER.error("Error in WebSocketListener.", error);
        tibberHandler.error(error.getMessage());
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        tibberClient.webSocketConnectionClosed(statusCode, reason);
        tibberHandler.finished();

        if(scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        return Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        TibberClient.LOGGER.warn("Get binary data. Cannot handle this :-(");
        return Listener.super.onBinary(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence message, boolean last) {
        TibberClient.LOGGER.debug("Get data (last:{}): {}", last, message);
        stringBuilder.append(message);
        if (last) {
            processCompleteTextMessage(gson.fromJson(stringBuilder.toString(), JsonObject.class), webSocket);
            stringBuilder = new StringBuilder();
        }
        return Listener.super.onText(webSocket, message, last);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        String stringMessage = StandardCharsets.UTF_8.decode(message).toString();
        TibberClient.LOGGER.debug("onPing called with message: '{}'. Reply pong with the same message", stringMessage);
        webSocket.sendPong(message);
        return Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        String stringMessage = StandardCharsets.UTF_8.decode(message).toString();
        TibberClient.LOGGER.debug("onPong called with message: '{}'. Do not reply anything", stringMessage);
        return Listener.super.onPong(webSocket, message);
    }

    public void stopSubscription(WebSocket webSocket) {
        CompleteMessage completeMessage = new CompleteMessage(uuid.toString());
        String json = gson.toJson(completeMessage);
        TibberClient.LOGGER.info("Send CompleteMessage '{}'", json);
        webSocket.sendText(json, true);
        webSocket.request(1);
    }

    private void processCompleteTextMessage(JsonObject jsonObject, WebSocket webSocket) {
        if(!jsonObject.has("type")) {
            TibberClient.LOGGER.warn("Received invalid json object. Attribute 'type' is missing: {}", jsonObject);
            return;
        }
        switch (jsonObject.get("type").getAsString()) {
            case "connection_ack":
                subscribe(webSocket);
                break;
            case "next":
                handleNext(webSocket, jsonObject);
                break;
            case "ping":
                handlePing(webSocket, jsonObject);
            default:
                TibberClient.LOGGER.warn("Received invalid type: {}", jsonObject);
        }
    }

    private void handlePing(WebSocket webSocket, JsonObject jsonObject) {
        PongMessage pong = new PongMessage();
        if(jsonObject.has("payload")) {
            pong.setPayload(jsonObject.get("payload"));
        }
        String pongJson = gson.toJson(pong);
        webSocket.sendText(pongJson, true);
        webSocket.request(1);
    }

    private void handleNext(WebSocket webSocket, JsonObject jsonObject) {
        if(!jsonObject.has("payload")) {
            TibberClient.LOGGER.error("Payload is missing: {}", jsonObject);
            return;
        }
        if(jsonObject.getAsJsonObject("payload").has("data")) {
            JsonObject dataObject = jsonObject.getAsJsonObject("payload").getAsJsonObject("data");
            if(dataObject.has("liveMeasurement")) {
                JsonObject liveMeasurementObject = dataObject.getAsJsonObject("liveMeasurement");
                LiveMeasurement liveMeasurement = gson.fromJson(liveMeasurementObject, LiveMeasurement.class);
                try {
                    tibberHandler.onLiveMeasurement(liveMeasurement);
                }
                catch (Throwable throwable) {
                    TibberClient.LOGGER.error("TibberHandler threw an exception", throwable);
                }
            }
            else {
                TibberClient.LOGGER.error("Payload and data object did not contain liveMeasurement attribute: {}", dataObject);
            }
        }
        else {
            TibberClient.LOGGER.error("Attribute 'data' is missing in payload: {}", jsonObject);
        }
    }

    private void subscribe(WebSocket webSocket) {
        String subscription = Configuration.getGraphQL("liveMeasurementSubscription", tibberClient.getHomeId());
        SubscribeMessage subscribeMessage = new SubscribeMessage();
        subscribeMessage.setId(uuid.toString());
        SubscribeMessage.Payload payload = new SubscribeMessage.Payload();
        payload.setQuery(subscription);
        subscribeMessage.setPayload(payload);
        String json = gson.toJson(subscribeMessage);
        webSocket.sendText(json, true);
        TibberClient.LOGGER.info("Send subscription: '{}'", json);
        webSocket.request(1);
    }

    private void startPing(WebSocket webSocket) {
        if (scheduledFuture == null) {
            TibberClient.LOGGER.info("Start Ping sender.");
            Runnable beeper = () -> {
                ByteBuffer byteBuffer = ByteBuffer.wrap("Ping from Java TibberClient".getBytes(StandardCharsets.UTF_8));
                webSocket.sendPing(byteBuffer);
            };
            scheduledFuture = scheduler.scheduleAtFixedRate(beeper, 1, 1, TimeUnit.MINUTES);
        }
    }
}
