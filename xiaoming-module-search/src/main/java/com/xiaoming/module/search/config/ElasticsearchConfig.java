package com.xiaoming.module.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch配置
 *
 * 注意：Spring Data 的仓库扫描默认只看启动类所在包（com.xiaoming.web），
 *      不会扫到本模块的 repository 包，所以必须在这里显式指定，否则
 *      NoteSearchRepository 不会被注册成 Bean，导致 NoteSearchService 注入失败。
 *
 *      另外 Spring Data 的 Repository 需要容器里存在一个名为 elasticsearchTemplate
 *      的 ElasticsearchOperations Bean，这里显式声明，避免依赖自动配置的装配顺序。
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.xiaoming.module.search.repository")
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.rest.uris}")
    private String elasticsearchUrl;

    @Bean
    public RestClient restClient() {
        HttpHost host = HttpHost.create(elasticsearchUrl);
        return RestClient.builder(host).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = restClient();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    /**
     * Spring Data Elasticsearch 4.x 的 ElasticsearchRestTemplate 底层需要 RestHighLevelClient，
     * 而 Spring Data 的 Repository 又要求容器里有一个名为 elasticsearchTemplate 的
     * ElasticsearchOperations Bean，所以这里显式提供，保证仓库能正常装配。
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        HttpHost host = HttpHost.create(elasticsearchUrl);
        return new RestHighLevelClient(RestClient.builder(host));
    }

    @Bean
    public ElasticsearchRestTemplate elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(restHighLevelClient());
    }
}
