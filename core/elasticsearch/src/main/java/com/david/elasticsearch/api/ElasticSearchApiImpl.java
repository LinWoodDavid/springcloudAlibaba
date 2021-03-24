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
        //处理响应结果
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
            throw new RuntimeException("Elasticsearch服务异常: " + searchResponse);
        }
    }

    private SearchResponse executeSearch(QueryCriteria queryCriteria, String... indices) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indices);
        //源过滤
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (null != queryCriteria.getIncludes() || null != queryCriteria.getExcludes()) {
            searchSourceBuilder.fetchSource(queryCriteria.getIncludes(), queryCriteria.getExcludes());
        }
        //查询条件
        searchSourceBuilder.query(queryCriteria.getQueryBuilder());
        //排序
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
        //分页
        searchSourceBuilder.from(queryCriteria.getFrom());
        searchSourceBuilder.size(queryCriteria.getSize());
        searchRequest.source(searchSourceBuilder);
        //同步请求
        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * 按条件搜索
     *
     * @param tClass        类
     * @param queryCriteria 查询条件
     * @param <T>           泛型
     * @param indices       索引
     * @return
     * @throws IOException ES服务异常
     */
    @Override
    public <T> SearchResult<T> search(Class<T> tClass, QueryCriteria queryCriteria, String... indices) throws IOException {
        SearchResponse searchResponse = executeSearch(queryCriteria, indices);
        //处理响应结果
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
            throw new RuntimeException("Elasticsearch服务异常: " + searchResponse);
        }
    }

    /**
     * 文档是否存在
     *
     * @param index      索引
     * @param documentId 文档id
     * @return true：存在，false:不存在
     * @throws IOException ES服务异常
     */
    @Override
    public Boolean existsDocument(String documentId, String index) throws IOException {
        GetRequest getRequest = new GetRequest(index).id(documentId);
        getRequest.fetchSourceContext(new FetchSourceContext(false));//禁用抓取_source
        getRequest.storedFields("_none_");//禁用提取字段
        return restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void insertDocument(String documentId, String documentJson, String index) throws IOException {
        insertDocument(documentId, documentJson, null, index);
    }

    @Override
    public void insertDocument(String documentId, String documentJson, String routing, String index) throws IOException {
        //刷新策略：立即刷新
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
     * 批量添加文档
     *
     * @param index     索引
     * @param documents 文档集合，key:文档id value-json文档
     * @throws IOException ES服务异常
     */
    @Override
    public void batchInsertDocuments(Map<String, String> documents, String index) throws IOException {
        //刷新策略：立即刷新
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

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //执行批量操作
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Elasticsearch批量新增失败,documents:").append(JSON.toJSONString(documents)).append(";失败响应:");
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
     * 更新文档
     *
     * @param index      索引
     * @param documentId 文档id
     * @param json       文档
     * @throws IOException ES服务异常
     */
    @Override
    public void updateDocumentByDocumentId(String documentId, String json, String index) throws IOException {
        //刷新策略：立即刷新
        updateDocumentByDocumentId(documentId, json, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void updateDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        //创建更新请求
        UpdateRequest request = new UpdateRequest(index, documentId);
        //部分文档更新
        request.doc(json, XContentType.JSON);

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //同步执行
        restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    @Override
    public void upsertDocumentByDocumentId(String documentId, String json, String index) throws IOException {
        //刷新策略：立即刷新
        upsertDocumentByDocumentId(documentId, json, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void upsertDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        //创建更新请求
        UpdateRequest request = new UpdateRequest(index, documentId);
        //不存则插入文档
        request.doc(json, XContentType.JSON);
        request.upsert(json, XContentType.JSON);

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //同步执行
        restHighLevelClient.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量更新文档
     *
     * @param index     索引
     * @param documents 文档集合，key:文档id value-json文档
     * @throws IOException ES服务异常
     */
    @Override
    public void batchUpdateDocuments(Map<String, String> documents, String index) throws IOException {
        //刷新策略：立即刷新
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

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //执行批量操作
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    @Override
    public void batchUpsertDocuments(Map<String, String> documents, String index) throws IOException {
        //刷新策略：立即刷新
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

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //执行批量操作
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

        if (bulkResponse.hasFailures()) {
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Elasticsearch批量Upsert失败,documents:").append(JSON.toJSONString(documents)).append(";失败响应:");
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
     * 根据id删除文档
     *
     * @param index      索引
     * @param documentId 文档id
     * @throws IOException ES服务异常
     */
    @Override
    public void deleteDocumentByDocumentId(String documentId, String index) throws IOException {
        //刷新策略：立即刷新
        deleteDocumentByDocumentId(documentId, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void deleteDocumentByDocumentId(String documentId, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(index, String.valueOf(documentId));

        //刷新策略
        if (refreshPolicy != null) {
            //立即刷新
            deleteRequest.setRefreshPolicy(refreshPolicy);
            deleteRequest.setRefreshPolicy("true");
        }

        restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    /**
     * 根据id删除文档
     *
     * @param index       索引
     * @param documentIds 文档id
     * @throws IOException ES服务异常
     */
    @Override
    public void deleteDocumentByDocumentIds(List<String> documentIds, String index) throws IOException {
        //刷新策略：立即刷新
        deleteDocumentByDocumentIds(documentIds, index, WriteRequest.RefreshPolicy.IMMEDIATE);
    }

    @Override
    public void deleteDocumentByDocumentIds(List<String> documentIds, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException {
        BulkRequest request = new BulkRequest();
        documentIds.forEach(documentId -> {
            DeleteRequest deleteRequest = new DeleteRequest(index, String.valueOf(documentId));
            request.add(deleteRequest);
        });

        //刷新策略
        if (refreshPolicy != null) {
            request.setRefreshPolicy(refreshPolicy);
            request.setRefreshPolicy("true");
        }

        //执行批量操作
        restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
    }

    /**
     * 索引是否存在
     *
     * @param index 索引
     * @return true：存在， false:不存在
     * @throws IOException ES服务异常
     */
    @Override
    public Boolean existsIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 创建索引
     *
     * @param index    索引
     * @param shards   分片数
     * @param replicas 副本数
     * @return true or false
     * @throws IOException ES服务异常
     */
    @Override
    public Boolean createIndex(Integer shards, Integer replicas, String index) throws IOException {
        //创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(index);
        //索引设置
        request.settings(Settings.builder()
                .put("index.number_of_shards", shards)
                .put("index.number_of_replicas", replicas)
        );
        //同步创建索引
        CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        //获取索引响应
        return createIndexResponse.isAcknowledged();//指示是否所有节点都已确认请求
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
     * 设置 mapping
     *
     * @param index
     * @param mapping
     * @return
     * @throws IOException ES服务异常
     */
    @Override
    public Boolean putMapping(String mapping, String index) throws IOException {
        //创建映射请求
        PutMappingRequest request = new PutMappingRequest(index);
        //设置映射
        request.source(mapping, XContentType.JSON);
        //同步执行
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().putMapping(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();
    }

    /**
     * 删除索引
     *
     * @param index 索引
     * @return true:成功，false:失败
     * @throws IOException ES服务异常
     */
    @Override
    public Boolean deleteIndex(String index) throws IOException {
        //创建删除索引请求
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        //同步删除索引
        AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return acknowledgedResponse.isAcknowledged();//指示是否所有节点都已确认请求
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
            log.info("索引不存在开始创建索引。index={},shards={},replicas={},mapping={}", index, shards, replicas, mapping);
            //创建索引
            createIndex(shards, replicas, index);
            //设置mapping
            putMapping(mapping, index);
            if (maxResult != null) {
                //设置ES最大返回结果数(默认1万)
                updateMaxResult(maxResult, index);
            }
        }
    }

}
