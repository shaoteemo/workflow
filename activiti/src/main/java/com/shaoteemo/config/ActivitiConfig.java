package com.shaoteemo.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.shaoteemo.listener.MyEventListener;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;

@Configuration
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {

    @Autowired
    private MyEventListener listener;

    @Autowired
    private DataSource dataSource;

//    @Autowired
//    private ProcessEngine processEngine;

    /*@Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource getDataSource(){
        return new DruidDataSource();
    }*/

    /*@Bean("dataSource")
    @ConfigurationProperties(prefix = "mysql.datasource")
    public DataSource getDataSource()
    {
        return DataSourceBuilder.create().type(BasicDataSource.class).build();
    }*/

    /**
     * 独立流程引擎配置法
     *
     * @return
     */
    /*@Bean
    public StandaloneProcessEngineConfiguration processEngineConfiguration()
    {
        StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setDatabaseSchemaUpdate(String.valueOf(true));
        configuration.setJobExecutorActivate(false);
        configuration.setAsyncExecutorEnabled(true);
        configuration.setAsyncExecutorActivate(false);

        return configuration;

    }
*/
    @Bean
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(SpringAsyncExecutor springAsyncExecutor) throws IOException {

        SpringProcessEngineConfiguration configuration = baseSpringProcessEngineConfiguration
                (dataSource, getPlatformTransactionManager(), springAsyncExecutor);
        //添加监听器
        configuration.setEventListeners(Arrays.asList(listener));
        /*
            指定流程部署的方式
                官方支持的由三种：
                    default：将所有资源分组到一个部署中，过滤重复部署。默认值。
                    single-resource：为每个单独的资源创建一个单独的部署，过滤重复部署。
                    resource-parent-folder：为共享同一父文件夹的资源创建单独的部署，过滤重复部署。
                            此值可用于为大多数资源创建单独的部署，但仍然可以通过将它们放在共享文件夹中来对一些资源进行分组。

            自定义部署方式可以继承SpringProcessEngineConfiguration并重写getAutoDeploymentStrategy(String deploymentMode)方法。
        */

//        configuration.setDeploymentMode("single-resource");

        /*
         * 部署流程图乱码
         * 原因：默认的字符集(Arial)没有对应的中文字符编码
         *
         */
        configuration.setActivityFontName("宋体");
        configuration.setLabelFontName("宋体");

        /*
         * 开启任务执行器或异步任务执行器。默认：true
         * 可用于定时任务，必须开启此项之一。
         * */
//        configuration.setAsyncExecutorActivate(true);
        /*
         * 设置业务时间及日历格式。默认：GregorianCalendar
         * */
//        configuration.setClock();

        //关闭Activiti引擎自动生成流程图片
//        configuration.setCreateDiagramOnDeploy(false);
        return configuration;
    }

    @Bean
    public PlatformTransactionManager getPlatformTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    //Junit Using
    /*@Bean
    public ActivitiRule getActivitiRule(){
        ActivitiRule activitiRule = new ActivitiRule();
        activitiRule.setProcessEngine(processEngine);
        return activitiRule;
    }*/

}