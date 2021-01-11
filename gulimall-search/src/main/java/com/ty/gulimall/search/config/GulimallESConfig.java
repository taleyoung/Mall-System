package com.ty.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GulimallESConfig {

    public RestHighLevelClient esRestClient(){

        RestClientBuilder builder = null;
        builder = RestClient.builder(new HttpHost("39.105.35.4",9200,"http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);

        return client;
    }
}
