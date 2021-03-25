package com.david;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;

public class MyBaitsPlusCodeGenerator {

    static String author = "David";
    static String driverName = "com.mysql.cj.jdbc.Driver";
    static String url = "jdbc:mysql://192.168.112.129:3306/order?characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowMultiQueries=true&useTimezone=true&serverTimezone=GMT%2B8";
    static String username = "root";
    static String password = "root";
    static String outputDir = "/core/mybatis-plus-generator/src/test/java";
    static String resources = "/core/mybatis-plus-generator/src/test/resources/mapper/";
    static String packageParent = "com.david.order";
    static String[] include = {
            "order_info"
    };

    public static void main(String[] args) {
        build();
    }

    private static void build() {
        String projectPath = System.getProperty("user.dir");
        //配置 GlobalConfig
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(projectPath + outputDir);
        globalConfig.setAuthor(author);
        globalConfig.setOpen(false);
        //配置 DataSourceConfig
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl(url);
        dataSourceConfig.setDriverName(driverName);
        dataSourceConfig.setUsername(username);
        dataSourceConfig.setPassword(password);
        // 代码生成器
        AutoGenerator autoGenerator = new AutoGenerator();
        autoGenerator.setGlobalConfig(globalConfig);
        autoGenerator.setDataSource(dataSourceConfig);
        // 包配置
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent(packageParent);
        packageConfig.setService("service");
        packageConfig.setServiceImpl("service.impl");
        packageConfig.setEntity("entity");
        System.out.println(packageConfig.getModuleName());
        autoGenerator.setPackageInfo(packageConfig);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        // String templatePath = "/templates/mapper.xml.vm";
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig(templatePath) {// 自定义配置会被优先输出
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
//                return projectPath + resources + tableInfo.getEntityName() + "Dao" + ".xml";
                return projectPath + resources + tableInfo.getEntityName() + "Mapper" + ".xml";
            }
        });
        cfg.setFileOutConfigList(focList);
        autoGenerator.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        // 配置自定义输出模板
        //指定自定义模板路径，注意不要带上.ftl/.vm, 会根据使用的模板引擎自动识别
        //templateConfig.setEntity("templates/entity2.java");
        templateConfig.setEntity("templates/entity.java");
        // templateConfig.setService();
        // templateConfig.setController();
        templateConfig.setService(null);
        templateConfig.setServiceImpl(null);
        templateConfig.setController(null);
        templateConfig.setXml(null);
        autoGenerator.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //strategy.setSuperEntityClass("你自己的父类实体,没有就不用设置!");
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        //strategy.setSuperControllerClass("你自己的父类控制器,没有就不用设置!");//公共父类
        //strategy.setSuperEntityColumns("id");//写于父类中的公共字段
        strategy.setInclude(include);
        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(packageConfig.getModuleName() + "_");
        autoGenerator.setStrategy(strategy);
        autoGenerator.setTemplateEngine(new FreemarkerTemplateEngine());
        autoGenerator.execute();

    }

}
