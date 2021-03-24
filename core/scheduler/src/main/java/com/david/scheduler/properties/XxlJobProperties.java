package com.david.scheduler.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * xxl-job 基础 配置
 */
@Data
public class XxlJobProperties implements Serializable {
    private static final long serialVersionUID = 2722202786113931449L;

    private String adminAddresses;
    private String accessToken;

}
