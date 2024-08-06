package de.afrouper.api.clients.tibber.dto;

public class BaseMessage {

    private String type;

    protected BaseMessage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
