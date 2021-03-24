package com.david.scheduler.config;

import com.david.scheduler.properties.ExecutorProperties;
import com.david.scheduler.properties.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
public class XxlJobAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "xxl.job.base", name = "adminAddresses")
    @ConfigurationProperties(prefix = "xxl.job.base")
    public XxlJobProperties xxlJobProperties() {
        return new XxlJobProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "xxl.job.executor", name = "appname")
    @ConfigurationProperties(prefix = "xxl.job.executor")
    public ExecutorProperties executorProperties() {
        return new ExecutorProperties();
    }

    @Bean
    @ConditionalOnBean(value = {ExecutorProperties.class, XxlJobProperties.class})
    public XxlJobSpringExecutor xxlJobExecutor(@Autowired XxlJobProperties xxlJobProperties,
                                               @Autowired ExecutorProperties executorProperties) {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobSpringExecutor.setAccessToken(xxlJobProperties.getAccessToken());
        xxlJobSpringExecutor.setAppname(executorProperties.getAppname());
        xxlJobSpringExecutor.setAddress(executorProperties.getAddress());
        xxlJobSpringExecutor.setIp(executorProperties.getIp());
        if (executorProperties.getPort() != null) {
            xxlJobSpringExecutor.setPort(executorProperties.getPort());
        }
        xxlJobSpringExecutor.setLogPath(executorProperties.getLogPath());
        if (executorProperties.getLogRetentionDays() != null) {
            xxlJobSpringExecutor.setLogRetentionDays(executorProperties.getLogRetentionDays());
        }
        return xxlJobSpringExecutor;
    }

    /**
     * 针对多网卡、容器内部署等情况，可借助 "spring-cloud-commons" 提供的 "InetUtils" 组件灵活定制注册IP；
     *
     *      1、引入依赖：
     *          <dependency>
     *             <groupId>org.springframework.cloud</groupId>
     *             <artifactId>spring-cloud-commons</artifactId>
     *             <version>${version}</version>
     *         </dependency>
     *
     *      2、配置文件，或者容器启动变量
     *          spring.cloud.inetutils.preferred-networks: 'xxx.xxx.xxx.'
     *
     *      3、获取IP
     *          String ip_ = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
     */

    /**
     * 配置
     # web port
     server.port=9001
     spring.application.name=scheduler
     # no web
     #spring.main.web-environment=false
     # log config
     #logging.config=classpath:logback.xml
     ### xxl-job admin address list, such as "http://address" or "http://address01,http://address02"
     xxl.job.base.adminAddresses=http://47.114.137.37:18080/xxl-job-admin
     ### xxl-job, access token
     xxl.job.base.accessToken=
     ### xxl-job executor appname
     xxl.job.executor.appname=xxl-job-executor-sample
     ### xxl-job executor registry-address: default use address to registry , otherwise use ip:port if address is null
     xxl.job.executor.address=
     ### xxl-job executor server-info
     xxl.job.executor.ip=
     xxl.job.executor.port=9999
     ### xxl-job executor log-path
     xxl.job.executor.logpath=/data/logs/xxl-job/
     ### xxl-job executor log-retention-days
     xxl.job.executor.logretentiondays=30
     */
}
