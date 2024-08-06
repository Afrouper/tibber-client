package de.afrouper.api.clients.tibber;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Configuration {

    static String getGraphQlAsJson(String name, String homeId) {
        return surroundWithJson(getGraphQL(name, homeId));
    }

    static String getUserAgent() {
        return "API-Client/1.0.0 com.tibber/1.8.3";
    }

    static String surroundWithJson(String graphql) {
        JsonObject jso = new JsonObject();
        jso.addProperty("query", graphql);
        return jso.toString();
    }

    static String getGraphQL(String name, String homeId) {
        try(InputStream inputStream = Configuration.class.getResourceAsStream("/graphql/" + name + ".graphql")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Could not find graphql file: " + name);
            }
            int read;
            StringBuilder stringBuilder = new StringBuilder();
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, read, StandardCharsets.UTF_8));
            }
            return stringBuilder.toString().replace("${HOME_ID}", homeId);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Unable to read graphql file: " + name, e);
        }
    }
}
