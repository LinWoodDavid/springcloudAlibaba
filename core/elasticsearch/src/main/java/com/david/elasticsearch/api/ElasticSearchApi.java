package com.david.elasticsearch.api;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticSearchApi {

    /**
     * 按条件搜索
     *
     * @param queryCriteria
     * @param indices
     * @return
     * @throws IOException
     */
    SearchResult<String> search(QueryCriteria queryCriteria, String... indices) throws IOException;

    /**
     * 按条件搜索
     *
     * @param tClass
     * @param queryCriteria 搜索条件queryCriteria
     * @param indices       索引
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> SearchResult<T> search(Class<T> tClass, QueryCriteria queryCriteria, String... indices) throws IOException;

    /**
     * 判断文档是否存在。
     * 批量判断文档是否存在可采用查询接口查询，查询不到判断不存在
     *
     * @param documentId 文档id
     * @param index      索引
     * @return
     * @throws IOException IOException
     */
    Boolean existsDocument(String documentId, String index) throws IOException;

    /**
     * 添加文档
     *
     * @param documentId   文档id
     * @param documentJson 文档
     * @param index        索引
     * @throws IOException IOException
     */
    void insertDocument(String documentId, String documentJson, String index) throws IOException;

    /**
     * 添加文档
     *
     * @param documentId   文档id
     * @param documentJson 文档
     * @param routing      路由
     * @param index        索引
     * @throws IOException IOException
     */
    void insertDocument(String documentId, String documentJson, String routing, String index) throws IOException;

    /**
     * 添加文档
     *
     * @param documentId    文档id
     * @param documentJson  文档
     * @param routing       路由
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @return
     * @throws IOException
     */
    IndexResponse insertDocument(String documentId, String documentJson, String routing, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 批量添加文档
     *
     * @param documents 文档集合。key:文档id,value：json文档
     * @param index     索引
     * @throws IOException IOException
     */
    void batchInsertDocuments(Map<String, String> documents, String index) throws IOException;

    /**
     * 批量添加文档
     *
     * @param documents     文档集合。key:文档id,value：json文档
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void batchInsertDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 更新文档
     *
     * @param documentId 文档id
     * @param json       文档
     * @param index      索引
     * @throws IOException IOException
     */
    void updateDocumentByDocumentId(String documentId, String json, String index) throws IOException;

    /**
     * 更新文档
     *
     * @param documentId    文档id
     * @param json          文档
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void updateDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 更新文档,不存在则添加
     *
     * @param documentId 文档id
     * @param json       文档
     * @param index      索引
     * @throws IOException IOException
     */
    void upsertDocumentByDocumentId(String documentId, String json, String index) throws IOException;

    /**
     * 更新文档,不存在则添加
     *
     * @param documentId    文档id
     * @param json          文档
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void upsertDocumentByDocumentId(String documentId, String json, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 批量更新文档
     *
     * @param documents 文档 key:文档id,value：json文档
     * @param index     索引
     * @throws IOException IOException
     */
    void batchUpdateDocuments(Map<String, String> documents, String index) throws IOException;

    /**
     * 批量更新文档
     *
     * @param documents     文档 key:文档id,value：json文档
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void batchUpdateDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 批量更新文档,不存在则添加
     *
     * @param documents 文档 key:文档id,value：json文档
     * @param index     索引
     * @throws IOException IOException
     */
    void batchUpsertDocuments(Map<String, String> documents, String index) throws IOException;

    /**
     * 批量更新文档,不存在则添加
     *
     * @param documents     文档 key:文档id,value：json文档
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void batchUpsertDocuments(Map<String, String> documents, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 根据文档id删除文档
     *
     * @param documentId 文档id
     * @param index      索引
     * @throws IOException IOException
     */
    void deleteDocumentByDocumentId(String documentId, String index) throws IOException;

    /**
     * 根据文档id删除文档
     *
     * @param documentId    文档id
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void deleteDocumentByDocumentId(String documentId, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 根据文档id集合删除文档
     *
     * @param documentIds 文档id
     * @param index       索引
     * @throws IOException IOException
     */
    void deleteDocumentByDocumentIds(List<String> documentIds, String index) throws IOException;

    /**
     * 根据文档id集合删除文档
     *
     * @param documentIds   文档id
     * @param index         索引
     * @param refreshPolicy 刷新策略
     * @throws IOException IOException
     */
    void deleteDocumentByDocumentIds(List<String> documentIds, String index, WriteRequest.RefreshPolicy refreshPolicy) throws IOException;

    /**
     * 索引是否存在
     *
     * @param index 索引
     * @return true:存在，false:不存在
     * @throws IOException
     */
    Boolean existsIndex(String index) throws IOException;

    /**
     * 创建索引
     *
     * @param shards   分片数
     * @param replicas 副本数
     * @param index    索引
     * @return
     * @throws IOException
     */
    Boolean createIndex(Integer shards, Integer replicas, String index) throws IOException;

    /**
     * 获取 mapping
     *
     * @param index index
     * @return
     * @throws IOException
     */
    Map<String, Object> getMapping(String index) throws IOException;

    /**
     * 设置mapping
     *
     * @param mapping mapping
     * @param index   index
     * @return true:成功，false:失败
     * @throws IOException
     */
    Boolean putMapping(String mapping, String index) throws IOException;

    /**
     * 删除索引
     *
     * @param index index
     * @return
     * @throws IOException
     */
    Boolean deleteIndex(String index) throws IOException;

    /**
     * 设置ES最大返回结果数(默认1万)
     *
     * @param maxResult 最大返回结果数
     * @param indices   索引
     * @return true:成功，false:失败
     * @throws IOException IOException
     */
    Boolean updateMaxResult(int maxResult, String... indices) throws IOException;

    /**
     * 索引不存在创建索引
     *
     * @param mapping   mapping
     * @param index     索引
     * @param shards    分片数
     * @param replicas  副本数
     * @param maxResult
     * @throws IOException IOException
     */
    void initCreateIndex(String mapping, String index, Integer shards, Integer replicas, Integer maxResult) throws IOException;

}
