package de.afrouper.api.clients.tibber;

import de.afrouper.api.clients.tibber.dto.LiveMeasurement;

public interface TibberHandler {

    void onLiveMeasurement(LiveMeasurement liveMeasurement);

    void finished();

    void error(String error);
}
