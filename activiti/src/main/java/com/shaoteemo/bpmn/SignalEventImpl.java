package com.shaoteemo.bpmn;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * 抛出事件信号
 *
 * @author shaoteemo
 * @Date 2021/8/4
 */
public class SignalEventImpl implements SignalEvent
{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RuntimeService runtimeService;

    @Override
    public Object querySignalEvent(String signalName)
    {

        List<Execution> list = this.runtimeService.createExecutionQuery()
                .signalEventSubscriptionName(signalName)
                .list();
        log.info("符合信号{}的执行器ID有：{}" , signalName , list);
        return list;
    }
}

interface SignalEvent{

    Object querySignalEvent(String signalName);

}
