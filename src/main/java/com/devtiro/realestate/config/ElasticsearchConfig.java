package com.devtiro.realestate.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.URISyntaxException;

@Configuration
@RequiredArgsConstructor
@EnableElasticsearchRepositories(basePackages = "com.devtiro.realestate.repositories")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Bean
    public RestClient restClient() throws URISyntaxException {
        var httpHost = HttpHost.create(elasticsearchUrl);
        var builder = RestClient.builder(httpHost);

        // Add authentication if credentials are provided
        if (username != null && !username.isEmpty()) {
            var credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password)
            );
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        return builder.build();
    }

    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Support for Java 8 date/time types
        // IMPORTANT: Ignore unknown properties like _class
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }


}