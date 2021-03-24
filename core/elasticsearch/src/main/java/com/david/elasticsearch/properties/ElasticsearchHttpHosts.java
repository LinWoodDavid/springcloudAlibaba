package com.david.elasticsearch.properties;

import lombok.Data;

@Data
public class ElasticsearchHttpHosts {

    private String hostname;
    private int port;
    private String scheme;
}
