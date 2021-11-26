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
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Taah
 * Wrapper for https://phish.sinking.yachts/docs
 */

@Getter
@AllArgsConstructor
public class PhishingAPI {

    /**
     * According to the API, you NEED to have an identity to let yachts know who you are
     */
    private final String identity;

    /**
     * Asynchronously does a GET request to gather all domains from the API's endpoint /v2/all
     *
     * @return a CompletableFuture<List<String>> object, the List<String> contains the domains
     * @throws AuthenticationException
     */
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

    /**
     * Asynchronously does a GET request to check whether a domain is on the list
     *
     * @return a CompletableFuture<Boolean> object, boolean returns true if it is on the list, false if not
     * @throws AuthenticationException
     */
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

    /**
     * Asynchronously does a GET request to gather the number of domains on the list
     *
     * @return a CompletableFuture<Integer> object, integer gives the domain count
     * @throws AuthenticationException
     */
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

    /**
     * Asynchronously does a GET request to gather all recent changes x seconds ago
     *
     * @return a CompletableFuture<JSONArray> object, gives you all the recent changes in JSON Array format
     * @throws AuthenticationException
     */
    public CompletableFuture<JSONArray> recentJsonChanges(int seconds) throws Exception {
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

    /**
     * Asynchronously does a GET request to gather all recently added domains x seconds ago
     *
     * @return a CompletableFuture<List<String>> object, List<String> contains all recently added domains
     * @throws AuthenticationException
     */
    public CompletableFuture<List<String>> recentDomainAdditions(int seconds) throws Exception {
        return recentJsonChanges(seconds).thenCompose(jsonArray -> CompletableFuture.supplyAsync(() -> {
            List<String> domainList = new ArrayList<>();
            for (Object object : jsonArray) {
                JSONObject obj = new JSONObject(object.toString());
                if (obj.getString("type").equalsIgnoreCase("add")) {
                    JSONArray domains = obj.getJSONArray("domains");
                    domainList.addAll(domains.toList().stream().map(Object::toString).collect(Collectors.toList()));
                }
            }
            return domainList;
        }));
    }

    /**
     * Asynchronously does a GET request to gather all recently removed domains x seconds ago
     * @return a CompletableFuture<List<String>> object, List<String> contains all recently removed domains
     * @throws AuthenticationException
     */
    public CompletableFuture<List<String>> recentDomainRemovals(int seconds) throws Exception {
        return recentJsonChanges(seconds).thenCompose(jsonArray -> CompletableFuture.supplyAsync(() -> {
            List<String> domainList = new ArrayList<>();
            for (Object object : jsonArray) {
                JSONObject obj = new JSONObject(object.toString());
                if (obj.getString("type").equalsIgnoreCase("delete")) {
                    JSONArray domains = obj.getJSONArray("domains");
                    domainList.addAll(domains.toList().stream().map(Object::toString).collect(Collectors.toList()));
                }
            }
            return domainList;
        }));
    }

    /**
     * @return boolean, whether identity is null or empty
     */
    private boolean checkIdentity() {
        if (identity == null || identity.isEmpty()) {
            return false;
        }
        return true;
    }


}
