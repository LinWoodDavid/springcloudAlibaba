# 一、环境准备

## 1.1安装nacos并运行

## 1.2进入Nacos控制台,创建seata命名空间

    命名空间ID为 seata_namespace_id
    命名空间名称为 seata

## 1.3 Seata数据库创建

    创建名称为seata的数据库并执行sql
    脚本链接: 
    https://github.com/seata/seata/blob/1.4.1/script/server/db/mysql.sql

# 二. 使用docker进行seata-server安装

## 2.1创建registry.conf文件

## 2.2推送Seata依赖配置至Nacos

    从Seata的GitHub官方源码获取配置文件(config.txt)和推送脚本文件(nacos/nacos-config.sh)
    地址：https://github.com/seata/seata/blob/develop/script/config-center

    ├── config.txt
    └── nacos
    └── nacos-config.sh

### 2.2.1修改配置文件 config.txt

    vim /opt/seata/config.txt

### 2.2.2修改事务组和MySQL连接信息，修改信息如下：

    service.vgroupMapping.mall_tx_group=default 
    store.mode=db
    store.db.driverClassName=com.mysql.cj.jdbc.Driver
    store.db.url=jdbc:mysql://192.168.112.129:3306/seata?useUnicode=true&rewriteBatchedStatements=true
    store.db.user=root
    store.db.password=root

### 2.2.3执行推送命令

-t seata_namespace_id 指定Nacos配置命名空间ID -g SEATA_GROUP 指定Nacos配置组名称

    bash nacos-config.sh -h 192.168.112.130 -p 8848 -g SEATA_GROUP -t seata_namespace_id -u nacos -w nacos

### 2.2.4创建 registry.conf、docker-compose.yml文件

    vim registry.conf
    vim docker-compose.yml

### 2.2.5 docker部署seata-server

    docker-compose up -d

## 三、Seata客户端配置

### 3.1分别在项目的数据库中创建undo_log表

    github网址:
    https://github.com/seata/seata/blob/1.4.1/script/client/at/db/mysql.sql

### 3.2添加依赖

    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        <version>2.2.1.RELEASE</version>
    </dependency>

### 3.3 yml配置

    Seata官方Github源码库Spring配置链接：
    https://github.com/seata/seata/blob/1.4.1/script/client/spring/application.yml

    tx-service-group: mall_tx_group 配置事务群组,其中群组名称 mall_tx_group 需和服务端的配置 service.vgroupMapping.mall_tx_group=default 一致
    enable-auto-data-source-proxy: true 自动为Seata开启了代理数据源，实现集成对undo_log表操作
    namespace: seata_namespace_id seata-server一致
    group: SEATA_GROUP seata-server一致
###