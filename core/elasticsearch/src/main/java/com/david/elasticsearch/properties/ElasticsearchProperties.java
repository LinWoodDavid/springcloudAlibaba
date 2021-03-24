package com.david.elasticsearch.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

    private List<ElasticsearchHttpHosts> httpHosts;

}
