package com.taahyt.phishingapi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class PhishingAPI {
    private final String identity;

    public CompletableFuture<List<String>> getDomains() throws AuthenticationException {
        if (!checkIdentity()) throw new AuthenticationException("Please provide a proper identity.");

        return CompletableFuture.supplyAsync(() -> {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet get = new HttpGet("https://phish.sinking.yachts/v2/all");
            get.setHeader("X-Identity", this.identity);

            try (CloseableHttpResponse response = client.execute(get)) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return new JSONArray(jsonResponse).toList().stream().map(Object::toString).collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        });
    }

    public CompletableFuture<Boolean> checkDomain(String domain) throws AuthenticationException {
        if (!checkIdentity()) throw new AuthenticationException("Please provide a proper identity.");

        return CompletableFuture.supplyAsync(() -> {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet get = new HttpGet("https://phish.sinking.yachts/v2/check/" + domain);
            get.setHeader("X-Identity", this.identity);

            try (CloseableHttpResponse response = client.execute(get)) {
                String boolResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return Boolean.parseBoolean(boolResponse);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public CompletableFuture<Integer> domainCount() throws AuthenticationException {
        if (!checkIdentity()) throw new AuthenticationException("Please provide a proper identity.");

        return CompletableFuture.supplyAsync(() -> {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet get = new HttpGet("https://phish.sinking.yachts/v2/dbsize");
            get.setHeader("X-Identity", this.identity);

            try (CloseableHttpResponse response = client.execute(get)) {
                String intResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return Integer.parseInt(intResponse);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        });
    }

    public CompletableFuture<JSONArray> recentChanges(int seconds) throws Exception {
        if (!checkIdentity()) throw new AuthenticationException("Please provide a proper identity.");
        if (seconds > 604800) throw new Exception("Seconds must be less than or equal to 604800.");

        return CompletableFuture.supplyAsync(() -> {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet get = new HttpGet("https://phish.sinking.yachts/v2/recent/" + seconds);
            get.setHeader("X-Identity", this.identity);

            try (CloseableHttpResponse response = client.execute(get)) {
                String jsonResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return new JSONArray(jsonResponse);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private boolean checkIdentity() {
        if (identity == null || identity.isEmpty()) {
            return false;
        }
        return true;
    }

}
