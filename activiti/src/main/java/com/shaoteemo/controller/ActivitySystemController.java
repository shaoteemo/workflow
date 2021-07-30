package com.shaoteemo.controller;

import com.shaoteemo.service.ActivitySystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author shaoteemo
 *
 * 测试接口
 *
 */
@RestController
@RequestMapping("act/sys")
public class ActivitySystemController
{

    @Autowired
    private ActivitySystemService activityService;


    @GetMapping("close")
    public boolean shutdownProcessEngine(){
        return !this.activityService.shutdownProcessEngine();
    }

    @GetMapping("status")
    public boolean processEngineStatus(){
        return this.activityService.processEngineStatus();
    }

    @GetMapping("start")
    public boolean startProcessEngine(){
        return this.activityService.startProcessEngine();
    }

    @GetMapping("restart")
    public boolean restartProcessEngine(){
        return this.activityService.restartProcessEngine();
    }

    @GetMapping("deployCount")
    public Object deploymentCount(){
        return this.activityService.queryDeploymentProcessCount();
    }

    @GetMapping("instCount")
    public Object processInstanceCount(){
        return this.activityService.queryStartedProcessInstance();
    }


}
