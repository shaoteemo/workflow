package com.shaoteemo.service.impl;

import com.shaoteemo.service.ActivityService;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * @author shaoteemo
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repository;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private IdentityService identityService;

    @Resource
    private HistoryService historyService;

    @Override
    public boolean deployBpmnXml(String fileName, String deployName) {
        try {
            this.repository.createDeployment()
                    .name(deployName)
                    .addClasspathResource("processes/" + fileName)
                    .deploy();

            log.info("当前已定义的流程数为：" + repository.createProcessDefinitionQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deployBpmnXml(String fileName) {
        try {
            this.repository.createDeployment()
                    .addClasspathResource("processes/" + fileName)
                    .deploy();

            log.info("当前已定义的流程数为：" + repository.createProcessDefinitionQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Task> getTaskByUser(String userId) {
        List<Task> list = this.taskService.createTaskQuery().taskAssignee(userId).list();
        log.info("{}", list);
        return list;
    }

    /**
     * 领取任务后其他用户将不可以再次领取任务。否则抛出{@link org.activiti.engine.ActivitiTaskAlreadyClaimedException}
     * 领取并不会判断该用户是否具备条件
     *
     * @param taskId
     * @param userId
     * @return
     */
    @Override
    public boolean claimTask(String taskId, String userId) {
        try {
            //此处应该根据流程实例来获取当前task
//            String processInstanceId = null;
//            List<Task> list = this.taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            this.taskService.claim(taskId, userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addUserToGroup(String userId, String groupId) {
        try {
            this.identityService.createMembership(userId, groupId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Object createUser(String firstName, String lastName) {
        String id = UUID.randomUUID().toString();
        User user = this.identityService.newUser(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword("111111");
        this.identityService.saveUser(user);
        return id;
    }

    @Override
    public Object createGroup(String groupName) {
        String id = UUID.randomUUID().toString();
        Group group = this.identityService.newGroup(id);
        group.setName(groupName);
        this.identityService.saveGroup(group);
        return id;
    }

    @Override
    public boolean deployBpmnXmlAndImg(String xmlFile, String image, String deployName) {
        try {
            this.repository.createDeployment()
                    //部署名称
                    .name(deployName)
                    .addClasspathResource("processes/" + xmlFile)
                    .addClasspathResource("processes/image/" + image)
                    //指定部署类别
//                    .category("")
                    .deploy();
            DeploymentQuery deploymentQuery = this.repository.createDeploymentQuery().deploymentName(deployName);
            log.info("流程[" + deployName + "]部署成功!ID为：" + deploymentQuery.singleResult().getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deployBpmnZip(String zipFile) {
        try {
            this.repository.createDeployment()
                    .addZipInputStream(new ZipInputStream(new FileInputStream(zipFile), StandardCharsets.UTF_8))
                    .deploy();
            log.info("当前已定义的流程数为：" + repository.createProcessDefinitionQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deployBpmnModel(String resourceName, BpmnModel model) {
        try {
            this.repository.createDeployment()
                    .addBpmnModel(resourceName, model)
                    .deploy();
            log.info("当前已定义流程数为：" + this.repository.createProcessDefinitionQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ProcessDefinition getProcessDefinitionByDeployId(String deployId) {

        return this.repository.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
    }

    @Override
    public boolean startProcessById(String processId, Map<String, Object> variables) {
        try {
            if (processId == null || processId.trim().equals("")) return false;
            /*
             * 该API会通过部署xml在数据库生成的ID进行部署。格式大致为key:version:id
             * */
            ProcessInstance processInstance = this.runtimeService.startProcessInstanceById(processId, variables);
            log.info("实例ID：" + processInstance.getProcessInstanceId());
            log.info("当前启动的流程实例数量为：" + runtimeService.createProcessInstanceQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Object getTaskListCandidateUser(String userName) {
        try {
            if (StringUtils.hasLength(userName)) {
                List<Task> list = this.taskService.createTaskQuery().taskCandidateUser(userName).list();
                return list;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object getTaskListCandidateGroup(String group) {
        try {
            if (StringUtils.hasLength(group)) {

                List<Task> list = this.taskService.createTaskQuery().taskCandidateGroup(group).list();
                log.info("{}", list);
                return list;
            }
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean startProcess(String processKey, Map<String, Object> variables) {
        try {
            if (processKey == null || processKey.trim().equals("")) return false;
            /*
             * 该API会通过bpmn20流程定义xml的id进行部署。详细见[ActivityBpmn20.bpmn20.xml]
             * */
            ProcessInstance processInstance = null;
            if (!CollectionUtils.isEmpty(variables))
                processInstance = this.runtimeService.startProcessInstanceByKey(processKey, variables);
            else
                processInstance = this.runtimeService.startProcessInstanceByKey(processKey);
            log.info("实例ID：" + processInstance.getProcessInstanceId());
            log.info("当前启动的流程实例数量为：" + runtimeService.createProcessInstanceQuery().count());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public InputStream getProcessImageResource(String processKey) {
        ProcessDefinition processDefinition = this.repository.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .singleResult();
        String resourceName = processDefinition.getDiagramResourceName();
        return this.repository.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
    }

    /**
     * 当前的案例流程定义有Group的概念。并且为"management"。因此这儿不应该为固定值。
     *
     * @return
     */
    @Override
    public Object getAllTaskForManagement() {
        return this.taskService.createTaskQuery().taskCandidateGroup("management").list().stream().map(TaskInfo::getName).collect(Collectors.toList());
    }

    @Override
    public Object getCurrentTaskByInstanceId(String instId) {
        List<Task> list = this.taskService.createTaskQuery().processInstanceId(instId).list();
        log.info("{}", list);
        return list;
//                .stream().map(TaskInfo::getName).collect(Collectors.toList());
    }

    /**
     * 某些条件下完成任务并不会判断当前人是否符合条件
     *
     * @param taskId
     * @param variables
     * @return
     */
    @Override
    public boolean completeTaskByTaskId(String taskId, Map<String, Object> variables) {
        try {
            if (CollectionUtils.isEmpty(variables))
                this.taskService.complete(taskId);
            else
                this.taskService.complete(taskId, variables);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Object completeTask(String instanceId, Map<String, Object> variables) {

        try {
            if (!processIsShutdown(instanceId)) {
                List<Task> task = this.taskService.createTaskQuery().processInstanceId(instanceId).list();
                this.taskService.complete(task.get(0).getId(), variables);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean processIsShutdown(String instanceId) {
        ProcessInstance processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        return processInstance == null;
    }

    @Override
    public Map<String, Object> getProcessInstanceEndInfo(String processInstanceId) {
        Map<String, Object> variables = new HashMap<>();
        HistoricProcessInstance historyInstance = this.historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (historyInstance == null) return null;
        variables.put("流程ID：", historyInstance.getId());
        variables.put("流程定义ID：", historyInstance.getProcessDefinitionId());
        variables.put("流程部署ID：", historyInstance.getDeploymentId());
        variables.put("发起人：", historyInstance.getStartUserId());
        variables.put("开始时间：", historyInstance.getStartTime());
        variables.put("结束时间：", historyInstance.getEndTime());

        return variables;
    }

    @Override
    public boolean suspendProcessByKey(String processKey) {
        try {
            this.repository.suspendProcessDefinitionById(processKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean suspendInstanceById(String instanceId) {
        this.runtimeService.suspendProcessInstanceById(instanceId);
        return true;
    }

    @Override
    public boolean processIsSuspend(String processKey) {
        return this.repository.isProcessDefinitionSuspended(processKey);
    }

    @Override
    public boolean processInstanceIsSuspend(String instanceId) {
        ProcessInstance suspended = this.runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).suspended().singleResult();
        return suspended.isSuspended();
    }

    @Override
    public boolean activateProcessByKey(String processKey) {
        this.repository.activateProcessDefinitionById(processKey);
        return true;
    }

    @Override
    public boolean activateProcessInstanceById(String instanceId) {
        this.runtimeService.activateProcessInstanceById(instanceId);
        return true;
    }


}
