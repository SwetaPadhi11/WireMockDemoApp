package com.testvagrant;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.impl.client.HttpClients.createDefault;

public class Main {
    public static void main(String[] args) {
        // Create a WireMock server instance as a proxy
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().proxyVia(ProxySettings.NO_PROXY));
        wireMockServer.start();

        // Configure WireMock to return a mock response when someone calls the external API
        configureWireMock();

        // Your application logic here
        HttpClient httpClient = createDefault();
        HttpGet httpGet = new HttpGet("http://jsonplaceholder.typicode.com/posts/1");

        // Configure the HTTP client to use WireMock as a proxy
        HttpHost proxy = new HttpHost("localhost", 8080); // Assuming WireMock is running on localhost:8080
        RequestConfig config = RequestConfig.custom()
                .setProxy(proxy)
                .build();
        httpGet.setConfig(config);

        try {
            org.apache.http.HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("Response from External API:");
            System.out.println(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stop the WireMock server when done
        wireMockServer.stop();
    }

    private static void configureWireMock() {
        WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/posts/1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"userId\": 1, \"id\": 1, \"title\": \"mocked title\", \"body\": \"mocked body\"}")
                )
        );
    }
}
