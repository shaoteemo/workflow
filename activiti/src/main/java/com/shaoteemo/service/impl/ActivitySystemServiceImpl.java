package com.shaoteemo.service.impl;

import com.shaoteemo.service.ActivitySystemService;
import org.activiti.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author shaoteemo
 * <p>
 * Activity Core
 */
@Service
public class ActivitySystemServiceImpl implements ActivitySystemService {
    /*获取默认的流程引擎*/
    @Resource
    private ProcessEngine processEngine /*= ProcessEngines.getDefaultProcessEngine()*/;

    /*
     *
     * 用于启动流程定义的新的流程实例
     *
     * */
    @Resource
    private RuntimeService runtimeService /*= processEngine.getRuntimeService()*/;

    /*
     *
     * 提供管理、操作部署和流程定义的操作（流程定义是 BPMN 2.0 流程的 Java 副本）
     *
     * */
    @Resource
    private RepositoryService repositoryService /*= processEngine.getRepositoryService()*/;

    /*
     *
     * 提供任务的获取查询。任务创建。并完成任务。
     *
     * */
    @Resource
    private TaskService taskService /*= processEngine.getTaskService()*/;

    /*检索有关数据库表和表元数据的信息*/
    @Resource
    private ManagementService managementService /*= processEngine.getManagementService()*/;

    /*用于对用户和组进行管理（CRUD）*/
    @Resource
    private IdentityService identityService /*= processEngine.getIdentityService()*/;

    /*用于查询ActivitiEngine收集的所有历史数据*/
    @Resource
    private HistoryService historyService /*= processEngine.getHistoryService()*/;

    /*可选服务。用于启动表单和任务表单数据查询和提交*/
    @Resource
    private FormService formService /*= processEngine.getFormService()*/;

    /*动态Bpmn服务*/
//    private DynamicBpmnService dynamicBpmnService = processEngine.getDynamicBpmnService();

    /*独立的引擎可以通过内部的初始化或销毁正常启动销毁流程引擎*/
    public void processEngineInitAndDestroy() {
        /*手动初始化流程引擎
         * 初始化时会加载activiti.cfg.xml和activiti-context.xml配置文件并初始话ProcessEngine
         * */
        ProcessEngines.init();
        //手动销毁流程引擎
        ProcessEngines.destroy();
    }

    @Override
    public boolean startProcessEngine() {
        ProcessEngines.init();
        return ProcessEngines.isInitialized();
    }

    @Override
    public boolean restartProcessEngine() {
        shutdownProcessEngine();
        startProcessEngine();
        return ProcessEngines.isInitialized();
    }

    @Override
    public Object queryDeploymentProcessCount() {
        return this.repositoryService.createProcessDefinitionQuery().count();
    }

    @Override
    public Object queryStartedProcessInstance() {
        return this.runtimeService.createProcessInstanceQuery().count();
    }

    @Override
    public boolean shutdownProcessEngine() {
        ProcessEngines.destroy();
        return ProcessEngines.isInitialized();
    }

    @Override
    public boolean processEngineStatus() {
        return ProcessEngines.isInitialized();
    }
}
