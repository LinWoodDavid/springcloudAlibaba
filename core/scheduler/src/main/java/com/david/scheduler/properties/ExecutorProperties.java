package com.david.scheduler.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * xxl-job 执行器配置
 */
@Data
public class ExecutorProperties implements Serializable {
    private static final long serialVersionUID = 4403913825200478772L;

    private String appname;
    private String address;
    private String ip;
    private Integer port;
    private String logPath;
    private Integer logRetentionDays;

}
