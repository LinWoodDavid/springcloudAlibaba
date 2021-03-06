package com.david.elasticsearch.api;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ElasticSearchApiImpl implements ElasticSearchApi {

    private RestHighLevelClient restHighLevelClient;

    public ElasticSearchApiImpl(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public SearchResult<String> search(QueryCriteria queryCriteria, String... indices) throws IOException {
        SearchResponse searchResponse = executeSearch(queryCriteria, indices);
        //??????????????????
        if (searchResponse.status().equals(RestStatus.OK)) {
            List<String> result = new ArrayList<>();
            SearchHits hits = searchResponse.getHits();

            if (hits.getTotalHits().value > 0) {
                for (SearchHit hit : hits) {
                    result.add(hit.getSourceAsString());
                }
            }
            SearchResult<String> searchResult = new SearchResult<>(queryCriteria.getPage(), queryCriteria.getSize(), hits.getTotalHits().value);
            searchResult.setResult(result);
            return searchResult;
        } else {
            throw new RuntimeException("Elasticsearch????????????: " + searchResponse);
        }
    }

    private SearchResponse executeSearch(QueryCriteria queryCriteria, String... indices) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        //?????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (null != queryCriteria.getIncludes() || null != queryCriteria.getExcludes()) {
            searchSourceBuilder.fetchSource(queryCriteria.getIncludes(), queryCriteria.getExcludes());
        }
        //????????????
        searchSourceBuilder.query(queryCriteria.getQueryBuilder());
        //??????
        if (!CollectionUtils.isEmpty(queryCriteria.getSorts())) {
            queryCriteria.getSorts().forEach(sort -> {
                FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(sort.getName())
                        .order(sort.getOrder() ? SortOrder.ASC : SortOrder.DESC);
                if (StringUtils.isNotBlank(sort.getPath())) {
                    NestedSortBuilder nestedSortBuilder = new NestedSortBuilder(sort.getPath());
                    fieldSortBuilder.setNestedSort(nestedSortBuilder);
                }
                searchSourceBuilder.sort(fieldSortBuilder);
            });
        }
        //??????
        searchSourceBuilder.from(queryCriteria.getFrom());
        searchSourceBuilder.size(queryCriteria.getSize());
        searchRequest.source(searchSourceBuilder);
        //????????????
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * ???????????????
     *
     * @param tClass        ???
     * @param queryCriteria ????????????
     * @param <T>           ??????
     * @param indices       ??????
     * @return
     * @throws IOException ES????????????
     */
    @Override
    public <T> SearchResult<T> search(Class<T> tClass, QueryCriteria queryCriteria, String... indices) throws IOException {
        SearchResponse searchResponse = executeSearch(queryCriteria, indices);
        //??????????????????
        if (searchResponse.status().equals(RestStatus.OK)) {
            List<T> result = new ArrayList<>();
            SearchHits hits = searchResponse.getHits();
            if (hits.getTotalHits().value > 0) {
                for (SearchHit hit : hits) {
                    result.add(JSON.parseObject(hit.getSourceAsString(), tClass));
                }
            }
            SearchResult<T> searchResult = new SearchResult<T>(queryCriteria.getPage(), queryCriteria.getSize(), hits.getTotalHits().value);
            searchResult.setResult(result);
            return searchResult;
        } else {
            throw new RuntimeException("Elasticsearch????????????: " + searchResponse);
        }
    }

    /**
     * ??????????????????
     *
     * @param index      ??????
     * @param documentId ??????id
     * @return true????????????false:?????????
     * @throws IOException ES????????????
     */
    @Override
    public Boolean existsDocument(String documentId, String index) throws IOException {
        GetRequest getRequest = new GetRequest(index).id(documentId);
        getRequest.fetchSourceContext(new FetchSourceContext(false));//????????????_source
        getRequest.storedFields("_none_");//??????????????????
        return restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void insertDocument(String documentId, String documentJson, String index) throws IOException {
        insertDocument(documentId, documentJson, null, index);
    }

    @Override
    public void insertDocument(String documentId, String documentJson, String routing, String index) throws IOException {
        //???????????????????????????
        insertDocument(documentId, documentJson, routing, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public IndexResponse insertDocument(String documentId, String documentJson, String routing, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        IndexRequest request = new IndexRequest(index).id(documentId);

        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        if (StringUtils.isNotBlank(routing)) {
            request.routing(routing);
        }
        request.source(documentJson, XContentType.JSON);
        return restHighLevelClient.index(request, RequestOptions.DEFAULT);
    }

    /**
     * ??????????????????
     *
     * @param index     ??????
     * @param documents ???????????????key:??????id value-json??????
     * @throws IOException ES????????????
     */
    @Override
    public void batchInsertDocuments(Map<String, String> documents, String index) throws IOException {
        //???????????????????????????
        batchInsertDocuments(documents, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void batchInsertDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        BulkRequest request = new BulkRequest();
        for (String documentId : documents.keySet()) {
            IndexRequest indexRequest = new IndexRequest(index).id(documentId);
            indexRequest.source(documents.get(documentId), XContentType.JSON);
            request.add(indexRequest);
        }

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //??????????????????
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Elasticsearch??????????????????,documents:").append(JSON.toJSONString(documents)).append(";????????????:");
            List<BulkItemResponse> bulkItemResponses = new ArrayList<>();
            for (BulkItemResponse response : bulkResponse) {
                if (response.isFailed()) {
                    bulkItemResponses.add(response);
                }
            }
            errorMessage.append(JSON.toJSONString(bulkItemResponses));
            throw new IOException(errorMessage.toString());
        }
    }

    /**
     * ????????????
     *
     * @param index      ??????
     * @param documentId ??????id
     * @param json       ??????
     * @throws IOException ES????????????
     */
    @Override
    public void updateDocumentByDocumentId(String documentId, String json, String index) throws IOException {
        //???????????????????????????
        updateDocumentByDocumentId(documentId, json, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void updateDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        //??????????????????
        UpdateRequest request = new UpdateRequest(index, documentId);
        //??????????????????
        request.doc(json, XContentType.JSON);

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //????????????
        restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    @Override
    public void upsertDocumentByDocumentId(String documentId, String json, String index) throws IOException {
        //???????????????????????????
        upsertDocumentByDocumentId(documentId, json, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void upsertDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        //??????????????????
        UpdateRequest request = new UpdateRequest(index, documentId);
        //?????????????????????
        request.doc(json, XContentType.JSON);
        request.upsert(json, XContentType.JSON);

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //????????????
        restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    /**
     * ??????????????????
     *
     * @param index     ??????
     * @param documents ???????????????key:??????id value-json??????
     * @throws IOException ES????????????
     */
    @Override
    public void batchUpdateDocuments(Map<String, String> documents, String index) throws IOException {
        //???????????????????????????
        batchUpdateDocuments(documents, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void batchUpdateDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        BulkRequest request = new BulkRequest();
        for (String documentId : documents.keySet()) {
            UpdateRequest updateRequest = new UpdateRequest(index, documentId);
            updateRequest.doc(documents.get(documentId), XContentType.JSON);
            request.add(updateRequest);
        }

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //??????????????????
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    @Override
    public void batchUpsertDocuments(Map<String, String> documents, String index) throws IOException {
        //???????????????????????????
        batchUpsertDocuments(documents, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void batchUpsertDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        BulkRequest request = new BulkRequest();
        for (String documentId : documents.keySet()) {
            UpdateRequest updateRequest = new UpdateRequest(index, documentId);
            updateRequest.doc(documents.get(documentId), XContentType.JSON);
            updateRequest.upsert(documents.get(documentId), XContentType.JSON);
            request.add(updateRequest);
        }

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //??????????????????
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Elasticsearch??????Upsert??????,documents:").append(JSON.toJSONString(documents)).append(";????????????:");
            List<BulkItemResponse> bulkItemResponses = new ArrayList<>();
            for (BulkItemResponse response : bulkResponse) {
                if (response.isFailed()) {
                    bulkItemResponses.add(response);
                }
            }
            errorMessage.append(JSON.toJSONString(bulkItemResponses));
            throw new IOException(errorMessage.toString());
        }
    }

    /**
     * ??????id????????????
     *
     * @param index      ??????
     * @param documentId ??????id
     * @throws IOException ES????????????
     */
    @Override
    public void deleteDocumentByDocumentId(String documentId, String index) throws IOException {
        //???????????????????????????
        deleteDocumentByDocumentId(documentId, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void deleteDocumentByDocumentId(String documentId, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(index, String.valueOf(documentId));

        //????????????
        if (refreshPolicy != null) {
            //????????????
            deleteRequest.setRefreshPolicy(refreshPolicy);
            deleteRequest.setRefreshPolicy("true");
        }

        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * ??????id????????????
     *
     * @param index       ??????
     * @param documentIds ??????id
     * @throws IOException ES????????????
     */
    @Override
    public void deleteDocumentByDocumentIds(List<String> documentIds, String index) throws IOException {
        //???????????????????????????
        deleteDocumentByDocumentIds(documentIds, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void deleteDocumentByDocumentIds(List<String> documentIds, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        BulkRequest request = new BulkRequest();
        documentIds.forEach(documentId -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, String.valueOf(documentId));
            request.add(deleteRequest);
        });

        //????????????
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //??????????????????
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * ??????????????????
     *
     * @param index ??????
     * @return true???????????? false:?????????
     * @throws IOException ES????????????
     */
    @Override
    public Boolean existsIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * ????????????
     *
     * @param index    ??????
     * @param shards   ?????????
     * @param replicas ?????????
     * @return true or false
     * @throws IOException ES????????????
     */
    @Override
    public Boolean createIndex(Integer shards, Integer replicas, String index) throws IOException {
        //??????????????????
        CreateIndexRequest request = new CreateIndexRequest(index);
        //????????????
        request.settings(Settings.builder()
                .put("index.number_of_shards", shards)
                .put("index.number_of_replicas", replicas)
        );
        //??????????????????
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        //??????????????????
        return createIndexResponse.isAcknowledged();//??????????????????????????????????????????
    }

    @Override
    public Map<String, Object> getMapping(String index) throws IOException {
        GetMappingsRequest request = new GetMappingsRequest().indices(index);
        GetMappingsResponse getMappingResponse = restHighLevelClient.indices().getMapping(request, RequestOptions.DEFAULT);
        Map<String, MappingMetadata> mappings = getMappingResponse.mappings();
        MappingMetadata mappingMetadata = mappings.get(index);
        Map<String, Object> mapping = mappingMetadata.sourceAsMap();
        return mapping;
    }

    /**
     * ?????? mapping
     *
     * @param index
     * @param mapping
     * @return
     * @throws IOException ES????????????
     */
    @Override
    public Boolean putMapping(String mapping, String index) throws IOException {
        //??????????????????
        PutMappingRequest request = new PutMappingRequest(index);
        //????????????
        request.source(mapping, XContentType.JSON);
        //????????????
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();
    }

    /**
     * ????????????
     *
     * @param index ??????
     * @return true:?????????false:??????
     * @throws IOException ES????????????
     */
    @Override
    public Boolean deleteIndex(String index) throws IOException {
        //????????????????????????
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        //??????????????????
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();//??????????????????????????????????????????
    }

    @Override
    public Boolean updateMaxResult(int maxResult, String... indices) throws IOException {
        UpdateSettingsRequest request = new UpdateSettingsRequest(indices);
        Settings settings = Settings.builder()
                .put("max_result_window", maxResult)
                .build();
        request.settings(settings);
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putSettings(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();
    }

    @Override
    public void initCreateIndex(String mapping, String index, Integer shards, Integer replicas, Integer maxResult) throws IOException {
        boolean existsIndex = existsIndex(index);
        if (!existsIndex) {
            log.info("????????????????????????????????????index={},shards={},replicas={},mapping={}", index, shards, replicas, mapping);
            //????????????
            createIndex(shards, replicas, index);
            //??????mapping
            putMapping(mapping, index);
            if (maxResult != null) {
                //??????ES?????????????????????(??????1???)
                updateMaxResult(maxResult, index);
            }
        }
    }

}
