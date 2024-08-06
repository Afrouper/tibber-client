package de.afrouper.api.clients.tibber.dto;

import com.google.gson.JsonObject;

public class SubscribeMessage extends BaseMessage {

    private String id;
    private Payload payload;

    public SubscribeMessage() {
        super("subscribe");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        private String query;
        private String operationName;
        private JsonObject variables;
        private JsonObject extensions;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public JsonObject getVariables() {
            return variables;
        }

        public void setVariables(JsonObject variables) {
            this.variables = variables;
        }

        public JsonObject getExtensions() {
            return extensions;
        }

        public void setExtensions(JsonObject extensions) {
            this.extensions = extensions;
        }
    }
}
