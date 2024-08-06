package de.afrouper.api.clients.tibber.dto;

import com.google.gson.JsonObject;


public class ConnectionInitMessage extends BaseMessage {

    private JsonObject payload;

    public ConnectionInitMessage() {
        super("connection_init");
    }

    public void addStringPayload(String key, String value) {
        if(payload == null) {
            payload = new JsonObject();
        }
        payload.addProperty(key, value);
    }

    public JsonObject getPayload() {
        return payload;
    }

    public void setPayload(JsonObject payload) {
        this.payload = payload;
    }
}
