package com.shaoteemo.bpmn;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 消息事件
 * <p>
 * 注意：消息的接受应该与如JMS之类的消息队列服务或框架来处理。所以应该将这部分集成至引擎
 *
 * @author shaoteemo
 * @Date 2021/8/9
 */
public class MessageEventImpl implements MessageEvent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RepositoryService repositoryService;

    @Override
    public Object startProcessInstanceByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
        return this.startProcessByMessage(messageName, businessKey, processVariables).getProcessInstanceId();
    }

    @Override
    public Object messageEventReceived(String messageName, String executionId, Map<String, Object> processVariables) {
        if (!CollectionUtils.isEmpty(processVariables)){
            this.runtimeService.messageEventReceived(messageName, executionId, processVariables);
            return true;
        }
        this.runtimeService.messageEventReceived(messageName, executionId);
        return true;
    }

    @Override
    public Object queryMessageEventByDefinition(String messageName) {
        ProcessDefinition processDefinition = this.repositoryService.createProcessDefinitionQuery()
                .messageEventSubscriptionName(messageName)
                //特定的消息订阅只有可能返回一个或者零个
                .singleResult();
        log.info("符合的流程定义：{}" , processDefinition);
        return processDefinition;
    }

    /**
     *
     * 提示：本类查询为关联查询，会有一些条件进行筛选，因此应返回单个结果。
     *
     */
    @Override
    public Object queryExecutionBySubscriptMessage(String messageName) {
        Execution execution = this.runtimeService.createExecutionQuery()
                .messageEventSubscriptionName(messageName)
//                .processVariableValueEquals("", "")
//                .variableValueEquals("", "")
                .singleResult();
        log.info("符合的执行器：{}" , execution);
        return execution;
    }

    private ProcessInstance startProcessByMessage(String messageName, String businessKey, Map<String, Object> processVariables) {
        if (StringUtils.hasLength(businessKey) && !CollectionUtils.isEmpty(processVariables))
            return this.runtimeService.startProcessInstanceByMessage(messageName, businessKey, processVariables);

        if (!StringUtils.hasLength(businessKey) && !CollectionUtils.isEmpty(processVariables))
            return this.runtimeService.startProcessInstanceByMessage(messageName, processVariables);

        return this.runtimeService.startProcessInstanceByMessage(messageName);
    }
}

interface MessageEvent {

    /**
     * 当接收到消息触发新的流程实例启动的时候可以使用下面的方式开启新的流程实例
     * 以下方法可以使用引用的消息名启动流程实例
     *
     * @param messageName 声明的消息名称
     * @return 流程实例ID
     */
    Object startProcessInstanceByMessage(@NonNull String messageName, String businessKey, Map<String, Object> processVariables);

    /**
     * 订阅消息触发操作。将消息传递给执行器ID
     * @param messageName 消息名称
     * @param executionId 执行器ID
     * @param processVariables 流程实例参数
     * @return --
     */
    Object messageEventReceived(@NonNull String messageName, @NonNull String executionId, Map<String, Object> processVariables);

    /**
     * 根据消息名称获取对应的流程定义
     * @param messageName 消息名称
     * @return --
     */
    Object queryMessageEventByDefinition(@NonNull String messageName);

    /**
     * 查询订阅消息的执行器
     * @param messageName 消息名称
     * @return --
     */
    Object queryExecutionBySubscriptMessage(@NonNull String messageName);

}
