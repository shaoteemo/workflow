package com.shaoteemo.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.springframework.stereotype.Component;

/**
 * @author shaoteemo
 * <p>
 * 事件监听器的实现
 */
@Component
public class MyEventListener implements ActivitiEventListener {
    /**
     * 事件的捕获
     *
     * @param event 事件对象
     */
    @Override
    public void onEvent(ActivitiEvent event) {
        ActivitiEventType type = event.getType();
        switch (type) {
            case JOB_EXECUTION_SUCCESS:
                System.out.println("一个任务执行完了！");
                break;
            case JOB_EXECUTION_FAILURE:
                System.out.println("一个任务执行异常！");
                break;
            default:
                System.out.println(this.getClass().getName() + "监听器收到了一个消息：" + type);
        }
    }

    /**
     * 该方法在onEvent方法抛出异常时的执行。false则表示忽略异常
     * true则抛出异常使对应的方法执行失败
     *
     * @return
     */
    @Override
    public boolean isFailOnException() {
        System.out.println("执行任务出错！但是不会影响任务后面的流程。");
        return false;
    }
}
