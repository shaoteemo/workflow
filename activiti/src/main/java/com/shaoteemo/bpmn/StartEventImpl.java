package com.shaoteemo.bpmn;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;

import javax.annotation.Resource;

/**
 * 开始事件
 * <p>
 * 主要演示开始事件的已认证用户的存储
 *
 * @author shaoteemo
 * @Date 2021/8/27
 */
public class StartEventImpl implements StartEvent{

    @Resource
    private IdentityService identityService;

    @Resource
    private RuntimeService runtimeService;

    @Override
    public Object startProcessWithIndicates(String authenticatedUserId , String processKey) {
        try {
            /*设置已认证的用户ID*/
            this.identityService.setAuthenticatedUserId(authenticatedUserId);
            this.runtimeService.startProcessInstanceByKey(processKey);
            return true;
        } finally {
            /*还原认证用户状态*/
            this.identityService.setAuthenticatedUserId(null);
        }
    }
}

interface StartEvent{

    /**
     * 在启动时存储已认证的身份ID
     * @param authenticatedUserId 已认证的用户ID
     */
    Object startProcessWithIndicates(String authenticatedUserId , String processKey);
}
