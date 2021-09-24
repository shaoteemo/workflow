# Activiti指南（草稿）

## 写在前面

撰写人：ShaoTeemo

本文档翻译原文档为[官方文档](https://www.activiti.org/userguide/)。其中包含了项目搭建过程的文件信息。文中出现的译注为本人(译者)观点。**注意：本文档并不会完全翻译所有章节，只会翻译核心内容。**

## 环境概述

|  环境栏目   | 版本号 |
| :---------: | ------ |
| Spring-Boot | 2.5.1  |
|  Activiti   | 6.0.0  |
|    MySQL    | 5.7    |
| Oracle JDK  | 1.8.x  |

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
①RepositoryService：官方说：“这可能是使用Activiti需要的第一个服务”	作用：提供管理、操作部署和流程定义的操作（流程定义是 BPMN 2.0 流程的 Java 副本）。部署是 Activiti 引擎中的打包单元.流程图是定义每个步骤流程结构和行为，	一个部署可以包含多个 BPMN 2.0 xml 文件和任何其他资源。	部署会交由引擎进行检查解析，并存入数据库。	查询引擎已知的部署和流程定义，暂停和激活流程的部署，检索资源如流程部署的文件或引擎生成的流程图，检索流程定义的 POJO 版本……因此RepositoryService 关于静态信息存储获取，一般这些数据很少改动或不变。	②TaskService：需要由实际用户执行的任务是 BPM 引擎（例如 Activiti）的核心会使用到该服务。	作用：		1.查询分配给用户或组的任务。		2.创建新的与流程实例无关的独立任务。		3.控制分配给某个用户或某个用户以某种方式参与当前任务。		4.获取并完成任务。这个人可能是委托人。③IdentityService：用于对用户和组进行管理（CRUD）。	事实上Activiti在运行时不会对用户进行任何检查。如任务可以分配给任何用户但引擎不会验证该用户是否属于当前系统。④FormService：可选服务。用于启动表单和任务表单数据查询和提交。	Activiti BPMN2.0中可以定义这些表单数据。⑤RuntimeService：用于启动流程定义的新的流程实例。	流程实例：即流程定义的一次执行。通常一个流程定义会有多个实例同时进行（即有多个审批流运行）。	执行：执行是指向流程实例当前所在位置的指针（当前流程执行到流程图的哪一步了）。	该服务也用于检索流程变量和流程变量的存储服务，这些数据仅用于流程实例。提供流程查询实例和执行情况。	只要流程实例等待外部触发器并且流程需要继续，就会一直使用该服务。⑥ManagementService：检索有关数据库表和表元数据的信息。	一般的，自定义Activiti程序时不需要使用到该服务。⑦HistoryService：用于查询ActivitiEngine收集的所有历史数据	在流程执行时，引擎可以保留大量数据（可配置），例如：流程实例启动时间、谁执行了哪些任务、完成任务需要多长时间、每个流程实例中遵循的路径等。⑧DynamicBpmnService：动态Bpmn服务	可以用于更改流程定义中的一部分而无需从新部署。		例如，您可以更改流程定义中用户任务的受理人定义，或更改服务任务的类名。
```

### 异常策略

来源于一个基本的org.activiti.engine.ActivitiException非检查时异常

```java
//部分常见的异常类型|-- ActivitiException    |-- ActivitiWrongDbException//据库模式版本和引擎版本不匹配时抛出。    |-- ActivitiOptimisticLockingException//同一数据条目的并发访问导致数据存储中发生乐观锁定    |-- ActivitiClassLoadingException    |-- ActivitiObjectNotFoundException//请求或操作的对象不存在    |-- ActivitiIllegalArgumentException//非法参数    |-- ActivitiTaskAlreadyClaimedException//重复申明任务
```

### Activiti的使用（实践）

````
1.绘制流程图（bpmn20.xml或bpmn）2.部署流程3.启动实例4.完成节点任务	流程或实例的暂停或激活	查询：	1.Activiti提供了QueryAPI用于查询各种数据	2.对于自定义的查询可以使用SQL查询createNativeTaskQuery()进行查询docUrl:https://www.activiti.org/userguide/#queryAPI	流程变量	一个流程实例可以有任意数量的变量。每个变量都存储在 ACT_RU_VARIABLE 数据库表中的**一行**中。	流程变量可以在流程运行期间添加变量。	setVariable：为实例级变量。整个执行树可见。	setVariableLocal等方法一般给特定的执行设值变量。该变量仅在该执行中可见，在执行树中不可见。	变量通常用于 Java 委托、表达式、执行或任务侦听器、脚本等。	在调用流程参数方法时会从数据库中获取所有相关变量并缓存。在特定的场景中Activiti ver. >=5.17 提供了对应的这种行为查询。（fetchAllVariables）瞬态变量	docUrl:https://www.activiti.org/userguide/#apiTransientVariablesUEL和JUEL表达式	统一表达式语言（${var},${var == 0}...）	这种表达式可用于：Java Service tasks、执行监听器、任务监听听器和条件序列流。	表达式类型		1.值表达式：解析为一个值		2.方法表达式：调用一个方法。​```java${printer.print()}//无参${myBean.addNewOrder('orderName')}//固定值${myBean.doSomething(myVar, execution)}//有参```	友情提示：这些表达式支持：比较、beans、数组、list，maps(映射?)单元测试（docUrl:https://www.activiti.org/userguide/#apiUnitTesting）	继承：org.activiti.engine.test.ActivitiTestCase部署方式配置设置	见代码Spring、Spring-boot集成	见代码
````

## 部署

### 	流程部署的方式

```
1.流程定义文件部署(bpmn20.xml,.bpmn)2.zip包统一部署3.二进制流4.BpmnModel		……
```

### 	流程版本相关

```
Activiti没有版本控制相关概念。流程定义的版本是在部署期间创建的。Activiti 会在ProcessDefinition时存储到数据库之前为其分配一个版本。流程定义步骤（首先会初始化id、key、version、name）xml->db	相关表：act_re_procdef	1.XML文本文件中的id属性用于流程定义的Key	2.name属性用作名称属性。如果没有name则使用id作为name值。	3.第一次部署key时对应的version为1.对于所有具有相同key的后续部署version都>1。key用于区分流程定义。	4.id的生成规则为：{流程定义的key}:{流程定义版本号}:{自动生成唯一ID}自动生成唯一ID(generate-id)是添加的唯一编号，以保证集群环境中进程定义缓存的进程 id 的唯一性。示例docUrl:https://www.activiti.org/userguide/#versioningOfProcessDefinitions注意：Activiti执行流程实例进程时只认定Id
```

### 	流程图片的部署

```
部署的图片流程文件保存在数据库中(act_ge_bytearray).如果部署没有提供流程图像ProcessEngine会自动为标准的bpmn20生成.png的流程图片并存入数据库(act_ge_bytearray)中。如果需要手动部署则文件名需满足如下格式：{bpmn文件名}.{流程Key}.{图片后缀(.png、.jpg……)}否则不会被流程引擎使用。（一个流程文件可能有多个process，以此区分不同的流程图像）自动生成流程图片默认是开启的如需关闭配置：createDiagramOnDeploy即可。自定义类别（targetNamespace）：见代码。（EventListener.xml，ActivityServiceImpl）
```

## BPMN2.0

### 	配置文件详解

```xml
<?xml version="1.0" encoding="UTF-8"?><!--    BPMN的根元素        该标签下可以有个process标签(推荐一个流程一个bpmn20文件)        targetNamespace：可以是任何内容，对流程定义进行分类很有用。        添加如下命明空间使用在线模式--><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="Examples">    <!--process元素        有两个主要元素：            1.id该属性时必须的，对应于定义流程的Key。通过这个ID可以通过startProcessInstanceByKey启动一个新的流程实例，                注意该方法只会通过部署的最新版本流程启动流程实例。该方式区别于startProcessInstanceById见API（ActivityService.startProcessById）            2.name可选属性。用于流程定义名称。引擎本身不使用此属性，因此它可用于在用户界面中显示更人性化的名称。[数据表：act_re_procdef]    -->    <process id="myProcessBpmn20" name="myProcessBpmn20" >    </process></definitions>
```

### 	官方入门案例

```
目标：了解Activiti和基本的BPMN2.0概念内容：某公司财务报表汇总发送给所有股东的审批。流程：开始->编写每月的财务报表->上层层管理某一员审批->结束。流程图描述：大致是一个开始事件（左边的圆圈），然后是两个用户任务：“编写月度财务报告”和“验证月度财务报告”，以一个结束事件结束（右边有粗边框的圆圈）。URl:https://www.activiti.org/userguide/#_getting_started_10_minute_tutorial基本步骤	注意：代码示例中并未按照教程完整实现该功能，主要以API使用为主。	1.bpmn20.xml编写
```

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"             xmlns:activiti="http://activiti.org/bpmn"             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"             xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"             targetNamespace="http://www.activiti.org/processdef">    <process id="financialReport" name="Monthly financial report reminder process" isExecutable="true">        <startEvent id="theStart"/>        <sequenceFlow sourceRef="theStart" targetRef="writeReportTask" id="flow1"/>        <userTask id="writeReportTask" name="Write monthly financial report">            <documentation>                Write monthly financial report for publication to shareholders.            </documentation>            <!--定义当前节点操作组-->            <potentialOwner>                <resourceAssignmentExpression>                    <formalExpression>accountancy</formalExpression>                </resourceAssignmentExpression>            </potentialOwner>        </userTask>        <sequenceFlow sourceRef="writeReportTask" targetRef="verifyReportTask" id="flow2"/>        <userTask name="Verify monthly financial report" id="verifyReportTask">            <documentation>                Verify monthly financial report composed by the accountancy department.                This financial report is going to be sent to all the company shareholders.            </documentation>            <!--定义当前节点操作组-->            <potentialOwner>                <resourceAssignmentExpression>                    <formalExpression>management</formalExpression>                </resourceAssignmentExpression>            </potentialOwner>        </userTask>        <sequenceFlow sourceRef="verifyReportTask" targetRef="theEnd" id="flow3"/>        <endEvent id="theEnd"/>    </process>    <bpmndi:BPMNDiagram id="BPMNDiagram_financialReport">        <bpmndi:BPMNPlane bpmnElement="financialReport" id="BPMNPlane_financialReport">            <bpmndi:BPMNShape bpmnElement="theStart">                <omgdc:Bounds height="30.0" width="30.0" x="75.0" y="225.0"/>            </bpmndi:BPMNShape>            <bpmndi:BPMNShape bpmnElement="writeReportTask">                <omgdc:Bounds height="80.0" width="100.0" x="165.0" y="200.0"/>            </bpmndi:BPMNShape>            <bpmndi:BPMNShape bpmnElement="verifyReportTask">                <omgdc:Bounds height="80.0" width="100.0" x="330.0" y="200.0"/>            </bpmndi:BPMNShape>            <bpmndi:BPMNShape bpmnElement="theEnd">                <omgdc:Bounds height="28.0" width="28.0" x="480.0" y="226.0"/>            </bpmndi:BPMNShape>            <bpmndi:BPMNEdge bpmnElement="flow1">                <omgdi:waypoint x="105.0" y="240.0"/>                <omgdi:waypoint x="165.0" y="240.0"/>            </bpmndi:BPMNEdge>            <bpmndi:BPMNEdge bpmnElement="flow2">                <omgdi:waypoint x="265.0" y="240.0"/>                <omgdi:waypoint x="330.0" y="240.0"/>            </bpmndi:BPMNEdge>            <bpmndi:BPMNEdge bpmnElement="flow3">                <omgdi:waypoint x="430.0" y="240.0"/>                <omgdi:waypoint x="480.0" y="240.0"/>            </bpmndi:BPMNEdge>        </bpmndi:BPMNPlane>    </bpmndi:BPMNDiagram></definitions>
```

```
2.部署流程并启动实例3.通过TaskService检索任务，可根据用户检索，也可以根据组检索。（此处演示不适用。详见官方文档演示步骤。此处将以API调用的方式创建组和用户。并与之关联详见代码。）4.领取任务（适用于组）5.根据TaskID完成任务
```

## BPMN2.0详解

写在前面：介绍Activiti支持的BPMN20构造以及对BPMN标准的自定义扩展

### 		Events

```
事件：用于对生命周期过程发生的事情进行建模。图标总是一个⚪.在BPMN2.0中事件主要有如下两种类别。	1.捕获：通过未填充的内部图标（即白色），捕捉事件与投掷事件在视觉上有所区别。	2.抛出：通过填充黑色的内部图标在视觉上与捕获事件区分开来。
```

#### 1.定时器事件定义（TimerEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="定时器事件定义演示">    <process id="timer_event" name="timerEvent">        <startEvent id="theStart">            <!--                申明一个定时器事件任务                    businessCalendarName：指向流程引擎配置中的业务日历。当省略业务日历时，将使用默认业务日历。            -->            <timerEventDefinition activiti:businessCalendarName="custom">                <documentation>                    这里就是一个开始事件的定时器事件的定义                    定时器事件是由定义的定时器触发的事件。                    开始时间、中间事件、边界事件都可以用。该事件的行为取决于使用的日历时间。                    每个计时器事件都有一个默认的业务日历，但也可以在计时器事件定义上定义业务日历。                </documentation>                <!--                    定时器定义必须仅包含以下元素之一                -->                <timeDate>                    2011-03-11T12:13:14                    <documentation>指定固定日期，何时触发触发器。（格式标准：ISO 8601）</documentation>                </timeDate>                <timeDuration>                    P10D                    <documentation>指定计时器在触发之前应该运行多长时间。如隔10天后触发（格式标准：ISO 8601）</documentation>                </timeDuration>                <!--                    endTime：为该标签的可选属性。当达到该时间后程序将停止创建其他任务。                    endTime也可以写在表达式中。如果两个都有被指定则系统默认使用属性的结束时间。                -->                <timeCycle activiti:endDate="2015-02-25T16:42:11+00:00">                    R3/PT10H 或者写成 R3/PT10H/${EndDate}                    <documentation>                        指定重复执行间隔，多用于定期启动进程或为过期的用户任务发送多个提醒等。                        时间周期可以有两种格式：                            1.根据ISO 8601标准格式如上所示：R3/PT10H。                            2.也可以是CRON表达式每5分钟触发一次，从整小时开始：0 0/5 * * * ?                    </documentation>                </timeCycle>            </timerEventDefinition>        </startEvent>        <!--            通用边界事件定义：            以下内容也是一个定时器任务        -->        <boundaryEvent attachedToRef="firstLineSupport" id="escalationTimer" cancelActivity="true">            <timerEventDefinition>                <timeDuration>${duration}</timeDuration>            </timerEventDefinition>        </boundaryEvent>    </process></definitions><!--    注意：计时器仅在启用作业或异步执行器时触发（即需要在 activiti.cfg.xml 中将 jobExecutorActivate 或 asyncExecutorActivate 设置为 true，    因为默认情况下禁用作业和异步执行器）。    ps：SpringBoot集成Activiti6 asyncExecutorActivate默认是true？-->
```

#### 2.错误事件定义（ErrorEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="错误事件定义演示">    <!--        提示：BPMN20中的错误与Java中的异常完全不同，两者也没有任何关联。            BPMN错误事件是一种对业务异常建模的方法。而Java一样有其独有的异常处理方式。    -->    <process id="error_event" name="errorEvent">        <endEvent>            <errorEventDefinition errorRef="myError"/>        </endEvent>    </process></definitions>
```

#### 3.信号事件定义（SignalEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="信号事件定义演示"><!--documentUrl:https://www.activiti.org/userguide/#bpmnSignalEventDefinitionQuery-->    <!--        声明信号         activiti:scope:配置信号作用范围默认为“global”。（非BPMN2.0标准！）    -->    <signal id="alertSignal" name="alert" activiti:scope="processInstance">        <documentation>            信号事件是引用命名的信号事件。        </documentation>    </signal>    <process id="signal_event" name="signalEvent">        <intermediateThrowEvent id="throwSignalEvent" name="Alert">            <documentation>抛出事件信号定义</documentation>            <!--                信号事件定义。                signalRef：值为声明的信号元素。            -->            <signalEventDefinition signalRef="alertSignal" />        </intermediateThrowEvent>        <!--.....-->        <intermediateCatchEvent id="catchSignalEvent" name="On Alert">            <documentation>捕获事件信号定义</documentation>            <signalEventDefinition signalRef="alertSignal"/>        </intermediateCatchEvent>        <!--.....-->    </process></definitions><!--    本Xml涉及少量的Java代码：SignalEventImpl.java    信号事件作用域范围        默认情况下，信号广播范围为整个引擎。因此如果在某个流程实例中抛出一个信号事件，        其他具有不同流程定义的流程实例可以对这个事件触发做出反应。        然而在某些情况下我们只希望对同一流程实例中的信号事件做出反应。            eg.一个用例是流程实例中使用同步机制，如过两个或多个活动互斥。--><!--    一些本节中出现的标记文字描述        MessageStartEvent：消息开始事件，一个圆⚪中间一个信封✉图标-->
```

#### 4.消息事件定义（MeaasgeEventDefinition）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="消息事件定义演示">    <!--DocumentUrl:https://www.activiti.org/userguide/#bpmnMessageEventDefinition-->    <!--        消息事件是引用命名消息的事件。消息具有名称和有效载荷。        与信号不同，消息事件总是针对单个接收者。    -->    <!--消息声明-->    <message id="newInvoice" name="newInvoiceMessage"/>    <message id="payment" name="paymentMessage"/>    <process id="message_event" name="messageEvent">        <startEvent id="messageStart">            <documentation>这是一个消息开始事件</documentation>            <!--消息定义-->            <messageEventDefinition messageRef="newInvoice"/>        </startEvent>        <!--...-->        <intermediateCatchEvent id="paymentEvt">            <documentation>这是一个捕获消息事件</documentation>            <messageEventDefinition messageRef="payment"/>        </intermediateCatchEvent>        <!--...-->    </process></definitions><!--    关联的Java: com.shaoteemo.bpmn.MessageEventImpl-->
```

#### 5.开始事件（StartEvents）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="开始事件演示"><!--    开始时间指示流程开始的位置        启动事件的类型（进程在消息到达时启动，在特定的时间间隔等），        定义流程的启动方式在事件的可视化表示中显示为一个小图标（timer_event.xml\message_event.xml等）。    事件总是为捕获事件：概念上，事件是（在任何时候）等待某个触发器发生。-->    <process id="start_event" name="startEvent">        <!--            initiator:标识在进程启动时将存储经过身份验证的用户ID的变量名称。        -->        <startEvent id="request" activiti:initiator="initiator"/>    </process></definitions><!--关联的Java:com.shaoteemo.bpmn.StartEventImpl-->
```

#### 6.空开始事件（None Start Event）常用的一个事件

![空开始事件](http://rep.shaoteemo.com/activiti/bpmn.none.start.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="空开始事件演示">    <!--        空开始事件：未指定启动流程实例的触发器事件。            这意味着这样的流程需要手动启动。通过RuntimeService中的API启动。            NOTE：子流程总是有一个空开始事件    -->    <process id="none_start_event" name="noneStartEvent">        <!--空开始事件的XML表示是正常的开始事件声明，没有任何子元素（其他开始事件类型都有一个声明类型的子元素）。-->        <startEvent id="start" name="my start event"/>        <!--            对空开始事件的自定义扩展            formKey：对用户在启动新流程实例时必须填写的表单模板的引用。        -->        <startEvent id="startFormKey" activiti:formKey="org/activiti/examples/taskforms/request.form"/>    </process></definitions>
```

#### 7.定时器启动事件（Timer Start Event)

![定时器图片样式](http://rep.shaoteemo.com/activiti/bpmn.clock.start.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="定时器启动事件演示">    <!--        计时器启动事件用于在给定时间创建流程实例。既可以用于一次启动流程，也可以在某一事件间隔启动多次流程。        NOTE：            1.子流程不能有计时器启动事件。            2.流程部署会立即安排启动计时器事件。不需要调用RuntimeService中的启动API就会自动根据条件启动。        定时器启动事件的 XML 表示是普通的启动事件声明，带有定时器定义子元素.    -->    <process id="timer_start_event" name="timerStartEvent">        <!--示例1：流程将从2011年3月11日12:13开始，以5分钟为间隔启动4次-->        <startEvent id="theStartExample1">            <timerEventDefinition>                <timeCycle>R4/2011-03-11T12:13/PT5M</timeCycle>            </timerEventDefinition>        </startEvent>        <!--示例：进程将在选定日期开始一次-->        <startEvent id="theStartExample2">            <timerEventDefinition>                <timeDate>2088-09-01T00:00:00</timeDate>            </timerEventDefinition>        </startEvent>    </process></definitions>
```

#### 8.消息开始事件（Message Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.message.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="消息启动事件演示">    <!--        消息启动事件：可用于使用命名消息启动流程实例。有效地让我们使用消息名称从一组备选启动事件中选择符合条件的事件启动。        使用一个或多个消息启动事件部署流程定义时，应注意以下事项：            1.消息启动事件的名称在给定的流程定义中必须是唯一的。流程定义不能有多个同名的消息启动事件,否则              Activiti 在部署流程定义时抛出异常，如果两个及以上的消息启动事件引用具有相同消息名称的消息，              则两个或多个消息启动事件引用相同的消息。            2.消息启动事件的名称在所有部署的流程定义中必须是唯一的，否则，Activiti 在部署流程定义时抛出异常，              使得一个及以上的消息启动事件引用由不同流程定义部署的消息启动事件同名的消息。            3.流程版本控制：在部署流程定义的新版本时，将取消先前版本的消息订阅。对于新版本中不存在的消息事件也是如此。        启动对应的流程实例API方式见：Java: com.shaoteemo.bpmn.MessageEventImpl            *JavaAPI中的传入属性messageName：是在 messageEventDefinition 的 messageRef 属性引用的消息元素的 name 属性中给出的名称。            启动流程实例时，注意事项：                1.消息启动事件仅在主流程上受支持。嵌入式子流程不支持消息启动事件。                2.如果流程定义有多个消息启动事件，runtimeService.startProcessInstanceByMessage(…)允许选择适当的启动事件。                3.如果流程定义有多个消息启动事件和一个无启动事件，runtimeService.startProcessInstanceByKey(…)和                  runtimeService.startProcessInstanceById(…)使用无启动事件启动流程实例。                4.如果流程定义有多个消息启动事件并且没有无启动事件，runtimeService.startProcessInstanceByKey(…)和                  runtimeService.startProcessInstanceById(… )会抛出异常。                5.如果流程定义具有单个消息启动事件，runtimeService.startProcessInstanceByKey(…)和                  runtimeService.startProcessInstanceById(… )会使用消息启动事件启动一个新的流程实例。                6.如果流程是以活动调用方式启动的，则仅在以下情况下才支持消息启动事件                    6.1.除了消息开始事件之外，流程还有一个无开始事件                    6.2.该流程只有一个消息启动事件，没有其他启动事件。    -->    <message id="newInvoice" name="newInvoiceMessage" />    <process id="message_start_event" name="messageStartEvent">        <startEvent id="messageStart" >            <messageEventDefinition messageRef="newInvoice"/>        </startEvent>    </process></definitions>
```

#### 9.信号开始事件（Signal Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.signal.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="信号启动事件演示">    <!--        信号启动事件：可用于使用命名信号启动流程实例。        信号启动事件启动方式：            1.中间信号抛出事件            2.通过 API（runtimeService.signalEventReceivedXXX 方法）从流程实例中触发信号。                （Java Class:com.shaoteemo.bpmn.SignalEventImpl）            注意，在以上两种情况下，还可以在流程实例的同步和异步启动之间进行选择。    -->    <signal id="signal" name="theSignal"/>    <process id="signal_start_event" name="signalStartEvent">        <startEvent id="start">            <signalEventDefinition id="theSignalEventDefinition" signalRef="signal"  />        </startEvent>        <sequenceFlow sourceRef="start" targetRef="task"/>        <userTask id="task" name="Task in process A" />        <sequenceFlow id="flow2" sourceRef="task" targetRef="end" />        <endEvent id="end" />    </process></definitions>
```

#### 10.错误开始事件（Error Start Event）

![](http://rep.shaoteemo.com/activiti/bpmn.start.error.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="错误开始事件演示">    <!--        错误启动事件可用于触发事件子流程。        错误启动事件不能用于启动流程实例。        错误启动事件总是中断。    -->    <process id="error_start_event" name="errorStartEvent">        <startEvent id="messageStart" >            <errorEventDefinition errorRef="someError" />        </startEvent>    </process></definitions>
```

#### 11.结束事件（End Events）

结束事件表示（子）流程的（路径的）结束。结束事件总是抛出。这意味着当流程执行到达结束事件时，会抛出一个结果。结果的类型由事件的内部黑色图标描述。

#### 12.空结束事件（None End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.none.end.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="空结束事件演示">    <!--        空结束事件意味着到达事件时抛出的结果是未指定的。        因此，除了结束当前的执行路径外，引擎不会做任何额外的事情。    -->    <process id="none_end_event" name="noneEndEvent">        <endEvent id="end" name="my end event"/>    </process></definitions>
```

#### 13.错误结束事件（Error End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.error.end.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="错误结束事件演示">    <!--        当流程执行到达错误结束事件时，当前执行路径结束并抛出错误。        此错误可以被匹配的中间边界错误事件捕获。如果没有找到匹配的边界错误事件，则会抛出异常。    -->    <error id="myError" errorCode="123"/>    <process id="error_end_event" name="errorEndEvent">        <!--            错误结束事件表示为结束事件，带有一个 errorEventDefinition 子元素。            errorRef 属性可以引用在进程外定义的错误元素：        -->        <endEvent id="end">            <errorEventDefinition errorRef="myError"/>        </endEvent>    </process>    <!--        errorCode 用于查找匹配的捕获边界错误事件。        如果 errorRef 与任何定义的错误都不匹配，则 errorRef 会匹配 errorCode。    -->    <error id="myError2" errorCode="error123"/>    <process id="myProcess">        <endEvent id="myErrorEndEvent">            <errorEventDefinition errorRef="myError2"/>        </endEvent>        <!--这两个效果相同-->        <endEvent id="myErrorEndEvent2">            <errorEventDefinition errorRef="error123" />        </endEvent>    </process></definitions>
```

#### 14.终止结束事件（Terminate End Event）

![](http://rep.shaoteemo.com/activiti/bpmn.terminate.end.event.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="终止结束事件演示">    <!--        当达到终止结束事件时，当前流程实例或子流程将被终止。        从概念上讲，当执行到达终止结束事件时，将确定并结束第一个范围（流程或子流程）。        这个规则一般适用：            例如当有一个多实例调用活动或嵌入的子流程时，只有那个实例会被结束，其他实例和流程实例不受影响。        注意:            在 BPMN 2.0 中，子流程可以是嵌入式子流程、调用活动、事件子流程或事务子流程。    -->    <process id="terminate_end_event" name="terminateEndEvent">        <endEvent>            <!--                可选属性 terminateAll 默认值为false：                    当为true时，无论终止结束事件在流程定义中的位置如何，                    也无论是否在子流程（甚至嵌套）中，主流程实例都将被终止。            -->            <terminateEventDefinition activiti:terminateAll="true"/>        </endEvent>    </process></definitions>
```

#### 15.取消结束事件（Cancel End Event）

![](http://rep.shaoteemo.com/activiti/%E5%8F%96%E6%B6%88%E7%BB%93%E6%9D%9F%E4%BA%8B%E4%BB%B6.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="取消结束事件演示">    <!--        取消结束事件只能与 bpmn 事务子流程结合使用。        当到达取消结束事件时，将抛出取消事件，该事件必须由取消边界事件捕获。然后取消边界事件取消事务并触发补偿。    -->    <process id="cancel_event_end" name="cancelEventEnd">        <endEvent>            <cancelEventDefinition/>        </endEvent>    </process></definitions>
```

### Boundary Events

#### 1.边界事件（Boundary Events）

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="边界事件演示">    <!--        边界事件捕获附加到活动的事件（边界事件永远不会抛出）。这意味着当活动正在运行时，事件正在侦听某种类型的触发器。        当事件被捕获时，活动被中断并遵循从事件出去的序列流。        边界事件定义：            1.唯一标识符（流程范围内）            2.对通过attachedToRef 属性将事件附加到的活动的引用。注意，边界事件与它们所依附的活动在同一级别上定义（即，活动中不包含边界事件）。    -->    <process id="boundary_event" name="boundaryEvent">        <!--所有边界事件的定义方式-->        <boundaryEvent attachedToRef="theActivity" id="myBoundaryEvent">            <XXXEventDefinition/>        </boundaryEvent>    </process></definitions>
```

#### 2.定时器边界事件（Timer Boundary Event）

##### 描述

定时器边界事件充当秒表和闹钟。当执行到达附加边界事件的活动时，将启动计时器。当计时器触发时（例如，在指定的间隔之后），活动被中断，边界事件随之而来。

##### 图形符号

计时器边界事件可视化为典型的边界事件（即边界上的圆圈），计时器图标位于内部。

![](http://rep.shaoteemo.com/activiti/%E5%AE%9A%E6%97%B6%E5%99%A8%E8%BE%B9%E7%95%8C%E4%BA%8B%E4%BB%B6.png)

##### XML表示

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="定时器边界事件演示">    <process id="timer_boundary_event" name="timerBoundaryEvent">        <boundaryEvent attachedToRef="firstLineSupport" cancelActivity="true" id="escalationTimer">            <timerEventDefinition>                <!--如不知写法的请查看 定时器事件定义-->                <timeDuration>PT4H</timeDuration>            </timerEventDefinition>        </boundaryEvent>    </process></definitions>
```

在图形表示中，圆的线是点状的，如您在下面的示例中所见:

![](http://rep.shaoteemo.com/activiti/timer_boundary_event.png)
虚线则表示：cancelActivity属性为false。表示不会中断流程。

典型用例：额外发送升级电子邮件，但不会中断正常的流程。

从 BPMN 2.0 开始，中断和非中断定时器事件之间存在差异。中断是默认设置（cancelActivity=true）。不中断导致原始活动没有中断但活动停留在那里。而是创建一个额外的执行并通过事件的传出转换发送。在 XML 表示中，cancelActivity 属性设置为 false：

```xml
<boundaryEvent id="escalationTimer" cancelActivity="false" attachedToRef="firstLineSupport"/>
```

注意：边界计时器事件仅在启用作业或异步执行器时触发。

边界事件的一些问题：

在使用任何类型的边界事件时，存在一个关于并行的已知问题。目前，不可能将多个传出序列流附加到边界事件（参见问题 <a href="https://activiti.atlassian.net/browse/ACT-47">ACT-47</a>）。此问题的一种解决方案是使用一个去往并行网关的传出序列流。

![](http://rep.shaoteemo.com/activiti/bpmn.known.issue.boundary.event.png)

#### 3.错误边界事件（Error Boundary Event）

##### 描述

活动边界上的中间捕获错误，或简称边界错误事件，捕获在定义它的活动范围内抛出的错误。

定义边界错误事件对嵌入的子流程或调用活动最有意义，因为子流程为子流程内的所有活动创建了一个范围。错误结束事件引发错误。此类错误将向上传播其父作用域，直到找到定义了与错误事件定义匹配的边界错误事件的作用域。

当错误事件被捕获时，定义边界事件的活动被销毁，同时销毁其中的所有当前执行（例如并行活动、嵌套子流程等）。流程执行继续遵循边界事件的传出序列流。

##### 图形示例

边界错误事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有错误图标。错误图标为白色，表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.error.event.png)

##### XML示例

典型的边界事件：

```xml
<boundaryEvent attachedToRef="mySubProcess" id="catchError">      <errorEventDefinition errorRef="myError"/></boundaryEvent>
```

与错误结束事件一样，errorRef 引用在流程元素之外定义的错误：

```xml
<error id="myError" errorCode="123" />...<process id="myProcess">...
```

errorCode属性用于匹配捕获的错误：

- 如果省略errorRef，则边界错误事件将捕获任何错误事件，而不管错误的errorCode。
- 如果提供了 errorRef 并且它引用了现有错误，则边界事件将仅捕获具有相同错误代码的错误。
- 如果提供了 errorRef，但 BPMN 2.0 文件中没有定义错误，则 errorRef 用作 errorCode（类似于错误结束事件）。

##### 示例

以下示例过程显示了如何使用错误结束事件。当通过声明提供的信息不足而完成“*Review profitability*”用户任务时，将引发错误。当在子流程的边界上捕获此错误时，“*Review sales lead*”子流程中的所有活动都将被销毁（即使“*Review customer rating*”尚未完成），并创建“*Provide additional details*”用户任务.

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.error.example.png)

此过程作为演示设置中的示例提供。流程 XML 和单元测试可以在 org.activiti.examples.bpmn.event.error 包中找到。（译者注：见Activiti源码包或编译包）

#### 4.信号边界事件（Signal Boundary Event）

##### 描述

在活动边界上附加的中间捕获信号，或简称为边界信号事件，捕获与引用的信号定义具有相同信号名称的信号。

注意:

- 与边界错误事件等其他事件相反，边界信号事件不仅捕获从它所连接的范围抛出的信号事件，而且信号事件具有全局范围（广播语义），这意味着信号可以从任何地方抛出，甚至可以从不同的流程实例抛出。
- 与错误事件等其他事件相反，如果信号被捕获，则不会消耗信号。如果您有两个活动的信号边界事件捕获同一个信号事件，则两个边界事件都会被触发，即使它们属于不同流程实例的一部分。

##### 图形示例

边界信号事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆形），内部带有信号图标。信号图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.signal.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="信号边界事件演示">    <process id="signal_boundary_event" name="signalBoundaryEvent">        <boundaryEvent id="boundary" attachedToRef="task" cancelActivity="true">            <signalEventDefinition signalRef="alertSignal"/>        </boundaryEvent>    </process></definitions>
```

#### 5.消息边界事件（Message Boundary Event）

##### 描述

活动边界上附加的中间捕获消息，或简称边界消息事件，捕获与引用的消息定义具有相同消息名称的消息。

##### 图形示例

边界消息事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有消息图标。消息图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.message.event.png)注意，边界消息事件可以是中断（右侧）和非中断（左侧)。

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="消息边界事件">    <process id="message_boundary_event" name="messageBoundaryEvent">        <boundaryEvent id="boundary" attachedToRef="task" cancelActivity="true">            <messageEventDefinition messageRef="newCustomerMessage"/>        </boundaryEvent>    </process></definitions>
```

#### 6.取消边界事件（Cancel Boundary Event）

##### 描述

**事务子流程**边界上的附加中间捕获取消，或简称边界取消事件，在事务取消时触发。当取消边界事件被触发时，它首先中断当前作用域中所有活动的执行。接下来，它开始对事务范围内所有活动的补偿边界事件进行补偿。补偿是同步执行的，即边界事件在补偿完成之前等待，然后再离开事务。当补偿完成时，事务子流程使用运行在取消边界事件之外的序列流离开。

注意：

- 事务子流程只允许单个取消边界事件。
- 如果事务子流程承载嵌套的子流程，则仅对成功完成的子流程触发补偿。
- 如果将取消边界事件放在具有多实例特征的事务子流程上，如果一个实例触发取消，则边界事件取消所有实例。

##### 图形示例

取消边界事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有取消图标。取消图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.cancel.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="取消边界事件演示">    <process id="cancel_boundary_event" name="cancelBoundaryEvent">        <boundaryEvent id="boundary" attachedToRef="transaction" >            <cancelEventDefinition />        </boundaryEvent>    </process></definitions>
```

由于取消边界事件总是中断流程，因此不需要cancelActivity属性。

#### 7.补偿边界事件（Compensation Boundary Event）

##### 描述

在活动边界上附加的中间捕获补偿或简称补偿边界事件，可用于将补偿处理程序附加到活动。

补偿边界事件必须使用定向关联引用单个补偿处理程序。

补偿边界事件与其他边界事件具有不同的激活策略。其他边界事件（例如信号边界事件）在它们所连接的活动开始时被激活。当活动离开时，它们被停用并取消相应的事件订阅。补偿边界事件不同。当附加到的**活动成功完成时**，将激活补偿边界事件。至此，相应的补偿事件订阅就创建完成了。当触发补偿事件或相应的流程实例结束时，订阅将被删除。由此可知：

- 当补偿被触发时，与补偿边界事件关联的补偿处理程序被调用的次数与它所附加到的活动成功完成的次数相同。
- 如果补偿边界事件附加到具有多个实例特征的活动，则为每个实例创建补偿事件订阅。
- 如果补偿边界事件附加到包含在循环内的活动，则每次执行活动时都会创建补偿事件订阅。
- 如果流程实例结束，则取消对补偿事件的订阅。

注意：嵌入式子流程不支持补偿边界事件。使用association标签连接补偿。

##### 图形示例

补偿边界事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有补偿图标。补偿图标为白色（未填充），表示捕获语义。除了补偿边界事件外，下图显示了使用单向关联与边界事件关联的补偿处理程序：

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.compensation.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="补偿边界事件演示">    <process id="compensation_boundary_event" name="compensationBoundaryEvent">        <boundaryEvent id="compensateBookHotelEvt" attachedToRef="bookHotel" >            <compensateEventDefinition />        </boundaryEvent>        <association associationDirection="One" id="a1"  sourceRef="compensateBookHotelEvt" targetRef="undoBookHotel" />        <serviceTask id="undoBookHotel" isForCompensation="true" activiti:class="..." />    </process></definitions>
```

由于在活动成功完成后才激活补偿边界事件，因此不支持**cancelActivity**属性。

### Intermediate Events

#### 1.中间捕获事件（Intermediate Catching Events）

所有中间捕获事件都以相同的方式定义

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="中间捕获事件演示">    <process id="intermediate_catching_events" name="intermediateCatchingEvents">        <intermediateCatchEvent id="myIntermediateCatchEvent" >            <XXXEventDefinition/>        </intermediateCatchEvent>    </process></definitions>
```

中间捕获事件定义为

- 唯一标识符（进程范围内）
- XXXEventDefinition（例如 TimerEventDefinition 等）形式的 XML 子元素，用于定义中间捕获事件的类型。有关更多详细信息，请参阅特定的捕获事件类型。

#### 2.定时器中间捕获事件（Timer Intermediate Catching Event）

##### 描述

计时器中间事件充当秒表。当执行到达捕获事件活动时，将启动计时器。当计时器触发时（例如，在指定的时间间隔之后），将遵循计时器中间事件的序列流。

##### 图形示例

定时器中间事件可视化为中间捕获事件，内部带有定时器图标。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.timer.event.png)

##### XML示例

定时器中间事件被定义为中间捕获事件。在这种情况下，特定类型子元素是 timerEventDefinition 元素。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="定时器中间捕获事件演示">    <process id="timer_intermediate_catching_event" name="timerIntermediateCatchingEvent">        <intermediateCatchEvent id="timer">            <timerEventDefinition>                <timeDuration>PT5M</timeDuration>            </timerEventDefinition>        </intermediateCatchEvent>    </process></definitions>
```

更多定时器相关请查看定时器事件定义。

#### 3.信号中间捕获事件（Signal Intermediate Catching Event）

##### 描述

中间捕获信号事件捕获与引用的信号定义具有相同信号名称的信号。

注意：与错误事件等其他事件相反，如果信号被捕获，则不会消耗信号。如果您有两个活动的信号边界事件捕获同一个信号事件，则两个边界事件都会被触发，即使它们属于不同流程实例的一部分。

##### 图形示例

中间信号捕获事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有信号图标。信号图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.signal.catch.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="信号中间捕获事件演示">    <process id="signal_intermediate_catching_event" name="signalIntermediateCatchingEvent">        <intermediateCatchEvent id="signal">            <!--指向信号的ID-->            <signalEventDefinition signalRef="newCustomerSignal" />        </intermediateCatchEvent>    </process></definitions>
```

更多信号相关请查看信号事件定义。

#### 4.消息中间捕获事件（Message Intermediate Catching Event）

##### 描述

中间捕获消息事件捕获具有指定名称的消息。

##### 图形示例

中间捕获消息事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有消息图标。消息图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.message.catch.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="消息中间捕获事件演示">    <process id="message_intermediate_catching_event" name="messageIntermediateCatchingEvent">        <intermediateCatchEvent id="message">            <messageEventDefinition messageRef="newCustomerMessage" />        </intermediateCatchEvent>    </process></definitions>
```

更多消息相关请查看消息事件定义。

#### 5.中间抛出事件（Intermediate Throwing Event）

所有中间抛出事件都以相同的方式定义：

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="中间抛出事件演示">    <process id="intermediate_throwing_event" name="intermediateThrowingEvent">        <intermediateThrowEvent id="myIntermediateThrowEvent" >            <XXXEventDefinition/>        </intermediateThrowEvent>    </process></definitions>
```

中间抛出事件定义为

- 唯一标识符（进程范围内）
- XXXEventDefinition 形式的 XML 子元素（例如，signalEventDefinition 等），用于定义中间抛出事件的类型。有关更多详细信息，请参阅特定的抛出事件类型。

#### 6.空中间抛出事件（Intermediate Throwing None Event）

##### 图形示例

下面的流程图显示了一个中间无事件的简单示例，它通常用于指示在流程中达到的某种状态。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.none.event.png)

这可以是监控某些 KPI 的一个很好的钩子，基本上是通过添加一个执行侦听器。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="空中间抛出事件演示">    <process id="intermediate_throwing_none_event" name="intermediateThrowingNoneEvent">        <intermediateThrowEvent id="noneEvent">            <extensionElements>                <activiti:executionListener class="org.activiti.engine.test.bpmn.event.IntermediateNoneEventTest$MyExecutionListener" event="start" />            </extensionElements>        </intermediateThrowEvent>    </process></definitions>
```

在那里您可以添加一些自己的代码，以便向您的 BAM 工具或 DWH 发送一些事件。引擎本身在那个事件中不做任何事情，它只是通过。

#### 7.信号中间抛出事件（Signal Intermediate Throwing Event）

##### 描述

中间抛出信号事件为定义的信号抛出信号事件。

在 Activiti 中，信号被广播到所有活动处理程序（即所有捕获信号事件）。信号可以同步或异步发布。

- 在默认配置中，信号是同步传送的。这意味着抛出流程实例会等待，直到将信号传递给所有捕获流程实例。捕获流程实例也在与抛出流程实例相同的事务中得到通知，这意味着如果被通知的实例之一产生技术错误（抛出异常），则所有涉及的实例都会失败。
- 信号也可以异步传递。在这种情况下，确定在达到抛出信号事件时哪些处理程序处于活动状态。对于每个活动处理程序，由 JobExecutor 存储和传递异步通知消息 (Job)。

##### 图形示例

中间信号抛出事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有信号图标。信号图标为黑色（填充），表示抛出语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.signal.throw.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="信号中间抛出事件演示">    <process id="signal_intermediate_throwing_event" name="signalIntermediateThrowingEvent">        <intermediateThrowEvent id="signal">            <signalEventDefinition signalRef="newCustomerSignal" />        </intermediateThrowEvent>        <!--异步写法-->        <intermediateThrowEvent id="signal2">            <signalEventDefinition signalRef="newCustomerSignal" activiti:async="true" />        </intermediateThrowEvent>    </process></definitions>
```

更多信号相关请查看信号事件定义。

#### 8.补偿中间抛出事件（Compensation Intermediate Throwing Event）

[不稳定的]

##### 描述

中间抛出补偿事件可用于触发补偿。

触发补偿：可以为指定的活动或托管补偿事件的范围触发补偿。补偿是通过执行与活动关联的补偿处理程序来执行的。

- 为活动抛出补偿时，关联的补偿处理程序执行的次数与活动成功竞争的次数相同。
- 如果为当前范围抛出补偿，则补偿当前范围内的所有活动，包括并行分支上的活动。
- 补偿按层次触发：如果要补偿的活动是子流程，则为包含在子流程中的所有活动触发补偿。如果子流程具有嵌套的活动，则会递归地抛出补偿。但是，补偿不会传播到流程的“上层”：如果在子流程内触发补偿，则不会传播到子流程范围之外的活动。BPMN 规范指出，在“子流程的同一级别”的活动会触发补偿。
- 在 Activiti 中，补偿以执行的相反顺序执行。这意味着最后完成的活动首先得到补偿，依此类推。
- 中间抛出补偿事件可用于补偿竞争成功的事务子进程。

注意：如果在包含子流程的范围内抛出补偿，并且子流程包含带有补偿处理程序的活动，则只有在抛出补偿时子流程成功完成，补偿才会传播到子流程。如果嵌套在子流程中的某些活动已完成并附加了补偿处理程序，并且包含这些活动的子流程尚未完成，则不会执行补偿处理程序。考虑以下示例：

![](http://rep.shaoteemo.com/activiti/bpmn.throw.compensation.example1.png)

在这个过程中，我们有两个并行执行，一个执行嵌入式子流程，另一个执行“charge credit card”活动。假设两个执行都已启动，第一个并行执行正在等待用户完成“review bookings”任务。第二个执行“charge credit card”活动并抛出错误，导致“cancel reservations”事件触发补偿。此时并行子流程尚未完成，这意味着补偿事件不会传播到子流程，因此不会执行“cancel hotel reservation”补偿处理程序。如果用户任务（以及嵌入式子流程）在执行“cancel reservations”之前完成，补偿将传播到嵌入式子流程。

**流程变量：**在补偿嵌入的子流程时，用于执行补偿处理程序的执行可以访问子流程的**本地流程变量**，该变量处于子流程完成执行时所处的状态。为了实现这一点，获取与范围执行（为执行子流程而创建的执行）关联的流程变量的快照。从这个角度来看，有几个含义：

- 补偿处理程序无权访问添加到在子流程范围内创建的并行执行的变量。
- 与层次结构中更高级别的执行关联的流程变量，（例如，与流程实例执行关联的流程变量不包含在快照中：当补偿被抛出时，补偿处理程序可以在它们所处的状态下访问这些过程变量。
- 变量快照仅用于嵌入式子流程，而不用于其他活动。

**当前限制：**

- **waitForCompletion="false"**目前不支持。当使用中间抛出补偿事件触发补偿时，只有在补偿成功完成后才离开该事件。
- 补偿本身目前由并行执行来执行。并行执行以补偿活动完成的相反顺序开始。活动的未来版本可能包括按顺序执行补偿的选项。
- 变量快照仅用于嵌入式子流程，而不用于其他活动。

##### 图形示例

中间补偿抛出事件可视化为典型的中间事件（内部带有较小圆圈的圆圈），内部带有补偿图标。补偿图标为黑色（填充），表示抛出语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.compensation.throw.event.png)

##### XML示例

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="补偿中间抛出事件演示">    <process id="compensation_intermediate_throwing_event" name="compensationIntermediateThrowingEvent">        <!--基本定义-->        <intermediateThrowEvent id="throwCompensation">            <compensateEventDefinition />        </intermediateThrowEvent>        <!--            activityRef：触发特定范围/活动的补偿        -->        <intermediateThrowEvent id="throwCompensation2">            <compensateEventDefinition activityRef="bookHotel" />        </intermediateThrowEvent>    </process></definitions>
```

### Sequence Flow

#### 1.序列流（Sequence Flow）

##### 描述

序列流是流程的两个元素之间的连接器。在流程执行期间访问元素后，将遵循所有传出序列流。这意味着 BPMN 2.0 的默认性质是并行的：两个传出序列流将创建两个独立的并行执行路径。

##### 图形示例

序列流被可视化为从源元素到目标元素的箭头。箭头始终指向目标。

![](http://rep.shaoteemo.com/activiti/bpmn.sequence.flow.png)

##### XML示例

序列流需要有一个流程元素唯一的 id，以及对现有源和目标元素的引用。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="序列流演示">    <process id="sequence_flow" name="sequenceFlow">        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="theTask" />    </process></definitions>
```

#### 2.条件序列流（Conditional Sequence Flow）

##### 描述

序列流可以定义一个条件。当离开 BPMN 2.0 活动时，默认行为是评估传出序列流的条件。当条件评估为true时，将选择该传出序列流。当以这种方式选择多个序列流时，将生成多个执行并以并行方式继续该过程。

注意：以上内容适用于 BPMN 2.0 活动（和事件），但不适用于网关。网关将根据网关类型以特定方式处理带有条件的序列流。

##### 图形示例

条件序列流可视化为常规序列流，开头带有一个小菱形。条件表达式显示在序列流旁边。

![](http://rep.shaoteemo.com/activiti/bpmn.conditional.sequence.flow.png)

##### XML示例

条件序列流在 XML 中表示为常规序列流，其中包含一个 conditionExpression 子元素。请注意，目前仅支持 tFormalExpressions，省略 xsi:type="" 定义将简单地默认为仅支持的表达式类型。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="表达式序列流演示">    <process id="conditional_sequence_flow" name="conditionalSequenceFlow">        <sequenceFlow id="flow" sourceRef="theStart" targetRef="theTask">            <conditionExpression xsi:type="tFormalExpression">                <![CDATA[${order.price > 100 && order.price < 250}]]>            </conditionExpression>        </sequenceFlow>    </process></definitions>
```

当前条件表达式只能与 UEL 一起使用，有关这些的详细信息可以在表达式部分找到。使用的表达式应解析为布尔值，否则在评估条件时会抛出异常。

- 下面的示例通过 getter 以典型的 JavaBean 样式引用流程变量的数据。

  ```xml
  <conditionExpression xsi:type="tFormalExpression">
    <![CDATA[${order.price > 100 && order.price < 250}]]>
  </conditionExpression>
  ```

- 此示例调用解析为布尔值的方法。

  ```xml
  <conditionExpression xsi:type="tFormalExpression">
    <![CDATA[${order.isStandardOrder()}]]>
  </conditionExpression>
  ```

Activiti 发行版包含以下使用值和方法表达式的示例流程（请参阅 org.activiti.examples.bpmn.expression）：

![](http://rep.shaoteemo.com/activiti/bpmn.uel-expression.on.seq.flow.png)

#### 3.默认序列流（Default Sequence Flow）

##### 描述

所有 BPMN 2.0 任务和网关都可以有一个默认的序列流。当且仅当无法选择任何其他序列流时，才选择此序列流作为该活动的传出序列流。默认序列流上的条件总是被忽略。

##### 图形示例

默认序列流可视化为常规序列流，开头带有斜线标记。

![](http://rep.shaoteemo.com/activiti/bpmn.default.sequence.flow.png)

##### XML示例

某个活动的默认序列流由该活动的默认属性定义。例如，以下 XML 片段显示了一个具有默认序列流*flow 2*. 的独占网关。只有当conditionA 和conditionB 都评估为false 时，才会被选为网关的传出序列流。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="默认序列流演示">    <process id="default_sequence_flow" name="defaultSequenceFlow">        <!--default：默认序列流ID-->        <exclusiveGateway id="exclusiveGw" name="Exclusive Gateway" default="flow2" />        <sequenceFlow id="flow1" sourceRef="exclusiveGw" targetRef="task1">            <conditionExpression xsi:type="tFormalExpression">${}</conditionExpression>        </sequenceFlow>        <sequenceFlow id="flow2" sourceRef="exclusiveGw" targetRef="task2"/>        <sequenceFlow id="flow3" sourceRef="exclusiveGw" targetRef="task3">            <conditionExpression xsi:type="tFormalExpression">${conditionB}</conditionExpression>        </sequenceFlow>    </process></definitions>
```

### Gateways

网关用于控制执行流程（或如 BPMN 2.0 所描述的，执行令牌）。网关能够使用或生成令牌。

网关以图形方式显示为菱形，里面有一个图标。图标显示网关的类型。

![](http://rep.shaoteemo.com/activiti/bpmn.gateway.png)

#### 1.排他网关（Exclusive Gateway）

##### 描述

排他网关（也称为 XOR （异或）网关或更专业的基于数据的独占网关）用于对流程中的决策进行建模。当执行到达此网关时，所有传出序列流都按照定义的顺序进行评估。选择条件评估为真（或没有条件集，概念上在序列流上定义了“真”）的序列流以继续该过程。

**请注意，输出序列流的语义与 BPMN 2.0 中的一般情况不同。而一般情况下，所有条件评估为真的序列流都被选择以并行方式继续，而在使用排他网关时只选择一个序列流。如果多个序列流的条件评估为真，则选择 XML 中定义的第一个（并且只有那个！）来继续该过程。如果无法选择序列流，则会抛出异常。**

##### 图形示例

排他网关可视化为典型的网关（即菱形），内部带有 X 图标，表示 XOR 语义。请注意，内部没有图标的网关默认为排他网关。BPMN 2.0 规范不允许在同一流程定义中混合带有和不带有 X 的菱形。

![](http://rep.shaoteemo.com/activiti/bpmn.exclusive.gateway.notation.png)

##### XML示例

独占网关的 XML 表示是直截了当的：一行定义了在传出序列流上定义的网关和条件表达式。请参阅有关条件序列流（conditional sequence flow ）的部分以了解哪些选项可用于此类表达式。 以下面的模型为例：

![](http://rep.shaoteemo.com/activiti/bpmn.exclusive.gateway.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="专属网关演示">    <process id="exclusive_gateway" name="exclusiveGateway">        <exclusiveGateway id="exclusiveGw" name="Exclusive Gateway" />        <sequenceFlow id="flow2" sourceRef="exclusiveGw" targetRef="theTask1">            <conditionExpression xsi:type="tFormalExpression">${input == 1}</conditionExpression>        </sequenceFlow>        <sequenceFlow id="flow3" sourceRef="exclusiveGw" targetRef="theTask2">            <conditionExpression xsi:type="tFormalExpression">${input == 2}</conditionExpression>        </sequenceFlow>        <sequenceFlow id="flow4" sourceRef="exclusiveGw" targetRef="theTask3">            <conditionExpression xsi:type="tFormalExpression">${input == 3}</conditionExpression>        </sequenceFlow>    </process></definitions>
```

#### 2.并行网关（Parallel Gateway）

##### 描述

网关还可用于对进程中的并发建模。在流程模型中引入并发的最直接的网关是并行网关（**Parallel Gateway**），它允许分叉到多个执行路径或加入多个传入执行路径。

并行网关的功能基于传入和传出序列流：

- **fork**：所有传出的序列流并行执行，为每个序列流创建一个并发执行。
- **join**：到达并行网关的所有并发执行在网关中等待，直到每个传入序列流的执行到达。然后该过程继续通过加入网关。

请注意，如果同一并行网关有多个传入和传出序列流，则并行网关可以同时具有 fork 和 join 行为。在这种情况下，网关将首先加入所有传入的序列流，然后再拆分为多个并发执行路径。

**与其他网关类型的一个重要区别是并行网关不评估条件。如果在与并行网关连接的序列流上定义了条件，则它们会被忽略。**

##### 图形示例

并行网关可视化为内部带有加号的网关（菱形），指的是 AND 语义。

![](http://rep.shaoteemo.com/activiti/bpmn.parallel.gateway.png)

##### XML示例

定义一个并行网关需要一行 XML：

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="并行网关演示">    <process id="parallel_gateway" name="parallelGateway">        <parallelGateway id="myParallelGateway" />    </process></definitions>
```

实际行为（fork、join 或两者）由连接到并行网关的序列流定义。

例如，上面的模型归结为以下 XML：

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="并行网关演示">    <process id="parallel_gateway" name="parallelGateway">        <!--文档中的案例-->        <startEvent id="theStart" />        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />        <parallelGateway id="fork" />        <sequenceFlow sourceRef="fork" targetRef="receivePayment" />        <sequenceFlow sourceRef="fork" targetRef="shipOrder" />        <userTask id="receivePayment" name="Receive Payment" />        <sequenceFlow sourceRef="receivePayment" targetRef="join" />        <userTask id="shipOrder" name="Ship Order" />        <sequenceFlow sourceRef="shipOrder" targetRef="join" />        <parallelGateway id="join" />        <sequenceFlow sourceRef="join" targetRef="archiveOrder" />        <userTask id="archiveOrder" name="Archive Order" />        <sequenceFlow sourceRef="archiveOrder" targetRef="theEnd" />        <endEvent id="theEnd" />    </process></definitions>
```

上面的例子中，进程启动后，会创建两个任务：

```java
ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");TaskQuery query = taskService.createTaskQuery()                         .processInstanceId(pi.getId())                         .orderByTaskName()                         .asc();List<Task> tasks = query.list();assertEquals(2, tasks.size());Task task1 = tasks.get(0);assertEquals("Receive Payment", task1.getName());Task task2 = tasks.get(1);assertEquals("Ship Order", task2.getName());
```

当这两个任务完成时，第二个并行网关将加入两个执行，由于只有一个输出序列流，因此不会创建并发执行路径，只有存档订单任务将处于活动状态。

请注意，并行网关不需要平衡（即对应并行网关的传入/传出序列流的匹配数量）。并行网关将简单地等待所有传入的序列流，并为每个传出的序列流创建一个并发执行路径，不受流程模型中其他构造的影响。因此，以下过程在 BPMN 2.0 中是合法的：

![](http://rep.shaoteemo.com/activiti/bpmn.unbalanced.parallel.gateway.png)

#### 3.包含网关（Inclusive Gateway）

##### 描述

包含网关可以看作是排他网关和并行网关的组合。就像排他网关一样，您可以在传出序列流上定义条件，并且包含网关将评估它们。但主要区别在于包含网关可以采用多个序列流，就像并行网关一样。

包含网关的功能基于传入和传出序列流：

- **fork**：评估所有传出序列流条件，并且对于评估为true的序列流条件，流将并行执行，为每个序列流创建一个并行执行。
- **join**：到达包含网关的所有并行执行在网关中等待，直到每个具有进程令牌的传入序列流的执行到达。这是与并行网关的一个重要区别。因此换句话说，包含网关将只等待符合条件的传入序列流。加入后，该过程继续经过加入包含网关。

请注意，如果同一个包含网关有多个传入和传出序列流，则包含网关可以同时具有 fork 和 join 行为。在这种情况下，网关将首先加入所有具有进程令牌的传入序列流，然后将其拆分为多个并发执行路径，以用于具有评估为 true 的条件的传出序列流。

##### 图形示例

包容性网关可视化为内部带有圆形符号的网关（菱形）。

![](http://rep.shaoteemo.com/activiti/bpmn.inclusive.gateway.png)

##### XML示例

定义一个包容性网关需要一行 XML：

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="包含网关演示">    <process id="inclusive_gateway" name="inclusiveGateway">        <!--定义包含网关-->        <inclusiveGateway id="myInclusiveGateway" />    </process></definitions>
```

实际行为（fork、join 或两者）由连接到包含网关的序列流定义。

例如，上面的模型归结为以下 XML：

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="包含网关演示">    <process id="inclusive_gateway" name="inclusiveGateway">        <startEvent id="theStart" />        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />        <inclusiveGateway id="fork" />        <sequenceFlow sourceRef="fork" targetRef="receivePayment" >            <conditionExpression xsi:type="tFormalExpression">${paymentReceived == false}</conditionExpression>        </sequenceFlow>        <sequenceFlow sourceRef="fork" targetRef="shipOrder" >            <conditionExpression xsi:type="tFormalExpression">${shipOrder == true}</conditionExpression>        </sequenceFlow>        <userTask id="receivePayment" name="Receive Payment" />        <sequenceFlow sourceRef="receivePayment" targetRef="join" />        <userTask id="shipOrder" name="Ship Order" />        <sequenceFlow sourceRef="shipOrder" targetRef="join" />        <inclusiveGateway id="join" />        <sequenceFlow sourceRef="join" targetRef="archiveOrder" />        <userTask id="archiveOrder" name="Archive Order" />        <sequenceFlow sourceRef="archiveOrder" targetRef="theEnd" />        <endEvent id="theEnd" />    </process></definitions>
```

在上面的例子中，流程启动后，如果流程变量paymentReceived == false和shipOrder == true，则会创建两个任务。如果这些流程变量中只有一个等于 true，则只会创建一项任务。如果没有条件评估为true并抛出异常。这可以**通过指定默认的传出序列流来防止**。在以下示例中，将创建一项任务，即船舶订单任务：

```java
HashMap<String, Object> variableMap = new HashMap<String, Object>();          variableMap.put("receivedPayment", true);          variableMap.put("shipOrder", true);          ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");TaskQuery query = taskService.createTaskQuery()                         .processInstanceId(pi.getId())                         .orderByTaskName()                         .asc();List<Task> tasks = query.list();assertEquals(1, tasks.size());Task task = tasks.get(0);assertEquals("Ship Order", task.getName());
```

当此任务完成时，第二个包容网关将加入两次执行，并且由于只有一个传出序列流，因此不会创建并发执行路径，并且只有存档订单任务将处于活动状态。

请注意，包含网关不需要平衡（即对应包含网关的传入/传出序列流的匹配数量）。包含网关将简单地等待所有传入的序列流，并为每个传出的序列流创建一个并发执行路径，不受流程模型中其他构造的影响。

#### 4.事件网关（Event-based Gateway）

##### 描述

基于事件的网关允许根据事件做出决定。网关的每个传出序列流都需要连接到一个中间捕获事件。当流程执行到达基于事件的网关时，网关的行为类似于等待状态：执行被暂停。此外，对于每个传出序列流，都会创建一个事件订阅。

请注意，从基于事件的网关流出的序列流不同于普通的序列流。这些序列流从未真正“执行”过。相反，它们允许流程引擎确定到达基于事件的网关的执行需要订阅哪些事件。以下限制适用：

- 基于事件的网关必须有两个或多个传出序列流。
- 基于事件的网关只能连接到仅**intermediateCatchEvent**的元素。 （Activiti 不支持在基于事件的网关之后接收任务。）
- 连接到基于事件的网关的 **intermediateCatchEvent** 必须具有单个传入序列流。

##### 图形示例

一个基于事件的网关被可视化为一个菱形，就像其他 BPMN 网关一样，里面有一个特殊的图标。

![](http://rep.shaoteemo.com/activiti/bpmn.event.based.gateway.notation.png)

##### XML示例

用于定义基于事件的网关的 XML 元素是 eventBasedGateway。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="事件网关演示">    <process id="event-based_gateway" name="event-basedGateway">        <!--事件网关-->        <eventBasedGateway id="eventBasedGateway"/>    </process></definitions>
```

##### 例子

以下流程是基于事件的网关流程的示例。当执行到达基于事件的网关时，流程执行被暂停。此外，流程实例订阅警报信号事件并创建一个定时器，该定时器在 10 分钟后触发。这有效地导致流程引擎等待十分钟以等待信号事件。如果信号在 10 分钟内发生，则取消定时器并在信号后继续执行。如果未触发信号，则在计时器之后继续执行并取消信号订阅。

![](http://rep.shaoteemo.com/bpmn.event.based.gateway.example.png)

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="事件网关演示">    <!--文档案例-->    <signal id="alertSignal" name="alert" />    <process id="catchSignal">        <startEvent id="start" />        <sequenceFlow sourceRef="start" targetRef="gw1" />        <eventBasedGateway id="gw1" />        <sequenceFlow sourceRef="gw1" targetRef="signalEvent" />        <sequenceFlow sourceRef="gw1" targetRef="timerEvent" />        <intermediateCatchEvent id="signalEvent" name="Alert">            <signalEventDefinition signalRef="alertSignal" />        </intermediateCatchEvent>        <intermediateCatchEvent id="timerEvent" name="Alert">            <timerEventDefinition>                <timeDuration>PT10M</timeDuration>            </timerEventDefinition>        </intermediateCatchEvent>        <sequenceFlow sourceRef="timerEvent" targetRef="exGw1" />        <sequenceFlow sourceRef="signalEvent" targetRef="task" />        <userTask id="task" name="Handle alert"/>        <exclusiveGateway id="exGw1" />        <sequenceFlow sourceRef="task" targetRef="exGw1" />        <sequenceFlow sourceRef="exGw1" targetRef="end" />        <endEvent id="end" />    </process></definitions>
```

### Task

#### 1.用户任务（User Task）

##### 描述

用户任务用于对需要由人类参与者完成的工作进行建模。当流程执行到达这样的用户任务时，会在分配给该任务的用户或组的任务列表中创建一个新任务。

##### 图形示例

用户任务可视化为典型任务（圆角矩形），左上角有一个小用户图标。

![](http://rep.shaoteemo.com/bpmn.user.task.png)

##### XML示例

用户任务在 XML 中定义如下。 id 属性是必需的，name 属性是可选的。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="用户任务演示">    <process id="user_task" name="userTask">        <userTask id="theTask" name="Important task"/>    </process></definitions>
```

用户任务也可以有描述。事实上，任何 BPMN 2.0 元素都可以有描述。描述是通过添加文档元素来定义的。

```xml
<userTask id="theTask" name="Schedule meeting" >            <documentation>                Schedule an engineering meeting for next week with the new hire.            </documentation></userTask>
```

描述文本可以通过标准 Java 方式从任务中检索：

```java
task.getDescription();
```

##### 到期日

每个任务都有一个字段，指示该任务的截止日期。 Query API 可用于查询在特定日期之前、之前或之后(之间)到期的任务。

有一个活动扩展，它允许您在任务定义中指定一个表达式，以在创建任务时设置它的初始截止日期。该表达式应始终解析为 java.util.Date、java.util.String（ISO8601 格式）、ISO8601 持续时间（例如 PT50M）或 null。例如，您可以使用在流程中的先前（节点）表单中输入或在先前服务任务中计算的日期。如果使用 time-duration，到期日期将根据当前时间计算，增加给定的时间段。例如，当“PT30M”用作dueDate 时，任务将在30 分钟后到期。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="用户任务演示">    <process id="user_task" name="userTask">        <!--具有过期时间的UserTask-->        <userTask id="theTask3" name="Important task" activiti:dueDate="${dateVariable}"/>    </process></definitions>
```

也可以使用 TaskService 或使用传递的 DelegateTask 在 TaskListeners 中更改任务的截止日期。

##### 用户分配

用户任务可以直接分配给用户。这是通过定义一个 humanPerformer 子元素来完成的。这样的**humanPerformer**定义需要一个实际定义用户的**resourceAssignmentExpression**。目前，仅支持**formalExpressions** 。

```xml
<!--指定分配用户--><userTask id='theTask4' name='important task' >    <humanPerformer>        <resourceAssignmentExpression>            <formalExpression>kermit</formalExpression>        </resourceAssignmentExpression>    </humanPerformer></userTask>
```

只能将一名用户分配为任务的人工执行者。在 Activiti 术语中，此用户称为受让人。具有受托人的任务在其他人的任务列表中不可见，而可以在受托人的所谓个人任务列表中找到。

可以通过 TaskService 检索直接分配给用户的任务，如下所示：

```java
List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
```

任务也可以放在所谓的人员候选任务列表中。在这种情况下，必须使用potentialOwner 构造。用法类似于 humanPerformer 构造。请注意，形式表达式中的每个元素都需要定义，以指定它是用户还是组（引擎无法猜测）。

```xml
<!--指定候选人列表或组--><userTask id='theTask5' name='important task' >    <potentialOwner>        <resourceAssignmentExpression>            <formalExpression>user(kermit), group(management)</formalExpression>        </resourceAssignmentExpression>    </potentialOwner></userTask>
```

使用潜在所有者构造定义的任务可以按如下方式检索（或类似于具有受托人的任务的 TaskQuery 用法）：

```java
List<Task> tasks = taskService.createTaskQuery().taskCandidateUser("kermit");
```

这将检索 kermit 是候选用户的所有任务，即正式表达式包含 *user(kermit)*。这还将检索分配给 kermit 所属组的所有任务（例如，*group(management)*，如果 kermit 是该组的成员并且使用了 Activiti 身份组件）。用户组在运行时解析，这些可以通过 IdentityService 进行管理。

如果没有给出给定文本字符串是用户还是组的具体信息，则引擎默认为组。因此，以下内容与声明 *group(accountancy)* 时相同。

```xml
<formalExpression>accountancy</formalExpression>
```

##### 用于任务分配的 Activiti 扩展

很明显，对于分配不复杂的用例，用户和组分配非常麻烦。为了避免这些复杂性，可以对用户任务进行自定义扩展。

- **assignee attribute**：此自定义扩展允许直接将用户任务分配给给定用户。

  ```xml
  <userTask id="theTask6" name="my task" activiti:assignee="kermit"/>
  ```

  这与使用上述定义的 humanPerformer 构造完全相同。

- **candidateUsers attribute**：此自定义扩展允许使用户成为任务的候选人。

  ```xml
  <userTask id="theTask7" name="my task" activiti:candidateUsers="kermit, gonzo"/>
  ```

  这与使用上述定义的potentialOwner 构造完全相同。请注意，不需要像潜在所有者构造那样使用 user(kermit) 声明，因为该属性只能用于用户。

- **candidateGroups attribute**: 此自定义扩展允许使组成为任务的候选者。

  ```xml
  <!--任务候选组(只能是组)--><userTask id="theTask8" name="my task" activiti:candidateGroups="management, accountancy"/>
  ```

  这与使用上述定义的potentialOwner 构造完全相同。请注意，不需要像潜在所有者构造那样使用组（管理）声明，因为该属性只能用于组。

- *CandidateUsers* 和*CandidateGroups* 都可以定义在同一个用户任务上。

注意：虽然 Activiti 提供了一个身份管理组件，它通过 IdentityService 管理，但是没有检查提供的用户是否被身份组件知道。这允许 Activiti 在嵌入到应用程序中时与现有的身份管理解决方案集成。

自定义身份链接类型（实验性）：

BPMN 标准支持单个分配的用户或 humanPerformer 或potentialOwners，这些用户形成potentialOwners中定义的潜在所有者池。此外，Activiti 为 User Task 定义了扩展属性元素，可以代表任务**assignee** 或**candidate owner**。

支持的 Activiti 身份链接类型有：

```java
package org.activiti.engine.task;import org.activiti.engine.TaskService;public class IdentityLinkType {  /* Activiti native roles */  public static final String ASSIGNEE = "assignee";  public static final String CANDIDATE = "candidate";  public static final String OWNER = "owner";  public static final String STARTER = "starter";  public static final String PARTICIPANT = "participant";}
```

BPMN 标准和 Activiti 示例授权标识是**user** 和**group**。如上一节所述，Activiti 身份管理实现不用于生产用途，但应根据支持的授权方案进行扩展。

如果需要其他链接类型，可以使用以下语法将自定义资源定义为扩展元素：

```xml
<!--(实验性的)自定义扩展链接类型--><userTask id="theTask9" name="make profit">    <extensionElements>        <activiti:customResource activiti:name="businessAdministrator">            <resourceAssignmentExpression>                <formalExpression>user(kermit), group(management)</formalExpression>            </resourceAssignmentExpression>        </activiti:customResource>    </extensionElements></userTask>
```

自定义链接表达式添加到 TaskDefinition 类：

```java
protected Map<String, Set<Expression>> customUserIdentityLinkExpressions =      new HashMap<String, Set<Expression>>();protected Map<String, Set<Expression>> customGroupIdentityLinkExpressions =      new HashMap<String, Set<Expression>>();public Map<String,         Set<Expression>> getCustomUserIdentityLinkExpressions() {  return customUserIdentityLinkExpressions;}public void addCustomUserIdentityLinkExpression(String identityLinkType,      Set<Expression> idList)  customUserIdentityLinkExpressions.put(identityLinkType, idList);}public Map<String,       Set<Expression>> getCustomGroupIdentityLinkExpressions() {  return customGroupIdentityLinkExpressions;}public void addCustomGroupIdentityLinkExpression(String identityLinkType,       Set<Expression> idList) {  customGroupIdentityLinkExpressions.put(identityLinkType, idList);}
```

它们在运行时由 UserTaskActivityBehavior handleAssignments 方法填充。

最后，必须扩展 IdentityLinkType 类以支持自定义身份链接类型：

```Java
public class IdentityLinkType    /*继承官方链接类型类以扩展类型*/    extends org.activiti.engine.task.IdentityLinkType{    public static final String ADMINISTRATOR = "administrator";    public static final String EXCLUDED_OWNER = "excludedOwner";}
```

通过任务监听器（TaskListener）自定义分配：

如果前面的方法还不够，可以使用 create event上的[task listener](https://www.activiti.org/userguide/#taskListeners)委托自定义分配逻辑：

```xml
<userTask id="task1" name="My task" >  <extensionElements>    <activiti:taskListener event="create" class="org.activiti.MyAssignmentHandler" />  </extensionElements></userTask>
```

实现**TaskListener**接口并传递**DelegateTask** 。允许设置assignee 和candidate-users/groups：

```java
public class MyAssignmentHandler implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    // Execute custom identity lookups here

    // and then for example call following methods:
    delegateTask.setAssignee("kermit");
    delegateTask.addCandidateUser("fozzie");
    delegateTask.addCandidateGroup("management");
    ...
  }

}
```

使用 Spring 时，可以使用上一节中描述的自定义分配属性，并使用带有[task listener](https://www.activiti.org/userguide/#taskListeners)创建事件的表达式的任务监听器委托给 Spring bean。在以下示例中，将通过调用 ldapService 中的 findManagerOfEmployee 来设置assignee 。传递的 emp 参数是一个流程变量。

```xml
<userTask id="task" name="My Task" activiti:assignee="${ldapService.findManagerForEmployee(emp)}"/>
```

这也适用于候选用户和组：

```xml
<userTask id="task" name="My Task" activiti:candidateUsers="${ldapService.findAllSales()}"/>
```

请注意，这仅在调用方法的返回类型为 String 或 Collection<String> 时才有效（对于候选用户和组）：

```java
public class FakeLdapService {

  public String findManagerForEmployee(String employee) {
    return "Kermit The Frog";
  }

  public List<String> findAllSales() {
    return Arrays.asList("kermit", "gonzo", "fozzie");
  }

}
```

#### 2.脚本任务（Script Task）

##### 描述

脚本任务是一项自动活动。当流程执行到达脚本任务时，会执行相应的脚本。

##### 图形示例

脚本任务可视化为典型的 BPMN 2.0 任务（圆角矩形），矩形左上角有一个小脚本图标。

![](http://rep.shaoteemo.com/bpmn.scripttask.png)

##### XML示例

脚本任务是通过指定脚本和脚本格式来定义的。

```xml
<?xml version="1.0" encoding="UTF-8"?><definitions        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"        xmlns:activiti="http://activiti.org/bpmn"        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"        targetNamespace="脚本任务">    <process id="scriptTask" name="Script Task">        <scriptTask id="theScriptTask" name="Execute script" scriptFormat="groovy">            <script>                sum = 0                for ( i in inputArray ) {                sum += i                }            </script>        </scriptTask>    </process></definitions>
```

scriptFormat 属性的值必须是与[JSR-223](http://jcp.org/en/jsr/detail?id=223)（Java 平台脚本）兼容的名称。默认情况下，每个 JDK 中都包含 JavaScript，因此不需要任何额外的 jar。如果您想使用另一个（与 JSR-223 兼容的）脚本引擎，将相应的 jar 添加到类路径并使用适当的名称就足够了。例如，Activiti 单元测试经常使用 Groovy，因为其语法与 Java 的语法非常相似。

请注意，Groovy 脚本引擎与 groovy-all jar 捆绑在一起。在 2.0 版之前，脚本引擎是常规 Groovy jar 的一部分。因此，现在必须添加以下依赖项：

```xml
<dependency>      <groupId>org.codehaus.groovy</groupId>      <artifactId>groovy-all</artifactId>      <version>2.x.x<version></dependency>
```

##### 脚本中的变量

通过到达脚本任务的执行可访问的所有流程变量都可以在脚本中使用。在示例中，脚本变量“inputArray”实际上是一个过程变量（整数数组）。

```xml
<script>    sum = 0    for ( i in inputArray ) {    sum += i    }</script>
```

也可以在脚本中设置流程变量，只需调用 execution.setVariable("variableName", variableValue)。默认情况下，不会自动存储任何变量（注意：在 Activiti 5.12 之前就是这种情况！）。通过将 scriptTask 上的 autoStoreVariables 属性设置为 true，可以自动存储脚本中定义的任何变量（例如上面示例中的 sum）。但是，最佳做法是不要这样做并使用显式 execution.setVariable() 调用，因为在某些最新版本的 JDK 上，自动存储变量对某些脚本语言不起作用。有关更多详细信息，请参阅此链接。

```xml
<!--设置自动存储变量。默认值：false--><scriptTask id="script" scriptFormat="JavaScript" activiti:autoStoreVariables="false"/>
```

该参数的默认值为false，意味着如果在脚本任务定义中省略该参数，则所有声明的变量将仅在脚本运行期间存在。

关于如何在脚本中设置变量的示例：

```xml
<script>    def scriptVar = "test123"    execution.setVariable("myVar", scriptVar)</script>
```

注意：以下名称是保留的，不能用作变量名称：out、out:print、lang:import、context、elcontext。

脚本结果：

通过将流程变量名称指定为脚本任务定义的“activiti:resultVariable”属性的文字值，可以将脚本任务的返回值分配给现有的或新的流程变量。特定流程变量的任何现有值都将被脚本执行的结果值覆盖。当未指定结果变量名称时，脚本结果值将被忽略。

```xml
<!--脚本结果接受.resultVariable：变量名-->
<scriptTask id="theScriptTask2" name="Execute script" scriptFormat="juel" activiti:resultVariable="myVar">
    <script>#{echo}</script>
</scriptTask>
```

在上面的例子中，脚本执行的结果（解析表达式'#{echo}'的值）在脚本完成后被设置为名为'myVar'的流程变量。

##### 安全

当使用 javascript 作为脚本语言时，也可以使用安全脚本。请参阅安全脚本部分。

#### 3.Java服务任务（Java Service Task）

##### 描述

Java 服务任务用于调用外部 Java 类。

##### 图形示例

服务任务显示为左上角带有小齿轮图标的圆角矩形。

![](http://rep.shaoteemo.com/bpmn.java.service.task.png)

##### XML示例

有 4 种方式声明如何调用 Java 逻辑：

- 指定实现 JavaDelegate 或 ActivityBehavior 的类
- 评估解析为委托对象的表达式
- 调用方法表达式
- 评估值表达式

要指定在流程执行期间调用的类，需要由 activiti:class 属性提供完全限定的类名。

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
        targetNamespace="Java服务任务演示">

    <process id="service_task" name="serviceTask">
        <!--activiti:class:-->
        <serviceTask id="javaService" name="My Java Service Task" activiti:class="org.activiti.MyJavaDelegate" />
    </process>
</definitions>
```

有关如何使用此类的更多详细信息，请参阅实现部分。

也可以使用解析为对象的表达式。此对象必须遵循与使用 activiti:class 属性时创建的对象相同的规则（请参阅下面的实现部分）。

```xml
<serviceTask id="serviceTask" activiti:delegateExpression="${delegateExpressionBean}" />
```

此处，delegateExpressionBean 是一个实现 JavaDelegate 接口的 bean，例如在 Spring 容器中定义。

要指定应评估的 UEL 方法表达式，请使用属性 activiti:expression。

```xml
<serviceTask id="javaService" name="My Java Service Task" activiti:expression="#{printer.printMessage()}" />
```

将在命名为**printer**的对象上调用方法 printMessage（）。

也可以使用表达式中使用的方法传递参数。

```xml
<serviceTask id="javaService3" name="My Java Service Task" activiti:expression="#{printer.printMessage(execution, myVar)}" />
```

将在名为**printer**的对象上调用方法 printMessage。传递的第一个参数是 DelegateExecution，它在表达式上下文中默认可用作为**execution**。传递的第二个参数是当前执行中名为 myVar 的变量的值。

要指定应评估的 UEL 值表达式，请使用属性 activiti:expression。

```xml
<serviceTask id="javaService4" name="My Java Service Task" activiti:expression="#{split.ready}" />
```

属性 ready 的 getter 方法 getReady（）将在名为 split 的 bean 上调用。命名对象在执行的流程变量和（如果适用）Spring 上下文中解析。

##### 实现方式

为了实现一个可以在流程执行过程中调用的类，这个类需要实现org.activiti.engine.delegate中的接口。JavaDelegate 接口在 execute 方法中提供所需的逻辑。当流程执行到达此特定步骤时，它将执行该方法中定义的此逻辑，并以默认的 BPMN 2.0 方式保留活动。

例如，让我们创建一个 Java 类，该类可用于将流程变量 String 更改为大写。这个类需要实现org.activiti.engine.delegate.JavaDelegate接口，这需要我们实现execute(DelegateExecution)方法。引擎会调用这个操作并执行里面的业务逻辑。流程实例信息，如流程变量等，可以通过 DelegateExecution 接口访问和操作（点击[DelegateExecution](http://activiti.org/javadocs/org/activiti/engine/delegate/DelegateExecution.html)链接查看其操作的详细 Javadoc）。

```java
public class ToUppercase implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    String var = (String) execution.getVariable("input");
    var = var.toUpperCase();
    execution.setVariable("input", var);
  }

}
```

注意：只有一个 Java 类的实例是为它定义的 serviceTask 创建的。所有流程实例共享将用于调用 execute(DelegateExecution) 的同一个类实例。这意味着该类不能使用任何成员变量并且必须是线程安全的，因为它可以从不同的线程同时执行。这也会影响字段注入的处理方式。（译注：单纯的用于业务逻辑处理）

在流程定义中引用的类（即通过使用 activiti:class）在部署期间不会被实例化。只有当流程执行第一次到达流程中使用该类的点时，才会创建该类的实例。如果找不到该类，则会抛出 ActivitiException。这样做的原因是您部署时的环境（更具体地说是类路径）通常与实际的运行时环境不同。比如在Activiti Explorer中使用ant或者业务归档上传部署流程时，classpath中不包含引用的类。

[[INTERNAL: non-public implementation classes\]](https://www.activiti.org/userguide/#internal)也可以提供一个实现 org.activiti.engine.impl.pvm.delegate.ActivityBehavior 接口的类。然后实现可以访问更强大的 ActivityExecution，例如，它也允许影响流程的控制流。但是请注意，这不是一个很好的做法，应尽可能避免。因此，建议仅将 ActivityBehavior 接口用于高级用例并且如果您确切地知道自己在做什么。

##### 属性注入

可以将值注入到委托类的字段中。支持以下类型的注入：

- 固定字符串值
- 表达式

如果可用，该值将通过委托类上的公共 setter 方法注入，遵循 Java Bean 命名约定（例如，字段 firstName 具有 setFirstName(… )）。如果该字段没有可用的Setter，则将在委托上设置私有成员的值。某些环境中的 SecurityManagers 不允许修改私有字段，因此为要注入的字段公开公共 setter-method 更安全。

**无论流程定义中声明的值类型如何，注入目标上的 setter/private 字段的类型应始终为 org.activiti.engine.delegate.Expression。解析表达式后，可以将其强制转换为适当的类型。**

使用“actviti:class”属性时支持字段注入。当使用 activiti:delegateExpression 属性时，字段注入也是可能的，但是关于线程安全的特殊规则适用（见下一节）。

以下代码片段显示了如何将常量值注入到类中声明的字段中。请注意，我们需要在实际的字段注入声明之前声明一个 extensionElements XML 元素，这是 BPMN 2.0 XML Schema 的要求。

```xml
<!--使用actviti:class在类中属性常量注入-->
<serviceTask id="javaService5"
             name="Java service invocation"
             activiti:class="org.activiti.examples.bpmn.servicetask.ToUpperCaseFieldInjected">
    <extensionElements>
        <!--name：对应属性名-->
        <activiti:field name="text" stringValue="Hello World" />
    </extensionElements>
</serviceTask>
```

ToUpperCaseFieldInjected 类有一个字段文本，其类型为 org.activiti.engine.delegate.Expression。调用 text.getValue(execution) 时，会返回配置好的字符串值 Hello World：

```java
public class ToUpperCaseFieldInjected implements JavaDelegate {

  private Expression text;

  public void execute(DelegateExecution execution) {
    execution.setVariable("var", ((String)text.getValue(execution)).toUpperCase());
  }

}
```

或者，对于长文本（例如内嵌电子邮件），可以使用 'activiti:string' 子元素：

```xml
<!--上面标签的长文本的使用-->
<serviceTask id="javaService6"
             name="Java service invocation"
             activiti:class="org.activiti.examples.bpmn.servicetask.ToUpperCaseFieldInjected">
    <extensionElements>
        <activiti:field name="text">
            <activiti:string>
                This is a long string with a lot of words and potentially way longer even!
            </activiti:string>
        </activiti:field>
    </extensionElements>
</serviceTask>
```

要注入在运行时动态解析的值，可以使用表达式。这些表达式可以使用流程变量或 Spring 定义的 bean（如果使用 Spring）。如服务任务实现中所述，当使用 activiti:class 属性时，Java 类的实例在服务任务中的所有流程实例之间共享。要在字段中动态注入值，您可以在 org.activiti.engine.delegate.Expression 中注入值和方法表达式，这些表达式可以使用 execute 方法中传递的 DelegateExecution 进行evaluated/invoked。

下面的示例类使用注入的表达式并使用当前的 DelegateExecution 解析它们。在传递性别变量时使用了一个 genBean 方法调用。完整的代码和测试可以在**org.activiti.examples.bpmn.servicetask.JavaServiceTaskTest.testExpressionFieldInjection**

```xml
<serviceTask id="javaService7" name="Java service invocation"
             activiti:class="org.activiti.examples.bpmn.servicetask.ReverseStringsFieldInjected">

    <extensionElements>
        <activiti:field name="text1">
            <activiti:expression>${genderBean.getGenderString(gender)}</activiti:expression>
        </activiti:field>
        <activiti:field name="text2">
            <activiti:expression>Hello ${gender == 'male' ? 'Mr.' : 'Mrs.'} ${name}</activiti:expression>
        </activiti:field>
    </ extensionElements>
</ serviceTask>
```

```java
public class ReverseStringsFieldInjected implements JavaDelegate {

  private Expression text1;
  private Expression text2;

  public void execute(DelegateExecution execution) {
    String value1 = (String) text1.getValue(execution);
    execution.setVariable("var1", new StringBuffer(value1).reverse().toString());

    String value2 = (String) text2.getValue(execution);
    execution.setVariable("var2", new StringBuffer(value2).reverse().toString());
  }
}
```

或者，您也可以将表达式设置为属性而不是子元素，以使 XML 不那么冗长。

```xml
<activiti:field name="text1" expression="${genderBean.getGenderString(gender)}" />
<activiti:field name="text1" expression="Hello ${gender == 'male' ? 'Mr.' : 'Mrs.'} ${name}" />
```

##### 字段注入和线程安全

一般来说，使用带有 Java 委托和字段注入的服务任务是线程安全的。但是，在某些情况下无法保证线程安全，这取决于 Activiti 运行的设置或环境。

使用 activiti:class 属性时，使用字段注入始终是线程安全的。对于每个引用某个类的服务任务，都会实例化一个新的实例，并在创建实例时注入一次字段。在不同的任务或流程定义中多次重用同一个类是没有问题的。

使用 activiti:expression 属性时，无法使用字段注入。参数通过方法调用传递，并且这些始终是线程安全的。

当使用 activiti:delegateExpression 属性时，委托实例的线程安全将取决于表达式的解析方式。如果委托表达式在各种tasks and/or流程定义中重用，并且表达式始终返回相同的实例，则使用字段注入不是线程安全的。让我们看几个例子来澄清。

假设表达式是 ${factory.createDelegate(someVariable)}，其中 factory 是引擎已知的 Java bean（例如使用 Spring 集成时的 Spring bean），它在每次解析表达式时创建一个新实例。在这种情况下使用字段注入时，在线程安全方面没有问题：每次解析表达式时，字段都会注入到这个新实例上。

但是，假设表达式是 ${someJavaDelegateBean} ，它解析为 JavaDelegate 类的实现，并且我们在创建每个 bean 的单例实例的环境中运行（如 Spring，但也有许多其他实例）。在不同的tasks and/or流程定义中使用此表达式时，该表达式将始终解析为相同的实例。在这种情况下，使用字段注入不是线程安全的。例如：

```xml
<!--
    下面两个为不安全的属性注入方式示例
-->
<serviceTask id="serviceTask1" activiti:delegateExpression="${someJavaDelegateBean}">
    <extensionElements>
        <activiti:field name="someField" expression="${input * 2}"/>
    </extensionElements>
</serviceTask>

<!-- other process definition elements -->

<serviceTask id="serviceTask2" activiti:delegateExpression="${someJavaDelegateBean}">
    <extensionElements>
        <activiti:field name="someField" expression="${input * 2000}"/>
    </extensionElements>
</serviceTask>
```

此示例代码段有两个服务任务，它们使用相同的委托表达式，但为 Expression 字段注入了不同的值。如果表达式解析为同一个实例，则在执行进程时注入字段 someField 时，并发场景中可能存在竞争条件。

解决这个问题的最简单的方法是

- 重写 Java 委托以使用表达式并通过方法参数将所需数据传递给委托。
- 每次解析委托表达式时，返回委托类的新实例。例如在使用 Spring 时，这意味着必须将 bean 的作用域设置为原型（例如通过在委托类中添加 @Scope(SCOPE_PROTOTYPE) 注解）（译注：以非单例的形式创建）

从 Activiti 5.21 版开始，流程引擎配置可以配置为禁用在委托表达式上使用字段注入，通过设置 delegateExpressionFieldInjectionMode 属性的值（它采用 org.activiti.engine.imp.cfg.DelegateExpressionFieldInjectionMode 枚举中的值之一）。

可以进行以下设置：

- **DISABLED**: 使用委托表达式时完全禁用字段注入。不会尝试注入。当涉及到线程安全时，这是最安全的模式。
- **COMPATIBILITY**: 在这种模式下，行为将与 5.21 版之前的行为完全相同：使用委托表达式时可以进行字段注入，并且在委托类上未定义字段时将引发异常。这当然是线程安全方面最不安全的模式，但是为了向后兼容可能需要它，或者当委托表达式仅用于一组流程定义中的一个任务时可以安全地使用（因此不会发生并发竞争条件）。
- **MIXED**: 在使用 delegateExpressions 时允许注入，但在委托上未定义字段时不会抛出异常。这允许混合行为，其中一些委托具有注入（例如因为它们不是单例）而有些则没有。
- **Activiti 5.x 版的默认模式是 COMPATIBILITY。**
- **Activiti 6.x 版的默认模式是 MIXED。**

例如，假设我们使用的是 MIXED 模式并且我们正在使用 Spring 集成。假设我们在 Spring 配置中有以下 bean：

```java
<bean id="singletonDelegateExpressionBean"
  class="org.activiti.spring.test.fieldinjection.SingletonDelegateExpressionBean" />

<bean id="prototypeDelegateExpressionBean"
  class="org.activiti.spring.test.fieldinjection.PrototypeDelegateExpressionBean"
  scope="prototype" />
```

第一个 bean 是一个普通的 Spring bean，因此是一个单例。第二个将原型作为作用域，每次请求 bean 时，Spring 容器都会返回一个新实例。

给出以下流程定义：

```xml
<serviceTask id="serviceTask1" activiti:delegateExpression="${prototypeDelegateExpressionBean}">
    <extensionElements>
        <activiti:field name="fieldA" expression="${input * 2}"/>
        <activiti:field name="fieldB" expression="${1 + 1}"/>
        <activiti:field name="resultVariableName" stringValue="resultServiceTask1"/>
    </extensionElements>
</serviceTask>

<serviceTask id="serviceTask2" activiti:delegateExpression="${prototypeDelegateExpressionBean}">
    <extensionElements>
        <activiti:field name="fieldA" expression="${123}"/>
        <activiti:field name="fieldB" expression="${456}"/>
        <activiti:field name="resultVariableName" stringValue="resultServiceTask2"/>
    </extensionElements>
</serviceTask>

<serviceTask id="serviceTask3" activiti:delegateExpression="${singletonDelegateExpressionBean}">
    <extensionElements>
        <activiti:field name="fieldA" expression="${input * 2}"/>
        <activiti:field name="fieldB" expression="${1 + 1}"/>
        <activiti:field name="resultVariableName" stringValue="resultServiceTask1"/>
    </extensionElements>
</serviceTask>

<serviceTask id="serviceTask4" activiti:delegateExpression="${singletonDelegateExpressionBean}">
    <extensionElements>
        <activiti:field name="fieldA" expression="${123}"/>
        <activiti:field name="fieldB" expression="${456}"/>
        <activiti:field name="resultVariableName" stringValue="resultServiceTask2"/>
    </extensionElements>
</serviceTask>
```

我们有四个服务任务，其中第一个和第二个使用 ${prototypeDelegateExpressionBean} 委托表达式，第三个和第四个使用 ${singletonDelegateExpressionBean} 委托表达式。

我们先来看看原型bean：

```java
public class PrototypeDelegateExpressionBean implements JavaDelegate {

  public static AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

  private Expression fieldA;
  private Expression fieldB;
  private Expression resultVariableName;

  public PrototypeDelegateExpressionBean() {
    INSTANCE_COUNT.incrementAndGet();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Number fieldAValue = (Number) fieldA.getValue(execution);
    Number fieldValueB = (Number) fieldB.getValue(execution);

    int result = fieldAValue.intValue() + fieldValueB.intValue();
    execution.setVariable(resultVariableName.getValue(execution).toString(), result);
  }

}
```

当我们在运行上述流程定义的流程实例后检查 INSTANCE_COUNT 时，我们会得到两个，因为每次解析 ${prototypeDelegateExpressionBean} 时都会创建一个新实例。在这里可以毫无问题地注入字段，我们可以在这里看到三个 Expression 成员字段。

然而，单例 bean 看起来略有不同：

```java
public class SingletonDelegateExpressionBean implements JavaDelegate {

  public static AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

  public SingletonDelegateExpressionBean() {
    INSTANCE_COUNT.incrementAndGet();
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Expression fieldAExpression = DelegateHelper.getFieldExpression(execution, "fieldA");
    Number fieldA = (Number) fieldAExpression.getValue(execution);

    Expression fieldBExpression = DelegateHelper.getFieldExpression(execution, "fieldB");
    Number fieldB = (Number) fieldBExpression.getValue(execution);

    int result = fieldA.intValue() + fieldB.intValue();

    String resultVariableName = DelegateHelper.getFieldExpression(execution, "resultVariableName").getValue(execution).toString();
    execution.setVariable(resultVariableName, result);
  }

}
```

INSTANCE_COUNT 在这里总是一个，因为它是一个单例。在此委托中，没有 Expression 成员字段。这是可能的，因为我们在 MIXED 模式下运行。在 COMPATIBILITY 模式下，这将引发异常，因为它期望成员字段在那里。禁用模式也适用于这个 bean，但它会禁止使用上面使用字段注入的原型 bean。

在此委托代码中，使用了 org.activiti.engine.delegate.DelegateHelper 类，该类具有一些有用的实用方法来执行相同的逻辑，但是当委托是单例时以线程安全的方式执行。它不是注入表达式，而是通过 getFieldExpression 方法获取。这意味着当涉及到服务任务 xml 时，字段的定义与单例 bean 的定义完全相同。如果您查看上面的 xml 片段，您会发现它们在定义上是相同的，只有实现逻辑不同。

（技术说明：the *getFieldExpression* will introspect the BpmnModel and create the Expression on the fly when the method is executed, making it thread-safe）

- 对于 Activiti 版本 5.x，DelegateHelper 不能用于 ExecutionListener 或 TaskListener（由于架构缺陷）。要创建这些监听器的线程安全实例，请使用表达式或确保每次解析委托表达式时都会创建一个新实例。

- 对于 Activiti 6.x 版，DelegateHelper 在 ExecutionListener 和 TaskListener 实现中确实有效。例如在 6.x 版本中，可以使用 DelegateHelper 编写以下代码：

  ```xml
  <extensionElements>
      <activiti:executionListener
              delegateExpression="${testExecutionListener}" event="start">
          <activiti:field name="input" expression="${startValue}" />
          <activiti:field name="resultVar" stringValue="processStartValue" />
      </activiti:executionListener>
  </extensionElements>
  ```

  其中 testExecutionListener 解析为实现 ExecutionListener 接口的实例：

  ```java
  @Component("testExecutionListener")
  public class TestExecutionListener implements ExecutionListener {
  
    @Override
    public void notify(DelegateExecution execution) {
      Expression inputExpression = DelegateHelper.getFieldExpression(execution, "input");
      Number input = (Number) inputExpression.getValue(execution);
  
      int result = input.intValue() * 100;
  
      Expression resultVarExpression = DelegateHelper.getFieldExpression(execution, "resultVar");
      execution.setVariable(resultVarExpression.getValue(execution).toString(), result);
    }
  
  }
  ```

##### 服务任务结果

通过将流程变量名称指定为服务任务的“activiti:resultVariable”属性的文字值，可以将服务执行的返回值（仅适用于使用表达式的服务任务）分配给现有的或新的流程变量定义。特定流程变量的任何现有值都将被服务执行的结果值覆盖。当未指定结果变量名称时，服务执行结果值将被忽略。

```xml
<serviceTask id="aMethodExpressionServiceTask" activiti:expression="#{myService.doSomething()}" activiti:resultVariable="myVar" />
```

在上面的示例中，服务执行的结果（在流程变量或 Spring bean 中以名称“myService”提供的对象上的“doSomething()”方法调用的返回值）被设置服务执行完成后，转到名为“myVar”的流程变量。

##### 异常处理

在执行自定义逻辑时，往往需要捕捉某些业务异常，并在周边流程内部进行处理。 Activiti 提供了不同的选项来做到这一点。

BPMN 错误抛出

可以从服务任务或脚本任务中的用户代码中抛出 BPMN 错误。为了做到这一点，可以在 JavaDelegates、脚本、表达式和委托表达式中抛出一个名为 BpmnError 的特殊 ActivitiException。引擎将捕获此异常并将其转发到适当的错误处理程序，例如边界错误事件或错误事件子流程。

```java
public class ThrowBpmnErrorDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    try {
      executeBusinessLogic();
    } catch (BusinessException e) {
      throw new BpmnError("BusinessExceptionOccurred");
    }
  }

}
```

构造函数参数是一个错误代码，它将用于确定对错误负责的错误处理程序。有关如何捕获 BPMN 错误的信息，请参阅边界错误事件。

这种机制应该只用于由流程定义中建模的边界错误事件或错误事件子流程处理的业务故障。技术错误应由其他异常类型表示，并且通常不在流程内部处理。

##### 异常映射

也可以通过使用 mapException 扩展将 java 异常直接映射到业务异常。单映射是最简单的形式：

```xml
<!--异常映射处理-->
<serviceTask id="servicetask1" name="Service Task" activiti:class="...">
    <extensionElements>
        <activiti:mapException
                errorCode="myErrorCode1">org.activiti.SomeException</activiti:mapException>
    </extensionElements>
</serviceTask>
```

在上面的代码中，如果在服务任务中抛出 org.activiti.SomeException 的实例，它将被捕获并转换为具有给定 errorCode 的 BPMN 异常。此时，它将像正常的BPMN 异常一样处理。任何其他异常都将被视为没有映射到位。它将传播给 API 调用者。

可以使用 includeChildExceptions 属性在一行中映射某个异常的所有子异常。

```xml
<!--映射某个异常的所有子异常-->
<serviceTask id="servicetask2" name="Service Task" activiti:class="...">
    <extensionElements>
        <activiti:mapException errorCode="myErrorCode1"
                               includeChildExceptions="true">org.activiti.SomeException</activiti:mapException>
    </extensionElements>
</serviceTask>
```

上面的代码将导致 activiti 将 SomeException 的任何直接或间接子类转换为具有给定错误代码的 BPMN 错误。如果没有给出，includeChildExceptions 将被视为“false”。

最通用的映射是默认映射。默认映射是没有类的映射。它将匹配任何 java 异常：

```xml
<!--没有实体类的异常映射-->
<serviceTask id="servicetask3" name="Service Task" activiti:class="...">
    <extensionElements>
        <activiti:mapException errorCode="myErrorCode1"/>
    </extensionElements>
</serviceTask>
```

映射按顺序检查，从上到下，将遵循第一个找到的匹配项，默认映射除外。只有在所有映射检查失败后才会选择默认映射。只有第一个没有类的映射才会被视为默认映射。默认映射忽略 includeChildExceptions。

##### 异常序列流

[[INTERNAL: non-public implementation classes\]](https://www.activiti.org/userguide/#internal)

另一种选择是通过分支路由流程执行，以防发生某些异常。以下示例显示了这是如何完成的。

```xml
<!--分支流程以防止某些异常发生-->
<serviceTask id="javaService01"
             name="Java service invocation"
             activiti:class="org.activiti.ThrowsExceptionBehavior">
</serviceTask>

<sequenceFlow id="no-exception" sourceRef="javaService01" targetRef="theEnd" />
<sequenceFlow id="exception" sourceRef="javaService01" targetRef="fixException" />
```

在这里，服务任务有两个传出序列流，一个为**exception**，另一个为**no-exception**。如果出现异常，此序列流 ID 将用于指导流程：

```java
public class ThrowsExceptionBehavior implements ActivityBehavior {

  public void execute(ActivityExecution execution) throws Exception {
    String var = (String) execution.getVariable("var");

    PvmTransition transition = null;
    try {
      executeLogic(var);
      transition = execution.getActivity().findOutgoingTransition("no-exception");
    } catch (Exception e) {
      transition = execution.getActivity().findOutgoingTransition("exception");
    }
    execution.take(transition);
  }

}
```

##### 在 JavaDelegate 中使用 Activiti 服务

对于某些用例，可能需要从 Java 服务任务中使用 Activiti 服务（例如，如果 callActivity 不适合您的需要，则通过 RuntimeService 启动流程实例）。org.activiti.engine.delegate.DelegateExecution 允许通过 org.activiti.engine.EngineServices 接口轻松使用这些服务：

```java
public class StartProcessInstanceTestDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    RuntimeService runtimeService = execution.getEngineServices().getRuntimeService();
    runtimeService.startProcessInstanceByKey("myProcess");
  }

}
```

所有的 Activiti 服务 API 都可以通过这个接口使用。

由于使用这些 API 调用而发生的所有数据更改都将成为当前事务的一部分。这也适用于依赖注入的环境，如 Spring 和 CDI，无论是否启用 JTA 数据源。例如，下面的代码片段将与上面的代码片段执行相同的操作，但是现在 RuntimeService 是注入的，而不是通过 org.activiti.engine.EngineServices 接口获取的。

```java
@Component("startProcessInstanceDelegate")
public class StartProcessInstanceTestDelegateWithInjection {

    @Autowired
    private RuntimeService runtimeService;

    public void startProcess() {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
    }

}
```

**重要技术说明：**由于服务调用是作为当前事务的一部分完成的，因此在服务任务执行之前生成或更改的任何数据尚未刷新到数据库中。所有 API 调用都对数据库数据起作用，这意味着这些未提交的更改在服务任务的 api 调用中是不可见的。

#### 4.Web  Service任务（Web Service Task）

##### 描述

Web Service 任务用于同步调用外部 Web 服务。

##### 图形示例

Web Service任务与 Java 服务任务相同。

![](http://rep.shaoteemo.com/bpmn.web.service.task.png)

##### XML示例

要使用 Web Serivce，我们需要导入它的operations和complex types。这可以通过使用指向 Web Service的 WSDL 的导入标记自动完成：

```xml
<!--使用Web服务任务需要导入如下命名空间。-->
<import importType="http://schemas.xmlsoap.org/wsdl/"
        location="http://localhost:63081/counter?wsdl"
        namespace="http://webservice.activiti.org/" />
```

前面的声明告诉 Activiti 导入定义，但它不会为您创建项目定义和消息。假设我们要调用一个名为 prettyPrint 的特定方法，因此我们需要为请求和响应消息创建相应的消息和项目定义：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- auth:ShaoTeemo -->
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="Web服务任务演示">

    <!--使用Web服务任务需要导入如下命名空间。-->
    <import importType="http://schemas.xmlsoap.org/wsdl/"
            location="http://localhost:63081/counter?wsdl"
            namespace="http://webservice.activiti.org/" />

    <!--请求与响应消息定义，与对应项定义-->
    <message id="prettyPrintCountRequestMessage" itemRef="tns:prettyPrintCountRequestItem" />
    <message id="prettyPrintCountResponseMessage" itemRef="tns:prettyPrintCountResponseItem" />

    <itemDefinition id="prettyPrintCountRequestItem" structureRef="counter:prettyPrintCount" />
    <itemDefinition id="prettyPrintCountResponseItem" structureRef="counter:prettyPrintCountResponse" />

    <process id="web_service_task" name="webServiceTask">
    </process>

</definitions>
```

在声明服务任务之前，我们必须定义实际引用 Web Service的 BPMN 接口和操作。基本上，我们定义了接口和所需的操作。对于每个操作，我们重用之前定义的消息进行输入和输出。例如，以下声明定义了计数器接口和 prettyPrintCountOperation 操作：

```xml
<!--接口定义。（以下为计数器接口）-->
<interface name="Counter Interface" implementationRef="counter:Counter">
    <operation id="prettyPrintCountOperation" name="prettyPrintCount Operation"
               implementationRef="counter:prettyPrintCount">
        <inMessageRef>tns:prettyPrintCountRequestMessage</inMessageRef>
        <outMessageRef>tns:prettyPrintCountResponseMessage</outMessageRef>
    </operation>
</interface>
```

然后我们可以通过使用##WebService 实现和对Web 服务操作的引用来声明一个Web 服务任务。

```xml
<!--Web服务声明-->
<serviceTask id="webService"
             name="Web service invocation"
             implementation="##WebService"
             operationRef="tns:prettyPrintCountOperation">
</serviceTask>
```

##### Web 服务任务 IO 规范

除非我们对数据输入和输出关联使用简单的方法（见下文），否则每个 Web Service任务都需要声明一个 IO 规范，该规范说明哪些是任务的输入和输出。他的方法非常简单并且符合 BPMN 2.0 ，对于我们的 prettyPrint 示例，我们根据之前声明的项目定义输入和输出集：

```xml
<serviceTask id="webService"
             name="Web service invocation"
             implementation="##WebService"
             operationRef="tns:prettyPrintCountOperation">
    <!--I/O定义-->
    <ioSpecification>
        <dataInput itemSubjectRef="tns:prettyPrintCountRequestItem" id="dataInputOfServiceTask" />
        <dataOutput itemSubjectRef="tns:prettyPrintCountResponseItem" id="dataOutputOfServiceTask" />
        <inputSet>
            <dataInputRefs>dataInputOfServiceTask</dataInputRefs>
        </inputSet>
        <outputSet>
            <dataOutputRefs>dataOutputOfServiceTask</dataOutputRefs>
        </outputSet>
    </ioSpecification>
</serviceTask>
```

##### Web Service任务数据输入关联

有两种指定数据输入关联的方法：

- 使用表达式
- 使用简单的方法

要使用表达式指定数据输入关联，我们需要定义源项和目标项并指定每个项的字段之间的相应分配。在以下示例中，我们为项目分配前缀和后缀字段：

```xml
<serviceTask id="webService"
             name="Web service invocation"
             implementation="##WebService"
             operationRef="tns:prettyPrintCountOperation">

    <ioSpecification>
        <dataInput itemSubjectRef="tns:prettyPrintCountRequestItem" id="dataInputOfServiceTask" />
        <dataOutput itemSubjectRef="tns:prettyPrintCountResponseItem" id="dataOutputOfServiceTask" />
        <inputSet>
            <dataInputRefs>dataInputOfServiceTask</dataInputRefs>
        </inputSet>
        <outputSet>
            <dataOutputRefs>dataOutputOfServiceTask</dataOutputRefs>
        </outputSet>
    </ioSpecification>
    <!--指定数据输入关联-->
    <dataInputAssociation>
        <sourceRef>dataInputOfProcess</sourceRef>
        <targetRef>dataInputOfServiceTask</targetRef>
        <assignment>
            <from>${dataInputOfProcess.prefix}</from>
            <to>${dataInputOfServiceTask.prefix}</to>
        </assignment>
        <assignment>
            <from>${dataInputOfProcess.suffix}</from>
            <to>${dataInputOfServiceTask.suffix}</to>
        </assignment>
    </dataInputAssociation>
</serviceTask>
```

另一方面，我们可以使用简单得多的方法。sourceRef 元素是一个 Activiti 变量名，而 targetRef 元素是项目定义的一个属性。在以下示例中，我们将变量 PrefixVariable 的值分配给*prefix*字段，并将变量 SuffixVariable 的值分配给*suffix*字段。

```xml
<!--第二种数据输入关联-->
<dataInputAssociation>
    <sourceRef>PrefixVariable</sourceRef>
    <targetRef>prefix</targetRef>
</dataInputAssociation>
<dataInputAssociation>
    <sourceRef>SuffixVariable</sourceRef>
    <targetRef>suffix</targetRef>
</dataInputAssociation>
```

##### Web Service任务数据输出关联

有两种方法可以指定数据输出关联：

- 使用表达式
- 使用简单的方法

要使用表达式指定数据输出关联，我们需要定义目标变量和源表达式。该方法非常简单且类似的数据输入关联：

```xml
<!--指定数据输出关联-->
<dataOutputAssociation>
    <targetRef>dataOutputOfProcess</targetRef>
    <transformation>${dataOutputOfServiceTask.prettyPrint}</transformation>
</dataOutputAssociation>
```

另一方面，我们可以使用简单得多的方法。sourceRef 元素是项目定义的一个属性，而 targetRef 元素是一个 Activiti 变量名。该方法非常简单且类似的数据输入关联：

```xml
<!--第二种数据输出关联-->
<dataOutputAssociation>
    <sourceRef>prettyPrint</sourceRef>
    <targetRef>OutputVariable</targetRef>
</dataOutputAssociation>
```

##### 进一步了解

1.[activiti webservice task 的一个简单执行和配置实例](https://blog.csdn.net/iteye_18253/article/details/82332221)

2.[activiti5第五弹 serviceTask中的webserviceTask 以及 shellTask](https://blog.csdn.net/u012613903/article/details/42709917)

#### 5.业务规则任务（Business Rule Task）

[[EXPERIMENTAL\]](https://www.activiti.org/userguide/#experimental)

##### 描述

业务规则任务用于同步执行一个或多个规则。Activiti 使用 Drools Expert，即 Drools 规则引擎来执行业务规则。目前，包含业务规则的 .drl 文件必须与定义业务规则任务的流程定义一起部署以执行这些规则。这意味着流程中使用的所有 .drl 文件都必须打包在流程 BAR 文件中，例如任务表单。有关为 Drools Expert 创建业务规则的更多信息，请参阅 [JBoss Drools](http://www.jboss.org/drools/documentation) 上的 Drools 文档

如果您想插入规则任务的实现，例如因为你想以不同的方式使用 Drools 或者你想使用一个完全不同的规则引擎，那么您可以在 BusinessRuleTask 上使用 class 或 expression 属性，它的行为将与 ServiceTask 完全一样。

##### 图形示例

业务规则任务通过表格图标进行可视化。

![](http://rep.shaoteemo.com/bpmn.business.rule.task.png)

##### XML示例

要执行部署在与流程定义相同的 BAR 文件中的一个或多个业务规则，我们需要定义输入和结果变量。对于输入变量定义，可以定义一个过程变量列表，以逗号分隔。输出变量定义只能包含一个变量名，用于将执行的业务规则的输出对象存储在流程变量中。请注意，结果变量将包含一个对象列表。如果默认情况下未指定结果变量名称，则使用 org.activiti.engine.rules.OUTPUT。

以下业务规则任务执行与流程定义一起部署的所有业务规则：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- auth:ShaoTeemo -->
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:activiti="http://activiti.org/bpmn"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL
                    https://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd"
        xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
        xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
        xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
        targetNamespace="业务规则任务演示">

    <process id="business_rule_task" name="businessRuleTask">
        <startEvent id="theStart" />
        <sequenceFlow sourceRef="theStart" targetRef="businessRuleTask" />

        <businessRuleTask id="businessRuleTask" activiti:ruleVariablesInput="${order}"
                          activiti:resultVariable="rulesOutput" />

        <sequenceFlow sourceRef="businessRuleTask" targetRef="theEnd" />

        <endEvent id="theEnd" />
    </process>

</definitions>
```

还可以将业务规则任务配置为仅执行已部署的 .drl 文件中定义的一组规则。为此必须指定由逗号分隔的规则名称列表。

```xml
<!--.drl文件中的规则使用-->
<businessRuleTask id="businessRuleTask2" activiti:ruleVariablesInput="${order}" activiti:rules="rule1, rule2" />
```

在这种情况下，只执行 rule1 和 rule2。

您还可以定义应从执行中排除的规则列表。

```xml
<!--排除定义的规则-->
<businessRuleTask id="businessRuleTask3" activiti:ruleVariablesInput="${order}" activiti:rules="rule1, rule2" exclude="true" />
```

在这种情况下，除了 rule1 和 rule2 之外，所有部署在与流程定义相同的 BAR 文件中的规则都将被执行。

如前所述，另一种选择是自己挂钩 BusinessRuleTask (org.activiti.bpmn.model)的实现：

```xml
<!--自定义业务规则实现-->
<businessRuleTask id="businessRuleTask" activiti:class="${MyRuleServiceDelegate}" />
```

现在，BusinessRuleTask 的行为与 ServiceTask 完全一样，但仍保留 BusinessRuleTask 图标，以可视化我们在此处进行业务规则处理。（org.activiti.engine.delegate.BusinessRuleTaskDelegate）

#### 6.邮件服务任务（Email Task）

##### 描述

Activiti 允许通过向一个或多个收件人发送电子邮件的自动邮件服务任务来增强业务流程，包括支持抄送、密送、HTML 内容等。请注意，邮件任务不是 BPMN 2.0 规范的正式任务（因此它没有专用图标）。因此，在 Activiti 中，邮件任务被实现为一个专门的**服务任务（Service Task）**。

##### 邮件服务配置

Activiti 引擎通过具有 SMTP 功能的外部邮件服务器发送电子邮件。要实际发送电子邮件，引擎需要知道如何到达邮件服务器。可以在 activiti.cfg.xml（Spring Boot在yml配置） 配置文件中设置以下属性：

|     **Property**      |         **Required?**         |                       **Description**                        |
| :-------------------: | :---------------------------: | :----------------------------------------------------------: |
|    mailServerHost     |              no               | 您的邮件服务器的主机名（例如 mail.mycorp.com）。默认为本地主机 |
|    mailServerPort     |              yes              |          邮件服务器上 SMTP 通信的端口。默认值为 25           |
| mailServerDefaultFrom |              no               | 电子邮件发件人的默认电子邮件地址，当用户未提供时。默认为 activiti@activiti.org |
|  mailServerUsername   | if applicable for your server |     某些邮件服务器需要凭据才能发送电子邮件。默认不设置。     |
|  mailServerPassword   | if applicable for your server |     某些邮件服务器需要凭据才能发送电子邮件。默认不设置。     |
|   mailServerUseSSL    | if applicable for your server |       某些邮件服务器需要 ssl 通信。默认设置为 false。        |
|   mailServerUseTLS    | if applicable for your server | 某些邮件服务器（例如 gmail）需要 TLS 通信。默认设置为 false。 |

##### XML示例

电子邮件任务作为专用服务任务（Service Task）实现，并通过为服务任务类型设置“邮件”来定义。

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
        targetNamespace="邮件服务任务演示">

    <process id="email_service_task">
        <!--由于邮件服务是专有的服务任务 ，因此没有类似的emailTask-->
        <serviceTask id="sendMail" activiti:type="mail"/>
    </process>

</definitions>
```

Email 任务是通过字段注入配置的。这些属性的所有值都可以包含 EL 表达式，这些表达式在流程执行期间在运行时解析。可以设置以下属性：

|     **Property**      | **Required?** |                       **Description**                        |
| :-------------------: | :-----------: | :----------------------------------------------------------: |
|          to           |      yes      |   收件人如果是电子邮件。在逗号分隔的列表中定义了多个收件人   |
|         from          |      no       |  发件人电子邮件地址。如果未提供，则使用从地址配置的默认值。  |
|        subject        |      no       |                       电子邮件的主题。                       |
|          cc           |      no       |      电子邮件的抄送。在逗号分隔的列表中定义了多个收件人      |
|          bcc          |      no       |    电子邮件的密件抄送。在逗号分隔的列表中定义了多个收件人    |
|        charset        |      no       |    允许更改电子邮件的字符集，这是许多非英语语言所必需的。    |
|         html          |      no       |                 一段 HTML 是电子邮件的内容。                 |
|         text          |      no       | 电子邮件的内容，以防万一需要发送普通的非丰富电子邮件。可与html结合使用，用于不支持富内容的电子邮件客户端。然后，客户端将回退到这种纯文本替代方案。 |
|        htmlVar        |      no       | 保存作为电子邮件内容的 HTML 的流程变量的名称。这与 html 之间的主要区别在于，此内容在被邮件任务发送之前将替换表达式。 |
|        textVar        |      no       | 保存电子邮件纯文本内容的流程变量的名称。这与 html 之间的主要区别在于，此内容在被邮件任务发送之前将替换表达式。 |
|    ignoreException    |      no       | 处理电子邮件时的失败是否会引发 ActivitiException。默认情况下，这设置为 false。 |
| exceptionVariableName |      no       | 当电子邮件处理不抛出异常时，因为 ignoreException = true 具有给定名称的变量用于保存失败消息 |

##### 用法示例

以下 XML 片段为使用电子邮件任务的示例。

```xml
<!--用法示例-->
<serviceTask id="sendMail2" activiti:type="mail">
    <extensionElements>
        <activiti:field name="from" stringValue="order-shipping@thecompany.com"/>
        <activiti:field name="to" expression="${recipient}"/>
        <activiti:field name="subject" expression="Your order ${orderId} has been shipped"/>
        <activiti:field name="html">
            <activiti:expression>
                <![CDATA[
  <html>
    <body>
      Hello ${male ? 'Mr.' : 'Mrs.' } ${recipientName},<br/><br/>
      As of ${now}, your order has been <b>processed and shipped</b>.<br/><br/>
      Kind regards,<br/>
      TheCompany.
    </body>
  </html>
]]>
            </activiti:expression>
        </activiti:field>
    </extensionElements>
</serviceTask>
```

结果如下：

![](http://rep.shaoteemo.com/email.task.result.png)

#### 7.Mule任务（Mule Task）

##### 描述

mule 任务允许向 Mule 发送消息以增强 Activiti 的集成功能。请注意，mule 任务不是 BPMN 2.0 规范的官方任务（因此它没有专用图标）。因此，在 Activiti 中，mule 任务被实现为一个专用的**服务任务（Service Task）**。

##### XML示例

Mule 任务被实现为一个专用的服务任务，并通过为服务任务的类型设置 'mule' 来定义。

```xml
 <!--mule任务定义-->
 <serviceTask id="sendMule" activiti:type="mule"/>
```

Mule 任务通过字段注入进行配置。这些属性的所有值都可以包含 EL 表达式，这些表达式在流程执行期间在运行时解析。可以设置以下属性：

| **Property**      | **Required?** | **Description**                           |
| ----------------- | ------------- | ----------------------------------------- |
| endpointUrl       | yes           | 要调用的 Mule 端点。                      |
| language          | yes           | 要用于评估 payloadExpression 字段的语言。 |
| payloadExpression | yes           | 作为消息负载的表达式。                    |
| resultVariable    | no            | 存储调用结果的变量的名称。                |

##### 用法示例

以下 XML 片段显示了使用 Mule 任务的示例。

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
        targetNamespace="Mule任务演示">

    <process id="mule_task">
        <!--mule任务定义-->
        <serviceTask id="sendMule" activiti:type="mule">
            <!--用法示例-->
            <extensionElements>
                <activiti:field name="endpointUrl">
                    <activiti:string>vm://in</activiti:string>
                </activiti:field>
                <activiti:field name="language">
                    <activiti:string>juel</activiti:string>
                </activiti:field>
                <activiti:field name="payloadExpression">
                    <activiti:string>"hi"</activiti:string>
                </activiti:field>
                <activiti:field name="resultVariable">
                    <activiti:string>theVariable</activiti:string>
                </activiti:field>
            </extensionElements>
        </serviceTask>
    </process>

</definitions>
```

#### 8.Camel任务（Camel Task）

##### 描述

Camel 任务允许向 Camel 发送消息和从 Camel 接收消息，从而增强了 Activiti 的集成功能。请注意，Camel 任务不是 BPMN 2.0 规范的官方任务（因此它没有专用图标）。因此，在 Activiti 中，Camel 任务是作为专用**服务任务（Service Task）**实现的。还要注意在您的项目中包含 Activiti Camel 模块以使用 Camel 任务功能。

##### XML示例

Camel 任务作为专用服务任务实现，并通过为服务任务类型设置“camel”来定义。

```xml
<!--Camel任务定义-->
<serviceTask id="sendCamel" activiti:type="camel"/>
```

除了服务任务上的Camel类型定义之外，流程定义本身不需要其他任何东西。集成逻辑全部委托给 Camel 容器。默认情况下，Activiti 引擎会在 Spring 容器中查找 camelContext bean。camelContext bean 定义将由 Camel 容器加载的 Camel 路由。在以下示例中，路由是从特定的 Java 包加载的，但您也可以直接在 Spring 配置本身中定义路由。

```xml
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
    <packageScan>
        <package>org.activiti.camel.route</package>
    </packageScan>
</camelContext>
```

有关 Camel 路线的更多文档，您可以查看 [Camel网站](http://camel.apache.org/)。本文档中的一些小示例演示了基本概念。在第一个示例中，我们将从 Activiti 工作流中执行最简单的 Camel 调用形式。我们称之为 SimpleCamelCall。

如果你想定义多个 Camel 上下文 bean 并且（或者） 想使用不同的 bean 名称，这可以在 Camel 任务定义上被覆盖，如下所示：

```xml
<!--使用不同的Bean名称-->
<serviceTask id="serviceTask1" activiti:type="camel">
    <extensionElements>
        <activiti:field name="camelContext" stringValue="customCamelContext" />
    </extensionElements>
</serviceTask>
```

##### 简单的Camel调用示例

与本示例相关的所有文件都可以在 activiti-camel 模块的 org.activiti.camel.examples.simpleCamelCall 包中找到。目标只是激活特定的camel路由。首先，我们需要一个 Spring 上下文，其中包含对前面提到的路由的介绍。下面的代码用于此需求：

```xml
<camelContext id="camelContext" xmlns="http://camel.apache.org/schema/spring">
    <packageScan>
        <package>org.activiti.camel.examples.simpleCamelCall</package>
    </packageScan>
</camelContext>
```

```java
public class SimpleCamelCallRoute extends RouteBuilder {

  @Override
  public void configure() throws Exception {
    from("activiti:SimpleCamelCallProcess:simpleCall").to("log:org.activiti.camel.examples.SimpleCamelCall");
  }
}
```

该路由仅记录消息正文，仅此而已。注意 from 端点的格式。它由三部分组成：

| **Endpoint Url Part**  | **Description**         |
| ---------------------- | ----------------------- |
| activiti               | Activiti 端点           |
| SimpleCamelCallProcess | 流程名称                |
| simpleCall             | 流程中 Camel 服务的名称 |

Ok，我们的路由现在已经正确配置并且可以访问Camel。现在是工作流程部分。工作流程如下所示：

```xml
<!--简单的调用示例-->
<process id="SimpleCamelCallProcess">
    <startEvent id="start"/>
    <sequenceFlow id="flow1" sourceRef="start" targetRef="simpleCall"/>
    <serviceTask id="simpleCall" activiti:type="camel"/>
    <sequenceFlow id="flow2" sourceRef="simpleCall" targetRef="end"/>
    <endEvent id="end"/>
</process>
```

##### Ping Pong 示例

我们的例子可以使用，但在 Camel 和 Activiti 之间没有任何真正的转移，而且没有多少优点。在这个例子中，我们尝试向 Camel 发送和接收数据。我们发送一个字符串，Camel将一些东西连接到它并返回结果。发送者部分是微不足道的，我们以变量的形式将我们的消息发送到 Camel Task。这是我们的调用方代码：

```java
@Deployment
public void testPingPong() {
  Map<String, Object> variables = new HashMap<String, Object>();

  variables.put("input", "Hello");
  Map<String, String> outputMap = new HashMap<String, String>();
  variables.put("outputMap", outputMap);

  runtimeService.startProcessInstanceByKey("PingPongProcess", variables);
  assertEquals(1, outputMap.size());
  assertNotNull(outputMap.get("outputValue"));
  assertEquals("Hello World", outputMap.get("outputValue"));
}
```

变量“input”实际上是 Camel 路由的输入，而 outputMap 用于从 Camel 捕获结果。这个流程应该是这样的：

```xml
<!--Ping-Pong案例-->
<process id="PingPongProcess">
    <startEvent id="start"/>
    <sequenceFlow id="flow1" sourceRef="start" targetRef="ping"/>
    <serviceTask id="ping" activiti:type="camel"/>
    <sequenceFlow id="flow2" sourceRef="ping" targetRef="saveOutput"/>
    <serviceTask id="saveOutput"  activiti:class="org.activiti.camel.examples.pingPong.SaveOutput" />
    <sequenceFlow id="flow3" sourceRef="saveOutput" targetRef="end"/>
    <endEvent id="end"/>
</process>
```

请注意，SaveOutput 服务任务将“Output”变量的值从上下文存储到前面提到的 OutputMap。现在我们必须知道变量是如何发送到 Camel 并返回的。这里引入了Camel行为的概念。变量与 Camel 通信的方式可通过 CamelBehavior 进行配置。在这里，我们在示例中使用一个 Default ，然后是其他一些的简短描述。使用这样的代码，您可以配置所需的Camel行为：

```xml
<!--用于配置Camel的行为配置-->
<serviceTask id="serviceTask1" activiti:type="camel">
    <extensionElements>
        <activiti:field name="camelBehaviorClass" stringValue="org.activiti.camel.impl.CamelBehaviorCamelBodyImpl" />
    </extensionElements>
</serviceTask>
```

如果您不指定特定行为，则 org.activiti.camel.impl.CamelBehaviorDefaultImpl 将被设置。此行为将变量复制到同名的 Camel 属性。作为回报，无论选择的行为如何，如果Camel消息体是一个映射，那么它的每个元素都被复制为一个变量，否则整个对象被复制到一个名为“camelBody”的特定变量中。知道这一点，这条Camel路由结束了我们的第二个例子：

```java
@Override
public void configure() throws Exception {
  from("activiti:PingPongProcess:ping").transform().simple("${property.input} World");
}
```

在此路由中，字符串“world”连接到名为“input”的属性的末尾，结果将在消息正文中。可以通过在 java 服务任务中检查“camelBody”变量并复制到“outputMap”并在测试用例中检查来访问它。现在关于其默认行为的示例有效，让我们看看其他可能性是什么。在启动每个Camel路由时，流程实例 ID 将被复制到具有特定名称“PROCESS_ID_PROPERTY”的Camel属性中。它稍后用于关联流程实例和Camel路由。它也可以在Camel路由中被利用。

Activiti 中已经提供了三种不同的开箱即用的行为。该行为可以被路由 URL 中的特定短语覆盖。以下是覆盖 URL 中已定义行为的示例：

```java
from("activiti:asyncCamelProcess:serviceTaskAsync2?copyVariablesToProperties=true").
```

下表概述了三种可用的Camel行为：

| **Behaviour**              | In Url                    | Description                                            |
| -------------------------- | ------------------------- | ------------------------------------------------------ |
| CamelBehaviorDefaultImpl   | copyVariablesToProperties | 将 Activati 变量复制为 Camel 属性                      |
| CamelBehaviorCamelBodyImpl | copyCamelBodyToBody       | 仅复制名为“camelBody”的 Activiti 变量作为Camel消息正文 |
| CamelBehaviorBodyAsMapImpl | copyVariablesToBodyAsMap  | 将映射中的所有 Activiti 变量复制为 Camel 消息正文      |

上表解释了 Activiti 变量将如何转移到 Camel。下表解释了如何将 Camel 变量返回给 Activiti。这只能在路由 URL 中配置。

| Url                         | Description                                                  |
| --------------------------- | ------------------------------------------------------------ |
| Default                     | 如果 Camel body 是映射，则将每个元素复制为 Activiti 变量，否则将整个 Camel body 复制为 "camelBody" Activiti 变量 |
| copyVariablesFromProperties | 将 Camel 属性复制为同名的 Activiti 变量                      |
| copyCamelBodyToBodyAsString | 和默认一样，但是如果camel Body不是map，先把它转成String，然后复制到“camelBody”中 |
| copyVariablesFromHeader     | 另外将Camel头文件复制到同名的 Activiti 变量                  |

##### 变量返回

上面提到的关于传递变量的内容仅适用于变量传递的起始端，从 Camel 到 Activiti 的两个方向，反之亦然。

需要注意的是，由于 Activiti 的特殊非阻塞行为，变量不会从 Activiti 自动返回给 Camel。为此，可以使用特殊语法。Camel 路由 URL 中可以有一个或多个参数，格式为 var.return.someVariableName。所有名称等于这些参数之一而没有 var.return 部分的变量将被视为输出变量，并将作为具有相同名称的Camel属性复制回来。

例如在像这样的路由中：

```java
from("direct:start").to("activiti:process?var.return.exampleVar").to("mock:result");
```

名为exampleVar 的Activiti 变量将被视为输出变量，并将作为camel 中具有相同名称的属性复制回来。

##### 异步Ping Pong示例

之前的示例都是同步的。工作流停止，直到Camel路线结束并返回。在某些情况下，我们可能需要 Activiti 工作流程才能继续。为此，Camel 服务任务的异步功能非常有用。您可以通过将 Camel 服务任务的 async 属性设置为 true 来使用此功能。

```xml
<!--异步Camel-->
<serviceTask id="serviceAsyncPing" activiti:type="camel" activiti:async="true"/>
```

通过设置这个特性，指定的 Camel 路由被 Activiti 作业执行器异步激活。当您在 Camel 路由中定义队列时，Activiti 流程将继续执行 Camel 服务任务之后的活动。Camel 路由将与流程执行完全异步执行。如果您想在流程定义中的某处等待 Camel 服务任务的响应，您可以使用接收任务。

```xml
<!--接受异步camel消息-->
<receiveTask id="receiveAsyncPing" name="Wait State" />
```

流程实例将等待直到接收到信号，例如来自 Camel 的信号。在 Camel 中，您可以通过向适当的 Activiti 端点发送消息来向流程实例发送信号。

```java
from("activiti:asyncPingProcess:serviceAsyncPing").to("activiti:asyncPingProcess:receiveAsyncPing");
```

- 常量字符串“activiti”
- 流程名称
- 接收任务名称

##### 从Camel路由实例化工作流

在我们之前的所有示例中，Activiti 工作流首先启动，Camel 路线在工作流中启动。从另一边也可以。工作流可能是从已经启动的Camel路由实例化的。它与信令接收任务非常相似，只是没有最后一部分。这是一个示例路线：

```java
from("direct:start").to("activiti:camelProcess");
```

如您所见，url 有两个部分，第一个是常量字符串“activiti”，第二个名称是进程的名称。显然，该流程应该已经通过引擎配置部署和启动。

还可以将进程的发起者设置为 Camel 标头中提供的某个经过身份验证的用户 ID。要实现这一点，首先必须在流程定义中指定一个启动器变量：

```xml
<startEvent id="start" activiti:initiator="initiator" />
```

然后假设用户 ID 包含在名为 CamelProcessInitiatorHeader 的 Camel 标头中，Camel 路由可以定义如下：

```java
from("direct:startWithInitiatorHeader")
    .setHeader("CamelProcessInitiatorHeader", constant("kermit"))
    .to("activiti:InitiatorCamelCallProcess?processInitiatorHeaderName=CamelProcessInitiatorHeader");
```

#### 9.手动任务（Manual Task）

##### 描述

手动任务定义了 BPM 引擎外部的任务。它用于对某人完成的工作进行建模，引擎不需要知道这些工作，也不需要系统或 UI 接口。对于引擎，手动任务作为传递活动处理，从流程执行到达的那一刻起自动继续流程。（译注：直接通过的任务。）

##### 图形示例

手动任务被可视化为一个圆角矩形，左上角有一个小手形图标

![](http://rep.shaoteemo.com/bpmn.manual.task.png)

##### XML示例

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
        targetNamespace="手动任务演示">

    <process id="manual_task">
        <manualTask id="myManualTask" name="Call client for more information" />
    </process>

</definitions>
```

#### 10.Java接收任务（Java Receive Task）

##### 描述

接收任务是一个简单的任务，它等待某个消息的到来。目前，我们仅为此任务实现了 Java 语义。当流程执行到达接收任务时，流程状态被提交到持久性存储。这意味着进程将保持在此等待状态，直到引擎接收到特定消息，这会触发进程继续执行接收任务。

##### 图形示例

接收任务可视化为左上角带有消息图标的任务（圆角矩形）。消息是白色的（黑色消息图标将具有发送语义）

![](http://rep.shaoteemo.com/bpmn.receive.task.png)

##### XML示例

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
        targetNamespace="java接收任务演示">

    <process id="java_receive_task">
        <receiveTask id="waitState" name="wait" />
    </process>

</definitions>
```

要继续当前正在等待此类接收任务的流程实例，必须使用到达接收任务的执行的 id 调用 runtimeService.signal(executionId)。下面的代码片段展示了这在实践中是如何工作的：

```java
ProcessInstance pi = runtimeService.startProcessInstanceByKey("receiveTask");
Execution execution = runtimeService.createExecutionQuery()
  .processInstanceId(pi.getId())
  .activityId("waitState")
  .singleResult();
assertNotNull(execution);

runtimeService.signal(execution.getId());
```

#### 11.Shell任务（Shell Task）

##### 描述

shell 任务允许运行 shell 脚本和命令。请注意，Shell 任务不是 BPMN 2.0 规范的正式任务（因此它没有专用图标）。

##### XML示例

shell 任务被实现为一个专用的**服务任务（Service Task）**，并通过为服务任务的类型设置“shell”来定义。

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
        targetNamespace="Shell任务演示">

    <process id="shell_task">
        <serviceTask id="shellEcho" activiti:type="shell"/>
    </process>

</definitions>
```

Shell 任务通过字段注入(见JavaServiceTask属性注入)进行配置。这些属性的所有值都可以包含 EL 表达式，这些表达式在流程执行期间在运行时解析。可以设置以下属性：

|     Property      | Required? |    Type    |               Description               |            Default             |
| :---------------: | :-------: | :--------: | :-------------------------------------: | :----------------------------: |
|      command      |    yes    |   String   |          要执行的 Shell 命令。          |               -                |
|      arg0-5       |    no     |   String   |             参数 0 到参数 5             |               -                |
|       wait        |    no     | true/false | 如有必要，请等待，直到 shell 进程终止。 |              true              |
|   redirectError   |    no     | true/false |       将标准错误与标准输出合并。        |             false              |
|     cleanEnv      |    no     | true/false |       Shell 进程不继承当前环境。        |             false              |
|  outputVariable   |    no     |   String   |           包含输出的变量名称            |       包含输出的变量名称       |
| errorCodeVariable |    no     |   String   |       包含结果错误代码的变量名称        | Error level is not registered. |
|     directory     |    no     |   String   |           shell进程的默认目录           |            当前目录            |

##### 用法示例

以下 XML 片段显示了使用 shell 任务的示例。它运行 shell 脚本“cmd /c echo EchoTest”，等待它被终止并将结果放入 resultVar

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
        targetNamespace="Shell任务演示">

    <process id="shell_task">
        <!--一个简单的用法示例-->
        <serviceTask id="shellEcho" activiti:type="shell" >
            <extensionElements>
                <activiti:field name="command" stringValue="cmd" />
                <activiti:field name="arg1" stringValue="/c" />
                <activiti:field name="arg2" stringValue="echo" />
                <activiti:field name="arg3" stringValue="EchoTest" />
                <activiti:field name="wait" stringValue="true" />
                <activiti:field name="outputVariable" stringValue="resultVar" />
            </extensionElements>
        </serviceTask>
    </process>

</definitions>
```

#### 12.Execution监听器（Execution Listener）

**兼容性说明：**5.3 发布后，我们发现执行监听器和任务监听器和表达式仍然在非公共 API 中。这些类位于 org.activiti.engine.impl… 的子包中，其中包含 impl）。org.activiti.engine.impl.pvm.delegate.ExecutionListener, org.activiti.engine.impl.pvm.delegate.TaskListener 和 org.activiti.engine.impl.pvm.el.Expression 已被弃用。从现在开始，你应该使用 org.activiti.engine.delegate.ExecutionListener、org.activiti.engine.delegate.TaskListener 和 org.activiti.engine.delegate.Expression。在新的公开可用 API 中，对 ExecutionListenerExecution.getEventSource() 的访问已被删除。除了弃用编译器警告之外，现有代码应该可以正常运行。但考虑切换到新的公共 API 接口（包名称中没有 .impl.）。

执行侦听器允许您在流程执行期间发生某些事件时执行外部 Java 代码或评估表达式。可以捕获的事件有：

- 流程实例的开始和结束。
- 采取过渡。（原文：Taking a transition.）
- 活动的开始和结束。
- 网关的开始和结束。
- 中间事件的开始和结束。
- 结束->开始事件或开始->结束事件。（原文：Ending a start event or starting an end event.）

以下流程定义包含 3 个执行监听器：

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
        targetNamespace="Execution监听器">
    <!--案例-->
    <process id="executionListenersProcess">

        <extensionElements>
            <activiti:executionListener class="org.activiti.examples.bpmn.executionlistener.ExampleExecutionListenerOne" event="start" />
        </extensionElements>

        <startEvent id="theStart" />
        <sequenceFlow sourceRef="theStart" targetRef="firstTask" />

        <userTask id="firstTask" />
        <sequenceFlow sourceRef="firstTask" targetRef="secondTask">
            <extensionElements>
                <activiti:executionListener class="org.activiti.examples.bpmn.executionListener.ExampleExecutionListenerTwo" />
            </extensionElements>
        </sequenceFlow>

        <userTask id="secondTask" >
            <extensionElements>
                <activiti:executionListener expression="${myPojo.myMethod(execution.event)}" event="end" />
            </extensionElements>
        </userTask>
        <sequenceFlow sourceRef="secondTask" targetRef="thirdTask" />

        <userTask id="thirdTask" />
        <sequenceFlow sourceRef="thirdTask" targetRef="theEnd" />

        <endEvent id="theEnd" />

    </process>

</definitions>
```

第一个执行监听器在进程启动时收到通知。侦听器是一个外部 Java 类（如 ExampleExecutionListenerOne）并且应该实现 org.activiti.engine.delegate.ExecutionListener 接口。当事件发生（在本例中为结束事件（译者：？是不是有什么不对的地方。不应该是开始吗？））时，将调用方法 notify(ExecutionListenerExecution execution)。

```java
public class ExampleExecutionListenerOne implements ExecutionListener {

  public void notify(ExecutionListenerExecution execution) throws Exception {
    execution.setVariable("variableSetInExecutionListener", "firstValue");
    execution.setVariable("eventReceived", execution.getEventName());
  }
}
```

也可以使用实现 org.activiti.engine.delegate.JavaDelegate 接口的委托类。然后可以在其他构造中重用这些委托类，例如 serviceTask 的委托。

当进行转换时调用第二个执行监听器。请注意，监听器元素没有定义事件，因为在转换时只会触发 take 事件。**在转换上定义监听器时，event属性中的值将被忽略。**

当活动 secondTask 结束时调用最后一个执行监听器。不是在监听器声明中使用该类，而是定义了一个表达式，该表达式在触发事件时被 evaluated/invoked。

```xml
<activiti:executionListener expression="${myPojo.myMethod(execution.eventName)}" event="end" />
```

与其他表达式一样，执行变量已解析并可使用。因为执行实现对象有一个公开事件名称的属性，所以可以使用 execution.eventName 将事件名称传递给您的方法。

执行侦听器还支持使用**delegateExpression**，类似于服务任务。

```xml
<activiti:executionListener event="start" delegateExpression="${myExecutionListenerBean}" />
```

在 Activiti 5.12 中，我们还引入了一种新类型的执行监听器，org.activiti.engine.impl.bpmn.listener.ScriptExecutionListener。此脚本执行监听器允许您为执行监听器事件执行一段脚本逻辑。

```xml
<!--Activiti 5.12 新的脚本监听器允许执行脚本-->
<extensionElements>
    <activiti:executionListener event="start" class="org.activiti.engine.impl.bpmn.listener.ScriptExecutionListener" >
        <activiti:field name="script">
            <activiti:string>
                def bar = "BAR";  // local variable
                foo = "FOO"; // pushes variable to execution context
                execution.setVariable("var1", "test"); // test access to execution instance
                bar // implicit return value
            </activiti:string>
        </activiti:field>
        <activiti:field name="language" stringValue="groovy" />
        <activiti:field name="resultVariable" stringValue="myVar" />
    </activiti:executionListener>
</extensionElements>
```

##### 执行监听器上的字段注入

使用配置了 class 属性的执行侦听器时，可以应用于字段注入。这与使用的 Service Task字段注入机制完全相同，其中包含对字段注入提供的可能性的概述。

下面的片段显示了一个简单的示例流程，其中包含一个注入了字段的执行监听器。

```xml
<!--为监听器注入属性-->
<process id="executionListenersProcess">
    <extensionElements>
        <activiti:executionListener class="org.activiti.examples.bpmn.executionListener.ExampleFieldInjectedExecutionListener" event="start">
            <activiti:field name="fixedValue" stringValue="Yes, I am " />
            <activiti:field name="dynamicValue" expression="${myVar}" />
        </activiti:executionListener>
    </extensionElements>
    <startEvent id="theStart" />
    <sequenceFlow sourceRef="theStart" targetRef="firstTask" />
    <userTask id="firstTask" />
    <sequenceFlow sourceRef="firstTask" targetRef="theEnd" />
    <endEvent id="theEnd" />
</process>
```

```java
public class ExampleFieldInjectedExecutionListener implements ExecutionListener {

  private Expression fixedValue;

  private Expression dynamicValue;

  public void notify(ExecutionListenerExecution execution) throws Exception {
    execution.setVariable("var", fixedValue.getValue(execution).toString() + dynamicValue.getValue(execution).toString());
  }
}
```

ExampleFieldInjectedExecutionListener 类连接了 2 个注入的字段（一个是固定的，另一个是动态的）并将其存储在流程变量 var 中。

```java
@Deployment(resources = {"org/activiti/examples/bpmn/executionListener/ExecutionListenersFieldInjectionProcess.bpmn20.xml"})
public void testExecutionListenerFieldInjection() {
  Map<String, Object> variables = new HashMap<String, Object>();
  variables.put("myVar", "listening!");

  ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("executionListenersProcess", variables);

  Object varSetByListener = runtimeService.getVariable(processInstance.getId(), "var");
  assertNotNull(varSetByListener);
  assertTrue(varSetByListener instanceof String);

  // Result is a concatenation of fixed injected field and injected expression
  assertEquals("Yes, I am listening!", varSetByListener);
}
```

请注意，有关线程安全的相同规则适用于服务任务。请阅读相关部分以获取更多信息（Java Service Task）。

#### 13.任务监听器（Task Listener）

任务监听器用于在发生某个与任务相关的事件时执行自定义 Java 逻辑或表达式。

任务监听器**只能作为用户任务**的子元素添加到流程定义中。请注意，这也必须作为 BPMN 2.0 extensionElements 和 activiti 命名空间的子项发生，因为任务监听器是 Activiti 特定的构造。

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
        targetNamespace="任务监听器演示">

    <process id="task_listener">
        <userTask id="myTask" name="My Task" >
            <!--定义任务监听器-->
            <extensionElements>
                <activiti:taskListener event="create" class="org.activiti.MyTaskCreateListener" />
            </extensionElements>
        </userTask>
    </process>

</definitions>
```

任务监听器支持以下属性：

- **event（必需）：**将在其上调用任务侦听器的任务事件的类型。可能的事件是
  - **create：**在创建任务并设置所有任务属性时发生。
  - **assignment**: 当任务分配给某人时发生。注意：当流程执行到达用户任务时，首先会触发分配事件，然后再触发创建事件。这似乎是一个不自然的顺序，但原因是务实的：在接收 create 事件时，我们通常希望检查任务的所有属性，包括受让人。
  - **complete**: 在任务完成时和任务从运行时数据中删除之前发生。
  - **delete**:发生在任务将被删除之前。请注意，它也会在通过 completeTask 正常完成任务时执行。
- **class：**必须调用的委托类。这个类必须实现 org.activiti.engine.delegate.TaskListener 接口。

```java
public class MyTaskCreateListener implements TaskListener {

  public void notify(DelegateTask delegateTask) {
    // Custom logic goes here
  }

}
```

也可以使用字段注入将流程变量或执行传递给委托类。请注意，委托类的实例是在流程部署时创建的（就像 Activiti 中的任何类委托一样），这意味着该实例在所有流程实例执行之间共享。

- **expression**: (不能与class属性一起使用)：指定事件发生时将执行的表达式。可以将 DelegateTask 对象和事件的名称（使用 task.eventName）作为参数传递给被调用的对象。

  ```xml
  <!--表达式-->
  <activiti:taskListener event="create" expression="${myObject.callMethod(task, task.eventName)}" />
  ```

- **delegateExpression** 允许指定解析为实现 TaskListener 接口的对象的表达式，类似于服务任务。

  ```xml
  <!--委托表达式-->
  <activiti:taskListener event="create" delegateExpression="${myTaskListenerBean}" />
  ```

- 在 Activiti 5.12 中，我们还引入了一种新类型的任务监听器，org.activiti.engine.impl.bpmn.listener.ScriptTaskListener。此脚本任务侦听器允许您为任务侦听器事件执行一段脚本逻辑。

  ```xml
  <!--Activiti 5.12 新的脚本监听器的使用-->
  <activiti:taskListener event="complete" class="org.activiti.engine.impl.bpmn.listener.ScriptTaskListener" >
      <activiti:field name="script">
          <activiti:string>
              def bar = "BAR";  // local variable
              foo = "FOO"; // pushes variable to execution context
              task.setOwner("kermit"); // test access to task instance
              bar // implicit return value
          </activiti:string>
      </activiti:field>
      <activiti:field name="language" stringValue="groovy" />
      <activiti:field name="resultVariable" stringValue="myVar" />
  </activiti:taskListener>
  ```

#### 14.多实例(for each)（Multi-instance (for each)）

##### 描述

多实例活动是为业务流程中的某个步骤定义重复的一种方式。在编程概念中，多实例匹配 for each 构造：它允许为给定集合中的每个项目依次或并行执行某个步骤甚至完整的子流程。

多实例是一个常规活动，它定义了额外的属性（所谓的“多实例特征”），这将导致活动在运行时多次执行。以下活动可以成为多实例活动：

- User Task
- Script Task
- Java Service Task
- Web Service Task]
- Business Rule Task
- Email Task
- Manual Task
- Receive Task
- (Embedded) Sub-Process
- Call Activity

网关或事件不能成为多实例。

根据规范的要求，为每个实例创建的执行的每个父执行将具有以下变量：

- **nrOfInstances**: 实例总数
- **nrOfActiveInstances**: 当前活动（即尚未完成）实例的数量。对于顺序多实例，这将始终为 1。
- **nrOfCompletedInstances**:已经完成的实例数。

这些值可以通过调用 execution.getVariable(x) 方法来检索。

此外，每个创建的执行都将有一个执行局部变量（execution-local variable）（即对其他执行不可见，并且不存储在流程实例级别）：

- loopCounter：指示该特定实例的 for-each 循环中的索引。 loopCounter 变量可以通过 Activiti elementIndexVariable 属性重命名。

##### 图形示例

如果活动是多实例，则在该活动底部用三条短线表示。三条垂直线表示实例将并行执行，而三条水平线表示顺序执行。

![](http://rep.shaoteemo.com/bpmn.multi.instance.png)

##### XML示例

要使活动成为多实例，活动 xml 元素必须具有 multiInstanceLoopCharacteristics 子元素。

```xml
<!--多实例定义-->
<multiInstanceLoopCharacteristics isSequential="false|true">
    ...
</multiInstanceLoopCharacteristics>
```

sSequential 属性指示该活动的实例是按顺序执行还是并行执行。

实例数在**进入活动时计算一次**。有几种配置方法。方法是直接指定一个数字，通过使用 loopCardinality 子元素。

```xml
<!--多实例定义-->
<multiInstanceLoopCharacteristics isSequential="false|true">
    <!--次数-->
    <loopCardinality>5</loopCardinality>
</multiInstanceLoopCharacteristics>
```

解析为正数的表达式也是可能的：

```xml
<multiInstanceLoopCharacteristics isSequential="false|true">
    <!--正则表示次数-->
    <loopCardinality>${nrOfOrders-nrOfCancellations}</loopCardinality>
</multiInstanceLoopCharacteristics>
```

定义实例数量的另一种方法是使用 loopDataInputRef 子元素指定作为集合的流程变量的名称。对于集合中的每个项目，将创建一个实例。或者，可以使用 inputDataItem 子元素为实例设置集合的特定项目。这在以下 XML 示例中显示：

```xml
<userTask id="miTasks" name="My Task ${loopCounter}" activiti:assignee="${assignee}">
    <multiInstanceLoopCharacteristics isSequential="false">
        <loopDataInputRef>assigneeList</loopDataInputRef>
        <inputDataItem name="assignee" />
    </multiInstanceLoopCharacteristics>
</userTask>
```

假设变量assigneeList 包含值\[kermit, gonzo, fozzie\]。在上面的代码片段中，将并行创建三个用户任务。每个执行都将有一个名为assignee 的流程变量，其中包含集合的一个值，在本示例中用于分配用户任务。

loopDataInputRef 和 inputDataItem 的缺点是 1) 名称很难记住 2) 由于 BPMN 2.0 模式限制，它们不能包含表达式。Activiti 通过在 multiInstanceCharacteristics 上提供 collection 和 elementVariable 属性来解决这个问题：

```xml
<!--上面的另一种简单易记支持表达式的方式-->
<userTask id="miTasks2" name="My Task" activiti:assignee="${assignee}">
    <multiInstanceLoopCharacteristics isSequential="true"
                                      activiti:collection="${myService.resolveUsersForTask()}" activiti:elementVariable="assignee" >
    </multiInstanceLoopCharacteristics>
</userTask>
```

当所有实例都完成时，多实例活动结束。但是，可以指定每次实例结束时计算的表达式。当这个表达式的计算结果为真时，所有剩余的实例都会被销毁，多实例活动结束，继续这个流程。这样的表达式必须在 completionCondition 子元素中定义。

```xml
<!--控制多实例结束-->
<userTask id="miTasks3" name="My Task" activiti:assignee="${assignee}">
    <multiInstanceLoopCharacteristics isSequential="false"
                                      activiti:collection="assigneeList" activiti:elementVariable="assignee" >
        <completionCondition>${nrOfCompletedInstances/nrOfInstances >= 0.6 }</completionCondition>
    </multiInstanceLoopCharacteristics>
</userTask>
```

在此示例中，将为assigneeList 集合的每个元素创建并行实例。但是，当完成 60% 的任务时，将删除其他任务并继续该流程。

##### 边界事件和多实例

由于多实例是常规活动，因此可以在其边界上定义边界事件。在中断边界事件的情况下，当事件被捕获时，所有仍处于活动状态的实例将被销毁。以下面的多实例子流程为例：

![](http://rep.shaoteemo.com/bpmn.multi.instance.boundary.event.png)

在这里，当计时器触发时，子流程的所有实例都将被销毁，无论有多少实例或当前尚未完成哪些内部活动。

##### 多实例和执行监听器

（适用于 Activiti 5.18 及更高版本）

将执行监听器与多实例结合使用时有一个警告。以下面的 BPMN 2.0 xml 片段为例，它定义在与设置 multiInstanceLoopCharacteristics xml 元素相同的级别上：

```xml
<!--多实例监听器-->
<userTask>
    <extensionElements>
        <activiti:executionListener event="start" class="org.activiti.MyStartListener"/>
        <activiti:executionListener event="end" class="org.activiti.MyEndListener"/>
    </extensionElements>
</userTask>
```

对于正常的 BPMN 活动，在活动开始和结束时会调用这些监听器。

但是，当活动是多实例时，行为是不同的：

- 当进入多实例活动时，在执行任何内部活动之前，会抛出一个开始事件。 loopCounter 变量尚未设置（为空）。
- 对于访问的每个实际活动，都会抛出一个开始事件。设置了 loopCounter 变量。

同样的推理适用于结束事件：

- 当实际活动结束时，会抛出一个结束偶数。设置了 loopCounter 变量。
- 当多实例活动作为一个整体完成时，会抛出一个结束事件。未设置 loopCounter 变量。

例如：

```xml
<!--一个关于多实例监听器案例-->
<subProcess id="subprocess1" name="Sub Process">
    <extensionElements>
        <activiti:executionListener event="start" class="org.activiti.MyStartListener"/>
        <activiti:executionListener event="end" class="org.activiti.MyEndListener"/>
    </extensionElements>
    <multiInstanceLoopCharacteristics isSequential="false">
        <loopDataInputRef>assignees</loopDataInputRef>
        <inputDataItem name="assignee"></inputDataItem>
    </multiInstanceLoopCharacteristics>
    <startEvent id="startevent2" name="Start"></startEvent>
    <endEvent id="endevent2" name="End"></endEvent>
    <sequenceFlow id="flow3" name="" sourceRef="startevent2" targetRef="endevent2"></sequenceFlow>
</subProcess>
```

在此示例中，假设受理人列表包含三个项目。运行时会发生以下情况：

- 为整个多实例抛出一个开始事件。启动执行侦听器被调用。不会设置 loopCounter 和assignee 变量（即它们将为空）。
- 为每个活动实例抛出一个开始事件。启动执行侦听器被调用 3 次。将设置 loopCounter 或assignee 变量（即不同于null）。
- 因此，启动执行侦听器总共被调用了四次。

请注意，当 multiInstanceLoopCharacteristics 也定义在子进程之外的其他东西上时，这同样适用。例如，如果上面的例子是一个简单的 userTask，同样的推理仍然适用。

#### 15.补偿处理程序（Compensation Handlers）

##### 描述

[[EXPERIMENTAL\]](https://www.activiti.org/userguide/#experimental)

如果一个活动用于补偿另一个活动的影响，则可以将其声明为补偿处理程序。补偿处理程序不包含在正常流程中，仅在抛出补偿事件时执行。

补偿处理程序不得有传入或传出序列流。

补偿处理程序必须使用定向关联与补偿边界事件相关联。

##### 图形示例

如果活动是补偿处理程序，则补偿事件图标显示在中心底部区域。下面的流程图摘录显示了一个服务任务，它带有一个附加的补偿边界事件，该事件与补偿处理程序相关联。请注意“取消酒店预订”服务任务底部慢速区域中的补偿处理程序图标。

![](http://rep.shaoteemo.com/bpmn.boundary.compensation.event.png)

##### XML示例

为了将活动声明为补偿处理程序，我们需要将属性 isForCompensation 设置为 true：

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
        targetNamespace="补偿处理程序演示">

    <process id="compensation_handlers">
        <serviceTask id="undoBookHotel" isForCompensation="true" activiti:class="...">
        </serviceTask>
    </process>

</definitions>
```

### Sub-Processes and Call Activities

#### 1.子流程（Sub-Process）

##### 描述

子流程是包含其他活动、网关、事件等的活动。它本身形成一个过程，是更大过程的一部分。子进程完全定义在父进程内（这就是为什么它通常被称为嵌入式子进程）。 

子流程有两个主要用例：

- 子流程允许分层建模。许多建模工具允许折叠子流程，隐藏子流程的所有细节并显示业务流程的高级端到端概述。
- 子流程为事件创建新的范围。在子流程执行期间抛出的事件可以被子流程边界上的边界事件捕获，而为该事件创建一个仅限于子流程的范围。

使用子流程确实会施加一些限制：

- 一个子流程只能有一个非启动事件，不允许有其他启动事件类型。一个子流程必须至少有一个结束事件。请注意，BPMN 2.0 规范允许省略子流程中的开始和结束事件，但当前的 Activiti 实现不支持这一点。
- **序列流不能跨越子流程边界。**

##### 图形示例

一个子流程被可视化为一个典型的活动，即一个圆角矩形。如果子流程被折叠，则只显示名称和加号，提供流程的高级概述：

![](http://rep.shaoteemo.com/bpmn.collapsed.subprocess.png)

如果子流程被展开，子流程的步骤将显示在子流程边界内：

![](http://rep.shaoteemo.com/bpmn.expanded.subprocess.png)

使用子流程的主要原因之一是为某个事件定义范围。以下流程模型突出了这一点：*investigate software/investigate hardware*任务都需要并行完成，但是这两项任务都需要在咨询等级 2 支持之前的特定时间内完成。在这里，计时器的范围（即哪些活动必须及时完成）受子流程的约束。

![](http://rep.shaoteemo.com/bpmn.subprocess.with.boundary.timer.png)

##### XML示例

子流程由子流程元素定义。作为子流程一部分的所有活动、网关、事件等都需要包含在此元素中。

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
        targetNamespace="子流程演示">

    <process id="sub_process" name="sub_process">

        <!--定义一个子流程-->
        <subProcess id="subProcess">

            <startEvent id="subProcessStart" />

            <!--... other Sub-Process elements ...-->

            <endEvent id="subProcessEnd" />

        </subProcess>
    </process>
</definitions>
```

#### 2.事件子流程（Event Sub-Process）

##### 描述

事件子流程是 BPMN 2.0 中的新增内容。事件子流程是由事件触发的子流程。可以在流程级别或任何子流程级别添加事件子流程。用于触发事件子流程的事件是使用开始事件配置的。由此可知，事件子流程不支持任何启动事件。事件子流程可以使用消息事件、错误事件、信号事件、计时器事件或补偿事件等事件触发。当创建托管事件子流程的范围（流程实例或子流程）时，将创建对开始事件的订阅。当范围被销毁时，订阅被删除。

事件子流程可以是中断的或非中断的。中断子流程会取消当前范围内的任何执行。一个非中断的事件子流程产生一个新的并发执行。虽然中断事件子流程只能为托管它的范围的每次激活触发一次，但非中断事件子流程可以触发多次。子流程是否中断的事实是使用触发事件子流程的开始事件配置的。

事件子流程不得有任何传入或传出序列流。由于事件子流程是由事件触发的，因此传入的序列流没有意义。当事件子流程结束时，当前范围结束（在中断事件子流程的情况下），或者为非中断子流程产生的并发执行结束。

**当前限制：**

- Activiti 只支持中断事件子流程。
- Activiti 仅支持使用错误启动事件或消息启动事件触发的事件子流程。

##### 图形示例

一个事件子流程可能被可视化为一个带有虚线轮廓的嵌入式子流程。

![](http://rep.shaoteemo.com/bpmn.subprocess.eventSubprocess.png)

##### XML示例

事件子流程以与嵌入子流程相同的方式使用 XML 表示。此外，属性 triggerByEvent 必须具有值 true：

```xml
<!--事件子流程定义-->
<subProcess id="eventSubProcess" triggeredByEvent="true">
    <!--...-->
</subProcess>
```

##### 例子

以下是使用错误启动事件触发的事件子流程的示例。事件子流程位于“流程级别”，即范围为流程实例：

![](http://rep.shaoteemo.com/bpmn.subprocess.eventSubprocess.example.1.png)

这是事件子流程在 XML 中的样子：

```xml
<!--案例1的XML标识-->
<subProcess id="eventSubProcess2" triggeredByEvent="true">
    <startEvent id="catchError">
        <errorEventDefinition errorRef="error" />
    </startEvent>
    <sequenceFlow id="flow2" sourceRef="catchError" targetRef="taskAfterErrorCatch" />
    <userTask id="taskAfterErrorCatch" name="Provide additional data" />
</subProcess>
```

如前所述，事件子流程也可以添加到嵌入式子流程中。如果将其添加到嵌入式子流程中，它将成为边界事件的替代方案。考虑以下两个流程图。在这两种情况下，嵌入式子流程都会引发错误事件。两次都使用 用户任务捕获和处理错误。

![](http://rep.shaoteemo.com/bpmn.subprocess.eventSubprocess.example.2a.png)

与：

![](http://rep.shaoteemo.com/bpmn.subprocess.eventSubprocess.example.2b.png)

在这两种情况下，都会执行相同的任务。但是，两种建模方案之间存在差异：

- 嵌入的子流程使用与执行它所在相同的作用域来执行。这意味着嵌入式子流程可以访问其范围内的本地变量。当使用边界事件时，为执行嵌入的子流程而创建的执行被离开边界事件的序列流删除。这意味着由嵌入式子流程创建的变量不再可用。
- 使用事件子流程时，事件完全由添加到的子流程处理。使用边界事件时，事件由父进程处理。

这两个差异可以帮助您确定边界事件还是嵌入式子流程更适合解决特定的流程modeling / implementation问题。

#### 3.事务子流程（Transaction Sub Process）

[[EXPERIMENTAL\]](https://www.activiti.org/userguide/#experimental)

##### 描述

事务子流程是一个嵌入式子流程，可用于将多个活动分组到一个事务中。事务是一个逻辑工作单元，它允许对一组单独的活动进行分组，以便它们共同成功或失败。

事务的可能结果：一个事务可以有三种不同的结果：

- 事务成功，如果它既没有被取消也没有被异常终止。如果事务子流程成功，则使用传出序列流保留。如果在流程后期抛出补偿事件，则可能会对成功的事务进行补偿。注意：就像“ordinary”嵌入式子流程一样，可以使用中间抛出补偿事件在成功完成后对事务进行补偿。
- 如果执行到达取消结束事件，则事务取消。在这种情况下，所有执行都将终止并删除。然后将剩余的单个执行设置为取消边界事件，从而触发补偿。补偿完成后，使用取消边界事件的传出序列流离开事务子流程。
- 事务因异常导致结束，如果抛出错误事件，则不会在事务子流程的范围内捕获。（这也适用于在事务子流程的边界上捕获错误的情况。）在这种情况下，不执行补偿。

下图说明了三种不同的结果：

![](http://rep.shaoteemo.com/bpmn.transaction.subprocess.example.1.png)

与 ACID 事务的关系：不要将 bpmn 事务子流程与技术 (ACID) 事务混淆，这一点很重要。 bpmn 事务子流程不是一种确定技术事务范围的方法。为了理解 Activiti 中的事务管理，请阅读有关并发和事务的部分。bpmn 事务在以下方面与技术事务不同：

- 虽然 ACID 事务通常是短暂的，但 bpmn 事务可能需要数小时、数天甚至数月才能完成。（考虑按事务分组的活动之一是用户任务的情况，通常人们的响应时间比应用程序长。或者，在另一种情况下，bpmn 事务可能会等待某些业务事件发生，例如特定订单已完成的事实。）与更新数据库中的记录或使用事务队列存储消息相比，此类操作通常需要更长的时间才能完成。
- 因为不可能将技术事务的范围限定为业务活动的持续时间，所以 bpmn 事务通常跨越多个 ACID 事务。
- 由于 bpmn 事务跨越多个 ACID 事务，我们失去了 ACID 属性。例如，考虑上面给出的例子。让我们假设“book hotel”和“charge credit card”操作是在单独的 ACID 事务中执行的。我们还假设“book hotel”活动是成功的。现在我们有一个中间不一致的状态，因为我们已经执行了一次酒店预订但还没有从信用卡中扣款。现在，在 ACID 事务中，我们还将按顺序执行不同的操作，因此也会有一个中间不一致的状态。这里的不同之处在于不一致的状态在事务范围之外是可见的。例如，如果使用外部预订服务进行预订，使用相同预订服务的其他方可能已经看到酒店已被预订。这意味着，在实现业务事务时，我们完全失去了隔离属性（当然：在处理 ACID 事务时，我们通常也会放宽隔离以允许更高级别的并发性，但在那里我们有细粒度的控制，并且中间不一致只出现在很短的时间内）。
- 传统意义上的 bpmn 业务事务也不能回滚。由于它跨越多个 ACID 事务，其中一些 ACID 事务可能在 bpmn 事务被取消时已经提交。此时，它们无法再回滚。

由于 bpmn 事务本质上是长时间运行的，因此需要以不同的方式处理缺乏隔离和回滚机制。在实践中，通常没有比以特定领域的方式处理这些问题更好的解决方案：

- 使用补偿执行回滚。如果在事务范围内抛出取消事件，则所有成功执行并具有补偿处理程序的活动的影响都会得到补偿。
- 缺乏隔离也经常使用领域特定的解决方案来解决。例如，在上面的例子中，在我们真正确定第一个客户可以支付之前，酒店房间可能看起来是给第二个客户预订的。由于从业务角度来看这可能是不可取的，因此预订服务可能会选择允许一定数量的超额预订。
- 此外，由于事务可以在发生异常的情况下中止，因此预订服务必须处理预订了酒店房间但从未尝试付款的情况（因为交易已中止）。在这种情况下，预订服务可能会选择这样一种策略，即在最长时间内预订酒店房间，如果在此之前未收到付款，则预订将被取消。

总结一下：虽然 ACID 事务为此类问题（回滚、隔离级别和启发式结果）提供了通用解决方案，但在实现业务事务时，我们需要针对这些问题找到特定于领域的解决方案。

**当前限制：**

- BPMN 规范要求流程引擎对底层事务协议发出的事件做出反应，例如，如果底层协议中发生取消事件，则取消事务。作为一个可嵌入的引擎，Activiti 目前不支持这个。 （有关这方面的一些后果，请参阅下面关于一致性的段落。）

在 ACID 事务和乐观并发之上的一致性：bpmn 事务在某种意义上保证一致性，即所有活动都成功竞争，或者如果某些活动无法执行，则所有其他成功活动的影响都会得到补偿。因此，无论哪种方式，我们最终都会处于一致的状态。然而，重要的是要认识到，在 Activiti 中，bpmn 事务的一致性模型叠加在流程执行的一致性模型之上。Activiti 以事务的方式执行流程。使用乐观锁解决并发问题。在 Activiti 中，bpmn 错误、取消和补偿事件建立在相同的 acid 事务和乐观锁之上。在 Activiti 中，bpmn 错误、取消和补偿事件建立在相同的 acid 事务和乐观锁之上。例如，取消结束事件只有在实际到达时才能触发补偿。如果之前服务任务抛出了一些未声明的异常，则不会到达。或者，如果底层 ACID 事务中的某个其他参与者将事务设置为仅回滚状态，则无法提交补偿处理程序的效果。或者，当两个并发执行达到取消结束事件时，补偿可能会被触发两次并因乐观锁定异常而失败。所有这一切都是说，在 Activiti 中实现 bpmn 事务时，应用与实现“ordinary”流程和子流程相同的规则集。因此，为了有效地保证一致性，以一种确实考虑到乐观、事务执行模型的方式来实施流程是很重要的。

##### 图形示例

事务子流程可能被视为具有双重轮廓的嵌入式子流程。

![](http://rep.shaoteemo.com/bpmn.transaction.subprocess.png)

##### XML示例

事务子流程使用 xml 使用事务标记表示：

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
        targetNamespace="事务子流程演示">

    <process id="transaction_subProcess" name="transaction_subProcess">
        <transaction id="myTransaction" >
            <!--...-->
        </transaction>
    </process>
</definitions>
```

##### 案例

以下是事务子流程的示例：

![](http://rep.shaoteemo.com/bpmn.transaction.subprocess.example.2.png)

##### 本节概念扩展

1.悲观锁与乐观锁

2.乐观并发与悲观并发

3.事务四大特性(ACID)

#### 4.活动调用 (Call Activity subprocess)

##### 描述

BPMN 2.0 将常规子流程（通常也称为嵌入式子流程）与看起来非常相似的调用活动区分开来。从概念的角度来看，当流程执行到达活动时，两者都会调用子流程。

不同之处在于调用活动引用了流程定义外部的流程，而子流程则嵌入在原始流程定义中。调用活动的主要用例是拥有可从多个其他流程定义调用的可重用流程定义。

当流程执行到达调用活动时，会创建一个新的执行，它是到达调用活动的执行的sub-execution。然后使用该sub-execution来执行子流程，可能会像在常规流程中一样创建并行子执行。super-execution会一直等到子流程完全结束，然后再继续原来的进程。

##### 图形示例

调用活动的可视化与子流程相同，但边框较粗（折叠和展开）。根据建模工具，也可以展开调用活动，但默认的可视化是折叠的子流程表示。

![](http://rep.shaoteemo.com/bpmn.collapsed.call.activity.png)

##### XML示例

调用活动是一个常规活动，它需要一个*calledElement* ，该元素通过**key**引用流程定义。实际上，这意味着在*calledElement*中使用了流程的 id。

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
        targetNamespace="">

    <process id="call_activity" name="call_activity">
        <!--定义活动调用-->
        <callActivity id="callCheckCreditProcess" name="Check credit" calledElement="checkCreditProcess" />
    </process>
</definitions>
```

请注意，子流程的流程定义是在运行时解析的。这意味着如果需要，子流程可以独立于调用流程进行部署。

##### 变量的转递

您可以将流程变量传递给子流程，反之亦然。数据在启动时复制到子进程中，并在结束时复制回主进程。

```xml
<!--值传递-->
<callActivity id="callSubProcess" calledElement="checkCreditProcess" >
    <extensionElements>
        <activiti:in source="someVariableInMainProcess" target="nameOfVariableInSubProcess" />
        <activiti:out source="someVariableInSubProcess" target="nameOfVariableInMainProcess" />
    </extensionElements>
</callActivity>
```

我们使用 Activiti Extension 作为 BPMN 标准元素的快捷方式，称为 dataInputAssociation 和 dataOutputAssociation，它们仅在您以 BPMN 2.0 标准方式声明流程变量时才有效。

这里也可以使用表达式：

```xml
<!--支持表达式-->
<callActivity id="callSubProcess2" calledElement="checkCreditProcess" >
    <extensionElements>
        <activiti:in sourceExpression="${x+5}" target="y" />
        <activiti:out source="${y+5}" target="z" />
    </extensionElements>
</callActivity>
```

所以最后 z = y+5 = x+5+5

callActivity 元素还支持使用自定义 activiti 属性扩展在启动的子流程实例上设置业务键。 businessKey 属性可用于在子流程实例上设置自定义业务键值。

```xml
<!--自定义Business键-->
<callActivity id="callSubProcess3" calledElement="checkCreditProcess" activiti:businessKey="${myVariable}">
    ...
</callActivity>
```

定义值为true 的inheritBusinessKey 属性会将子流程上的业务键值设置为调用流程中定义的业务键值。

```xml
<callActivity id="callSubProcess4" calledElement="checkCreditProcess" activiti:inheritBusinessKey="true">
    ...
</callActivity>
```

##### 例子

以下流程图显示了订单的简单处理。由于客户信用的检查对于许多其他流程来说可能是通用的，因此检查信用步骤在这里被建模为活动调用。

![](http://rep.shaoteemo.com/bpmn.call.activity.super.process.png)

该过程如下所示：

```xml
<!--案例-->
<process id="example">
    <startEvent id="theStart" />
    <sequenceFlow id="flow1" sourceRef="theStart" targetRef="receiveOrder" />
    <manualTask id="receiveOrder" name="Receive Order" />
    <sequenceFlow id="flow2" sourceRef="receiveOrder" targetRef="callCheckCreditProcess" />
    <callActivity id="callCheckCreditProcess" name="Check credit" calledElement="checkCreditProcess" />
    <sequenceFlow id="flow3" sourceRef="callCheckCreditProcess" targetRef="prepareAndShipTask" />
    <userTask id="prepareAndShipTask" name="Prepare and Ship" />
    <sequenceFlow id="flow4" sourceRef="prepareAndShipTask" targetRef="end" />
    <endEvent id="end" />
</process>
```

子流程如下所示：

![](http://rep.shaoteemo.com/bpmn.call.activity.sub.process.png)

子流程的流程定义没有什么特别之处。它也可以在不被另一个进程调用的情况下使用。

### 事务和并发（Transactions and Concurrency）

#### 1. Asynchronous Continuations

Activiti 以事务性的方式执行流程，可以根据您的需求进行配置。让我们先看看 Activiti 如何正常界定事务。如果您触发 Activiti（即启动一个流程、完成一个任务、发出执行信号），Activiti 将在流程中前进，直到它在每个活动执行路径上达到等待状态。更具体地说，它通过流程图执行深度优先搜索，如果在每个执行分支上都达到等待状态，则返回。等待状态是“稍后”执行的任务，这意味着 Activiti 会持久化当前执行并等待再次触发。触发器可以来自外部源，例如，如果我们有一个用户任务或接收消息任务，或者来自 Activiti 本身，如果我们有一个计时器事件。如下图所示：

![](http://rep.shaoteemo.com/activiti.async.example.no.async.png)

我们看到一个 BPMN 流程的一部分，其中包含一个用户任务、一个服务任务和一个计时器事件。完成用户任务和验证地址是同一个工作单元的一部分，因此它应该自动成功或失败。这意味着如果服务任务抛出异常，我们希望回滚当前事务，以便执行回溯到用户任务并且用户任务仍然存在于数据库中。这也是 Activiti 的默认行为。在 (1) 应用程序或客户端线程完成任务。在同一个线程中 Activiti 现在正在执行服务并前进直到它达到等待状态，在这种情况下是计时器事件(2).然后它将控制权返回给调用者 (3) 可能提交事务（如果它是由 Activiti 启动的）。

在某些情况下，这不是我们想要的。有时我们需要对流程中的事务边界进行自定义控制，以便能够确定逻辑工作单元的范围。这就是异步延续发挥作用的地方。考虑以下过程（片段）：

![](http://rep.shaoteemo.com/activiti.async.example.async.png)

这次我们正在完成用户任务，生成发票，然后将该发票发送给客户。这次发票的生成不是同一工作单元的一部分，因此如果生成发票失败，我们不想回滚用户任务的完成。所以我们想让 Activiti 做的是完成用户任务（1)，提交事务并将控制权返回给调用应用程序。然后我们想在后台线程中异步生成发票。这个后台线程是 Activiti 作业执行器（实际上是一个线程池），它会定期轮询数据库中的作业。所以在后台，当我们到达“生成发票”任务时，我们正在为 Activiti 创建一个作业“消息”，以便稍后继续该过程并将其持久化到数据库中。该作业然后由作业执行器拾取并执行。我们还向本地作业执行程序提供了一个新作业的提示，以提高性能。

为了使用这个特性，我们可以使用 activiti:async="true" 扩展。例如，服务任务将如下所示：

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
        targetNamespace="Asynchronous Continuations演示">

    <process id="asynchronous_continuations" name="asynchronous_continuations">
        <!--开启异步-->
        <serviceTask id="service1" name="Generate Invoice" activiti:class="my.custom.Delegate" activiti:async="true" />
    </process>
</definitions>
```

可以在以下 BPMN 任务类型上指定 activiti:async：task、serviceTask、scriptTask、businessRuleTask、sendTask、receiveTask、userTask、subProcess、callActivity

在 userTask、receiveTask 或其他等待状态上，异步延续允许我们在单独的线程/事务中执行开始执行侦听器。

#### 2.错误重试（Fail Retry）

Activiti 在其默认配置中，会在作业执行过程中出现任何异常时重新运行作业 3 次。这也适用于异步任务作业。在某些情况下，需要更大的灵活性。有两个参数需要配置：

- 重试次数

- 重试之间的延迟 这些参数可以通过 activiti:failedJobRetryTimeCycle 元素进行配置。这是一个示例用法：

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
          targetNamespace="错误重试演示">
  
      <process id="fail_retry" name="fail_retry">
          <!--重试次数、间隔-->
          <serviceTask id="failingServiceTask" activiti:async="true" activiti:class="org.activiti.engine.test.jobexecutor.RetryFailingDelegate">
              <extensionElements>
                  <activiti:failedJobRetryTimeCycle>R5/PT7M</activiti:failedJobRetryTimeCycle>
              </extensionElements>
          </serviceTask>
      </process>
  </definitions>
  ```

时间周期表达式遵循 ISO 8601 标准，就像定时器事件表达式一样。上面的示例使作业执行程序重试作业 5 次，并在每次重试之前等待 7 分钟。

#### 3.Exclusive Jobs

从 Activiti 5.9 开始，JobExecutor 确保来自单个流程实例的作业永远不会并发执行。为什么是这样？

##### 为什么要exclusive Jobs?

考虑以下流程定义：

![](http://rep.shaoteemo.com/bpmn.why.exclusive.jobs.png)

我们有一个并行网关，后跟三个服务任务，它们都执行asynchronous continuation。结果，三个作业被添加到数据库中。一旦这样的作业出现在数据库中，它就可以由 JobExecutor 处理。JobExecutor 获取作业并将它们委托给实际处理作业的工作线程的线程池。这意味着使用asynchronous continuation，您可以将工作分配到该线程池（在集群场景中甚至可以跨集群中的多个线程池）。这通常是一件好事。然而它也有一个固有的问题：一致性。考虑服务任务之后的并行连接。当一个服务任务的执行完成时，我们到达并行连接，需要决定是否等待其他执行或我们是否可以继续前进。这意味着，对于到达并行连接的每个分支，我们需要决定是否可以继续，或者是否需要等待其他分支上的一个或多个其他执行。

为什么这是个问题？由于服务任务是使用异步延续配置的，因此可能会同时获取相应的作业，并由 JobExecutor 委托给不同的工作线程。结果是执行服务的事务和 3 个单独的执行到达并行连接的事务可能重叠。如果他们这样做，每个单独的事务将不会“看到”另一个事务同时到达同一个并行连接，因此假设它必须等待其他事务。但是，如果每个事务都假定它必须等待其他事务，则在并行连接之后没有任何事务会继续该过程，并且流程实例将永远保持该状态。

Activiti 如何解决这个问题？ Activiti 执行乐观锁。每当我们根据可能不是最新的数据做出决定时（因为另一个事务可能会在我们提交之前修改它，我们确保在两个事务中增加同一数据库行的版本）。这样，无论哪个事务先提交都会获胜，而其他事务因乐观锁定异常而失败。这解决了上面讨论的过程中的问题：如果多个执行同时到达并行连接，它们都假定必须等待，增加其父执行（流程实例）的版本，然后尝试提交。无论先执行哪个都可以提交，而其他执行将因乐观锁定异常而失败。由于执行是由作业触发的，Activiti 会在等待一定时间后重试执行相同的作业，并希望这次通过同步网关。

这是一个很好的解决方案吗？正如我们所见，乐观锁允许 Activiti 防止不一致。它确保我们不会“停留在加入网关”，这意味着：要么所有执行都通过了网关，要么数据库中有作业确保我们重试通过它。然而，虽然从持久性和一致性的角度来看这是一个完美的解决方案，但这在更高级别上可能并不总是理想的行为：

- Activiti 只会在固定的最大次数内重试相同的作业（默认配置中为 3）。之后，该作业仍将存在于数据库中，但不再主动重试。这意味着操作员需要手动触发作业。
- 如果作业具有非事务性副作用，则失败的事务不会回滚这些副作用。例如，如果“预订音乐会门票”服务与 Activiti 不共享同一事务，则如果我们重试该作业，我们可能会预订多张门票。

##### 什么是exclusive Jobs?

一个exclusive jobs 不能与来自同一流程实例的另一个独占作业同时执行。考虑上面展示的流程：如果我们声明服务任务是独占的，JobExecutor 将确保相应的作业不是并发执行的。相反，它将确保每当它从某个流程实例获取独占作业时，它会从同一流程实例获取所有其他独占作业并将它们委托给同一个工作线程。这确保了作业的顺序执行。

如何启用此功能？从 Activiti 5.9 开始，exclusive jobs是默认配置。因此，默认情况下所有异步延续和计时器事件都是独占的。此外，如果您希望作业是非独占的，您可以使用 activiti:exclusive="false" 将其配置为非独占。例如，以下 servicetask 将是异步但非排他的。

```xml
<serviceTask id="service" activiti:expression="${myService.performBooking(hotel, dates)}" activiti:async="true" activiti:exclusive="false" />
```

这是一个很好的解决方案吗？我们有一些人询问这是否是一个好的解决方案。他们担心这会阻止您并行“doing things”，从而导致性能问题。同样，必须考虑两件事：

- 如果您是专家并且知道自己在做什么（并且已经理解了名为“Why exclusive Jobs？”的部分），则可以将其关闭。除此之外，对于大多数用户来说，如果异步延续和计时器之类的东西正常工作，它会更直观。
- 这实际上不是性能问题。性能是重负载下的问题。重负载意味着作业执行器的所有工作线程一直都很忙。对于独占作业，Activiti 将简单地以不同方式分配负载。独占作业意味着来自单个流程实例的作业由同一线程按顺序执行。但请考虑：您有不止一个流程实例。来自其他流程实例的作业被委托给其他线程并并发执行。这意味着对于独占作业，Activiti 不会并发执行来自同一个流程实例的作业，但它仍然会并发执行多个实例。从整体吞吐量的角度来看，这在大多数情况下都是可取的，因为它通常会导致更快地完成单个实例。此外，执行同一流程实例的后续作业所需的数据将已经在执行集群节点的缓存中。如果作业没有此节点关联，则可能需要再次从数据库中提取该数据。

### 流程启动权限（Process Initiation Authorization）

默认情况下，每个人都可以启动已部署流程定义的新流程实例。流程启动授权功能允许定义用户和组，以便 Web 客户端可以选择性地限制用户启动新流程实例。请注意，Activiti 引擎不会以任何方式验证授权定义。此功能仅适用于开发人员在 Web 客户端中简化授权规则的实现。语法类似于用户任务的用户分配语法。可以使用 \<activiti:potentialStarter\> 标记将用户或组分配为进程的潜在发起者。下面是一个例子：

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
        targetNamespace="流程启动权限演示">

    <process id="process_initiation_authorization" name="process_initiation_authorization">
        <extensionElements>
            <activiti:potentialStarter>
                <resourceAssignmentExpression>
                    <formalExpression>group2, group(group3), user(user3)</formalExpression>
                </resourceAssignmentExpression>
            </activiti:potentialStarter>
        </extensionElements>

        <startEvent id="theStart"/>
        <!--……-->
    </process>
</definitions>
```

在上面的 xml 摘录中，user(user3) 直接指的是用户 user3，而 group(group3) 指的是组 group3。

没有指标将默认为组类型。也可以使用 <process> 标签的属性，即 \<activiti:candidateStarterUsers\> 和 \<activiti:candidateStarterGroups\>。下面是一个例子：

```xml
<process id="potentialStarter" activiti:candidateStarterUsers="user1, user2"
         activiti:candidateStarterGroups="group1">
...
</process>
```

可以同时使用这两个属性。

定义流程启动授权后，开发人员可以使用以下方法检索授权定义。此代码检索可由给定用户启动的流程定义列表：

```java
processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("userxxx").list();
```

还可以检索被定义为特定流程定义的潜在启动者的所有身份链接

```java
identityLinks = repositoryService.getIdentityLinksForProcessDefinition("processDefinitionId");
```

以下示例显示了如何获取可以启动给定进程的用户列表：

```java
List<User> authorizedUsers =  identityService().createUserQuery().potentialStarter("processDefinitionId").list();
```

以完全相同的方式，可以检索配置为给定流程定义的潜在启动器的组列表：

```java
List<Group> authorizedGroups =  identityService().createGroupQuery().potentialStarter("processDefinitionId").list();
```

### 数据对象

[[EXPERIMENTAL\]](https://www.activiti.org/userguide/#experimental)

BPMN 提供了将数据对象定义为流程或子流程元素的一部分的可能性。根据 BPMN 规范，可以包含可能从 XSD 定义导入的复杂 XML 结构。作为在 Activiti 中支持数据对象的第一次开始，支持以下 XSD 类型：

```xml
<dataObject id="dObj1" name="StringTest" itemSubjectRef="xsd:string"/>
<dataObject id="dObj2" name="BooleanTest" itemSubjectRef="xsd:boolean"/>
<dataObject id="dObj3" name="DateTest" itemSubjectRef="xsd:datetime"/>
<dataObject id="dObj4" name="DoubleTest" itemSubjectRef="xsd:double"/>
<dataObject id="dObj5" name="IntegerTest" itemSubjectRef="xsd:int"/>
<dataObject id="dObj6" name="LongTest" itemSubjectRef="xsd:long"/>
```

数据对象定义将使用名称属性值作为新变量的名称自动转换为流程变量。除了数据对象的定义之外，Activiti 还提供了一个扩展元素来为变量分配一个默认值。以下 BPMN 片段提供了一个示例：

```xml
<process id="dataObjectScope" name="Data Object Scope" isExecutable="true">
    <dataObject id="dObj123" name="StringTest123" itemSubjectRef="xsd:string">
        <extensionElements>
            <activiti:value>Testing123</activiti:value>
        </extensionElements>
    </dataObject>
</process>
```

## Forms

Activiti 提供了一种方便灵活的方式来为您的业务流程的手动步骤添加表单。我们支持两种处理表单的策略：使用表单属性的内置表单渲染和外部表单渲染。

### 表单属性

与业务流程相关的所有信息要么包含在流程变量本身中，要么通过流程变量引用。Activiti 支持将复杂的 Java 对象存储为流程变量，如 Serializable 对象、JPA 实体或整个 XML 文档作为字符串。

启动流程和完成用户任务是人们参与流程的地方。与人交流需要在某些 UI 技术中呈现表单。为了方便多种 UI 技术，流程定义可以包括将流程变量中复杂的 Java 类型对象转换为属性的 Map<String,String> 的逻辑。

任何 UI 技术都可以在这些属性之上构建一个表单，使用公开属性信息的 Activiti API 方法。性可以提供有关过程变量的专用（且更有限）的视图。显示表单所需的属性在 FormData 返回值中可用，例如

```java
StartFormData FormService.getStartFormData(String processDefinitionId)
```

或者

```java
TaskFormdata FormService.getTaskFormData(String taskId)
```

默认情况下，内置表单引擎会查看属性以及流程变量。因此，如果任务表单属性与流程变量 1-1 匹配，则无需声明任务表单属性。例如，使用以下声明：

```xml
<startEvent id="start" />
```

当执行到达 startEvent 时，所有流程变量都可用，但

```java
formService.getStartFormData(String processDefinitionId).getFormProperties()
```

将是空的，因为没有定义特定的映射。

在上述情况下，所有提交的属性都将存储为流程变量。这意味着只需在表单中添加一个新的输入字段，就可以存储一个新变量。

属性源自流程变量，但它们不必存储为流程变量。例如，流程变量可以是类 Address 的 JPA 实体。 UI 技术使用的表单属性 StreetName 可以与表达式 #{address.street} 匹配。

类似地，用户应该在表单中提交的属性可以存储为流程变量或流程变量之一中的嵌套属性，使用 UEL 值表达式，例如**#{address.street}** 。

类似于提交的属性的默认行为，除非 formProperty 声明另有说明，否则它们将存储为流程变量。

类型转换也可以作为表单属性和流程变量之间处理的一部分来应用。

例如：

```xml
<userTask id="task">
  <extensionElements>
    <activiti:formProperty id="room" />
    <activiti:formProperty id="duration" type="long"/>
    <activiti:formProperty id="speaker" variable="SpeakerName" writable="false" />
    <activiti:formProperty id="street" expression="#{address.street}" required="true" />
  </extensionElements>
</userTask>
```

- 表单属性**room**将作为字符串映射到流程变量**room**
- 表单属性**duration**将作为 java.lang.Long 映射到流程变量**duration**
- 表单属性**speaker**将映射到过程变量**SpeakerName**。它仅在 TaskFormData 对象中可用。如果提交了属性 Speaker，则会抛出 ActivitiException。类比，属性 readable="false" 可以从 FormData 中排除一个属性，但仍会在提交中进行处理。
- 表单属性**street**将作为字符串映射到进程变量地址中的Java bean 属性**street**。如果未提供该属性，则 required="true" 将在提交期间抛出异常。

还可以提供类型元数据作为从方法 StartFormData FormService.getStartFormData(String processDefinitionId) 和 TaskFormdata FormService.getTaskFormData(String taskId) 返回的 FormData 的一部分

我们支持以下表单属性类型：

- `string` (org.activiti.engine.impl.form.StringFormType）
- `long` (org.activiti.engine.impl.form.LongFormType)
- `enum` (org.activiti.engine.impl.form.EnumFormType)
- `date` (org.activiti.engine.impl.form.DateFormType)
- `boolean` (org.activiti.engine.impl.form.BooleanFormType)

对于声明的每个表单属性，以下 FormProperty 信息将通过 List<FormProperty> formService.getStartFormData(String processDefinitionId).getFormProperties() 提供并列出 <FormProperty> formService.getTaskFormData (String taskId) .getFormProperties ()

```java
public interface FormProperty {
  /** the key used to submit the property in {@link FormService#submitStartFormData(String, java.util.Map)}
   * or {@link FormService#submitTaskFormData(String, java.util.Map)} */
  String getId();
  /** the display label */
  String getName();
  /** one of the types defined in this interface like e.g. {@link #TYPE_STRING} */
  FormType getType();
  /** optional value that should be used to display in this property */
  String getValue();
  /** is this property read to be displayed in the form and made accessible with the methods
   * {@link FormService#getStartFormData(String)} and {@link FormService#getTaskFormData(String)}. */
  boolean isReadable();
  /** is this property expected when a user submits the form? */
  boolean isWritable();
  /** is this property a required input field */
  boolean isRequired();
}
```

例如：

```xml
<startEvent id="start">
  <extensionElements>
    <activiti:formProperty id="speaker"
      name="Speaker"
      variable="SpeakerName"
      type="string" />

    <activiti:formProperty id="start"
      type="date"
      datePattern="dd-MMM-yyyy" />

    <activiti:formProperty id="direction" type="enum">
      <activiti:value id="left" name="Go Left" />
      <activiti:value id="right" name="Go Right" />
      <activiti:value id="up" name="Go Up" />
      <activiti:value id="down" name="Go Down" />
    </activiti:formProperty>

  </extensionElements>
</startEvent>
```

所有这些信息都可以通过 API 访问。类型名称可以通过 formProperty.getType().getName() 获得。甚至日期模式也可以通过 formProperty.getType().getInformation("datePattern") 获得，枚举值也可以通过 formProperty.getType().getInformation("values") 访问

Activiti explorer 支持表单属性，并将根据表单定义渲染表单。以下 XML 片段

```xml
<startEvent>
  <extensionElements>
    <activiti:formProperty id="numberOfDays" name="Number of days" value="${numberOfDays}" type="long" required="true"/>
    <activiti:formProperty id="startDate" name="First day of holiday (dd-MM-yyy)" value="${startDate}" datePattern="dd-MM-yyyy hh:mm" type="date" required="true" />
    <activiti:formProperty id="vacationMotivation" name="Motivation" value="${vacationMotivation}" type="string" />
  </extensionElements>
</userTask>
```

在 Activiti Explorer 中使用时将呈现为流程启动表单

![](http://rep.shaoteemo.com/forms.explorer.png)

### 外部表单渲染

API 还允许您在 Activiti Engine 之外执行您自己的任务表单渲染。这些步骤解释了您可以用来自己呈现任务表单的钩子。

本质上，呈现表单所需的所有数据都在以下两种服务方法之一中组装：StartFormData FormService.getStartFormData(String processDefinitionId) 和 TaskFormdata FormService.getTaskFormData(String taskId)。

提交表单属性可以通过 ProcessInstance FormService.submitStartFormData(String processDefinitionId, Map<String,String> properties) 和 void FormService.submitTaskFormData(String taskId, Map<String,String> properties) 完成。

要了解表单属性如何映射到流程变量，请参阅表单属性

您可以将任何表单模板资源放置在您部署的业务档案中（以防您想将它们存储为流程版本）。它将作为部署中的资源可用，您可以使用以下方法进行检索：String ProcessDefinition.getDeploymentId() 和 InputStream RepositoryService.getResourceAsStream(String deploymentId, String resourceName);这可能是您的模板定义文件，您可以使用它在您自己的应用程序中render/show表单。

您也可以将此功能用于访问任务表单之外的部署资源以用于任何其他目的。

属性 `<userTask activiti:formKey="... " `由 API 通过 `String` `FormService.getStartFormData(String processDefinitionId).getFormKey()` 和 `String FormService.getTaskFormData(String taskId).getFormKey() `公开。您可以使用它在部署中存储模板的全名（例如 `org/activiti/example/form/my-custom-form.xml`），但这根本不是必需的。例如，您还可以在表单属性中存储一个通用键，并应用算法或转换来获取需要使用的实际模板。当您想为不同的 UI 技术呈现不同的表单时，这可能会很方便，例如一种用于普通屏幕大小的 Web 应用程序的表单，一种用于手机小屏幕的表单，甚至可能是 IM 表单或电子邮件表单的模板。

## JPA

您可以使用 JPA-Entities 作为流程变量，允许您：

- 根据可以在 userTask 中的表单中填写或在 serviceTask 中生成的流程变量更新现有 JPA-entities。
- 重用现有的域模型，而无需编写显式服务来获取实体和更新值
- 根据现有实体的属性做出决策（网关）。
- .....

### 1.基本要求

仅支持符合以下条件的实体：

- 实体应该使用 JPA 注释进行配置，我们支持字段和属性访问。也可以使用映射的超类。
- 实体应该有一个用`@Id` 注释的主键，不支持复合主键（`@EmbeddedId` 和`@IdClass`）。Id 字段/属性可以是 JPA 规范中支持的任何类型：原始类型及其包装器（不包括boolean）、`String`、`BigInteger`、`BigDecimal`、`java.util.Date` 和 `java.sql.Date`。

### 2.配置

为了能够使用 JPA 实体，引擎必须具有对 `EntityManagerFactory` 的引用。这可以通过配置引用或提供持久性单元名称来完成。用作变量的 JPA 实体将被自动检测并进行相应处理。

下面的示例配置使用 jpaPersistenceUnitName：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="processEngineConfiguration"
          class="org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration">

        <!-- Database configurations -->
        <property name="databaseSchemaUpdate" value="true" />
        <property name="jdbcUrl" value="jdbc:h2:mem:JpaVariableTest;DB_CLOSE_DELAY=1000" />

        <property name="jpaPersistenceUnitName" value="activiti-jpa-pu" />
        <property name="jpaHandleTransaction" value="true" />
        <property name="jpaCloseEntityManager" value="true" />

        <!-- job executor configurations -->
        <property name="jobExecutorActivate" value="false" />

        <!-- mail server configurations -->
        <property name="mailServerPort" value="5025" />
    </bean>

</beans>
```

下面的下一个示例配置提供了一个我们自己定义的 `EntityManagerFactory`（在本例中，是一个 open-jpa 实体管理器）。请注意，该代码段仅包含与示例相关的 bean，其他的都被省略了。可以在 activiti-spring-examples (`/activiti-spring/src/test/java/org/activiti/spring/test/jpa/JPASpringTest.java`) 中找到带有 open-jpa 实体管理器的完整工作示例

```xml
<!--EntityManagerFactory-->
<bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
    <property name="persistenceUnitManager" ref="pum"/>
    <property name="jpaVendorAdapter">
        <bean class="org.springframework.orm.jpa.vendor.OpenJpaVendorAdapter">
            <property name="databasePlatform" value="org.apache.openjpa.jdbc.sql.H2Dictionary" />
        </bean>
    </property>
</bean>
<bean id="processEngineConfiguration" class="org.activiti.spring.SpringProcessEngineConfiguration">
    <property name="dataSource" ref="dataSource" />
    <property name="transactionManager" ref="transactionManager" />
    <property name="databaseSchemaUpdate" value="true" />
    <property name="jpaEntityManagerFactory" ref="entityManagerFactory" />
    <property name="jpaHandleTransaction" value="true" />
    <property name="jpaCloseEntityManager" value="true" />
    <property name="jobExecutorActivate" value="false" />
</bean>
```

以Java代码方式构建引擎时也可以进行相同的配置，例如：

```java
ProcessEngine processEngine = ProcessEngineConfiguration
.createProcessEngineConfigurationFromResourceDefault()
.setJpaPersistenceUnitName("activiti-pu")
.buildProcessEngine();
```

配置属性：

- `jpaPersistenceUnitName`：要使用的持久性单元的名称。（确保持久单元在类路径上可用。根据规范，默认位置是 `/META-INF/persistence.xml`）。使用 `jpaEntityManagerFactory` 或 `paPersistenceUnitName`。
- `jpaEntityManagerFactory`: 对实现 `javax.persistence.EntityManagerFactory` 的 bean 的引用，该 bean 将用于加载实体和刷新更新。使用 jpaEntityManagerFactory 或 jpaPersistenceUnitName。
- `jpaHandleTransaction`: 指示引擎应该在使用的 EntityManager 实例上开始和提交/回滚事务的标志。使用 Java 事务 API (JTA) 时设置为 false。
- `jpaCloseEntityManager`:指示引擎应关闭从 `EntityManagerFactory` 获取的 `EntityManager` 实例的标志。当 EntityManager 由容器管理时（例如，当使用不限于单个事务的扩展持久性上下文时），设置为 false。

### 3.使用

#### 3.1基本使用

可以在 Activiti 源代码中的 JPAVariableTest 中找到使用 JPA 变量的示例。我们将逐步解释 `JPAVariableTest.testUpdateJPAEntityValues`。

首先，我们为我们的持久化单元创建一个 EntityManagerFactory，它基于 `META-INF/persistence.xml`。这包含应该包含在持久性单元中的类和一些特定于供应商的配置。

我们在测试中使用了一个简单的实体，它有一个 id 和 `String` 值属性，它也被持久化。在运行测试之前，我们创建一个实体并保存它。

```java
@Entity(name = "JPA_ENTITY_FIELD")
public class FieldAccessJPAEntity {

  @Id
  @Column(name = "ID_")
  private Long id;

  private String value;

  public FieldAccessJPAEntity() {
    // Empty constructor needed for JPA
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
```

我们启动一个新的流程实例，将实体添加为变量。与其他变量一样，它们存储在引擎的持久存储中。当下次请求该变量时，它将根据存储的类和 Id 从 `EntityManager` 加载。

```java
Map<String, Object> variables = new HashMap<String, Object>();
variables.put("entityToUpdate", entityToUpdate);

ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateJPAValuesProcess", variables);
```

我们流程定义中的第一个节点包含一个 `serviceTask`，它将调用 `entityToUpdate` 上的 `setValue` 方法，它解析为我们之前在启动流程实例时设置的 JPA 变量，并将从与当前引擎上下文关联的 `EntityManager` 加载。

```xml
<serviceTask id='theTask' name='updateJPAEntityTask'
  activiti:expression="${entityToUpdate.setValue('updatedValue')}" />
```

当服务任务完成时，流程实例在流程定义中定义的 userTask 中等待，这允许我们检查流程实例。此时，`EntityManager` 已被刷新，对实体的更改已推送到数据库。当我们获得变量 `entityToUpdate` 的值时，它会再次加载，我们将获得 `value` 属性设置为 `updatedValue` 的实体。

```java
// Servicetask in process 'UpdateJPAValuesProcess' should have set value on entityToUpdate.
Object updatedEntity = runtimeService.getVariable(processInstance.getId(), "entityToUpdate");
assertTrue(updatedEntity instanceof FieldAccessJPAEntity);
assertEquals("updatedValue", ((FieldAccessJPAEntity)updatedEntity).getValue());
```

#### 3.2JPA检索流程变量

您可以查询具有特定 JPA 实体作为变量值的 `ProcessInstances` 和 `Executions`。请注意，`ProcessInstanceQuery` 和 `ExecutionQuery` 上的 JPA-Entities 仅支持 `variableValueEquals(name, entity)`。方法 `variableValueNotEquals`、`variableValueGreaterThan`、`variableValueGreaterThanOrEqual`、`variableValueLessThan` 和 `variableValueLessThanOrEqual` 不受支持，并且当 JPA-Entity 作为值传递时将抛出 `ActivitiException`。

```java
ProcessInstance result = runtimeService.createProcessInstanceQuery()
    .variableValueEquals("entityToQuery", entityToQuery).singleResult();
```

#### 3.3使用 Spring bean 和 JPA 的高级示例

一个更高级的例子，`JPASpringTest`，可以在 `activiti-spring-examples` 中找到。它描述了以下简单用例：

- 使用 JPA 实体的现有 Spring-bean 已经存在，它允许存储请求。
- 使用 Activiti，我们可以使用通过现有 bean 获得的现有实体，并将它们用作我们流程中的变量。流程定义为以下步骤：
  - 创建新 LoanRequest 的服务任务，使用现有 `LoanRequestBean` 使用启动流程时的变量（例如可能来自启动表单）。创建的实体存储为变量，使用 `activiti:resultVariable` 将表达式结果存储为变量。
  - 允许管理审查请求和批准/不批准的 UserTask，它存储为布尔变量 ·allowedByManager`。
  - ServiceTask 更新请求实体，以便实体与流程同步。
  - 根据`approved`实体属性的值，使用独占网关来决定下一步要走的路径：当请求被批准时，流程结束，否则，将有一个额外的任务（发送拒绝信），因此可以通过拒绝信手动通知客户。

请注意，该过程不包含任何表单，因为它仅用于单元测试。

![](http://rep.shaoteemo.com/jpa.spring.example.process.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions id="taskAssigneeExample"
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:activiti="http://activiti.org/bpmn"
  targetNamespace="org.activiti.examples">

  <process id="LoanRequestProcess" name="Process creating and handling loan request">
    <startEvent id='theStart' />
    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='createLoanRequest' />

    <serviceTask id='createLoanRequest' name='Create loan request'
      activiti:expression="${loanRequestBean.newLoanRequest(customerName, amount)}"
      activiti:resultVariable="loanRequest"/>
    <sequenceFlow id='flow2' sourceRef='createLoanRequest' targetRef='approveTask' />

    <userTask id="approveTask" name="Approve request" />
    <sequenceFlow id='flow3' sourceRef='approveTask' targetRef='approveOrDissaprove' />

    <serviceTask id='approveOrDissaprove' name='Store decision'
      activiti:expression="${loanRequest.setApproved(approvedByManager)}" />
    <sequenceFlow id='flow4' sourceRef='approveOrDissaprove' targetRef='exclusiveGw' />

    <exclusiveGateway id="exclusiveGw" name="Exclusive Gateway approval" />
    <sequenceFlow id="endFlow1" sourceRef="exclusiveGw" targetRef="theEnd">
      <conditionExpression xsi:type="tFormalExpression">${loanRequest.approved}</conditionExpression>
    </sequenceFlow>
    <sequenceFlow id="endFlow2" sourceRef="exclusiveGw" targetRef="sendRejectionLetter">
      <conditionExpression xsi:type="tFormalExpression">${!loanRequest.approved}</conditionExpression>
    </sequenceFlow>

    <userTask id="sendRejectionLetter" name="Send rejection letter" />
    <sequenceFlow id='flow5' sourceRef='sendRejectionLetter' targetRef='theOtherEnd' />

    <endEvent id='theEnd' />
    <endEvent id='theOtherEnd' />
  </process>

</definitions>
```

## History

历史记录是捕获流程执行期间发生的事情并将其永久存储的组件。与运行时数据相反，历史数据在流程实例完成后也将保留在 DB 中。

有5个历史实体：

- `HistoricProcessInstance`包含有关当前和过去流程实例的信息。
- `HistoricVariableInstance`包含流程变量或任务变量的最新值。
- `HistoricActivityInstance`包含有关活动（流程中的节点）的单次执行的信息。
- `HistoricTaskInstance`包含有关当前和过去（已完成和已删除）任务实例的信息。
- `HistoricDetail`包含与历史流程实例、活动实例或任务实例相关的各种信息。

### 1.历史信息检索

在 API 中，可以查询所有 5 个历史实体。`HistoryService` 公开了 `createHistoricProcessInstanceQuery()`、`createHistoricVariableInstanceQuery()`、`createHistoricActivityInstanceQuery()`、`createHistoricDetailQuery() `和 `createHistoricTaskInstanceQuery() `方法。

下面是几个示例，展示了历史查询 API 的一些可能性。可以在 `org.activiti.engine.history `包中的 javadocs 中找到对可能性的完整描述。

#### 1.1HistoricProcessInstance检索

获取 10 个已完成的 `HistoricProcessInstances`，在定义为 XXX 的所有已完成流程中，完成时间最长（持续时间最长）。

```java
historyService.createHistoricProcessInstanceQuery()
  .finished()
  .processDefinitionId("XXX")
  .orderByProcessInstanceDuration().desc()
  .listPage(0, 10);
```

#### 1.2HistoricVariableInstance检索

从已完成的流程实例中获取所有 `HistoricVariableInstances`，id 为 xxx，按变量名称排序。

```java
historyService.createHistoricVariableInstanceQuery()
  .processInstanceId("XXX")
  .orderByVariableName.desc()
  .list();
```

#### 1.3HistoricActivityInstance检索

获取在任何使用具有 id 为 XXX 的 processDefinition 的进程中已完成的类型为 serviceTask 的最后一个 `HistoricActivityInstance`。

```java
historyService.createHistoricActivityInstanceQuery()
  .activityType("serviceTask")
  .processDefinitionId("XXX")
  .finished()
  .orderByHistoricActivityInstanceEndTime().desc()
  .listPage(0, 1);
```

#### 1.4HistoricDetail检索

下一个示例，获取所有在进程中已完成且 ID 为 123 的变量更新。此查询将仅返回 `HistoricVariableUpdates`。请注意，某个变量名称可能有多个 `HistoricVariableUpdate` 条目，每次在流程中更新该变量。您可以使用` orderByTime`（变量更新完成的时间）或 `orderByVariableRevision`（更新时运行时变量的修订）来找出它们发生的顺序。

```java
historyService.createHistoricDetailQuery()
  .variableUpdates()
  .processInstanceId("123")
  .orderByVariableName().asc()
  .list()
```

此示例获取在任何任务中或在以 id“123”启动流程时提交的所有表单属性。此查询将仅返回 `HistoricFormPropertiess`。

```java
historyService.createHistoricDetailQuery()
  .formProperties()
  .processInstanceId("123")
  .orderByVariableName().asc()
  .list()
```

最后一个示例获取对 ID 为“123”的任务执行的所有变量更新。这将返回在任务上设置的变量（任务局部变量）的所有 `HistoricVariableUpdates`，而不是在流程实例上。

```java
historyService.createHistoricDetailQuery()
  .variableUpdates()
  .taskId("123")
  .orderByVariableName().asc()
  .list()
```

可以使用 `TaskService`或在 `TaskListener` 内部的 `DelegateTask` 上设置任务局部变量：

```java
taskService.setVariableLocal("123", "myVariable", "Variable value");
```

```java
public void notify(DelegateTask delegateTask) {
  delegateTask.setVariableLocal("myVariable", "Variable value");
}
```

#### 1.5HistoricTaskInstance检索

获取 10 个已完成且完成时间最长（持续时间最长）的 `HistoricTaskInstances`。

```java
historyService.createHistoricTaskInstanceQuery()
  .finished()
  .orderByHistoricTaskInstanceDuration().desc()
  .listPage(0, 10);
```

获取因删除原因包含“无效”而被删除的 `HistoricTaskInstances`，这些实例最后分配给用户 kermit。

```java
historyService.createHistoricTaskInstanceQuery()
  .finished()
  .taskDeleteReasonLike("%invalid%")
  .taskAssignee("kermit")
  .listPage(0, 10);
```

### 2.历史配置

可以使用枚举 `org.activiti.engine.impl.history.HistoryLevel`（或 5.11 之前版本的 `ProcessEngineConfiguration` 上定义的 HISTORY 常量）以编程方式配置历史级别：

```java
ProcessEngine processEngine = ProcessEngineConfiguration
  .createProcessEngineConfigurationFromResourceDefault()
  .setHistory(HistoryLevel.AUDIT.getKey())
  .buildProcessEngine();
```

级别也可以在 activiti.cfg.xml 或 spring-context 中配置：

```xml
<bean id="processEngineConfiguration" class="org.activiti.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration">
  <property name="history" value="audit" />
  ...
</bean>
```

可以配置以下历史级别：

- `none`: 跳过所有历史存档。这是运行时流程执行的最高性能，但没有历史信息可用。
- `activity`: 归档所有流程实例和活动实例。在流程实例结束时，顶级流程实例变量的最新值将被复制到历史变量实例中。不会存档任何详细信息。
- `audit`: 这是**默认设置**。它归档所有流程实例、活动实例，使变量值持续同步以及提交的所有表单属性，以便所有用户通过表单进行的交互都是可追踪的并且可以被审计。
- `full`: 这是历史存档的最高级别，因此是最慢的。该级别存储与`audit`级别相同的所有信息以及所有其他可能的详细信息，主要是流程变量更新。

在 Activiti 5.11 之前，历史级别存储在数据库中（表 ACT_GE_PROPERTY，名称为 historyLevel 的属性）。从 5.11 开始，此值不再使用，并从数据库中忽略/删除。现在可以在引擎的 2 次启动之间更改历史记录，如果级别从之前的引擎启动更改，则不会抛出异常。

### 3.用于审计目的的历史记录

配置至少是`audit`级别进行配置时。然后记录通过方法 `FormService.submitStartFormData(String processDefinitionId, Map<String, String> properties)` 和 `FormService.submitTaskFormData(String taskId, Map<String, String> properties)` 提交的所有属性。

可以使用查询 API 检索表单属性，如下所示：

```java
historyService
      .createHistoricDetailQuery()
      .formProperties()
      ...
      .list();
```

在这种情况下，只返回 `HistoricFormProperty` 类型的历史细节。

如果您在使用 `IdentityService.setAuthenticatedUserId(String)` 调用提交方法之前设置了经过身份验证的用户，那么提交表单的经过身份验证的用户将可以在历史记录中以及使用 `HistoricProcessInstance.getStartUserId()` 访问启动表单和 `HistoricActivityInstance.getAssignee( )` 用于任务表单。

## Eclipse设计器的使用

本节略。详情请查看[Eclipse Designer](https://www.activiti.org/userguide/#activitiDesigner)

## REST API

本节略。详情请查看[REST API](https://www.activiti.org/userguide/#_rest_api)

## CDI集成

activiti-cdi 模块利用了 Activiti 的可配置性和 cdi 的可扩展性。 activiti-cdi最突出的特点是：

- 支持@BusinessProcessScoped bean（生命周期绑定到流程实例的Cdi bean），
- 用于从进程中解析 Cdi bean（包括 EJB）的自定义 El-Resolver，
- 使用注释对流程实例进行声明式控制，
- Activiti 连接到 cdi 事件总线，
- 适用于 Java EE 和 Java SE，适用于 Spring，
- 支持单元测试。

引入依赖：

```xml
<dependency>
	<groupId>org.activiti</groupId>
	<artifactId>activiti-cdi</artifactId>
	<version>5.x</version>
</dependency>
```

### 1.设置 activiti-cdi

Activiti cdi 可以设置在不同的环境中。在本节中，我们简要介绍了配置选项。

#### 1.1查找流程引擎

cdi 扩展需要访问 ProcessEngine。为此，在运行时查找接口 `org.activiti.cdi.spi.ProcessEngineLookup` 的实现。cdi 模块附带一个名为 `org.activiti.cdi.impl.LocalProcessEngineLookup` 的默认实现，它使用 `ProcessEngines`-Utility 类来查找 ProcessEngine。在默认配置中 `ProcessEngines#NAME_DEFAULT` 用于查找 ProcessEngine。此类可能会被子类化以设置自定义名称。注意：在classpath需要一个 `activiti.cfg.xml` 配置文件。

Activiti cdi 使用 java.util.ServiceLoader SPI 来解析 `org.activiti.cdi.spi.ProcessEngineLookup` 的实例。为了提供接口的自定义实现，我们需要在我们的部署中添加一个名为 `META-INF/services/org.activiti.cdi.spi.ProcessEngineLookup` 的纯文本文件，我们在其中指定实现的完全类名.

注意：如果你没有提供自定义的 `org.activiti.cdi.spi.ProcessEngineLookup` 实现，Activiti 将使用默认的 `LocalProcessEngineLookup` 实现。在这种情况下，您需要做的就是在类路径上提供一个 activiti.cfg.xml 文件（请参阅下一节）。

#### 1.2配置流程引擎

配置取决于所选的 ProcessEngineLookup-Strategy（参见上一节）。在这里，我们专注于与 LocalProcessEngineLookup 结合使用的配置选项，这需要我们在Classpath中提供 Spring activiti.cfg.xml 文件。

Activiti 提供了不同的 ProcessEngineConfiguration 实现，主要取决于底层的事务管理策略。activiti-cdi 模块不关心事务，这意味着可以使用潜在的任何事务管理策略（甚至是 Spring 事务抽象）。为方便起见，cdi 模块提供了两个自定义 ProcessEngineConfiguration 实现：

- `org.activiti.cdi.CdiJtaProcessEngineConfiguration`: Activiti JtaProcessEngineConfiguration 的子类，如果 JTA 管理的事务应该用于 Activiti，则可以使用
- `org.activiti.cdi.CdiStandaloneProcessEngineConfiguration`: Activiti StandaloneProcessEngineConfiguration 的子类，如果应该为 Activiti 使用普通的 JDBC 事务，则可以使用它。以下是 JBoss 7 的示例 activiti.cfg.xml 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<!-- lookup the JTA-Transaction manager -->
	<bean id="transactionManager" class="org.springframework.jndi.JndiObjectFactoryBean">
		<property name="jndiName" value="java:jboss/TransactionManager"></property>
		<property name="resourceRef" value="true" />
	</bean>

	<!-- process engine configuration -->
	<bean id="processEngineConfiguration"
		class="org.activiti.cdi.CdiJtaProcessEngineConfiguration">
		<!-- lookup the default Jboss datasource -->
		<property name="dataSourceJndiName" value="java:jboss/datasources/ExampleDS" />
		<property name="databaseType" value="h2" />
		<property name="transactionManager" ref="transactionManager" />
		<!-- using externally managed transactions -->
		<property name="transactionsExternallyManaged" value="true" />
		<property name="databaseSchemaUpdate" value="true" />
	</bean>
</beans>
```

这就是 Glassfish 3.1.1 的样子（假设正确配置了名为 jdbc/activiti 的数据源）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<!-- lookup the JTA-Transaction manager -->
	<bean id="transactionManager" class="org.springframework.jndi.JndiObjectFactoryBean">
		<property name="jndiName" value="java:appserver/TransactionManager"></property>
		<property name="resourceRef" value="true" />
	</bean>

	<!-- process engine configuration -->
	<bean id="processEngineConfiguration"
		class="org.activiti.cdi.CdiJtaProcessEngineConfiguration">
		<property name="dataSourceJndiName" value="jdbc/activiti" />
		<property name="transactionManager" ref="transactionManager" />
		<!-- using externally managed transactions -->
		<property name="transactionsExternallyManaged" value="true" />
		<property name="databaseSchemaUpdate" value="true" />
	</bean>
</beans>
```

注意上面的配置需要“spring-context”模块：

```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context</artifactId>
	<version>3.0.3.RELEASE</version>
</dependency>
```

Java SE 环境中的配置与创建 ProcessEngine 部分中提供的示例完全相同，将“CdiStandaloneProcessEngineConfiguration”替换为“StandaloneProcessEngineConfiguration”。

#### 1.3部署流程

可以使用标准的 activiti-api (`RepositoryService`) 部署流程。此外，activiti-cdi 提供了自动部署在名为 `processes.xml` 的文件中列出的进程的可能性，该文件位于类路径的顶层。这是一个示例 processes.xml 文件：

```xml
<?xml version="1.0" encoding="utf-8" ?>
<!-- list the processes to be deployed -->
<processes>
	<process resource="diagrams/myProcess.bpmn20.xml" />
	<process resource="diagrams/myOtherProcess.bpmn20.xml" />
</processes>
```

===使用 CDI 的上下文流程执行

在本节中，我们将简要介绍 Activiti cdi 扩展使用的上下文流程执行模型。BPMN 业务流程通常是一个长期运行的交互，由用户和系统任务组成。在运行时，一个进程被分成一组单独的工作单元，由用户和（或）应用程序逻辑执行。在 activiti-cdi 中，流程实例可以与 cdi 范围相关联，该关联代表一个工作单元。如果工作单元很复杂，例如，如果 UserTask 的实现是不同形式的复杂序列，并且在此交互期间需要保持“non-process-scoped”状态，则这特别有用。

在默认配置中，流程实例与“broadest”的活动范围相关联，从对话开始，如果对话上下文不活动，则回退到请求。

#### 1.4Associating a Conversation with a Process Instance

在解析 @BusinessProcessScoped bean 或注入流程变量时，我们依赖于活动 cdi 范围和流程实例之间的现有关联。Activiti-cdi 提供了 `org.activiti.cdi.BusinessProcess bean` 来控制关联，最突出的是：

- startProcessBy(… ) 方法，镜像由 Activiti `RuntimeService` 公开的相应方法，允许启动并随后关联业务流程，
- `resumeProcessById(String processInstanceId)`，允许将流程实例与提供的 id 相关联，
- `resumeTaskById(String taskId)`，允许将任务与提供的 id（以及相应的流程实例）相关联，

一旦完成一个工作单元（例如 UserTask），就可以调用 `completeTask()` 方法以将对话/请求与流程实例分离。这向 Activiti 发出信号，当前任务已完成，并使流程实例继续进行。

请注意，`BusinessProcess`-bean 是一个`@Named bean`，这意味着可以使用表达式语言调用公开的方法，例如从 JSF 页面调用。以下 JSF2 片段开始一个新对话并将其与用户任务实例相关联，其 id 作为请求参数传递（例如 `pageName.jsf?taskId=XX`）：

```XML
<f:metadata>
<f:viewParam name="taskId" />
<f:event type="preRenderView" listener="#{businessProcess.startTask(taskId, true)}" />
</f:metadata>
```

#### 1.5声明式控制流程

Activiti-cdi 允许使用注释以声明方式启动流程实例并完成任务。`@org.activiti.cdi.annotation.StartProcess` 注释允许通过“key”或“name”启动流程实例。请注意，流程实例是在带注释的方法返回后启动的。例子：

```java
@StartProcess("authorizeBusinessTripRequest")
public String submitRequest(BusinessTripRequest request) {
	// do some work
	return "success";
}
```

根据 Activiti 的配置，注解方法的代码和流程实例的启动会合并在同一个事务中。 `@org.activiti.cdi.annotation.CompleteTask`-annotation 的工作方式相同：

```JAVA
@CompleteTask(endConversation=false)
public String authorizeBusinessTrip() {
	// do some work
	return "success";
}
```

`@CompleteTask` 注释提供了结束当前对话的可能性。默认行为是在对 Activiti 的调用返回后结束对话。如上例所示，可以禁用结束对话。

#### 1.6从流程中引用 Bean

Activiti-cdi 使用自定义解析器将 CDI bean 暴露给 Activiti El。这使得从进程中引用 bean 成为可能：

```xml
<userTask id="authorizeBusinessTrip" name="Authorize Business Trip"
			activiti:assignee="#{authorizingManager.account.username}" />
```

其中“authorizingManager”可以是生产者方法提供的bean：

```java
@Inject	@ProcessVariable Object businessTripRequesterUsername;

@Produces
@Named
public Employee authorizingManager() {
	TypedQuery<Employee> query = entityManager.createQuery("SELECT e FROM Employee e WHERE e.account.username='"
		+ businessTripRequesterUsername + "'", Employee.class);
	Employee employee = query.getSingleResult();
	return employee.getManager();
}
```

我们可以使用相同的特性在服务任务中调用 EJB 的业务方法，使用 `activiti:expression="myEjb.method()"`-extension。请注意，这需要在 `MyEjb` 类上使用 `@Named`-annotation。

#### 1.7使用@BusinessProcessScoped

使用 activiti-cdi，可以将 bean 的生命周期绑定到流程实例。为此，提供了一个自定义上下文实现，即 BusinessProcessContext。 BusinessProcessScoped bean 的实例作为流程变量存储在当前流程实例中。BusinessProcessScoped bean 需要是 PassivationCapable（例如 Serializable）。以下是进程作用域 bean 的示例：

```java
@Named
@BusinessProcessScoped
public class BusinessTripRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private String startDate;
	private String endDate;
	// ...
}
```

有时，我们希望在没有与流程实例关联的情况下使用流程范围的 bean，例如在启动流程之前。如果当前没有流程实例处于活动状态，则 BusinessProcessScoped bean 的实例会临时存储在本地范围（即对话或请求，取决于上下文）中。如果此范围稍后与业务流程实例相关联，则 bean 实例将被刷新到流程实例。

#### 1.8注入流程变量

工艺变量可用于注射。 Activiti-CDI 支持

- 使用`@Inject \[additional qualifiers\] Type fieldName` 类型安全注入`@BusinessProcessScoped bean`
- 使用 `@ProcessVariable(name?)` 限定符不安全地注入其他流程变量：

```java
@Inject @ProcessVariable Object accountNumber;
@Inject @ProcessVariable("accountNumber") Object account
```

为了使用 EL 引用流程变量，我们有类似的选项：

- `@Named @BusinessProcessScoped` bean 可以直接引用，
- 可以使用 `ProcessVariables`-bean 引用其他流程变量：

```JAVA
#{processVariables['accountNumber']}
```

#### 1.9接收流程事件

[[EXPERIMENTAL\]](https://www.activiti.org/userguide/#experimental)

Activiti 可以连接到 CDI 事件总线。这允许我们使用标准的 CDI 事件机制来通知流程事件。为了开启Activiti的CDI事件支持，在配置中开启对应的解析监听器：

```xml
<property name="postBpmnParseHandlers">
	<list>
		<bean class="org.activiti.cdi.impl.event.CdiEventSupportBpmnParseHandler" />
	</list>
</property>
```

现在 Activiti 被配置为使用 CDI 事件总线发布事件。下面概述了如何在 CDI bean 中接收流程事件。在 CDI 中，我们可以使用 `@Observes`-annotation 声明性地指定事件观察者。事件通知是类型安全的。流程事件的类型是 `org.activiti.cdi.BusinessProcessEvent`。下面是一个简单的事件观察器方法的例子：

```JAVA
public void onProcessEvent(@Observes BusinessProcessEvent businessProcessEvent) {
	// handle event
}
```

该观察者将收到所有事件的通知。如果我们想限制观察者接收的事件集，我们可以添加限定符注释：

- `@BusinessProcess`: 将事件集限制为某个流程定义。例子：`@Observes @BusinessProcess("billingProcess") BusinessProcessEvent evt`
- `@StartActivity`: 通过某个活动限制事件集。例如：`@Observes @StartActivity("shipGoods") BusinessProcessEvent evt` 每当输入 ID 为“shipGoods”的活动时调用。
- `@EndActivity`: 通过某个活动限制事件集。例如：`@Observes @EndActivity("shipGoods") BusinessProcessEvent evt` 会在 id 为 "shipGoods" 的活动离开时被调用。
- `@TakeTransition`: 通过某个转换限制事件集。
- `@CreateTask`: 通过某个任务的创建来限制事件集。
- `@DeleteTask`: 通过删除某个任务来限制事件集。
- `@AssignTask`: 通过特定任务的分配来限制事件集。
- `@CompleteTask`: 通过特定任务的完成来限制事件集。

上面命名的限定符可以自由组合。例如，为了接收在“shipmentProcess”中离开“shipGoods”活动时生成的所有事件，我们可以编写以下观察者方法：

```java
public void beforeShippingGoods(@Observes @BusinessProcess("shippingProcess") @EndActivity("shipGoods") BusinessProcessEvent evt) {
	// handle event
}
```

在默认配置中，事件侦听器是在同一事务的上下文中同步调用的。CDI 事务观察器（仅与 JavaEE / EJB 结合使用）允许控制何时将事件传递给观察者方法。例如，使用事务观察者，我们可以确保只有在触发事件的事务成功时才通知观察者：

```java
public void onShipmentSuceeded(@Observes(during=TransactionPhase.AFTER_SUCCESS) @BusinessProcess("shippingProcess") @EndActivity("shipGoods") BusinessProcessEvent evt) {
	// send email to customer.
}
```

#### 1.10附加的功能

- ProcessEngine 以及可用于注入的服务：`@Inject ProcessEngine、RepositoryService、TaskService`，...
- 可以注入当前流程实例和任务：`@Inject ProcessInstance, Task`,
- 可以注入当前的业务key：`@Inject @BusinessKey String businessKey`，
- 当前注入的流程实例id：`@Inject @ProcessInstanceId String pid`,

### 2.已知限制

尽管 activiti-cdi 是针对 SPI 实现的并设计为“便携式扩展”，但它仅使用 Weld 进行测试。

## LDAP集成

公司通常已经拥有 LDAP 系统形式的用户和组存储。从 5.14 版本开始，Activiti 提供了一个开箱即用的解决方案，用于轻松配置 Activiti 应该如何与 LDAP 系统连接。

在 Activiti 5.14 之前，已经可以将 LDAP 与 Activiti 集成。但是，从 5.14 开始，配置已经简化了很多。但是，配置 LDAP 的旧方法仍然有效。更具体地说，简化的配置只是旧基础设施之上的一个包装器。

### 1.用法

要将 LDAP 集成代码添加到您的项目，只需将以下依赖项添加到您的 pom.xml：

```xml
<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-ldap</artifactId>
  <version>latest.version</version>
</dependency>
```

### 2.使用案例

LDAP 集成目前有两个主要用例：

- 允许通过 IdentityService 进行身份验证。当通过 IdentityService 做所有事情时，这可能很有用。
- 获取用户的组。这在例如查询任务以查看某个用户可以看到哪些任务（即具有候选组的任务）时很重要。

### 3.配置

通过在流程引擎配置的 `configurators` 部分中注入 `org.activiti.ldap.LDAPConfigurator` 的实例，可以将 LDAP 系统与 Activiti 集成。这个类是高度可扩展的：如果默认实现不适合用例，方法可以很容易地被覆盖，并且许多依赖 bean 是可插入的。

这是一个示例配置（注意：当然，以编程方式创建引擎时，这是完全相似的）。现在不要担心所有属性，我们将在下一节详细介绍它们。

```xml
<bean id="processEngineConfiguration" class="...SomeProcessEngineConfigurationClass">
        ...
        <property name="configurators">
          <list>
              <bean class="org.activiti.ldap.LDAPConfigurator">

                <!-- Server connection params -->
                <property name="server" value="ldap://localhost" />
                <property name="port" value="33389" />
                <property name="user" value="uid=admin, ou=users, o=activiti" />
                <property name="password" value="pass" />

                <!-- Query params -->
                <property name="baseDn" value="o=activiti" />
                <property name="queryUserByUserId" value="(&(objectClass=inetOrgPerson)(uid={0}))" />
                <property name="queryUserByFullNameLike" value="(&(objectClass=inetOrgPerson)(|({0}=*{1}*)({2}=*{3}*)))" />
                <property name="queryGroupsForUser" value="(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))" />

                <!-- Attribute config -->
                <property name="userIdAttribute" value="uid" />
                <property name="userFirstNameAttribute" value="cn" />
                <property name="userLastNameAttribute" value="sn" />
                <property name="userEmailAttribute" value="mail" />


                <property name="groupIdAttribute" value="cn" />
                <property name="groupNameAttribute" value="cn" />

              </bean>
          </list>
        </property>
    </bean>
```

### 4.属性

可以在 org.activiti.ldap.LDAPConfigurator 上设置以下属性：

```
.LDAP configuration properties
```

| Property name              | Description                                                  | Type                                                         | Default value                    |
| -------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------------------- |
| server                     | 可以访问 LDAP 系统的服务器。例如 ldap://localhost:33389      | String                                                       |                                  |
| port                       | 运行 LDAP 系统的端口                                         | int                                                          |                                  |
| user                       | 用于连接到 LDAP 系统的用户 ID                                | String                                                       |                                  |
| password                   | 用于连接到 LDAP 系统的密码                                   | String                                                       |                                  |
| initialContextFactory      | 用于连接到 LDAP 系统的 InitialContextFactory 名称            | String                                                       | com.sun.jndi.ldap.LdapCtxFactory |
| securityAuthentication     | 用于连接到 LDAP 系统的 java.naming.security.authentication 属性的值 | String                                                       | simple                           |
| customConnectionParameters | 允许设置所有没有专用设置器的 LDAP 连接参数。有关自定义属性，请参见例如 http://docs.oracle.com/javase/tutorial/jndi/ldap/jndi.html。例如，此类属性用于配置连接池、特定安全设置等。创建到 LDAP 系统的连接时，将提供所有提供的参数。 | Map<String, String>                                          |                                  |
| baseDn                     | 开始搜索用户和组的基本专有名称 (DN)                          | String                                                       |                                  |
| userBaseDn                 | 从中开始搜索用户的基本专有名称 (DN)。如果未提供，将使用 baseDn（见上文） | String                                                       |                                  |
| groupBaseDn                | 从中开始搜索组的基本专有名称 (DN)。如果未提供，将使用 baseDn（见上文） | String                                                       |                                  |
| searchTimeLimit            | 在 LDAP 中进行搜索时使用的超时（以毫秒为单位）               | long                                                         | one hour                         |
| queryUserByUserId          | 按 userId 搜索用户时执行的查询。例如： (&(objectClass=inetOrgPerson)(uid={0})) 这里将返回 LDAP 中所有类 inetOrgPerson 且具有匹配 uid 属性值的对象。如示例中所示，用户 ID 是使用 {0} 注入的。如果单独设置查询不足以满足您的特定 LDAP 设置，您也可以插入不同的 LDAPQueryBuilder，它允许进行更多定制，而不仅仅是查询。 | String                                                       |                                  |
| queryUserByFullNameLike    | 按全名搜索用户时执行的查询。例如： (& (objectClass=inetOrgPerson) ( | ({0}={1})({2}={3})) ) 在这里，将返回 LDAP 中具有类 inetOrgPerson 并且具有匹配的名字和姓氏值的所有对象。请注意，{0} 注入 firstNameAttribute（如上定义）、{1} 和 {3} 搜索文本以及 {2} lastNameAttribute。如果单独设置查询不足以满足您的特定 LDAP 设置，您也可以插入不同的 LDAPQueryBuilder，它允许进行更多定制，而不仅仅是查询。 | string                           |
|                            | queryGroupsForUser                                           | 搜索特定用户的组时执行的查询。例如： (&(objectClass=groupOfUniqueNames)(uniqueMember={0})) 在这里，将返回 LDAP 中所有具有 groupOfUniqueNames 类并且提供的 DN（匹配用户的 DN）是 uniqueMember 的对象。如示例中所示，用户 ID 是通过使用 {0} 注入的。如果单独设置查询不足以满足您的特定 LDAP 设置，您也可以插入不同的 LDAP QueryBuilder，这样可以进行更多自定义，而不仅仅是查询。 | string                           |
|                            | userIdAttribute                                              | 与用户 ID 匹配的属性名称。该属性用于查找 User 对象并且完成 LDAP 对象和 Activiti User 对象之间的映射。 | string                           |
|                            | userFirstNameAttribute                                       | 与用户名字匹配的属性的名称。该属性用于查找 User 对象并且完成 LDAP 对象和 Activiti User 对象之间的映射。 | string                           |
|                            | userLastNameAttribute                                        | 与用户姓氏匹配的属性名称。该属性用于查找 User 对象并且完成 LDAP 对象和 Activiti User 对象之间的映射。 | string                           |
|                            | groupIdAttribute                                             | 与组 ID 匹配的属性的名称。该属性在查找 Group 对象时使用，并且 LDAP 对象和 Activiti Group 对象之间的映射已完成。 | string                           |
|                            | groupNameAttribute                                           | 与组名匹配的属性名称。该属性在查找 Group 对象时使用，并且 LDAP 对象和 Activiti Group 对象之间的映射已完成。 | String                           |
|                            | groupTypeAttribute                                           | 与组类型匹配的属性的名称。该属性在查找 Group 对象时使用，并且 LDAP 对象和 Activiti Group 对象之间的映射已完成。 | String                           |

以下属性是当人们想要自定义默认行为或引入组缓存时：

高级属性

| Property name                | Description                                                  | Type                                              | Default value |
| ---------------------------- | ------------------------------------------------------------ | ------------------------------------------------- | ------------- |
| ldapUserManagerFactory       | 如果默认实现不合适，则设置 LDAPUserManagerFactory 的自定义实现。 | instance of LDAPUserManagerFactory                |               |
| ldapGroupManagerFactory      | 如果默认实现不合适，则设置 LDAPGroupManagerFactory 的自定义实现。 | instance of LDAPGroupManagerFactory               |               |
| ldapMemberShipManagerFactory | 如果默认实现不合适，则设置 LDAPMembershipManagerFactory 的自定义实现。请注意，这不太可能，因为成员资格通常在 LDAP 系统本身中进行管理。 | An instance of LDAPMembershipManagerFactory       |               |
| ldapQueryBuilder             | 如果默认实现不合适，请设置自定义查询构建器。当 LDAPUserManager 或 LDAPGroupManage} 对 LDAP 系统进行实际查询时，将使用 LDAPQueryBuilder 实例。默认实现使用在此实例上设置的属性，例如 queryGroupsForUser 和 queryUserById | An instance of org.activiti.ldap.LDAPQueryBuilder |               |
| groupCacheSize               | 允许设置组缓存的大小。这是一个 LRU 缓存，用于为用户缓存组，从而避免每次需要知道用户组时访问 LDAP 系统。如果该值小于零，则不会实例化缓存。默认设置为 -1，因此不进行缓存。 | int                                               | -1            |
| groupCacheExpirationTime     | 以毫秒为单位设置组缓存的过期时间。当获取特定用户的组时，如果组缓存存在，则组将在此属性中设置的时间内存储在此缓存中。例.当在 00:00 获取组并且过期时间为 30 分钟时，00:30 之后对该用户的任何组获取都不会来自缓存，而是从 LDAP 系统再次获取。同样，该用户在 00:00 - 00:30 之间完成的所有组提取都将来自缓存。 | long                                              | one hour      |

使用Active Directory时的注意事项：Activiti论坛上有人反馈说对于Activiti Directory，InitialDirContext需要设置为Context.REFERRAL。这可以通过如上所述的 customConnectionParameters 映射传递。

## Advanced

以下部分涵盖了 Activiti 的高级用例，这些用例超出了 BPMN 2.0 流程的典型执行。因此，建议对 Activiti 有一定的熟练程度和经验，以了解此处描述的主题。

### 1.Async Executor

在 Activiti 版本 5（从版本 5.17.0 开始）中，除了现有的作业执行器之外，还添加了 Async 执行器。许多 Activiti 用户和我们的基准测试证明，Async Executor 比旧作业执行器的性能更高。

在 Activiti 6（及更高版本）中，异步执行器是唯一可用的。对于版本 6，异步执行器被完全重构以获得最佳性能和可插拔性（同时仍与现有 API 兼容）。

#### 1.1.Async Executor设计

存在两种类型的作业：计时器（如属于用户任务的边界事件的那些）和async continuations（属于具有 activiti:async="true" 属性的服务任务）。

计时器是最容易解释的：它们以特定的截止日期保存在 ACT_RU_TIMER_JOB 表中。异步执行器中有一个线程会定期检查是否有新的计时器触发（即截止日期在当前时间之前）。发生这种情况时，计时器将被删除，并创建并插入一个异步作业。

在流程实例步骤的执行期间（这意味着在进行某些 API 调用期间），将在数据库中插入一个异步作业。如果当前 Activiti 引擎的异步执行器处于活动状态，则异步作业实际上已经被锁定。这意味着作业条目被插入到 ACT_RU_JOB 表中，并将设置一个锁所有者和一个锁到期时间。成功提交 API 调用时触发的事务侦听器会触发同一引擎的异步执行器来执行作业（因此保证数据在数据库中）。为此，异步执行器有一个（可配置的）线程池，线程将从中执行作业并异步继续进程。如果 Activiti 引擎没有启用 async executor，异步作业会被插入到 ACT_RU_JOB 表中而不会被锁定。

与检查新计时器的线程类似，异步执行器有一个获取新异步作业的线程。这些是存在于表中且未锁定的作业。该线程将为当前的 Activiti 引擎锁定这些作业并将其传递给异步执行器。

执行作业的线程池使用内存队列从中获取作业。当此队列已满（这是可配置的）时，作业将被解锁并重新插入其表中。这样，其他异步执行程序就可以取而代之。

如果作业执行期间发生异常，异步作业将转换为具有截止日期的计时器作业。它将像常规计时器作业一样被拾取并再次成为异步作业，很快就会重试。当一个作业被重试（可配置）次数并继续失败时，该作业被假定为死亡并移至 ACT_RU_DEADLETTER_JOB。*deadletter* 概念广泛用于各种其他系统。管理员现在需要检查失败作业的异常并决定最佳操作方案。

流程定义和流程实例可以暂停。与这些定义或实例相关的挂起作业放在 ACT_RU_SUSPENDED_JOB 表中，以确保获取作业的查询在其 where 子句中具有尽可能少的条件。

对于熟悉 job/async executor 旧实现的人来说，从上面可以清楚地看出一件事：主要目标是让获取查询尽可能简单。在过去（版本 6 之前），所有作业类型/状态都使用一个表，这使得 where 条件很大，因为它可以满足所有用例。这个问题现在已经解决，我们的基准测试证明这种新设计提供了更好的性能并且更具可扩展性。

#### 1.2Async executor配置

异步执行器是一个高度可配置的组件。始终建议查看异步执行器的默认设置并验证它们是否符合您的流程要求。

或者，可以扩展默认实现或使用您自己的实现实现 `org.activiti.engine.impl.asyncexecutor.AsyncExecutor` 接口。

以下属性可通过 setter 在流程引擎配置中使用：

异步执行器配置选项：

| Name                                        | Default value | Description                                                  |
| ------------------------------------------- | ------------- | ------------------------------------------------------------ |
| asyncExecutorThreadPoolQueueSize            | 100           | 执行的作业在被获取后，在它们被线程池中的线程实际执行之前放置在其上的队列的大小 |
| asyncExecutorCorePoolSize                   | 2             | 在线程池中为作业执行而保持活动状态的最小线程数。             |
| asyncExecutorMaxPoolSize                    | 10            | 在线程池中为作业执行创建的最大线程数。                       |
| asyncExecutorThreadKeepAliveTime            | 5000          | 用于作业执行的线程在销毁之前必须保持活动状态的时间（以毫秒为单位）。设置 大于 0 会占用资源，但在许多作业执行的情况下，它始终避免创建新线程。如果为 0，线程将在用于作业执行后被销毁。 |
| asyncExecutorNumberOfRetries                | 3             | 作业在移至*deadletter* 表之前重试的次数。                    |
| asyncExecutorMaxTimerJobsPerAcquisition     | 1             | 在一次获取查询期间获取的计时器作业数。默认值为 1，因为这降低了乐观锁异常的可能性。较大的值可以更好地执行，但不同引擎之间发生乐观锁异常的机会也变得更大。 |
| asyncExecutorMaxAsyncJobsDuePerAcquisition  | 1             | 在一次获取查询期间获取的异步作业数。默认值为 1，因为这降低了乐观锁异常的可能性。较大的值可以更好地执行，但不同引擎之间发生乐观锁异常的机会也变得更大。 |
| asyncExecutorDefaultTimerJobAcquireWaitTime | 10000         | 定时器获取线程等待执行下一个获取查询的时间（以毫秒为单位）。当未找到新的计时器作业或获取的计时器作业少于 asyncExecutorMaxTimerJobsPerAcquisition 中设置的计时器作业时，就会发生这种情况。 |
| asyncExecutorDefaultAsyncJobAcquireWaitTime | 10000         | 异步作业获取线程等待执行下一个获取查询的时间（以毫秒为单位）。当未找到新的异步作业或获取的异步作业少于 asyncExecutorMaxAsyncJobsDuePerAcquisition 中设置的数量时，就会发生这种情况。 |
| asyncExecutorDefaultQueueSizeFullWaitTime   | 0             | 当内部作业队列已满以执行下一个查询时，异步作业（定时器和异步延续）获取线程将等待的时间（以毫秒为单位）。默认设置为 0（为了向后兼容）。将此属性设置为更高的值允许异步执行程序希望稍微清除其队列。 |
| asyncExecutorTimerLockTimeInMillis          | 5 minutes     | 异步执行器获取计时器作业时锁定的时间量（以毫秒为单位）。在这段时间内，没有其他异步执行器会尝试获取和锁定此作业。 |
| asyncExecutorAsyncJobLockTimeInMillis       | 5 minutes     | 异步作业在被异步执行器获取时被锁定的时间量（以毫秒为单位）在这段时间内，没有其他异步执行器会尝试获取和锁定此作业。。 |
| asyncExecutorSecondsToWaitOnShutdown        | 60            | 当请求执行程序（或流程引擎）关闭时，等待正常关闭用于作业执行的线程池的时间（以秒为单位）。 |
| asyncExecutorResetExpiredJobsInterval       | 60 seconds    | 两次连续检查过期作业之间的时间量（以毫秒为单位）。过期作业是被锁定的作业（锁定所有者 + 时间由某个执行者写入，但作业从未完成）。在此类检查期间，过期的作业将再次可用，这意味着锁定所有者和锁定时间将被删除。其他执行者现在可以获得该执行。如果锁定时间在当前日期之前，则认为作业已过期。 |
| asyncExecutorResetExpiredJobsPageSize       | 3             | 异步执行器的重置过期线程一次获取的作业数量。                 |

#### 1.3基于消息队列的异步执行器

在阅读异步执行器设计部分时，很明显该架构受到消息队列的启发。异步执行器的设计方式是，可以轻松地使用消息队列来接管线程池的作业和异步作业的处理。

基准测试表明，在吞吐量方面，使用消息队列优于线程池支持的异步执行器。然而，它确实带有一个额外的架构组件，这当然会使设置、维护和监控更加复杂。对于许多用户来说，线程池支持的异步执行器的性能绰绰有余。然而，很高兴知道，如果所需的性能增长，还有另一种选择。

目前，唯一支持开箱即用的选项是 JMS 和 Spring。优先支持 Spring 的原因是因为 Spring 有一些非常好的特性，当涉及到线程和处理多个消息消费者时，这些特性减轻了很多痛苦。而且，集成非常简单，可以轻松移植到任何消息队列实现和（或）协议（Stomp、AMPQ 等）。对于下一个因该实现应该是什么，我们期待您的反馈。

当引擎创建一个新的异步作业时，一条消息被放置在包含作业标识符的消息队列中（在事务提交的事务侦听器中，因此我们确定的作业条目在数据库中）。消息使用者然后使用此作业标识符来获取作业并执行作业。异步执行器将不再创建线程池。它将从一个单独的线程插入和查询计时器。当计时器触发时，它被移动到异步作业表，这意味着消息也被发送到消息队列。重置过期线程也将像往常一样解锁作业，因为消息队列也可能失败。现在将重新发送消息，而不是解锁工作。异步执行器将不再轮询异步作业。

实现由两个类组成：

- `org.activiti.engine.impl.asyncexecutor.JobManager` 接口的一个实现，它将消息放入消息队列而不是将其传递到线程池。
- 使用消息队列中的消息的 `javax.jms.MessageListener` 实现，使用消息中的作业标识符来获取和执行作业。

首先，将 activiti-jms-spring-executor 依赖添加到您的项目中：

```xml
<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-jms-spring-executor</artifactId>
  <version>${activiti.version}</version>
</dependency>
```

要启用基于消息队列的异步执行器，在流程引擎配置中，需要做以下工作：

- asyncExecutorActivate 必须像之前一样设置为 true
- asyncExecutorMessageQueueMode 需要设置为 true
- org.activiti.spring.executor.jms.MessageBasedJobManager 必须作为 JobManager 注入

下面是一个基于 Java 的配置的完整示例，使用 ActiveMQ 作为消息队列代理。

一些注意事项：

- MessageBasedJobManager 需要注入一个配置了正确 connectionFactory 的 JMSTemplate。
- 我们正在使用 Spring 的 MessageListenerContainer 概念，因为这大大简化了线程和多个消费者。

```java
@Configuration
public class SpringJmsConfig {

  @Bean
  public DataSource dataSource() {
    // Omitted
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager() {
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
    transactionManager.setDataSource(dataSource());
    return transactionManager;
  }

  @Bean
  public SpringProcessEngineConfiguration processEngineConfiguration() {
    SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
    configuration.setDataSource(dataSource());
    configuration.setTransactionManager(transactionManager());
    configuration.setDatabaseSchemaUpdate(SpringProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    configuration.setAsyncExecutorMessageQueueMode(true);
    configuration.setAsyncExecutorActivate(true);
    configuration.setJobManager(jobManager());
    return configuration;
  }

  @Bean
  public ProcessEngine processEngine() {
    return processEngineConfiguration().buildProcessEngine();
  }

  @Bean
  public MessageBasedJobManager jobManager() {
    MessageBasedJobManager jobManager = new MessageBasedJobManager();
    jobManager.setJmsTemplate(jmsTemplate());
    return jobManager;
  }

  @Bean
  public ConnectionFactory connectionFactory() {
      ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
      activeMQConnectionFactory.setUseAsyncSend(true);
      activeMQConnectionFactory.setAlwaysSessionAsync(true);
      return new CachingConnectionFactory(activeMQConnectionFactory);
  }

  @Bean
  public JmsTemplate jmsTemplate() {
      JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setDefaultDestination(new ActiveMQQueue("activiti-jobs"));
      jmsTemplate.setConnectionFactory(connectionFactory());
      return jmsTemplate;
  }

  @Bean
  public MessageListenerContainer messageListenerContainer() {
      DefaultMessageListenerContainer messageListenerContainer = new DefaultMessageListenerContainer();
      messageListenerContainer.setConnectionFactory(connectionFactory());
      messageListenerContainer.setDestinationName("activiti-jobs");
      messageListenerContainer.setMessageListener(jobMessageListener());
      messageListenerContainer.setConcurrentConsumers(2);
      messageListenerContainer.start();
      return messageListenerContainer;
  }

  @Bean
  public JobMessageListener jobMessageListener() {
    JobMessageListener jobMessageListener = new JobMessageListener();
    jobMessageListener.setProcessEngineConfiguration(processEngineConfiguration());
    return jobMessageListener;
  }

}
```

在上面的代码中，JobMessageListener 和 MessageBasedJobManager 是来自 activiti-jms-spring-executor 模块的唯一类。所有其他代码都来自 Spring。因此，当想要将其移植到其他队列/协议时，必须移植这些类。

### 2.连接到流程解析

一个 BPMN 2.0 xml 需要解析为 Activiti 内部模型才能在 Activiti 引擎上执行。这种解析发生在进程部署期间或在内存中找不到进程时，并且从数据库中获取 xml。

对于这些进程中的每一个，`BpmnParser` 类都会创建一个新的 `BpmnParse` 实例。此实例将用作解析期间完成的所有事情的容器。解析本身非常简单：对于每个 BPMN 2.0 元素，引擎中都有一个匹配的 `org.activiti.engine.parse.BpmnParseHandler` 实例。因此，解析器有一个映射，它基本上将 BPMN 2.0 元素类映射到 `BpmnParseHandler` 的实例。默认情况下，Activiti 有 `BpmnParseHandler` 实例来处理所有支持的元素，并使用它来将执行监听器附加到流程的步骤以创建历史记录。

可以将 ·org.activiti.engine.parse.BpmnParseHandler· 的自定义实例添加到 Activiti 引擎中。例如，一个常见的用例是向某些步骤添加执行监听器，这些步骤将事件触发到某个队列以进行事件处理。历史处理是在 Activiti 内部以这种方式完成的。要添加此类自定义处理程序，需要调整 Activiti 配置：

```xml
<property name="preBpmnParseHandlers">
  <list>
    <bean class="org.activiti.parsing.MyFirstBpmnParseHandler" />
  </list>
</property>

<property name="postBpmnParseHandlers">
  <list>
    <bean class="org.activiti.parsing.MySecondBpmnParseHandler" />
    <bean class="org.activiti.parsing.MyThirdBpmnParseHandler" />
  </list>
</property>
```

在 `preBpmnParseHandlers` 属性中配置的 `BpmnParseHandler` 实例列表添加在任何默认处理程序之前。同样，在这些之后添加 `postBpmnParseHandlers`。如果事物的顺序对自定义解析处理程序中包含的逻辑很重要。

`org.activiti.engine.parse.BpmnParseHandler` 是一个简单的接口：

```java
public interface BpmnParseHandler {

  Collection<Class>? extends BaseElement>> getHandledTypes();

  void parse(BpmnParse bpmnParse, BaseElement element);

}
```

`getHandledTypes()` 方法返回此解析器处理的所有类型的集合。可能的类型是 `BaseElement` 的子类，由集合的泛型类型指示。您还可以扩展 `AbstractBpmnParseHandler` 类并覆盖 `getHandledType()` 方法，该方法只返回一个类而不是集合。此类还包含许多默认解析处理程序共享的一些辅助方法。当解析器遇到此方法返回的任何类型时，将调用 `BpmnParseHandler` 实例。在下面的示例中，每当遇到 BPMN 2.0 xml 中包含的流程时，它都会执行 `executeParse` 方法中的逻辑（这是一个类型转换的方法，用于替换 `BpmnParseHandler` 接口上的常规解析方法）。

```JAVA
public class TestBPMNParseHandler extends AbstractBpmnParseHandler<Process> {

  protected Class<? extends BaseElement> getHandledType() {
    return Process.class;
  }

  protected void executeParse(BpmnParse bpmnParse, Process element) {
     ..
  }

}
```

**重要说明：**在编写自定义解析处理程序时，不要使用任何用于解析 BPMN 2.0 构造的内部类。这将导致难以发现错误。实现自定义处理程序的安全方法是实现 BpmnParseHandler 接口或扩展内部抽象类 `org.activiti.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler`。

可以（但不太常见）将负责解析 BPMN 2.0 元素的默认 `BpmnParseHandler` 实例替换为内部 Activiti 模型。这可以通过以下逻辑片段来完成：

```XML
<property name="customDefaultBpmnParseHandlers">
  <list>
    ...
  </list>
</property>
```

例如，一个简单的例子可以是强制所有服务任务异步：

```java
public class CustomUserTaskBpmnParseHandler extends ServiceTaskParseHandler {

  protected void executeParse(BpmnParse bpmnParse, ServiceTask serviceTask) {

    // Do the regular stuff
    super.executeParse(bpmnParse, serviceTask);

    // Make always async
    ActivityImpl activity = findActivity(bpmnParse, serviceTask.getId());
    activity.setAsync(true);
  }

}
```

### 3.用于高并发的 UUID id 生成器

在某些（非常）高并发负载情况下，默认 id 生成器可能会由于无法足够快地获取新的 id 块而导致异常。每个流程引擎都有一个 id 生成器。默认的 id 生成器在数据库中保留了一个 id 块，这样其他引擎就不能使用同一个块中的 id。在引擎运行期间，当默认的 id 生成器注意到 id 块用完时，将启动一个新事务以获取新块。在（非常）有限的用例中，当负载非常高时，这可能会导致问题。对于大多数用例，默认 id 生成器绰绰有余。默认的 `org.activiti.engine.impl.db.DbIdGenerator` 也有一个属性 idBlockSize 可以配置它来设置保留的 id 块的大小并调整 id 获取的行为。

默认 id 生成器的替代方案是 `org.activiti.engine.impl.persistence.StrongUuidGenerator`，它在本地生成唯一的 UUID 并将其用作所有实体的标识符。由于 UUID 是在不需要访问数据库的情况下生成的，因此它可以更好地处理并发性非常高的用例。请注意，性能可能与默认 id 生成器（正负）不同，具体取决于机器。

UUID生成器可以在activiti配置中配置如下：

```java
<property name="idGenerator">
    <bean class="org.activiti.engine.impl.persistence.StrongUuidGenerator" />
</property>
```

UUID id 生成器的使用取决于以下额外的依赖：

```xml
<dependency>
    <groupId>com.fasterxml.uuid</groupId>
    <artifactId>java-uuid-generator</artifactId>
    <version>3.1.3</version>
</dependency>
```

### 4.多租户

多租户一般是一个概念，其中软件能够为多个不同的组织提供服务。关键是数据是分区的，没有组织可以看到其他组织的数据。在这种情况下，这样的组织（或部门、团队或……）被称为租户。

请注意，这与多实例设置有根本的不同，在多实例设置中，Activiti Process Engine 实例为每个组织单独运行（并且具有不同的数据库模式）。尽管 Activiti 是轻量级的，并且运行 Process Engine 实例不会占用太多资源，但它确实增加了复杂性和更多的维护。但是，对于某些用例，它可能是正确的解决方案。

Activiti 中的多租户主要是围绕对数据进行分区来实现的。需要注意的是，Activiti 不强制执行多租户规则。这意味着它在查询和使用数据时不会验证执行操作的用户是否属于正确的租户。这应该在调用 Activiti 引擎的层中完成。Activiti 确实确保在检索过程数据时可以存储和使用租户信息。

将流程定义部署到 Activiti Process Engine 时，可以传递租户标识符。这是一个字符串（例如 UUID、部门 ID 等），限制为 256 个字符，用于唯一标识租户：

```java
repositoryService.createDeployment()
            .addClassPathResource(...)
            .tenantId("myTenantId")
            .deploy();
```

在部署期间传递租户 ID 具有以下含义：

- 部署中包含的所有流程定义都从该部署继承租户标识符。
- 从这些流程定义启动的所有流程实例都从流程定义继承此租户标识符。
- 在执行流程实例时在运行时创建的所有任务都从流程实例继承此租户标识符。独立任务也可以有租户标识符。
- 在流程实例执行期间创建的所有执行都从流程实例继承此租户标识符。
- 可以在提供租户标识符的同时触发信号抛出事件（在进程本身或通过 API）。信号只会在租户上下文中执行：即，如果有多个具有相同名称的信号捕获事件，则只会实际调用具有正确租户标识符的事件。
- 所有作业（计时器和异步延续）都从流程定义（例如计时器启动事件）或流程实例（在运行时创建作业时，例如异步延续）继承租户标识符。这可能用于在自定义作业执行程序中为某些租户提供优先级。
- 所有历史实体（历史流程实例、任务和活动）从它们的运行时对应物继承租户标识符。
- 作为旁注，模型也可以有一个租户标识符（模型被 Activiti Modeler 用来存储 BPMN 2.0 模型）。

为了在流程数据上实际使用租户标识符，所有查询 API 都具有对租户进行过滤的能力。例如（并且可以替换为其他实体的相关查询实现）：

```java
runtimeService.createProcessInstanceQuery()
    .processInstanceTenantId("myTenantId")
    .processDefinitionKey("myProcessDefinitionKey")
    .variableValueEquals("myVar", "someValue")
    .list()
```

查询 API 还允许过滤具有相似语义的租户标识符，并过滤掉没有租户 ID 的实体。

**重要的实现细节：**由于数据库怪癖（更具体地说：唯一约束中的空处理），指示没有租户的默认租户标识符值是空字符串。（流程定义键、流程定义版本、租户标识符）的组合需要是唯一的（并且有一个数据库约束检查这一点）。另请注意，不应将租户标识符设置为空，因为这会影响查询，因为某些数据库 (Oracle) 将空字符串视为空值（这就是查询 .withoutTenantId 对空字符串或空值进行检查的原因） 。这意味着可以为多个租户部署相同的流程定义（具有相同的流程定义键），每个租户都有自己的版本控制。不使用租约时，这不影响使用。

请注意，以上所有内容与在集群中运行多个 Activiti 实例并不冲突。

[Experimental] 可以通过调用repositoryService 上的changeDeploymentTenantId(String deploymentId, String newTenantId) 方法来更改租户标识符。这将更改之前继承的租户标识符。这在从非多租户设置转为多租户配置时非常有用。有关更多详细信息，请参阅有关该方法的 Javadoc。

### 5.执行自定义 SQL

Activiti API 允许使用高级 API 与数据库交互。例如，对于检索数据，Query API 和 Native Query API 的使用非常强大。但是，对于某些用例，它们可能不够灵活。以下部分描述了如何针对 Activiti 数据存储执行完全自定义的 SQL 语句（查询、插入、更新和删除是可能的），但完全在配置的流程引擎中执行（例如利用事务设置）。

为了定义自定义 SQL 语句，Activiti 引擎利用了其底层框架 MyBatis 的功能。更多信息可以在 MyBatis 用户指南中阅读。

#### 5.1基于注释的映射SQL语句

使用基于注解的映射语句时要做的第一件事是创建一个 MyBatis 映射器类。例如，假设对于某些用例，不需要整个任务数据，而只需要其中的一小部分。可以执行此操作的 Mapper 如下所示：

```java
public interface MyTestMapper {

    @Select("SELECT ID_ as id, NAME_ as name, CREATE_TIME_ as createTime FROM ACT_RU_TASK")
    List<Map<String, Object>> selectTasks();

}
```

此映射器必须提供给流程引擎配置，如下所示：

```java
...
<property name="customMybatisMappers">
  <set>
    <value>org.activiti.standalone.cfg.MyTestMapper</value>
  </set>
</property>
...
```

请注意，这是一个接口。底层的 MyBatis 框架将创建一个可以在运行时使用的实例。另请注意，该方法的返回值不是类型化的，而是映射列表（对应于具有列值的行列表）。如果需要，可以使用 MyBatis 映射器进行输入。

要执行上面的查询，必须使用 managementService.executeCustomSql 方法。此方法接受一个 CustomSqlExecution 实例。这是一个包装器，用于隐藏引擎的内部位，否则需要使其工作。

不幸的是，Java 泛型使它的可读性比它本来的要差一些。下面的两个泛型类型是映射器类和返回类型类。但是，实际的逻辑只是调用映射器方法并返回其结果（如果适用）。

```java
CustomSqlExecution<MyTestMapper, List<Map<String, Object>>> customSqlExecution =
          new AbstractCustomSqlExecution<MyTestMapper, List<Map<String, Object>>>(MyTestMapper.class) {

  public List<Map<String, Object>> execute(MyTestMapper customMapper) {
    return customMapper.selectTasks();
  }

};

List<Map<String, Object>> results = managementService.executeCustomSql(customSqlExecution);
```

在这种情况下，上面列表中的 Map 条目将仅包含 id、名称和创建时间，而不是完整的任务对象。 使用上述方法时，任何 SQL 都是可能的。另一个更复杂的例子：

```java
@Select({
        "SELECT task.ID_ as taskId, variable.LONG_ as variableValue FROM ACT_RU_VARIABLE variable",
        "inner join ACT_RU_TASK task on variable.TASK_ID_ = task.ID_",
        "where variable.NAME_ = #{variableName}"
    })
    List<Map<String, Object>> selectTaskWithSpecificVariable(String variableName);
```

使用这种方法，任务表将与变量表连接。只保留变量有一定名字的地方，返回任务id和对应的数值。

有关使用基于注释的映射语句的工作示例，请检查单元测试 org.activiti.standalone.cfg.CustomMybatisMapperTest 以及文件夹 src/test/java/org/activiti/standalone/cfg/ 和 src/test/resources 中的其他类和资源/org/activiti/standalone/cfg/

#### 5.2基于 XML 的映射语句

使用基于 XML 的映射语句时，语句在 XML 文件中定义。对于不需要整个任务数据，而只需要其中一小部分的用例。 XML 文件如下所示：

```xml
<mapper namespace="org.activiti.standalone.cfg.TaskMapper">

  <resultMap id="customTaskResultMap" type="org.activiti.standalone.cfg.CustomTask">
    <id property="id" column="ID_" jdbcType="VARCHAR"/>
    <result property="name" column="NAME_" jdbcType="VARCHAR"/>
    <result property="createTime" column="CREATE_TIME_" jdbcType="TIMESTAMP" />
  </resultMap>

  <select id="selectCustomTaskList" resultMap="customTaskResultMap">
    select RES.ID_, RES.NAME_, RES.CREATE_TIME_ from ACT_RU_TASK RES
  </select>

</mapper>
```

结果映射到 org.activiti.standalone.cfg.CustomTask 类的实例，如下所示：

```java
public class CustomTask {

  protected String id;
  protected String name;
  protected Date createTime;

  public String getId() {
    return id;
  }
  public String getName() {
    return name;
  }
  public Date getCreateTime() {
    return createTime;
  }
}
```

映射器 XML 文件必须提供给流程引擎配置，如下所示：

```xml
...
<property name="customMybatisXMLMappers">
  <set>
    <value>org/activiti/standalone/cfg/custom-mappers/CustomTaskMapper.xml</value>
  </set>
</property>
...
```

该语句可以按如下方式执行：

```java
List<CustomTask> tasks = managementService.executeCommand(new Command<List<CustomTask>>() {

      @SuppressWarnings("unchecked")
      @Override
      public List<CustomTask> execute(CommandContext commandContext) {
        return (List<CustomTask>) commandContext.getDbSqlSession().selectList("selectCustomTaskList");
      }
    });
```

对于需要更复杂语句的用例，XML Mapped Statements 可能会有所帮助。由于 Activiti 在内部使用 XML Mapped Statements，因此可以利用底层功能。

假设对于某些用例，需要根据 id、name、type、userId 等查询条件数据的能力！为了实现用例，可以按如下方式创建扩展 org.activiti.engine.impl.AbstractQuery 的查询类 AttachmentQuery：

```java
public class AttachmentQuery extends AbstractQuery<AttachmentQuery, Attachment> {

  protected String attachmentId;
  protected String attachmentName;
  protected String attachmentType;
  protected String userId;

  public AttachmentQuery(ManagementService managementService) {
    super(managementService);
  }

  public AttachmentQuery attachmentId(String attachmentId){
    this.attachmentId = attachmentId;
    return this;
  }

  public AttachmentQuery attachmentName(String attachmentName){
    this.attachmentName = attachmentName;
    return this;
  }

  public AttachmentQuery attachmentType(String attachmentType){
    this.attachmentType = attachmentType;
    return this;
  }

  public AttachmentQuery userId(String userId){
    this.userId = userId;
    return this;
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    return (Long) commandContext.getDbSqlSession()
                   .selectOne("selectAttachmentCountByQueryCriteria", this);
  }

  @Override
  public List<Attachment> executeList(CommandContext commandContext, Page page) {
    return commandContext.getDbSqlSession()
            .selectList("selectAttachmentByQueryCriteria", this);
  }
```

请注意，在扩展 AbstractQuery 扩展类时，应将 ManagementService 的实例传递给超级构造函数，并且需要实现方法 executeCount 和 executeList 以调用映射语句。

包含映射语句的 XML 文件如下所示：

```xml
<mapper namespace="org.activiti.standalone.cfg.AttachmentMapper">

  <select id="selectAttachmentCountByQueryCriteria" parameterType="org.activiti.standalone.cfg.AttachmentQuery" resultType="long">
    select count(distinct RES.ID_)
    <include refid="selectAttachmentByQueryCriteriaSql"/>
  </select>

  <select id="selectAttachmentByQueryCriteria" parameterType="org.activiti.standalone.cfg.AttachmentQuery" resultMap="org.activiti.engine.impl.persistence.entity.AttachmentEntity.attachmentResultMap">
    ${limitBefore}
    select distinct RES.* ${limitBetween}
    <include refid="selectAttachmentByQueryCriteriaSql"/>
    ${orderBy}
    ${limitAfter}
  </select>

  <sql id="selectAttachmentByQueryCriteriaSql">
  from ${prefix}ACT_HI_ATTACHMENT RES
  <where>
   <if test="attachmentId != null">
     RES.ID_ = #{attachmentId}
   </if>
   <if test="attachmentName != null">
     and RES.NAME_ = #{attachmentName}
   </if>
   <if test="attachmentType != null">
     and RES.TYPE_ = #{attachmentType}
   </if>
   <if test="userId != null">
     and RES.USER_ID_ = #{userId}
   </if>
  </where>
  </sql>
</mapper>
```

分页、排序、表名前缀等功能都可用，并可在语句中使用（因为 parameterType 是 AbstractQuery 的子类）。请注意，要映射结果，可以使用预定义的 org.activiti.engine.impl.persistence.entity.AttachmentEntity.attachmentResultMap resultMap。

最后，可以按如下方式使用 AttachmentQuery：

```java
....
// Get the total number of attachments
long count = new AttachmentQuery(managementService).count();

// Get attachment with id 10025
Attachment attachment = new AttachmentQuery(managementService).attachmentId("10025").singleResult();

// Get first 10 attachments
List<Attachment> attachments = new AttachmentQuery(managementService).listPage(0, 10);

// Get all attachments uploaded by user kermit
attachments = new AttachmentQuery(managementService).userId("kermit").list();
....
```

有关使用 XML 映射语句的工作示例，请检查单元测试 org.activiti.standalone.cfg.CustomMybatisXMLMapperTest 以及文件夹 src/test/java/org/activiti/standalone/cfg/ 和 src/test/resources/org/activiti/standalone/cfg/

### 6.使用 ProcessEngineConfigurator 的高级流程引擎配置

挂钩流程引擎配置的一种高级方法是使用 ProcessEngineConfigurator。这个想法是创建 org.activiti.engine.cfg.ProcessEngineConfigurator 接口的实现并将其注入流程引擎配置：

```xml
<bean id="processEngineConfiguration" class="...SomeProcessEngineConfigurationClass">

    ...

    <property name="configurators">
        <list>
            <bean class="com.mycompany.MyConfigurator">
                ...
            </bean>
        </list>
    </property>

    ...

</bean>
```

实现这个接口需要两种方法。 configure 方法，它获取 ProcessEngineConfiguration 实例作为参数。可以通过这种方式添加自定义配置，并且该方法将保证在创建流程引擎之前调用，但在所有默认配置完成之后。另一种方法是 getPriority 方法，它允许在某些配置器相互依赖的情况下对配置器进行排序。

此类配置器的一个示例是 LDAP 集成，其中配置器用于将默认用户和组管理器类替换为能够处理 LDAP 用户存储的类。因此，基本上配置器允许对流程引擎进行大量更改或调整，并且适用于非常高级的用例。另一个例子是用自定义版本交换流程定义缓存：

```java
public class ProcessDefinitionCacheConfigurator extends AbstractProcessEngineConfigurator {

    public void configure(ProcessEngineConfigurationImpl processEngineConfiguration) {
            MyCache myCache = new MyCache();
            processEngineConfiguration.setProcessDefinitionCache(enterpriseProcessDefinitionCache);
    }

}
```

还可以使用 ServiceLoader 方法从类路径中自动发现流程引擎配置器。这意味着必须将具有配置器实现的 jar 放在类路径上，在该 jar 的 META-INF/services 文件夹中包含一个名为 org.activiti.engine.cfg.ProcessEngineConfigurator 的文件。该文件的内容需要是自定义实现的完全限定类名。当流程引擎启动时，日志将显示找到这些配置器：

```
INFO  org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl  - Found 1 auto-discoverable Process Engine Configurators
INFO  org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl  - Found 1 Process Engine Configurators in total:
INFO  org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl  - class org.activiti.MyCustomConfigurator
```

请注意，这种 ServiceLoader 方法在某些环境中可能不起作用。可以使用 ProcessEngineConfiguration 的 enableConfiguratorServiceLoader 属性（默认为 true）显式禁用它。

### 7.高级查询API：运行时查询和历史任务查询无缝切换

任何 BPM 用户界面的一个核心组件是任务列表。通常，最终用户处理开放的运行时任务，使用各种设置过滤他们的收件箱。通常，历史任务也需要显示在这些列表中，并进行类似的过滤。为了使代码更容易，TaskQuery 和 HistoricTaskInstanceQuery 都有一个共享的父接口，其中包含所有常见操作（并且大多数操作都是常见的）。

这个通用接口是 org.activiti.engine.task.TaskInfoQuery 类。org.activiti.engine.task.Task 和 org.activiti.engine.task.HistoricTaskInstance 都有一个共同的超类 org.activiti.engine.task.TaskInfo（具有共同的属性），它是从 e.g. 返回的。 list() 方法。然而，Java 泛型有时弊大于利：如果你想直接使用 TaskInfoQuery 类型，它看起来像这样：

```java
TaskInfoQuery<? extends TaskInfoQuery<?,?>, ? extends TaskInfo> taskInfoQuery
```

嗯，没错。为了解决这个问题，可以使用 org.activiti.engine.task.TaskInfoQueryWrapper 类来避免泛型（以下代码可能来自返回任务列表的 REST 代码，用户可以在其中切换打开和已完成的任务）：

```java
TaskInfoQueryWrapper taskInfoQueryWrapper = null;
if (runtimeQuery) {
	taskInfoQueryWrapper = new TaskInfoQueryWrapper(taskService.createTaskQuery());
} else {
	taskInfoQueryWrapper = new TaskInfoQueryWrapper(historyService.createHistoricTaskInstanceQuery());
}

List<? extends TaskInfo> taskInfos = taskInfoQueryWrapper.getTaskInfoQuery().or()
	.taskNameLike("%k1%")
	.taskDueAfter(new Date(now.getTime() + (3 * 24L * 60L * 60L * 1000L)))
.endOr()
.list();
```

### 8.通过覆写标准 SessionFactory 自定义身份管理

如果您不想像在 LDAP 集成中那样使用完整的 ProcessEngineConfigurator 实现，但仍想插入您的自定义身份管理框架，那么您还可以直接在 ProcessEngineConfiguration 中覆盖 SessionFactory 类。在 Spring 中，这可以通过将以下内容添加到 ProcessEngineConfiguration bean 定义中来轻松完成：

```java
<bean id="processEngineConfiguration" class="...SomeProcessEngineConfigurationClass">

    ...

    <property name="customSessionFactories">
        <list>
            <bean class="com.mycompany.MyGroupManagerFactory"/>
            <bean class="com.mycompany.MyUserManagerFactory"/>
        </list>
    </property>

    ...

</bean>
```

MyGroupManagerFactory 和 MyUserManagerFactory 需要实现 org.activiti.engine.impl.interceptor.SessionFactory 接口。对 openSession() 的调用返回进行实际身份管理的自定义类实现。对于组，这是一个继承自 org.activiti.engine.impl.persistence.entity.GroupEntityManager 的类，对于管理用户，它必须继承自 org.activiti.engine.impl.persistence.entity.UserEntityManager。以下代码示例包含组的自定义管理器工厂：

```java
package com.mycompany;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;

public class MyGroupManagerFactory implements SessionFactory {

	@Override
	public Class<?> getSessionType() {
		return GroupIdentityManager.class;
	}

	@Override
	public Session openSession() {
		return new MyCompanyGroupManager();
	}

}
```

由工厂创建的 MyCompanyGroupManager 正在执行实际工作。不过，您不需要覆盖 GroupEntityManager 的所有成员，只需覆盖您的用例所需的成员。以下示例提供了这可能是什么样子的指示（仅显示了部分成员）：

```java
public class MyCompanyGroupManager extends GroupEntityManager {

    private static Logger log = LoggerFactory.getLogger(MyCompanyGroupManager.class);

    @Override
    public List<Group> findGroupsByUser(String userId) {
        log.debug("findGroupByUser called with userId: " + userId);
        return super.findGroupsByUser(userId);
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        log.debug("findGroupByQueryCriteria called, query: " + query + " page: " + page);
        return super.findGroupByQueryCriteria(query, page);
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        log.debug("findGroupCountByQueryCriteria called, query: " + query);
        return super.findGroupCountByQueryCriteria(query);
    }

    @Override
    public Group createNewGroup(String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteGroup(String groupId) {
        throw new UnsupportedOperationException();
    }
}
```

在适当的方法中添加您自己的实现以插入您自己的身份管理解决方案。您必须弄清楚必须覆盖基类的哪个成员。例如下面的调用：

```java
long potentialOwners = identityService.createUserQuery().memberOfGroup("management").count();
```

导致调用 UserIdentityManager 接口的以下成员：

```java
List<User> findUserByQueryCriteria(UserQueryImpl query, Page page);
```

LDAP 集成的代码包含如何实现这一点的完整示例。查看 Github 上的代码，特别是以下类 LDAPGroupManager 和 LDAPUserManager。

### 9.启用安全的 BPMN 2.0 xml

在大多数情况下，正在部署到 Activiti 引擎的 BPMN 2.0 流程受到严格控制，例如开发团队。但是，在某些用例中，可能需要将任意 BPMN 2.0 xml 上传到引擎。在这种情况下，考虑到恶意用户可能会按照此处所述关闭服务器。

为避免上述链接中描述的攻击，可以在流程引擎配置上设置属性 enableSafeBpmnXml：

```xml
<property name="enableSafeBpmnXml" value="true"/>
```

默认情况下，此功能是禁用的！这样做的原因是它依赖于 StaxSource 类的可用性。不幸的是，在某些平台（例如 JDK 6、JBoss 等）上，此类不可用（由于较旧的 xml 解析器实现），因此无法启用安全的 BPMN 2.0 xml 功能。

如果 Activiti 运行的平台确实支持它，请启用此功能。

### 10.事件日志记录（实验性）

从 Activiti 5.16 版开始，引入了（实验性）事件日志记录机制。日志机制建立在 Activiti 引擎的通用事件机制之上，默认情况下是禁用的。这个想法是捕获源自引擎的事件，并创建一个包含所有事件数据（以及更多）的映射并将其提供给 org.activiti.engine.impl.event.logger.EventFlusher，它将刷新这些数据到别处。默认情况下，使用简单的数据库支持的事件处理程序/刷新器，它使用 Jackson 将所述映射序列化为 JSON，并将其作为 EventLogEntryEntity 实例存储在数据库中。此数据库日志记录所需的表是默认创建的（称为 ACT_EVT_LOG）。如果未使用事件日志记录，则可以删除此表。

要启用数据库记录器：

```java
processEngineConfiguration.setEnableDatabaseEventLogging(true);
```

或在运行时：

```java
databaseEventLogger = new EventLogger(processEngineConfiguration.getClock());
runtimeService.addEventListener(databaseEventLogger);
```

EventLogger 类可以被子类化。特别是，如果不需要默认的数据库日志记录，createEventFlusher() 方法需要返回 org.activiti.engine.impl.event.logger.EventFlusher 接口的实例。managementService.getEventLogEntries(startLogNr, size);可用于通过 Activiti 检索 EventLogEntryEntity 实例。

很容易看出现在如何使用这些表数据将 JSON 输入到大数据 NoSQL 存储中，例如 MongoDB、Elastic Search 等。也很容易看出这里使用的类（org.activiti.engine. impl.event.logger.EventLogger/EventFlusher 和许多 EventHandler 类）是可扩展摒弃，可以根据您自己的用例进行调整（例如，不将 JSON 存储在数据库中，而是将其直接触发到队列或大数据存储中）。

请注意，此事件日志记录机制是 Activiti 传统历史记录管理器的补充。

尽管所有数据都在数据库表中，但它并未针对查询或轻松检索进行优化。真正的用例是审计跟踪并将其输入大数据存储。

### 11.禁用批量插入

默认情况下，引擎会将同一数据库表的多个插入语句组合在一起进行批量插入，从而提高性能。这已经针对所有支持的数据库进行了测试和实施。

但是，它可能是受支持和测试的数据库的特定版本不允许批量插入（例如，我们有针对 z/OS 上的 DB2 的报告，尽管 DB2 在一般情况下可以工作），可以在流程引擎上禁用批量插入配置：

```xml
<property name="bulkInsertEnabled" value="false" />
```

### 12.脚本安全

实验性：安全脚本功能已作为 Activiti 5.21 版本的一部分添加。

默认情况下，在使用脚本任务时，执行的脚本具有与 Java 委托类似的功能。它可以完全访问 JVM，可以永远运行（无限循环）或使用大量内存。但是，Java 委托需要编写并放在 jar 中的类路径上，并且它们与流程定义具有不同的生命周期。最终用户通常不会编写 Java 委托，因为这是开发人员的工作。

另一方面，脚本是流程定义的一部分，其生命周期是相同的。脚本任务不需要 jar 部署的额外步骤，但可以从流程定义部署的那一刻开始执行。有时，脚本任务的脚本不是由开发人员编写的。然而，这带来了如上所述的问题：脚本具有对 JVM 的完全访问权限，并且在执行脚本时可能会阻塞许多系统资源。因此，允许来自任何人的脚本并不是一个好主意。

为了解决这个问题，可以启用安全脚本功能。目前，此功能仅针对 javascript 脚本实现。要启用它，请将 activiti-secure-javascript 依赖项添加到您的项目中。使用 Maven 时：

```xml
<dependency>
    <groupId>org.activiti</groupId>
    <artifactId>activiti-secure-javascript</artifactId>
    <version>${activiti.version}</version>
</dependency>
```

添加此依赖项将传递性地引入 Rhino 依赖项（请参阅 https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino）。Rhino 是 JDK 的 javascript 引擎。它曾经包含在 JDK 版本 6 和 7 中，并被 Nashorn 引擎取代。但是，Rhino 项目在被包含在 JDK 中后继续开发。许多功能（包括 Activiti 用于实现安全脚本的功能）是后来添加的。在撰写本文时，Nashorn 引擎不具备实现安全脚本功能所需的功能。

这确实意味着脚本之间可能存在（通常很小）差异（例如，importPackage 在 Rhino 上工作，但 load() 必须在 Nashorn 上使用）。这些更改类似于从 JDK 7 切换到 8 。

配置安全脚本是通过专用的 Configurator 对象完成的，该对象在流程引擎实例化之前传递给流程引擎配置：

```java
SecureJavascriptConfigurator configurator = new SecureJavascriptConfigurator()
  .setWhiteListedClasses(new HashSet<String>(Arrays.asList("java.util.ArrayList")))
  .setMaxStackDepth(10)
  .setMaxScriptExecutionTime(3000L)
  .setMaxMemoryUsed(3145728L)
  .setNrOfInstructionsBeforeStateCheckCallback(10);

processEngineConfig.addConfigurator(configurator);
```

可以进行以下设置：

- **enableClassWhiteListing**: 当为 true 时，所有类都将被列入黑名单，所有要使用的类都需要单独列入白名单。这可以严格控制暴露给脚本的内容。默认为假。
- **whiteListedClasses**:与允许在脚本中使用的类的完全限定类名相对应的一组字符串。例如，要在脚本中公开执行对象，需要将 org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl String 添加到此 Set 集合中。默认为空。
- **maxStackDepth**: 在脚本中调用函数时限制堆栈大小。这可用于避免递归调用脚本中定义的方法时发生的 stackoverflow 异常。默认情况下 -1（禁用）。
- **maxScriptExecutionTime**: 允许脚本运行的最长时间。默认情况下 -1（禁用）。
- **maxMemoryUsed**: 允许脚本使用的最大内存（以字节为单位）。请注意，脚本引擎本身需要一些内存占用，这里也已经计算在内。默认情况下 -1（禁用）。
- **nrOfInstructionsBeforeStateCheckCallback**: 最大脚本执行时间和内存使用实现回调，该回调在脚本的每 x 条指令调用一次。注意，这些不是脚本指令，而是 java 字节码指令（这意味着一个脚本行可能是数百个字节码指令）。默认为 100。

注意：maxMemoryUsed 设置只能由支持 com.sun.management.ThreadMXBean#getThreadAllocatedBytes() 方法的 JVM 使用。 Oracle JDK 支持此项功能。

还有一个 ScriptExecutionListener 和 ScriptTaskListener 的安全变体：org.activiti.scripting.secure.listener.SecureJavascriptExecutionListener 和 org.activiti.scripting.secure.listener.SecureJavascriptTaskListener。

它的用法如下：

```xml
<activiti:executionListener event="start" class="org.activiti.scripting.secure.listener.SecureJavascriptExecutionListener">
  <activiti:field name="script">
	  <activiti:string>
		  <![CDATA[
        execution.setVariable('test');
			]]>
	  </activiti:string>
	</activiti:field>
  <activiti:field name="language" stringValue="javascript" />
</activiti:executionListener>
```

有关演示不安全脚本以及如何通过安全脚本功能使其安全的示例，请查看 Github 上的[单元测试](https://github.com/Activiti/Activiti/tree/master/modules/activiti-secure-javascript/src/test/resources)

## Tooling

### 1.JMX

#### 1.1介绍

可以使用标准 Java 管理扩展 (JMX) 技术连接到 Activiti 引擎，以获取信息或更改其行为。任何标准 JMX 客户端都可以用于此目的。启用和禁用 Job Executor、部署新的流程定义文件和删除它们只是使用 JMX 无需编写任何代码即可完成的示例。

#### 1.2快速开始

默认情况下未启用 JMX。要在其默认配置中启用 JMX，只需使用 Maven 或任何其他方式将 activiti-jmx jar 文件添加到您的Classpath中即可。如果您使用 Maven，您可以通过在 pom.xml 中添加以下依赖：

```xml
<dependency>
  <groupId>org.activiti</groupId>
  <artifactId>activiti-jmx</artifactId>
  <version>latest.version</version>
</dependency>
```

添加依赖并构建流程引擎后，即可使用 JMX 连接。只需运行标准 JDK 发行版中提供的 jconsole。在本地进程列表中，您将看到包含 Activiti 的 JVM。如果出于任何原因，“本地进程”部分中未列出正确的 JVM，请尝试使用“远程进程”部分中的此 URL 连接到它：

```
service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi/activiti
```

您可以在日志文件中找到本地 URL。连接后，您可以看到标准的 JVM 统计信息和 MBean。您可以通过选择 MBeans 选项卡并在右侧面板上选择“org.activiti.jmx.Mbeans”来查看 Activiti 特定的 MBean。通过选择任何 MBean，您可以查询信息或更改配置。此快照显示了 jconsole 的样子：

任何不限于 jconsole 的 JMX 客户端都可以用于访问 MBean。大多数数据中心监控工具都有一些连接器，使它们能够连接到 JMX MBean。

#### 1.3.属性和操作

这是目前可用的属性和操作列表。此列表可能会根据需要在未来版本中扩展。

| MBean                   | Type      | Name                                                         | Description                                                  |
| ----------------------- | --------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| ProcessDefinitionsMBean | Attribute | processDefinitions                                           | 已部署流程定义的 `Id`、`Name`、`Version`、`IsSuspended` 属性作为字符串列表 |
|                         | Attribute | deployments                                                  | 当前部署的 `Id`、`Name`、`TenantId` 属性                     |
|                         | method    | getProcessDefinitionById(String id)                          | 具有给定 id 的流程定义的 `Id`、`Name`、`Version` 和 `IsSuspended `属性 |
|                         | method    | deleteDeployment(String id)                                  | 删除具有给定 `ID` 的部署                                     |
|                         | method    | suspendProcessDefinitionById(String id)                      | 使用给定的 `Id` 暂停流程定义                                 |
|                         | method    | activatedProcessDefinitionById(String id)                    | 使用给定的 `Id` 激活流程定义                                 |
|                         | method    | suspendProcessDefinitionByKey(String id)                     | 使用给定的`key`挂起流程定义                                  |
|                         | method    | activatedProcessDefinitionByKey(String id)                   | 使用给定的使用给定的`key`激活流程定义                        |
|                         | method    | deployProcessDefinition(String resourceName, String processDefinitionFile) | 部署流程定义文件                                             |
| JobExecutorMBean        | attribute | isJobExecutorActivated                                       | 如果作业执行程序被激活，则返回 true，否则返回 false          |
|                         | method    | setJobExecutorActivate(Boolean active)                       | 根据给定的布尔值激活和停用作业执行程序                       |

#### 1.4.配置

JMX 使用默认配置，以便使用最常用的配置轻松部署。但是，更改默认配置很容易。您可以通过编程方式或通过配置文件来完成。以下代码摘录显示了如何在配置文件中完成此操作：

```xml
<bean id="processEngineConfiguration" class="...SomeProcessEngineConfigurationClass">
  ...
  <property name="configurators">
    <list>
	  <bean class="org.activiti.management.jmx.JMXConfigurator">

	    <property name="connectorPort" value="1912" />
        <property name="serviceUrlPath" value="/jmxrmi/activiti" />

		...
      </bean>
    </list>
  </property>
</bean>
```

下表显示了您可以配置哪些参数及其默认值：

| Name            | Default value           | Description                                        |
| --------------- | ----------------------- | -------------------------------------------------- |
| disabled        | false                   | 如果设置，即使存在依赖项，JMX 也不会启动           |
| domain          | org.activiti.jmx.Mbeans | Domain of MBean                                    |
| createConnector | true                    | 如果为 true，则为启动的 MbeanServer 创建一个连接器 |
| MBeanDomain     | DefaultDomain           | domain of MBean server                             |
| registryPort    | 1099                    | 作为注册端口出现在服务 URL 中                      |
| serviceUrlPath  | /jmxrmi/activiti        | appears in the service URL                         |
| connectorPort   | -1                      | 如果大于零，将作为连接器端口出现在服务 URL 中      |

#### 1.5.JMX服务URL

JMX 服务 URL 具有以下格式：

```
service:jmx:rmi://<hostName>:<connectorPort>/jndi/rmi://<hostName>:<registryPort>/<serviceUrlPath>
```

hostName 将自动设置为机器的网络名称。可以配置 connectorPort、registryPort 和 serviceUrlPath。

如果 connectionPort 小于零，则服务 URL 的相应部分将被删除并简化为：

```
service:jmx:rmi:///jndi/rmi://:<hostname>:<registryPort>/<serviceUrlPath>
```

### 2.Maven 原型

#### 2.1.创建测试用例

在开发过程中，有时在实际应用程序中实现一个想法或功能之前，创建一个小的测试用例来测试它是有帮助的。这有助于隔离被测对象。 JUnit 测试用例也是用于交流错误报告和功能请求的首选工具。将测试用例附加到错误报告或功能请求 jira 问题，大大减少了其修复时间。

为了便于创建测试用例，可以使用 maven 原型。通过使用这种原型，可以快速创建标准测试用例。原型应该已经在标准存储库中可用。如果没有，您只需在 tooling/archtypes 文件夹中键入 mvn install 即可轻松将其安装在本地 maven 存储库文件夹中。

以下命令创建单元测试项目：

```
mvn archetype:generate \
-DarchetypeGroupId=org.activiti \
-DarchetypeArtifactId=activiti-archetype-unittest \
-DarchetypeVersion=<current version> \
-DgroupId=org.myGroup \
-DartifactId=myArtifact
```

每个参数的作用如下表所示：

Unittest 生成原型参数

| Row  | Parameter           | Explanation                                                  |
| ---- | ------------------- | ------------------------------------------------------------ |
| 1    | archetypeGroupId    | Group id of the archetype. should be **org.activiti**        |
| 2    | archetypeArtifactId | Artifact if of the archetype. should be **activiti-archetype-unittest** |
| 3    | archetypeVersion    | Activiti version used in the generated test project          |
| 4    | groupId             | Group id of the generated test project                       |
| 5    | artifactId          | Artifact id of the generated test project                    |

生成的项目的目录结构如下：

```
.
├── pom.xml
└── src
    └── test
        ├── java
        │   └── org
        │       └── myGroup
        │           └── MyUnitTest.java
        └── resources
            ├── activiti.cfg.xml
            ├── log4j.properties
            └── org
                └── myGroup
                    └── my-process.bpmn20.xml
```

您可以修改 java 单元测试用例及其对应的流程模型，或者添加新的测试用例和流程模型。如果您使用该项目来阐明错误或功能，则测试用例最初应该会失败。在修复所需的错误或实现所需的功能后，它应该会通过。在发送之前，请确保通过键入 mvn clean 来清理项目。
