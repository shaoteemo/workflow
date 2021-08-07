package com.shaoteemo.service;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author shaoteemo
 */
public interface ActivityService {
    /*部署Classpath:/processes/下的流程文件*/
    boolean deployBpmnXml(String fileName, String deployName);

    /*部署Classpath:/processes/下的流程文件*/
    boolean deployBpmnXml(String fileName);

    /*部署Classpath:/processes/下的流程文件和流程图文件*/
    boolean deployBpmnXmlAndImg(String xmlFile, String image, String deployName);

    /*部署Zip包流程*/
    boolean deployBpmnZip(String zipFile);

    /*部署BPMN Model*/
    boolean deployBpmnModel(String resourceName, BpmnModel model);

    /*根据流程Key开启一个流程实例*/
    boolean startProcess(String processKey, Map<String, Object> variables);

    /*根据流程部署的ID启动流程实例*/
    boolean startProcessById(String processId, Map<String, Object> variables);

    /*根据流程Key获取部署的图片流*/
    InputStream getProcessImageResource(String processKey);

    /*更具部署的流程ID获取流程定义*/
    ProcessDefinition getProcessDefinitionByDeployId(String deployId);

    /*获取所有management下的Task节点信息*/
    Object getAllTaskForManagement();

    /*获取当前流程实例任务节点信息*/
    Object getCurrentTaskByInstanceId(String instId);

    /*完成流程实例当前任务*/
    Object completeTask(String instanceId, Map<String, Object> variables);

    /*流程实例是否结束*/
    boolean processIsShutdown(String instanceId);

    /*暂停当前部署流程（不暂停已启动实例）*/
    boolean suspendProcessByKey(String processKey);

    /*暂停当前流程实例*/
    boolean suspendInstanceById(String instanceId);

    /*部署流程是否暂停*/
    boolean processIsSuspend(String processKey);

    /*当前实例是否挂起*/
    boolean processInstanceIsSuspend(String instanceId);

    /*激活部署的流程*/
    boolean activateProcessByKey(String processKey);

    /*激活流程实例*/
    boolean activateProcessInstanceById(String instanceId);

    /*根据候选人查询Task列表*/
    Object getTaskListCandidateUser(String userName);

    /*根据候选组查询Task列表*/
    Object getTaskListCandidateGroup(String group);

    /*创建一个用户组*/
    Object createGroup(String groupName);

    /*创建一个用户*/
    Object createUser(String firstName, String lastName);

    /*将一个用户添加至一个组*/
    boolean addUserToGroup(String userId, String groupId);

    /*领取任务*/
    boolean claimTask(String taskId, String userId);

    /*根据用户ID获取当前的用户任务*/
    List<Task> getTaskByUser(String userId);

    /*根据TaskId完成当前任务*/
    boolean completeTaskByTaskId(String taskId, Map<String, Object> variables);

    /*通过历史记录来验证当前流程是否结束*/
    Map<String, Object> getProcessInstanceEndInfo(String processInstanceId);
}
