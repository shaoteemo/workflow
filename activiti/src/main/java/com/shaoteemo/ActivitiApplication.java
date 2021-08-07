package com.shaoteemo;

import com.shaoteemo.listener.MyEventListener;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableSwagger2
public class ActivitiApplication {
    @Autowired
    private MyEventListener listener;

    public static void main(String[] args) {
        SpringApplication.run(ActivitiApplication.class, args);
    }

    /*手动部署一个流程*/
    /*@Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
                                  final RuntimeService runtimeService,
                                  final TaskService taskService) {


        //测试添加监听器
//        runtimeService.addEventListener(listener);

        //移除监听器
//        runtimeService.removeEventListener(listener);

        //API任务事件监听
//        runtimeService.dispatchEvent();

        repositoryService.createDeployment()
                .addClasspathResource("processes/one-task-process.bpmn20.xml")//加载流程文件
                .name("one-task-process")//给流程定义命名
                .category("测试流程部署")//设置流程类型
                .deploy();

        return strings ->
        {
            System.out.println("Number of process definitions : "
                    + repositoryService.createProcessDefinitionQuery().count());
            System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
            runtimeService.startProcessInstanceByKey("oneTaskProcess");
            System.out.println("Number of tasks after process start: " + taskService.createTaskQuery().count());
        };
    }*/
}
