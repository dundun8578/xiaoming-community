package com.xiaoming.module.search.config;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/** ES 8 原生客户端；避免 Spring Data 4.x 的 ES 7 客户端与 ES 8 混用。 */
@Configuration
public class ElasticsearchConfig {
    @Bean(destroyMethod="close")
    public ElasticsearchTransport elasticsearchTransport(
            @Value("${spring.elasticsearch.rest.uris}") String uri, ObjectMapper mapper) {
        RestClient rest=RestClient.builder(HttpHost.create(uri)).build();
        return new RestClientTransport(rest,new JacksonJsonpMapper(mapper));
    }
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
