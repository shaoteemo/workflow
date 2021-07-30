package com.shaoteemo.controller;

import com.shaoteemo.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

@RestController
@RequestMapping("act")
@Api(tags = "Activity测试模块")
public class ActivityController
{

    @Autowired
    private ActivityService activityService;


    @PostMapping("deploy")
    @ApiOperation(value = "根据文件名流程部署")
    public Object deployBpmnForResources(String fileName , String deployName){
        return this.activityService.deployBpmnXml(fileName , deployName);
    }

    @GetMapping("deploys/{xml}/{image}/{deployName}")
    @ApiOperation(value = "根据文件名流程部署和流程图片")
    public Object deployBpmnAndImageForResources(@PathVariable String xml , @PathVariable String image , @PathVariable String deployName){
        return this.activityService.deployBpmnXmlAndImg(xml, image, deployName);
    }

    /*
    *
    *
     参数实例：
        {
            "employeeName":"Kermit",
            "numberOfDays":4,
            "vacationMotivation":"I'm really tired!"
        }
     启动的流程Key：vacationRequest
    * */
    @PostMapping("start")
    @ApiOperation(value = "根据流程key启动一个实例")
    public Object startProcessByKey(@RequestBody(required = false) Map<String , Object> constant , String key){
        return this.activityService.startProcess(key,constant);
    }


    @GetMapping("processPng/{processKey}")
    @ApiOperation(value = "根据流程key获取流程图片")
    public void getProcessPng(@PathVariable String processKey , HttpServletResponse response) throws Exception{
        InputStream inputStream = this.activityService.getProcessImageResource(processKey);
        OutputStream outputStream = response.getOutputStream();
        if (inputStream != null){
            for (int b = -1 ; (b = inputStream.read()) != -1 ;) outputStream.write(b);
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
    }

    @GetMapping("tasks")
    @ApiOperation(value = "获取Management组所有任务")
    public Object getAllTask(){
        return this.activityService.getAllTaskForManagement();
    }

    @GetMapping("tasksUser")
    @ApiOperation(value = "获取用户所有任务")
    public Object getAllTaskForUser(String username){
        return this.activityService.getTaskListCandidateUser(username);
    }

    @GetMapping("tasksGroup")
    @ApiOperation(value = "获取用户组所有任务")
    public Object getAllTaskForGroup(String group){
        return this.activityService.getTaskListCandidateGroup(group);
    }

    @GetMapping("task/{id}")
    @ApiOperation(value = "根据流程实例ID获取任务")
    public Object getTaskNameByInstanceId(@PathVariable String id){
        return this.activityService.getCurrentTaskByInstanceId(id);
    }

    /*
        审批数据：
            {
              "vacationApproved": false,
              "managerMotivation": "We have a tight deadline!"
            }
        ID ： 查询数据库获得（2501）

     */
    @PostMapping("complete")
    @ApiOperation(value = "完成任务")
    public Object completeTask(@RequestBody Map<String , Object> constant , String id){
        return this.activityService.completeTask(id , constant);
    }

    @PostMapping("completeByTaskId")
    @ApiOperation(value = "根据TaskId完成任务")
    public Object completeByTaskId(@RequestBody(required = false) Map<String , Object> constant , String id){
        return this.activityService.completeTaskByTaskId(id , constant);
    }

    @GetMapping("pauseProcess/{key}")
    @ApiOperation(value = "暂停当前部署流程。不暂停对应的流程实例")
    public Object suspendProcessByKey(@PathVariable String key){
        return this.activityService.suspendProcessByKey(key);
    }

    @GetMapping("pauseProcessInstance/{id}")
    @ApiOperation(value = "暂停当前流程实例")
    public Object suspendProcessInstanceById(@PathVariable String id){
        return this.activityService.suspendInstanceById(id);
    }

    @GetMapping("activateProcess/{key}")
    @ApiOperation(value = "激活当前部署流程")
    public Object activateProcessByKey(@PathVariable String key){
        return this.activityService.activateProcessByKey(key);
    }

    @GetMapping("activateProcessInstance/{id}")
    @ApiOperation(value = "激活当前部署流程")
    public Object activateProcessInstanceById(@PathVariable String id){
        return this.activityService.activateProcessInstanceById(id);
    }

    @PostMapping("saveGroup/{groupName}")
    @ApiOperation(value = "创建一个用户组")
    public Object addGroup(@PathVariable String groupName){
        return this.activityService.createGroup(groupName);
    }

    @PostMapping("saveUser")
    @ApiOperation(value = "创建一个用户")
    public Object addUser(String firstName , String lastName){
        return this.activityService.createUser(firstName, lastName);
    }

    @PostMapping("addGroup")
    @ApiOperation(value = "将一个用户添加至用户组")
    public Object addUserToGroup(String groupId , String userId){
        return this.activityService.addUserToGroup(userId , groupId);
    }

    @PostMapping("getTask")
    @ApiOperation(value = "领取任务")
    public Object getTask(String taskId , String userId){
        return this.activityService.claimTask(taskId, userId);
    }

    @GetMapping("getTaskByUser")
    @ApiOperation(value = "根据用户ID获取当前任务")
    public Object getTaskByUser(String userId){
        return this.activityService.getTaskByUser(userId);
    }

    @GetMapping("getHistoryProcess")
    @ApiOperation(value = "获取历史流程数据")
    public Map<String , Object> queryHistoryProcess(String processInstanceId){
        return this.activityService.getProcessInstanceEndInfo(processInstanceId);
    }
}
