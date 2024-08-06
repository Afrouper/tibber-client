package de.afrouper.api.clients.tibber.dto;

import com.google.gson.JsonElement;

public class PongMessage extends BaseMessage {

    private JsonElement payload;

    public PongMessage() {
        super("pong");
    }

    public JsonElement getPayload() {
        return payload;
    }

    public void setPayload(JsonElement payload) {
        this.payload = payload;
    }
}
