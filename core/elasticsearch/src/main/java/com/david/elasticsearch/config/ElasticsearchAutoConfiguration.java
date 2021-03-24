package com.david.elasticsearch.config;

import com.david.elasticsearch.api.ElasticSearchApi;
import com.david.elasticsearch.api.ElasticSearchApiImpl;
import com.david.elasticsearch.properties.ElasticsearchHttpHosts;
import com.david.elasticsearch.properties.ElasticsearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@EnableConfigurationProperties(value = ElasticsearchProperties.class)
public class ElasticsearchAutoConfiguration {

    @Bean
    public ElasticSearchApi elasticSearchApi(@Autowired RestHighLevelClient restHighLevelClient) {
        log.debug("自定义自动装配ElasticSearchApi");
        return new ElasticSearchApiImpl(restHighLevelClient);
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(@Autowired ElasticsearchProperties elasticsearchProperties) {
        log.debug("自定义自动装配RestHighLevelClient,elasticsearchProperties={}", elasticsearchProperties);
        HttpHost[] httpHosts = new HttpHost[elasticsearchProperties.getHttpHosts().size()];
        for (int i = 0; i < httpHosts.length; i++) {
            ElasticsearchHttpHosts elasticsearchHttpHosts = elasticsearchProperties.getHttpHosts().get(i);
            httpHosts[i] = new HttpHost(elasticsearchHttpHosts.getHostname(), elasticsearchHttpHosts.getPort(), elasticsearchHttpHosts.getScheme());
        }
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        httpHosts
                ));
        return client;
    }


}
