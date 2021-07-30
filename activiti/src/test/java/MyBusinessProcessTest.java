import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author shaoteemo
 * @Date 2021/7/13 10:16
 */
/*@ContextConfiguration("classpath:org/activiti/spring/test/junit4/springTypicalUsageTest-context.xml")
public class MyBusinessProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    @Rule
    public ActivitiRule activitiSpringRule;

    @Test
    @Deployment
    public void simpleProcessTest() {
        runtimeService.startProcessInstanceByKey("simpleProcess");
        Task task = taskService.createTaskQuery().singleResult();
//        assertEquals("My Task", task.getName());

        taskService.complete(task.getId());
//        assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    }
}*/
