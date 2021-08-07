package com.shaoteemo.bpmn;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 * 抛出事件信号
 *
 * @author shaoteemo
 * @Date 2021/8/4
 */
public class SignalEventImpl implements SignalEvent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RuntimeService runtimeService;

    @Override
    public Object querySignalEvent(String signalName) {

        List<Execution> list = this.runtimeService.createExecutionQuery()
                .signalEventSubscriptionName(signalName)
                .list();
        log.info("符合信号{}的执行器有：{}", signalName, list);
        return list;
    }

    @Override
    public Object receivedSignalEvent(String signalName, String... executionId) {
        if (executionId == null || executionId.length == 0)
            this.runtimeService.signalEventReceived(signalName);
        else
            this.runtimeService.signalEventReceived(signalName, executionId[0]);
        return null;
    }
}

interface SignalEvent {

    /**
     * 获取满足信号的执行器
     *
     * @param signalName 信号名称
     * @return 执行器集合
     */
    Object querySignalEvent(String signalName);

    /**
     * 通知接受获取信号消息
     *
     * @param signalName  信号名称
     * @param executionId 执行器ID（可选）
     * @return --
     */
    Object receivedSignalEvent(String signalName, String... executionId);

}
