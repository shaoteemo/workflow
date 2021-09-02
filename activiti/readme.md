# Activiti6.0.0实践报告（草稿）

## 环境概述

|  环境栏目   | 版本号 |
| :---------: | ------ |
| Spring-Boot | 2.5.1  |
|  Activiti   | 6.0.0  |
|    MySQL    | 5.7    |

## 开始

### Pom.xml

**本文全程使用SpringBoot进行**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.1</version>
    </parent>
    <groupId>com.shaoteemo</groupId>
    <artifactId>activitiDemo</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <activiti.version>6.0.0</activiti.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.activiti</groupId>
            <artifactId>activiti-spring-boot-starter-basic</artifactId>
            <version>${activiti.version}</version>
        </dependency>
        <!--<dependency>
            <groupId>org.activiti</groupId>
            <artifactId>activiti-spring-boot-starter-security</artifactId>
            <version>${activiti.version}</version>
        </dependency>-->
        <dependency>
            <artifactId>spring-boot-starter-web</artifactId>
            <groupId>org.springframework.boot</groupId>
        </dependency>
        <!--测试-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.38</version>
        </dependency>
        <!--jdbc-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <!--druid数据源-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.1.12</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.3</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.74</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 启动类

```java
@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class ActivitiApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ActivitiApplication.class , args);
    }
}
```

说明：SpringBoot整合Activiti时启动会出现Could not find class [org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration]异常（目前原因未知）。因此需要exclude掉该类的自动配置。

### 流程引擎配置类ProcessEngineConfiguration关系图谱

```java
|--ProcessEngineConfiguration
        |--ProcessEngineConfigurationImpl
        |--SpringProcessEngineConfiguration①
        |--MultiSchemaMultiTenantProcessEngineConfiguration②
        |--JtaProcessEngineConfiguration③
        |--StandaloneProcessEngineConfiguration
        |--StandaloneInMemProcessEngineConfiguration④
```

```
①Spring环境中使用。
②引擎以独立方式使用。
③引擎以独立模式运行时带有 JTA 事务使用。
④测试时使用。内存数据库使用默认H2.
```

org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration用于集成SpringBoot

### ActivitiProperties配置介绍(常用配置)

```
checkProcessDefinitions（true）：是否检查流程定义文件(默认路径：classpath:/resources/processes中的流程文件)。该配置项如果没有对应的流程文件会抛出I/O异常.
asyncExecutorActivate（true）：启用异步执行
restApiEnabled：restApi支持
deploymentName：部署名称
mailServerHost（"localhost"）：邮件服务地址
mailServerPort(1025)：邮件服务端口号
mailServerUserName：邮件服务用户名
mailServerPassword：邮件服务密码
mailServerDefaultFrom：邮件服务默认发件人
mailServerUseSsl：是否使用SSL
mailServerUseTls：有否使用TLS
databaseSchemaUpdate：
flase：activiti在启动时，会对比数据库表中保存的版本，如果没有表或者版本不匹配，将抛出异常。（生产环境常用）
true：默认值。activiti会对数据库中所有表进行更新操作。如果表不存在，则自动创建。（开发时常用）
create_drop：在activiti启动时创建表，在关闭时删除表（必须手动关闭	引擎，才能删除表）。（单元测试常用）
drop-create：在activiti启动时删除原来的旧表，然后在创建新表（不需	要手动关闭引擎）。
databaseSchema：
isDbIdentityUsed（true）：是否启用数据库认证用户表
isDbHistoryUsed（true）：是否启用数据库历史记录表
historyLevel(audit)：历史记录级别none、activity、audit、full
processDefinitionLocationPrefix（"classpath:/processes/"）：流程文件定义位置前缀
processDefinitionLocationSuffixes("\*\*.bpmn20.xml", "\*\*.bpmn")：流程文件定义位置后缀
restApiMapping（/api/*）：restAPI映射地址
restApiServletName（"activitiRestApi"）：restAPI Servlet 名称
jpaEnabled（true）：是否启用jps支持
customMybatisMappers：
customMybatisXMLMappers:
```

### 数据库配置项

**本文全程使用MySQL数据库，推荐使用MySQL数据库版本为5.6.4+**

1. Activity默认初始表简介：

   ACT_RE_*: RE代表数据存储库表（repository）。带有此前缀的表包含静态信息，例如流程定义和流程资源（图像、规则等）。

   ACT_RU_*: RU代表运行时存储表（runtime）。带有此前缀的表一般存储包含流程运行时的数据，包括流程实例，用户任务，变量，任务等等。Activiti 只在流程实例执行期间存储运行时数据，并在流程实例结束时移除记录。这样会使表更加轻、快。

   ACT_ID_*:ID代表身份等信息存储（identity）。带有此前缀的表一般存储包含身份信息，如用户、组等等信息。

   ACT_HI_*:HI代表身份等信息存储（history）。带有此前缀的表一般存储包含历史数据，例如过去的流程实例、变量、任务等。

   ACT_GE_*:GE代表一般数据（general），用于各种用例。

### JobExcutor作业执行器

documentUrl：https://www.activiti.org/userguide/#jobExecutorConfiguration

### 流程部署缓存配置及日志

documentUrl：https://www.activiti.org/userguide/#processDefinitionCacheConfiguration

### 事件监听器

```
用于监听流程引擎中的事件。
调度的所有事件都是 org.activiti.engine.delegate.event.ActivitiEvent 的子类型.
使用监听器需要实现org.activiti.engine.delegate.event.ActivitiEventListener。
API介绍：源码：com/shaoteemo/listener/MyEventListener.java
官方提供的基本事件监听器： org.activiti.engine.delegate.event.BaseEntityEventListener
```

监听器的使用配置：

```
1.org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration来配置使用监听器。(spring)
2.通过使用API：org.activiti.engine.RuntimeService来配置移除监听器。这种方式添加的监听器流程重启不会保存
3.添加监听器到特定流程定义：该方式通过流程图（bpmn）来添加监听器。
4.抛出BPMN流程事件监听器（测试）：
documentUrl：https://www.activiti.org/userguide/#_listeners_throwing_bpmn_events
5.API事件调度。通过RuntimeService给特定ActivitiEvent进行监听。
支持的事件类型一枚举的方式定义在：org.activiti.engine.delegate.event.ActivitiEventType
docUrl:https://www.activiti.org/userguide/#eventDispatcherEventTypes
```

所有的已 ENTITY_\\*相关的都与实体事件相关

```
ENTITY_CREATED, ENTITY_INITIALIZED, ENTITY_DELETED: Attachment, Comment, Deployment, Execution, Group, IdentityLink, Job, Model, ProcessDefinition, ProcessInstance, Task, User.

ENTITY_UPDATED: Attachment, Deployment, Execution, Group, IdentityLink, Job, Model, ProcessDefinition, ProcessInstance, Task, User.

ENTITY_SUSPENDED, ENTITY_ACTIVATED: ProcessDefinition, ProcessInstance/Execution, Task.
```

## ActivitiAPI

### ProcessEngineAPI&Service

![](http://rep.shaoteemo.com/activiti/api.services.png)

```java
|--ProcessEngineConfiguration  <-- activiti.cfg.xml
	|--ProcessEngine
		|--RepositoryService ①
		|--TaskService ②
		|--IdentityService ③
		|--FormService ④
		|--RuntimeService ⑤
		|--ManagementService ⑥
		|--HistoryService ⑦
```

ProcessEngines.getDefaultProcessEngine()获取默认的流程引擎

```
1.ProcessEngines是线程安全的。
2.ProcessEngine是单例的，在获取一次后再次获取会得到一个相同的引擎。
3.所有的服务都是无状态的，即使是在分布式的环境中不同的Activiti服务操作的都是同一个数据库，因此任意服务调用都是幂等的。
```

类说明解析

```
①RepositoryService：官方说：“这可能是使用Activiti需要的第一个服务”
	作用：提供管理、操作部署和流程定义的操作（流程定义是 BPMN 2.0 流程的 Java 副本）。部署是 Activiti 引擎中的打包单元.流程图是定义每个步骤流程结构和行为，
	一个部署可以包含多个 BPMN 2.0 xml 文件和任何其他资源。
	部署会交由引擎进行检查解析，并存入数据库。
	查询引擎已知的部署和流程定义，暂停和激活流程的部署，检索资源如流程部署的文件或引擎生成的流程图，检索流程定义的 POJO 版本……因此RepositoryService 关于静态信息存储获取，一般这些数据很少改动或不变。
	
②TaskService：需要由实际用户执行的任务是 BPM 引擎（例如 Activiti）的核心会使用到该服务。
	作用：
		1.查询分配给用户或组的任务。
		2.创建新的与流程实例无关的独立任务。
		3.控制分配给某个用户或某个用户以某种方式参与当前任务。
		4.获取并完成任务。这个人可能是委托人。

③IdentityService：用于对用户和组进行管理（CRUD）。
	事实上Activiti在运行时不会对用户进行任何检查。如任务可以分配给任何用户但引擎不会验证该用户是否属于当前系统。

④FormService：可选服务。用于启动表单和任务表单数据查询和提交。
	Activiti BPMN2.0中可以定义这些表单数据。

⑤RuntimeService：用于启动流程定义的新的流程实例。
	流程实例：即流程定义的一次执行。通常一个流程定义会有多个实例同时进行（即有多个审批流运行）。
	执行：执行是指向流程实例当前所在位置的指针（当前流程执行到流程图的哪一步了）。
	该服务也用于检索流程变量和流程变量的存储服务，这些数据仅用于流程实例。提供流程查询实例和执行情况。
	只要流程实例等待外部触发器并且流程需要继续，就会一直使用该服务。

⑥ManagementService：检索有关数据库表和表元数据的信息。
	一般的，自定义Activiti程序时不需要使用到该服务。

⑦HistoryService：用于查询ActivitiEngine收集的所有历史数据
	在流程执行时，引擎可以保留大量数据（可配置），例如：流程实例启动时间、谁执行了哪些任务、完成任务需要多长时间、每个流程实例中遵循的路径等。

⑧DynamicBpmnService：动态Bpmn服务
	可以用于更改流程定义中的一部分而无需从新部署。
		例如，您可以更改流程定义中用户任务的受理人定义，或更改服务任务的类名。
```

### 异常策略

来源于一个基本的org.activiti.engine.ActivitiException非检查时异常

```java
//部分常见的异常类型
|-- ActivitiException
    |-- ActivitiWrongDbException//据库模式版本和引擎版本不匹配时抛出。
    |-- ActivitiOptimisticLockingException//同一数据条目的并发访问导致数据存储中发生乐观锁定
    |-- ActivitiClassLoadingException
    |-- ActivitiObjectNotFoundException//请求或操作的对象不存在
    |-- ActivitiIllegalArgumentException//非法参数
    |-- ActivitiTaskAlreadyClaimedException//重复申明任务
```

### Activiti的使用（实践）

````
1.绘制流程图（bpmn20.xml或bpmn）
2.部署流程
3.启动实例
4.完成节点任务
	流程或实例的暂停或激活
	
查询：
	1.Activiti提供了QueryAPI用于查询各种数据
	2.对于自定义的查询可以使用SQL查询createNativeTaskQuery()进行查询docUrl:https://www.activiti.org/userguide/#queryAPI
	
流程变量
	一个流程实例可以有任意数量的变量。每个变量都存储在 ACT_RU_VARIABLE 数据库表中的**一行**中。
	流程变量可以在流程运行期间添加变量。
	setVariable：为实例级变量。整个执行树可见。
	setVariableLocal等方法一般给特定的执行设值变量。该变量仅在该执行中可见，在执行树中不可见。
	变量通常用于 Java 委托、表达式、执行或任务侦听器、脚本等。
	在调用流程参数方法时会从数据库中获取所有相关变量并缓存。在特定的场景中Activiti ver. >=5.17 提供了对应的这种行为查询。（fetchAllVariables）

瞬态变量
	docUrl:https://www.activiti.org/userguide/#apiTransientVariables

UEL和JUEL表达式
	统一表达式语言（${var},${var == 0}...）
	这种表达式可用于：Java Service tasks、执行监听器、任务监听听器和条件序列流。
	表达式类型
		1.值表达式：解析为一个值
		2.方法表达式：调用一个方法。

​```java
${printer.print()}//无参
${myBean.addNewOrder('orderName')}//固定值
${myBean.doSomething(myVar, execution)}//有参
```

	友情提示：这些表达式支持：比较、beans、数组、list，maps(映射?)

单元测试（docUrl:https://www.activiti.org/userguide/#apiUnitTesting）
	继承：org.activiti.engine.test.ActivitiTestCase

部署方式配置设置
	见代码

Spring、Spring-boot集成
	见代码
````

## 部署

### 	流程部署的方式

```
1.流程定义文件部署(bpmn20.xml,.bpmn)
2.zip包统一部署
3.二进制流
4.BpmnModel
		……
```

### 	流程版本相关

```
Activiti没有版本控制相关概念。流程定义的版本是在部署期间创建的。
Activiti 会在ProcessDefinition时存储到数据库之前为其分配一个版本。
流程定义步骤（首先会初始化id、key、version、name）xml->db
	相关表：act_re_procdef
	1.XML文本文件中的id属性用于流程定义的Key
	2.name属性用作名称属性。如果没有name则使用id作为name值。
	3.第一次部署key时对应的version为1.对于所有具有相同key的后续部署version都>1。key用于区分流程定义。
	4.id的生成规则为：{流程定义的key}:{流程定义版本号}:{自动生成唯一ID}
自动生成唯一ID(generate-id)是添加的唯一编号，以保证集群环境中进程定义缓存的进程 id 的唯一性。
示例docUrl:https://www.activiti.org/userguide/#versioningOfProcessDefinitions
注意：Activiti执行流程实例进程时只认定Id
```

### 	流程图片的部署

```
部署的图片流程文件保存在数据库中(act_ge_bytearray).

如果部署没有提供流程图像ProcessEngine会自动为标准的bpmn20生成.png的流程图片并存入数据库(act_ge_bytearray)中。如果需要手动部署则文件名需满足如下格式：{bpmn文件名}.{流程Key}.{图片后缀(.png、.jpg……)}否则不会被流程引擎使用。（一个流程文件可能有多个process，以此区分不同的流程图像）

自动生成流程图片默认是开启的如需关闭配置：createDiagramOnDeploy即可。

自定义类别（targetNamespace）：见代码。（EventListener.xml，ActivityServiceImpl）
```

## BPMN2.0

### 	配置文件详解

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!--
    BPMN的根元素
        该标签下可以有个process标签(推荐一个流程一个bpmn20文件)
        targetNamespace：可以是任何内容，对流程定义进行分类很有用。

        添加如下命明空间使用在线模式
-->
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="Examples">
    <!--process元素
        有两个主要元素：
            1.id该属性时必须的，对应于定义流程的Key。通过这个ID可以通过startProcessInstanceByKey启动一个新的流程实例，
                注意该方法只会通过部署的最新版本流程启动流程实例。该方式区别于startProcessInstanceById见API（ActivityService.startProcessById）
            2.name可选属性。用于流程定义名称。引擎本身不使用此属性，因此它可用于在用户界面中显示更人性化的名称。[数据表：act_re_procdef]
    -->
    <process id="myProcessBpmn20" name="myProcessBpmn20" >

    </process>
</definitions>

```

### 	官方入门案例

```
目标：了解Activiti和基本的BPMN2.0概念
内容：某公司财务报表汇总发送给所有股东的审批。
流程：开始->编写每月的财务报表->上层层管理某一员审批->结束。
流程图描述：大致是一个开始事件（左边的圆圈），然后是两个用户任务：“编写月度财务报告”和“验证月度财务报告”，以一个结束事件结束（右边有粗边框的圆圈）。URl:https://www.activiti.org/userguide/#_getting_started_10_minute_tutorial
基本步骤
	注意：代码示例中并未按照教程完整实现该功能，主要以API使用为主。
	1.bpmn20.xml编写
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:activiti="http://activiti.org/bpmn"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             targetNamespace="http://www.activiti.org/processdef">
    <process id="financialReport" name="Monthly financial report reminder process" isExecutable="true">
        <startEvent id="theStart"/>
        <sequenceFlow sourceRef="theStart" targetRef="writeReportTask" id="flow1"/>
        <userTask id="writeReportTask" name="Write monthly financial report">
            <documentation>
                Write monthly financial report for publication to shareholders.
            </documentation>
            <!--定义当前节点操作组-->
            <potentialOwner>
                <resourceAssignmentExpression>
                    <formalExpression>accountancy</formalExpression>
                </resourceAssignmentExpression>
            </potentialOwner>
        </userTask>
        <sequenceFlow sourceRef="writeReportTask" targetRef="verifyReportTask" id="flow2"/>
        <userTask name="Verify monthly financial report" id="verifyReportTask">
            <documentation>
                Verify monthly financial report composed by the accountancy department.
                This financial report is going to be sent to all the company shareholders.
            </documentation>
            <!--定义当前节点操作组-->
            <potentialOwner>
                <resourceAssignmentExpression>
                    <formalExpression>management</formalExpression>
                </resourceAssignmentExpression>
            </potentialOwner>
        </userTask>
        <sequenceFlow sourceRef="verifyReportTask" targetRef="theEnd" id="flow3"/>
        <endEvent id="theEnd"/>
    </process>
    <bpmndi:BPMNDiagram id="BPMNDiagram_financialReport">
        <bpmndi:BPMNPlane bpmnElement="financialReport" id="BPMNPlane_financialReport">
            <bpmndi:BPMNShape bpmnElement="theStart">
                <omgdc:Bounds height="30.0" width="30.0" x="75.0" y="225.0"/>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="writeReportTask">
                <omgdc:Bounds height="80.0" width="100.0" x="165.0" y="200.0"/>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="verifyReportTask">
                <omgdc:Bounds height="80.0" width="100.0" x="330.0" y="200.0"/>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNShape bpmnElement="theEnd">
                <omgdc:Bounds height="28.0" width="28.0" x="480.0" y="226.0"/>
            </bpmndi:BPMNShape>
            <bpmndi:BPMNEdge bpmnElement="flow1">
                <omgdi:waypoint x="105.0" y="240.0"/>
                <omgdi:waypoint x="165.0" y="240.0"/>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow2">
                <omgdi:waypoint x="265.0" y="240.0"/>
                <omgdi:waypoint x="330.0" y="240.0"/>
            </bpmndi:BPMNEdge>
            <bpmndi:BPMNEdge bpmnElement="flow3">
                <omgdi:waypoint x="430.0" y="240.0"/>
                <omgdi:waypoint x="480.0" y="240.0"/>
            </bpmndi:BPMNEdge>
        </bpmndi:BPMNPlane>
    </bpmndi:BPMNDiagram>
</definitions>
```

```
2.部署流程并启动实例
3.通过TaskService检索任务，可根据用户检索，也可以根据组检索。（此处演示不适用。详见官方文档演示步骤。此处将以API调用的方式创建组和用户。并与之关联详见代码。）
4.领取任务（适用于组）
5.根据TaskID完成任务
```

## BPMN2.0详解

写在前面：介绍Activiti支持的BPMN20构造以及对BPMN标准的自定义扩展

### 		Events

```
事件：用于对生命周期过程发生的事情进行建模。图标总是一个⚪.在BPMN2.0中事件主要有如下两种类别。
	1.捕获：通过未填充的内部图标（即白色），捕捉事件与投掷事件在视觉上有所区别。
	2.抛出：通过填充黑色的内部图标在视觉上与捕获事件区分开来。
```

#### 1.定时器事件（TimerEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="定时器事件定义演示">

    <process id="timer_event" name="timerEvent">
        <startEvent id="theStart">
            <!--
                申明一个定时器事件任务
                    businessCalendarName：指向流程引擎配置中的业务日历。当省略业务日历时，将使用默认业务日历。
            -->
            <timerEventDefinition activiti:businessCalendarName="custom">
                <documentation>
                    这里就是一个开始事件的定时器事件的定义

                    定时器事件是由定义的定时器触发的事件。
                    开始时间、中间事件、边界事件都可以用。该事件的行为取决于使用的日历时间。
                    每个计时器事件都有一个默认的业务日历，但也可以在计时器事件定义上定义业务日历。
                </documentation>
                <!--
                    定时器定义必须仅包含以下元素之一
                -->
                <timeDate>
                    2011-03-11T12:13:14
                    <documentation>指定固定日期，何时触发触发器。（格式标准：ISO 8601）</documentation>
                </timeDate>

                <timeDuration>
                    P10D
                    <documentation>指定计时器在触发之前应该运行多长时间。如隔10天后触发（格式标准：ISO 8601）</documentation>
                </timeDuration>

                <!--
                    endTime：为该标签的可选属性。当达到该时间后程序将停止创建其他任务。
                    endTime也可以写在表达式中。如果两个都有被指定则系统默认使用属性的结束时间。
                -->
                <timeCycle activiti:endDate="2015-02-25T16:42:11+00:00">
                    R3/PT10H 或者写成 R3/PT10H/${EndDate}
                    <documentation>
                        指定重复执行间隔，多用于定期启动进程或为过期的用户任务发送多个提醒等。
                        时间周期可以有两种格式：
                            1.根据ISO 8601标准格式如上所示：R3/PT10H。
                            2.也可以是CRON表达式每5分钟触发一次，从整小时开始：0 0/5 * * * ?
                    </documentation>
                </timeCycle>
            </timerEventDefinition>
        </startEvent>
        <!--
            通用边界事件定义：
            以下内容也是一个定时器任务
        -->
        <boundaryEvent attachedToRef="firstLineSupport" id="escalationTimer" cancelActivity="true">
            <timerEventDefinition>
                <timeDuration>${duration}</timeDuration>
            </timerEventDefinition>
        </boundaryEvent>
    </process>
</definitions>
<!--
    注意：计时器仅在启用作业或异步执行器时触发（即需要在 activiti.cfg.xml 中将 jobExecutorActivate 或 asyncExecutorActivate 设置为 true，
    因为默认情况下禁用作业和异步执行器）。
    ps：SpringBoot集成Activiti6 asyncExecutorActivate默认是true？
-->
```

#### 2.错误事件（ErrorEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="错误事件定义演示">

    <!--
        提示：BPMN20中的错误与Java中的异常完全不同，两者也没有任何关联。
            BPMN错误事件是一种对业务异常建模的方法。而Java一样有其独有的异常处理方式。
    -->
    <process id="error_event" name="errorEvent">
        <endEvent>
            <errorEventDefinition errorRef="myError"/>
        </endEvent>
    </process>
</definitions>

```

#### 3.信号事件（SignalEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="信号事件定义演示">
<!--documentUrl:https://www.activiti.org/userguide/#bpmnSignalEventDefinitionQuery-->
    <!--
        声明信号
         activiti:scope:配置信号作用范围默认为“global”。（非BPMN2.0标准！）
    -->
    <signal id="alertSignal" name="alert" activiti:scope="processInstance">
        <documentation>
            信号事件是引用命名的信号事件。
        </documentation>
    </signal>
    <process id="signal_event" name="signalEvent">
        <intermediateThrowEvent id="throwSignalEvent" name="Alert">
            <documentation>抛出事件信号定义</documentation>
            <!--
                信号事件定义。
                signalRef：值为声明的信号元素。
            -->
            <signalEventDefinition signalRef="alertSignal" />
        </intermediateThrowEvent>
        <!--.....-->
        <intermediateCatchEvent id="catchSignalEvent" name="On Alert">
            <documentation>捕获事件信号定义</documentation>
            <signalEventDefinition signalRef="alertSignal"/>
        </intermediateCatchEvent>
        <!--.....-->
    </process>
</definitions>
<!--
    本Xml涉及少量的Java代码：SignalEventImpl.java

    信号事件作用域范围
        默认情况下，信号广播范围为整个引擎。因此如果在某个流程实例中抛出一个信号事件，
        其他具有不同流程定义的流程实例可以对这个事件触发做出反应。

        然而在某些情况下我们只希望对同一流程实例中的信号事件做出反应。
            eg.一个用例是流程实例中使用同步机制，如过两个或多个活动互斥。
-->
<!--
    一些本节中出现的标记文字描述
        MessageStartEvent：消息开始事件，一个圆⚪中间一个信封✉图标

-->
```

#### 4.消息事件（MeaasgeEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="消息事件定义演示">
    <!--DocumentUrl:https://www.activiti.org/userguide/#bpmnMessageEventDefinition-->
    <!--
        消息事件是引用命名消息的事件。消息具有名称和有效载荷。
        与信号不同，消息事件总是针对单个接收者。
    -->
    <!--消息声明-->
    <message id="newInvoice" name="newInvoiceMessage"/>
    <message id="payment" name="paymentMessage"/>

    <process id="message_event" name="messageEvent">
        <startEvent id="messageStart">
            <documentation>这是一个消息开始事件</documentation>
            <!--消息定义-->
            <messageEventDefinition messageRef="newInvoice"/>
        </startEvent>
        <!--...-->
        <intermediateCatchEvent id="paymentEvt">
            <documentation>这是一个捕获消息事件</documentation>
            <messageEventDefinition messageRef="payment"/>
        </intermediateCatchEvent>
        <!--...-->
    </process>
</definitions>
<!--
    关联的Java: com.shaoteemo.bpmn.MessageEventImpl
-->
```

#### 5.开始事件（StartEvents）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="开始事件演示">
<!--
    开始时间指示流程开始的位置
        启动事件的类型（进程在消息到达时启动，在特定的时间间隔等），
        定义流程的启动方式在事件的可视化表示中显示为一个小图标（timer_event.xml\message_event.xml等）。

    事件总是为捕获事件：概念上，事件是（在任何时候）等待某个触发器发生。

-->
    <process id="start_event" name="startEvent">
        <!--
            initiator:标识在进程启动时将存储经过身份验证的用户ID的变量名称。
        -->
        <startEvent id="request" activiti:initiator="initiator"/>
    </process>
</definitions>
<!--关联的Java:com.shaoteemo.bpmn.StartEventImpl-->
```

#### 6.空开始事件（None Start Event）常用的一个事件

![空开始事件](http://rep.shaoteemo.com/activiti/bpmn.none.start.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="空开始事件演示">

    <!--
        空开始事件：未指定启动流程实例的触发器事件。
            这意味着这样的流程需要手动启动。通过RuntimeService中的API启动。

            NOTE：子流程总是有一个空开始事件

    -->
    <process id="none_start_event" name="noneStartEvent">
        <!--空开始事件的XML表示是正常的开始事件声明，没有任何子元素（其他开始事件类型都有一个声明类型的子元素）。-->
        <startEvent id="start" name="my start event"/>

        <!--
            对空开始事件的自定义扩展
            formKey：对用户在启动新流程实例时必须填写的表单模板的引用。
        -->
        <startEvent id="startFormKey" activiti:formKey="org/activiti/examples/taskforms/request.form"/>
    </process>
</definitions>

```

#### 7.定时器启动事件（Timer Start Event)

![定时器图片样式](http://rep.shaoteemo.com/activiti/bpmn.clock.start.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="定时器启动事件演示">

    <!--
        计时器启动事件用于在给定时间创建流程实例。既可以用于一次启动流程，也可以在某一事件间隔启动多次流程。

        NOTE：
            1.子流程不能有计时器启动事件。
            2.流程部署会立即安排启动计时器事件。不需要调用RuntimeService中的启动API就会自动根据条件启动。

        定时器启动事件的 XML 表示是普通的启动事件声明，带有定时器定义子元素.

    -->
    <process id="timer_start_event" name="timerStartEvent">
        <!--示例1：流程将从2011年3月11日12:13开始，以5分钟为间隔启动4次-->
        <startEvent id="theStartExample1">
            <timerEventDefinition>
                <timeCycle>R4/2011-03-11T12:13/PT5M</timeCycle>
            </timerEventDefinition>
        </startEvent>

        <!--示例：进程将在选定日期开始一次-->
        <startEvent id="theStartExample2">
            <timerEventDefinition>
                <timeDate>2088-09-01T00:00:00</timeDate>
            </timerEventDefinition>
        </startEvent>
    </process>
</definitions>

```

#### 8.消息开始事件（Message Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.message.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="消息启动事件演示">

    <!--
        消息启动事件：可用于使用命名消息启动流程实例。有效地让我们使用消息名称从一组备选启动事件中选择符合条件的事件启动。

        使用一个或多个消息启动事件部署流程定义时，应注意以下事项：
            1.消息启动事件的名称在给定的流程定义中必须是唯一的。流程定义不能有多个同名的消息启动事件,否则
              Activiti 在部署流程定义时抛出异常，如果两个及以上的消息启动事件引用具有相同消息名称的消息，
              则两个或多个消息启动事件引用相同的消息。

            2.消息启动事件的名称在所有部署的流程定义中必须是唯一的，否则，Activiti 在部署流程定义时抛出异常，
              使得一个及以上的消息启动事件引用由不同流程定义部署的消息启动事件同名的消息。

            3.流程版本控制：在部署流程定义的新版本时，将取消先前版本的消息订阅。对于新版本中不存在的消息事件也是如此。

        启动对应的流程实例API方式见：Java: com.shaoteemo.bpmn.MessageEventImpl

            *JavaAPI中的传入属性messageName：是在 messageEventDefinition 的 messageRef 属性引用的消息元素的 name 属性中给出的名称。

            启动流程实例时，注意事项：
                1.消息启动事件仅在主流程上受支持。嵌入式子流程不支持消息启动事件。

                2.如果流程定义有多个消息启动事件，runtimeService.startProcessInstanceByMessage(…)允许选择适当的启动事件。

                3.如果流程定义有多个消息启动事件和一个无启动事件，runtimeService.startProcessInstanceByKey(…)和
                  runtimeService.startProcessInstanceById(…)使用无启动事件启动流程实例。

                4.如果流程定义有多个消息启动事件并且没有无启动事件，runtimeService.startProcessInstanceByKey(…)和
                  runtimeService.startProcessInstanceById(… )会抛出异常。

                5.如果流程定义具有单个消息启动事件，runtimeService.startProcessInstanceByKey(…)和
                  runtimeService.startProcessInstanceById(… )会使用消息启动事件启动一个新的流程实例。

                6.如果流程是以活动调用方式启动的，则仅在以下情况下才支持消息启动事件
                    6.1.除了消息开始事件之外，流程还有一个无开始事件
                    6.2.该流程只有一个消息启动事件，没有其他启动事件。

    -->

    <message id="newInvoice" name="newInvoiceMessage" />

    <process id="message_start_event" name="messageStartEvent">
        <startEvent id="messageStart" >
            <messageEventDefinition messageRef="newInvoice"/>
        </startEvent>
    </process>
</definitions>
```

#### 9.信号开始事件（Signal Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.signal.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="信号启动事件演示">

    <!--
        信号启动事件：可用于使用命名信号启动流程实例。

        信号启动事件启动方式：
            1.中间信号抛出事件
            2.通过 API（runtimeService.signalEventReceivedXXX 方法）从流程实例中触发信号。
                （Java Class:com.shaoteemo.bpmn.SignalEventImpl）

            注意，在以上两种情况下，还可以在流程实例的同步和异步启动之间进行选择。

    -->

    <signal id="signal" name="theSignal"/>

    <process id="signal_start_event" name="signalStartEvent">
        <startEvent id="start">
            <signalEventDefinition id="theSignalEventDefinition" signalRef="signal"  />
        </startEvent>
        <sequenceFlow sourceRef="start" targetRef="task"/>
        <userTask id="task" name="Task in process A" />
        <sequenceFlow id="flow2" sourceRef="task" targetRef="end" />
        <endEvent id="end" />
    </process>
</definitions>
```

#### 10.错误开始事件（Error Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.error.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="错误开始事件演示">


    <!--
        错误启动事件可用于触发事件子流程。
        错误启动事件不能用于启动流程实例。

        错误启动事件总是中断。
    -->

    <process id="error_start_event" name="errorStartEvent">
        <startEvent id="messageStart" >
            <errorEventDefinition errorRef="someError" />
        </startEvent>
    </process>
</definitions>

```

#### 11.结束事件（End Events）

结束事件表示（子）流程的（路径的）结束。结束事件总是抛出。

这意味着当流程执行到达结束事件时，会抛出一个结果。结果的类型由事件的内部黑色图标描述。

#### 12.空结束事件（None End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.none.end.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="空结束事件演示">

    <!--
        空结束事件意味着到达事件时抛出的结果是未指定的。
        因此，除了结束当前的执行路径外，引擎不会做任何额外的事情。
    -->

    <process id="none_end_event" name="noneEndEvent">
        <endEvent id="end" name="my end event"/>
    </process>
</definitions>
```

#### 13.错误结束事件（Error End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.error.end.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="错误结束事件演示">

    <!--
        当流程执行到达错误结束事件时，当前执行路径结束并抛出错误。

        此错误可以被匹配的中间边界错误事件捕获。如果没有找到匹配的边界错误事件，则会抛出异常。
    -->


    <error id="myError" errorCode="123"/>

    <process id="error_end_event" name="errorEndEvent">
        <!--
            错误结束事件表示为结束事件，带有一个 errorEventDefinition 子元素。

            errorRef 属性可以引用在进程外定义的错误元素：
        -->
        <endEvent id="end">
            <errorEventDefinition errorRef="myError"/>
        </endEvent>
    </process>

    <!--
        errorCode 用于查找匹配的捕获边界错误事件。

        如果 errorRef 与任何定义的错误都不匹配，则 errorRef 会匹配 errorCode。
    -->
    <error id="myError2" errorCode="error123"/>

    <process id="myProcess">
        <endEvent id="myErrorEndEvent">
            <errorEventDefinition errorRef="myError2"/>
        </endEvent>
        <!--这两个效果相同-->
        <endEvent id="myErrorEndEvent2">
            <errorEventDefinition errorRef="error123" />
        </endEvent>
    </process>
</definitions>
```

#### 14.终止结束事件（Terminate End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.terminate.end.event.png)

