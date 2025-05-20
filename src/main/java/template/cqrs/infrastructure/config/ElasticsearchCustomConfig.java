package template.cqrs.infrastructure.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class ElasticsearchCustomConfig {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris; // e.g., http://localhost:9200

    @Value("${spring.elasticsearch.username:#{null}}") // Default to null if not set
    private String username;

    @Value("${spring.elasticsearch.password:#{null}}") // Default to null if not set
    private String password;

    /**
     * Creates a custom ElasticsearchClient bean.
     * This bean will be used by Spring Data Elasticsearch.
     * It ensures that the JacksonJsonpMapper used by the client
     * is configured with the primary ObjectMapper (which includes JavaTimeModule).
     *
     * @param primaryObjectMapper The primary ObjectMapper bean configured with JavaTimeModule.
     * @return A configured ElasticsearchClient.
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(@Qualifier("objectMapper") ObjectMapper primaryObjectMapper) {
        // Parse the URIs (assuming single URI for simplicity in this example)
        // For multiple URIs, you'd need to parse them and create multiple HttpHost objects.
        String effectiveUri = elasticsearchUris.split(",")[0].trim();
        HttpHost httpHost = HttpHost.create(effectiveUri);

        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);

        // Configure credentials if username and password are provided
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestClient restClient = restClientBuilder.build();

        // Create a JacksonJsonpMapper using the primary (customized) ObjectMapper
        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(primaryObjectMapper);

        // Create the transport with the RestClient and the custom JsonpMapper
        ElasticsearchTransport transport = new RestClientTransport(restClient, jsonpMapper);

        return new ElasticsearchClient(transport);
    }
}
