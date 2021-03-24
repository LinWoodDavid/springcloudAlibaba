package com.david.elasticsearch.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@Builder
public class Sort implements Serializable {
    private static final long serialVersionUID = -3650624579617762104L;

    /**
     * 排序字段名
     */
    private String name;

    /**
     * true:正序,false:倒序
     */
    private Boolean order;

    /**
     * 排序字段路径(用于子查询)
     */
    private String path;

}
