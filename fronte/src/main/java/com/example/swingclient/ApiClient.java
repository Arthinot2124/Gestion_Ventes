package com.example.swingclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8000/api/ventes";
    private static final String BASE_URL_STATS = "http://localhost:8000/api/stats/ventes";

    private static final Gson gson = new Gson();

    public static List<Vente> getVentes() throws IOException, ParseException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL);
            CloseableHttpResponse response = client.execute(request);
            String json = EntityUtils.toString(response.getEntity());
            return gson.fromJson(json, new TypeToken<List<Vente>>() {}.getType());
        }
    }

    public static void addVente(Vente vente) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BASE_URL);
            String json = gson.toJson(vente);
            request.setEntity(new StringEntity(json));
            request.setHeader("Content-Type", "application/json");
            client.execute(request);
        }
    }

    public static void updateVente(Vente vente) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(BASE_URL + "/" + vente.getNumProduit());
            String json = gson.toJson(vente);
            request.setEntity(new StringEntity(json));
            request.setHeader("Content-Type", "application/json");
            client.execute(request);
        }
    }
    public static void deleteVente(String numProduit) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpDelete request = new HttpDelete(BASE_URL + "/" + numProduit);
            client.execute(request);
        }
    }

    public static String getStats() throws IOException, ParseException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL_STATS + "/");
            CloseableHttpResponse response = client.execute(request);
            return EntityUtils.toString(response.getEntity());
        }
    }
}