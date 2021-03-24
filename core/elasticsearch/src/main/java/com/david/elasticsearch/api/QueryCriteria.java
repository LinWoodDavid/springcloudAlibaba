package com.david.elasticsearch.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@Builder
public class QueryCriteria implements Serializable {

    private BoolQueryBuilder queryBuilder;//查询条件

    private Integer page;//页码
    private Integer size;//每页显示大小

    private String[] includes;//返回字段
    private String[] excludes;//排除字段

    private List<Sort> sorts;//排序

    private Integer from;

    public Integer getFrom() {
        return (page - 1) * size;
    }

}
