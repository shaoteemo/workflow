# Activiti指南（草稿）

## 写在前面

撰写人：ShaoTeemo

本文档翻译原文档为[官方文档](https://www.activiti.org/userguide/)。其中包含了项目搭建过程的文件信息。文中出现的译著为本人(译者)观点。

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

#### 1.定时器事件定义（TimerEventDefinition）

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

#### 2.错误事件定义（ErrorEventDefinition）

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

#### 3.信号事件定义（SignalEventDefinition）

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

#### 4.消息事件定义（MeaasgeEventDefinition）

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

结束事件表示（子）流程的（路径的）结束。结束事件总是抛出。这意味着当流程执行到达结束事件时，会抛出一个结果。结果的类型由事件的内部黑色图标描述。

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
        targetNamespace="终止结束事件演示">


    <!--

        当达到终止结束事件时，当前流程实例或子流程将被终止。
        从概念上讲，当执行到达终止结束事件时，将确定并结束第一个范围（流程或子流程）。

        这个规则一般适用：
            例如当有一个多实例调用活动或嵌入的子流程时，只有那个实例会被结束，其他实例和流程实例不受影响。

        注意:
            在 BPMN 2.0 中，子流程可以是嵌入式子流程、调用活动、事件子流程或事务子流程。

    -->

    <process id="terminate_end_event" name="terminateEndEvent">

        <endEvent>
            <!--
                可选属性 terminateAll 默认值为false：
                    当为true时，无论终止结束事件在流程定义中的位置如何，
                    也无论是否在子流程（甚至嵌套）中，主流程实例都将被终止。
            -->
            <terminateEventDefinition activiti:terminateAll="true"/>
        </endEvent>
    </process>
</definitions>
```

#### 15.取消结束事件（Cancel End Event）

![](http://rep.shaoteemo.com/activiti/%E5%8F%96%E6%B6%88%E7%BB%93%E6%9D%9F%E4%BA%8B%E4%BB%B6.png)

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
        targetNamespace="取消结束事件演示">

    <!--
        取消结束事件只能与 bpmn 事务子流程结合使用。

        当到达取消结束事件时，将抛出取消事件，该事件必须由取消边界事件捕获。然后取消边界事件取消事务并触发补偿。
    -->
    <process id="cancel_event_end" name="cancelEventEnd">
        <endEvent>
            <cancelEventDefinition/>
        </endEvent>
    </process>
</definitions>
```

### Boundary Events

#### 1.边界事件（Boundary Events）

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
        targetNamespace="边界事件演示">


    <!--
        边界事件捕获附加到活动的事件（边界事件永远不会抛出）。这意味着当活动正在运行时，事件正在侦听某种类型的触发器。
        当事件被捕获时，活动被中断并遵循从事件出去的序列流。

        边界事件定义：
            1.唯一标识符（流程范围内）
            2.对通过attachedToRef 属性将事件附加到的活动的引用。注意，边界事件与它们所依附的活动在同一级别上定义（即，活动中不包含边界事件）。

    -->

    <process id="boundary_event" name="boundaryEvent">
        <!--所有边界事件的定义方式-->
        <boundaryEvent attachedToRef="theActivity" id="myBoundaryEvent">
            <XXXEventDefinition/>
        </boundaryEvent>
    </process>
</definitions>
```

#### 2.定时器边界事件（Timer Boundary Event）

描述：

定时器边界事件充当秒表和闹钟。当执行到达附加边界事件的活动时，将启动计时器。当计时器触发时（例如，在指定的间隔之后），活动被中断，边界事件随之而来。

图形符号：

计时器边界事件可视化为典型的边界事件（即边界上的圆圈），计时器图标位于内部。

![](http://rep.shaoteemo.com/activiti/%E5%AE%9A%E6%97%B6%E5%99%A8%E8%BE%B9%E7%95%8C%E4%BA%8B%E4%BB%B6.png)

XML表示：

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
        targetNamespace="定时器边界事件演示">

    <process id="timer_boundary_event" name="timerBoundaryEvent">
        <boundaryEvent attachedToRef="firstLineSupport" cancelActivity="true" id="escalationTimer">
            <timerEventDefinition>
                <!--如不知写法的请查看 定时器事件定义-->
                <timeDuration>PT4H</timeDuration>
            </timerEventDefinition>
        </boundaryEvent>
    </process>
</definitions>
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

描述：

活动边界上的中间捕获错误，或简称边界错误事件，捕获在定义它的活动范围内抛出的错误。

定义边界错误事件对嵌入的子流程或调用活动最有意义，因为子流程为子流程内的所有活动创建了一个范围。错误结束事件引发错误。此类错误将向上传播其父作用域，直到找到定义了与错误事件定义匹配的边界错误事件的作用域。

当错误事件被捕获时，定义边界事件的活动被销毁，同时销毁其中的所有当前执行（例如并行活动、嵌套子流程等）。流程执行继续遵循边界事件的传出序列流。

图形示例：

边界错误事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有错误图标。错误图标为白色，表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.error.event.png)

XML示例：

典型的边界事件：

```xml
<boundaryEvent attachedToRef="mySubProcess" id="catchError">
      <errorEventDefinition errorRef="myError"/>
</boundaryEvent>
```

与错误结束事件一样，errorRef 引用在流程元素之外定义的错误：

```xml
<error id="myError" errorCode="123" />
...
<process id="myProcess">
...
```

errorCode属性用于匹配捕获的错误：

- 如果省略errorRef，则边界错误事件将捕获任何错误事件，而不管错误的errorCode。
- 如果提供了 errorRef 并且它引用了现有错误，则边界事件将仅捕获具有相同错误代码的错误。
- 如果提供了 errorRef，但 BPMN 2.0 文件中没有定义错误，则 errorRef 用作 errorCode（类似于错误结束事件）。

示例：

以下示例过程显示了如何使用错误结束事件。当通过声明提供的信息不足而完成“*Review profitability*”用户任务时，将引发错误。当在子流程的边界上捕获此错误时，“*Review sales lead*”子流程中的所有活动都将被销毁（即使“*Review customer rating*”尚未完成），并创建“*Provide additional details*”用户任务.

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.error.example.png)

此过程作为演示设置中的示例提供。流程 XML 和单元测试可以在 org.activiti.examples.bpmn.event.error 包中找到。（译者注：见Activiti源码包或编译包）

#### 4.信号边界事件（Signal Boundary Event）

描述：

在活动边界上附加的中间捕获信号，或简称为边界信号事件，捕获与引用的信号定义具有相同信号名称的信号。

注意:

- 与边界错误事件等其他事件相反，边界信号事件不仅捕获从它所连接的范围抛出的信号事件，而且信号事件具有全局范围（广播语义），这意味着信号可以从任何地方抛出，甚至可以从不同的流程实例抛出。
- 与错误事件等其他事件相反，如果信号被捕获，则不会消耗信号。如果您有两个活动的信号边界事件捕获同一个信号事件，则两个边界事件都会被触发，即使它们属于不同流程实例的一部分。

图形示例：

边界信号事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆形），内部带有信号图标。信号图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.signal.event.png)

XML示例：

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
        targetNamespace="信号边界事件演示">

    <process id="signal_boundary_event" name="signalBoundaryEvent">
        <boundaryEvent id="boundary" attachedToRef="task" cancelActivity="true">
            <signalEventDefinition signalRef="alertSignal"/>
        </boundaryEvent>
    </process>
</definitions>
```

#### 5.消息边界事件（Message Boundary Event）

描述：

活动边界上附加的中间捕获消息，或简称边界消息事件，捕获与引用的消息定义具有相同消息名称的消息。

图形示例：

边界消息事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有消息图标。消息图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.message.event.png)注意，边界消息事件可以是中断（右侧）和非中断（左侧)。

XML示例：

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
        targetNamespace="消息边界事件">

    <process id="message_boundary_event" name="messageBoundaryEvent">
        <boundaryEvent id="boundary" attachedToRef="task" cancelActivity="true">
            <messageEventDefinition messageRef="newCustomerMessage"/>
        </boundaryEvent>
    </process>
</definitions>
```

#### 6.取消边界事件（Cancel Boundary Event）

描述：

**事务子流程**边界上的附加中间捕获取消，或简称边界取消事件，在事务取消时触发。当取消边界事件被触发时，它首先中断当前作用域中所有活动的执行。接下来，它开始对事务范围内所有活动的补偿边界事件进行补偿。补偿是同步执行的，即边界事件在补偿完成之前等待，然后再离开事务。当补偿完成时，事务子流程使用运行在取消边界事件之外的序列流离开。

注意：

- 事务子流程只允许单个取消边界事件。
- 如果事务子流程承载嵌套的子流程，则仅对成功完成的子流程触发补偿。
- 如果将取消边界事件放在具有多实例特征的事务子流程上，如果一个实例触发取消，则边界事件取消所有实例。

图形示例：

取消边界事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有取消图标。取消图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.cancel.event.png)

XML示例：

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
        targetNamespace="取消边界事件演示">

    <process id="cancel_boundary_event" name="cancelBoundaryEvent">
        <boundaryEvent id="boundary" attachedToRef="transaction" >
            <cancelEventDefinition />
        </boundaryEvent>
    </process>
</definitions>
```

由于取消边界事件总是中断流程，因此不需要cancelActivity属性。

#### 7.补偿边界事件（Compensation Boundary Event）

描述：

在活动边界上附加的中间捕获补偿或简称补偿边界事件，可用于将补偿处理程序附加到活动。

补偿边界事件必须使用定向关联引用单个补偿处理程序。

补偿边界事件与其他边界事件具有不同的激活策略。其他边界事件（例如信号边界事件）在它们所连接的活动开始时被激活。当活动离开时，它们被停用并取消相应的事件订阅。补偿边界事件不同。当附加到的**活动成功完成时**，将激活补偿边界事件。至此，相应的补偿事件订阅就创建完成了。当触发补偿事件或相应的流程实例结束时，订阅将被删除。由此可知：

- 当补偿被触发时，与补偿边界事件关联的补偿处理程序被调用的次数与它所附加到的活动成功完成的次数相同。
- 如果补偿边界事件附加到具有多个实例特征的活动，则为每个实例创建补偿事件订阅。
- 如果补偿边界事件附加到包含在循环内的活动，则每次执行活动时都会创建补偿事件订阅。
- 如果流程实例结束，则取消对补偿事件的订阅。

注意：嵌入式子流程不支持补偿边界事件。使用association标签连接补偿。

图形示例：

补偿边界事件可视化为边界上的典型中间事件（内部带有较小圆圈的圆圈），内部带有补偿图标。补偿图标为白色（未填充），表示捕获语义。除了补偿边界事件外，下图显示了使用单向关联与边界事件关联的补偿处理程序：

![](http://rep.shaoteemo.com/activiti/bpmn.boundary.compensation.event.png)

XML示例：

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
        targetNamespace="补偿边界事件演示">

    <process id="compensation_boundary_event" name="compensationBoundaryEvent">
        <boundaryEvent id="compensateBookHotelEvt" attachedToRef="bookHotel" >
            <compensateEventDefinition />
        </boundaryEvent>

        <association associationDirection="One" id="a1"  sourceRef="compensateBookHotelEvt" targetRef="undoBookHotel" />

        <serviceTask id="undoBookHotel" isForCompensation="true" activiti:class="..." />
    </process>
</definitions>
```

由于在活动成功完成后才激活补偿边界事件，因此不支持**cancelActivity**属性。

### Intermediate Events

#### 1.中间捕获事件（Intermediate Catching Events）

所有中间捕获事件都以相同的方式定义：

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
        targetNamespace="中间捕获事件演示">

    <process id="intermediate_catching_events" name="intermediateCatchingEvents">
        <intermediateCatchEvent id="myIntermediateCatchEvent" >
            <XXXEventDefinition/>
        </intermediateCatchEvent>
    </process>
</definitions>
```

中间捕获事件定义为

- 唯一标识符（进程范围内）
- XXXEventDefinition（例如 TimerEventDefinition 等）形式的 XML 子元素，用于定义中间捕获事件的类型。有关更多详细信息，请参阅特定的捕获事件类型。

#### 2.定时器中间捕获事件（Timer Intermediate Catching Event）

描述：

计时器中间事件充当秒表。当执行到达捕获事件活动时，将启动计时器。当计时器触发时（例如，在指定的时间间隔之后），将遵循计时器中间事件的序列流。

图形示例：

定时器中间事件可视化为中间捕获事件，内部带有定时器图标。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.timer.event.png)

XML示例：

定时器中间事件被定义为中间捕获事件。在这种情况下，特定类型子元素是 timerEventDefinition 元素。

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
        targetNamespace="定时器中间捕获事件演示">

    <process id="timer_intermediate_catching_event" name="timerIntermediateCatchingEvent">
        <intermediateCatchEvent id="timer">
            <timerEventDefinition>
                <timeDuration>PT5M</timeDuration>
            </timerEventDefinition>
        </intermediateCatchEvent>
    </process>
</definitions>
```

更多定时器相关请查看定时器事件定义。

#### 3.信号中间捕获事件（Signal Intermediate Catching Event）

描述：

中间捕获信号事件捕获与引用的信号定义具有相同信号名称的信号。

注意：与错误事件等其他事件相反，如果信号被捕获，则不会消耗信号。如果您有两个活动的信号边界事件捕获同一个信号事件，则两个边界事件都会被触发，即使它们属于不同流程实例的一部分。

图形示例：

中间信号捕获事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有信号图标。信号图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.signal.catch.event.png)

XML示例：

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
        targetNamespace="信号中间捕获事件演示">

    <process id="signal_intermediate_catching_event" name="signalIntermediateCatchingEvent">
        <intermediateCatchEvent id="signal">
            <!--指向信号的ID-->
            <signalEventDefinition signalRef="newCustomerSignal" />
        </intermediateCatchEvent>
    </process>
</definitions>
```

更多信号相关请查看信号事件定义。

#### 4.消息中间捕获事件（Message Intermediate Catching Event）

描述：

中间捕获消息事件捕获具有指定名称的消息。

图形示例：

中间捕获消息事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有消息图标。消息图标为白色（未填充），表示捕获语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.message.catch.event.png)

XML示例：

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
        targetNamespace="消息中间捕获事件演示">

    <process id="message_intermediate_catching_event" name="messageIntermediateCatchingEvent">
        <intermediateCatchEvent id="message">
            <messageEventDefinition messageRef="newCustomerMessage" />
        </intermediateCatchEvent>
    </process>
</definitions>
```

更多消息相关请查看消息事件定义。

#### 5.中间抛出事件（Intermediate Throwing Event）

所有中间抛出事件都以相同的方式定义：

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
        targetNamespace="中间抛出事件演示">

    <process id="intermediate_throwing_event" name="intermediateThrowingEvent">
        <intermediateThrowEvent id="myIntermediateThrowEvent" >
            <XXXEventDefinition/>
        </intermediateThrowEvent>
    </process>
</definitions>
```

中间抛出事件定义为

- 唯一标识符（进程范围内）
- XXXEventDefinition 形式的 XML 子元素（例如，signalEventDefinition 等），用于定义中间抛出事件的类型。有关更多详细信息，请参阅特定的抛出事件类型。

#### 6.空中间抛出事件（Intermediate Throwing None Event）

图形示例：

下面的流程图显示了一个中间无事件的简单示例，它通常用于指示在流程中达到的某种状态。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.none.event.png)

这可以是监控某些 KPI 的一个很好的钩子，基本上是通过添加一个执行侦听器。

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
        targetNamespace="空中间抛出事件演示">

    <process id="intermediate_throwing_none_event" name="intermediateThrowingNoneEvent">
        <intermediateThrowEvent id="noneEvent">
            <extensionElements>
                <activiti:executionListener class="org.activiti.engine.test.bpmn.event.IntermediateNoneEventTest$MyExecutionListener" event="start" />
            </extensionElements>
        </intermediateThrowEvent>
    </process>
</definitions>
```

在那里您可以添加一些自己的代码，以便向您的 BAM 工具或 DWH 发送一些事件。引擎本身在那个事件中不做任何事情，它只是通过。

#### 7.信号中间抛出事件（Signal Intermediate Throwing Event）

描述：

中间抛出信号事件为定义的信号抛出信号事件。

在 Activiti 中，信号被广播到所有活动处理程序（即所有捕获信号事件）。信号可以同步或异步发布。

- 在默认配置中，信号是同步传送的。这意味着抛出流程实例会等待，直到将信号传递给所有捕获流程实例。捕获流程实例也在与抛出流程实例相同的事务中得到通知，这意味着如果被通知的实例之一产生技术错误（抛出异常），则所有涉及的实例都会失败。
- 信号也可以异步传递。在这种情况下，确定在达到抛出信号事件时哪些处理程序处于活动状态。对于每个活动处理程序，由 JobExecutor 存储和传递异步通知消息 (Job)。

图形示例：

中间信号抛出事件被可视化为典型的中间事件（里面有小圆圈的圆圈），里面有信号图标。信号图标为黑色（填充），表示抛出语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.signal.throw.event.png)

XML示例：

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
        targetNamespace="信号中间抛出事件演示">

    <process id="signal_intermediate_throwing_event" name="signalIntermediateThrowingEvent">
        <intermediateThrowEvent id="signal">
            <signalEventDefinition signalRef="newCustomerSignal" />
        </intermediateThrowEvent>

        <!--异步写法-->
        <intermediateThrowEvent id="signal2">
            <signalEventDefinition signalRef="newCustomerSignal" activiti:async="true" />
        </intermediateThrowEvent>
    </process>
</definitions>
```

更多信号相关请查看信号事件定义。

#### 8.补偿中间抛出事件（Compensation Intermediate Throwing Event）

[不稳定的]

描述：

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

图形示例：

中间补偿抛出事件可视化为典型的中间事件（内部带有较小圆圈的圆圈），内部带有补偿图标。补偿图标为黑色（填充），表示抛出语义。

![](http://rep.shaoteemo.com/activiti/bpmn.intermediate.compensation.throw.event.png)

XML示例：

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
        targetNamespace="补偿中间抛出事件演示">

    <process id="compensation_intermediate_throwing_event" name="compensationIntermediateThrowingEvent">
        <!--基本定义-->
        <intermediateThrowEvent id="throwCompensation">
            <compensateEventDefinition />
        </intermediateThrowEvent>
        <!--
            activityRef：触发特定范围/活动的补偿
        -->
        <intermediateThrowEvent id="throwCompensation2">
            <compensateEventDefinition activityRef="bookHotel" />
        </intermediateThrowEvent>
    </process>
</definitions>
```

### Sequence Flow

#### 1.序列流（Sequence Flow）

描述：

序列流是流程的两个元素之间的连接器。在流程执行期间访问元素后，将遵循所有传出序列流。这意味着 BPMN 2.0 的默认性质是并行的：两个传出序列流将创建两个独立的并行执行路径。

图形示例：

序列流被可视化为从源元素到目标元素的箭头。箭头始终指向目标。

![](http://rep.shaoteemo.com/activiti/bpmn.sequence.flow.png)

XML示例：

序列流需要有一个流程元素唯一的 id，以及对现有源和目标元素的引用。

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
        targetNamespace="序列流演示">

    <process id="sequence_flow" name="sequenceFlow">
        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="theTask" />
    </process>
</definitions>
```

#### 2.条件序列流（Conditional Sequence Flow）

描述：

序列流可以定义一个条件。当离开 BPMN 2.0 活动时，默认行为是评估传出序列流的条件。当条件评估为true时，将选择该传出序列流。当以这种方式选择多个序列流时，将生成多个执行并以并行方式继续该过程。

注意：以上内容适用于 BPMN 2.0 活动（和事件），但不适用于网关。网关将根据网关类型以特定方式处理带有条件的序列流。

图形示例：

条件序列流可视化为常规序列流，开头带有一个小菱形。条件表达式显示在序列流旁边。

![](http://rep.shaoteemo.com/activiti/bpmn.conditional.sequence.flow.png)

XML示例：

条件序列流在 XML 中表示为常规序列流，其中包含一个 conditionExpression 子元素。请注意，目前仅支持 tFormalExpressions，省略 xsi:type="" 定义将简单地默认为仅支持的表达式类型。

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
        targetNamespace="表达式序列流演示">

    <process id="conditional_sequence_flow" name="conditionalSequenceFlow">
        <sequenceFlow id="flow" sourceRef="theStart" targetRef="theTask">
            <conditionExpression xsi:type="tFormalExpression">
                <![CDATA[${order.price > 100 && order.price < 250}]]>
            </conditionExpression>
        </sequenceFlow>
    </process>
</definitions>
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

描述：

所有 BPMN 2.0 任务和网关都可以有一个默认的序列流。当且仅当无法选择任何其他序列流时，才选择此序列流作为该活动的传出序列流。默认序列流上的条件总是被忽略。

图形示例：

默认序列流可视化为常规序列流，开头带有斜线标记。

![](http://rep.shaoteemo.com/activiti/bpmn.default.sequence.flow.png)

XML示例：

某个活动的默认序列流由该活动的默认属性定义。例如，以下 XML 片段显示了一个具有默认序列流*flow 2*. 的独占网关。只有当conditionA 和conditionB 都评估为false 时，才会被选为网关的传出序列流。

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
        targetNamespace="默认序列流演示">

    <process id="default_sequence_flow" name="defaultSequenceFlow">
        <!--default：默认序列流ID-->
        <exclusiveGateway id="exclusiveGw" name="Exclusive Gateway" default="flow2" />
        <sequenceFlow id="flow1" sourceRef="exclusiveGw" targetRef="task1">
            <conditionExpression xsi:type="tFormalExpression">${}</conditionExpression>
        </sequenceFlow>
        <sequenceFlow id="flow2" sourceRef="exclusiveGw" targetRef="task2"/>
        <sequenceFlow id="flow3" sourceRef="exclusiveGw" targetRef="task3">
            <conditionExpression xsi:type="tFormalExpression">${conditionB}</conditionExpression>
        </sequenceFlow>
    </process>
</definitions>
```

### Gateways

网关用于控制执行流程（或如 BPMN 2.0 所描述的，执行令牌）。网关能够使用或生成令牌。

网关以图形方式显示为菱形，里面有一个图标。图标显示网关的类型。

![](http://rep.shaoteemo.com/activiti/bpmn.gateway.png)

#### 1.排他网关（Exclusive Gateway）

描述：

排他网关（也称为 XOR （异或）网关或更专业的基于数据的独占网关）用于对流程中的决策进行建模。当执行到达此网关时，所有传出序列流都按照定义的顺序进行评估。选择条件评估为真（或没有条件集，概念上在序列流上定义了“真”）的序列流以继续该过程。

**请注意，输出序列流的语义与 BPMN 2.0 中的一般情况不同。而一般情况下，所有条件评估为真的序列流都被选择以并行方式继续，而在使用排他网关时只选择一个序列流。如果多个序列流的条件评估为真，则选择 XML 中定义的第一个（并且只有那个！）来继续该过程。如果无法选择序列流，则会抛出异常。**

图形示例：

排他网关可视化为典型的网关（即菱形），内部带有 X 图标，表示 XOR 语义。请注意，内部没有图标的网关默认为排他网关。BPMN 2.0 规范不允许在同一流程定义中混合带有和不带有 X 的菱形。

![](http://rep.shaoteemo.com/activiti/bpmn.exclusive.gateway.notation.png)

XML示例：

独占网关的 XML 表示是直截了当的：一行定义了在传出序列流上定义的网关和条件表达式。请参阅有关条件序列流（conditional sequence flow ）的部分以了解哪些选项可用于此类表达式。 以下面的模型为例：

![](http://rep.shaoteemo.com/activiti/bpmn.exclusive.gateway.png)

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
        targetNamespace="专属网关演示">

    <process id="exclusive_gateway" name="exclusiveGateway">
        <exclusiveGateway id="exclusiveGw" name="Exclusive Gateway" />

        <sequenceFlow id="flow2" sourceRef="exclusiveGw" targetRef="theTask1">
            <conditionExpression xsi:type="tFormalExpression">${input == 1}</conditionExpression>
        </sequenceFlow>

        <sequenceFlow id="flow3" sourceRef="exclusiveGw" targetRef="theTask2">
            <conditionExpression xsi:type="tFormalExpression">${input == 2}</conditionExpression>
        </sequenceFlow>

        <sequenceFlow id="flow4" sourceRef="exclusiveGw" targetRef="theTask3">
            <conditionExpression xsi:type="tFormalExpression">${input == 3}</conditionExpression>
        </sequenceFlow>
    </process>
</definitions>
```

#### 2.并行网关（Parallel Gateway）

描述：

网关还可用于对进程中的并发建模。在流程模型中引入并发的最直接的网关是并行网关（**Parallel Gateway**），它允许分叉到多个执行路径或加入多个传入执行路径。

并行网关的功能基于传入和传出序列流：

- **fork**：所有传出的序列流并行执行，为每个序列流创建一个并发执行。
- **join**：到达并行网关的所有并发执行在网关中等待，直到每个传入序列流的执行到达。然后该过程继续通过加入网关。

请注意，如果同一并行网关有多个传入和传出序列流，则并行网关可以同时具有 fork 和 join 行为。在这种情况下，网关将首先加入所有传入的序列流，然后再拆分为多个并发执行路径。

**与其他网关类型的一个重要区别是并行网关不评估条件。如果在与并行网关连接的序列流上定义了条件，则它们会被忽略。**

图形示例：

并行网关可视化为内部带有加号的网关（菱形），指的是 AND 语义。

![](http://rep.shaoteemo.com/activiti/bpmn.parallel.gateway.png)

XML示例：

定义一个并行网关需要一行 XML：

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
        targetNamespace="并行网关演示">

    <process id="parallel_gateway" name="parallelGateway">
        <parallelGateway id="myParallelGateway" />
    </process>
</definitions>

```

实际行为（fork、join 或两者）由连接到并行网关的序列流定义。

例如，上面的模型归结为以下 XML：

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
        targetNamespace="并行网关演示">

    <process id="parallel_gateway" name="parallelGateway">
        <!--文档中的案例-->
        <startEvent id="theStart" />
        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />

        <parallelGateway id="fork" />
        <sequenceFlow sourceRef="fork" targetRef="receivePayment" />
        <sequenceFlow sourceRef="fork" targetRef="shipOrder" />

        <userTask id="receivePayment" name="Receive Payment" />
        <sequenceFlow sourceRef="receivePayment" targetRef="join" />

        <userTask id="shipOrder" name="Ship Order" />
        <sequenceFlow sourceRef="shipOrder" targetRef="join" />

        <parallelGateway id="join" />
        <sequenceFlow sourceRef="join" targetRef="archiveOrder" />

        <userTask id="archiveOrder" name="Archive Order" />
        <sequenceFlow sourceRef="archiveOrder" targetRef="theEnd" />

        <endEvent id="theEnd" />
    </process>
</definitions>
```

上面的例子中，进程启动后，会创建两个任务：

```java
ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");
TaskQuery query = taskService.createTaskQuery()
                         .processInstanceId(pi.getId())
                         .orderByTaskName()
                         .asc();

List<Task> tasks = query.list();
assertEquals(2, tasks.size());

Task task1 = tasks.get(0);
assertEquals("Receive Payment", task1.getName());
Task task2 = tasks.get(1);
assertEquals("Ship Order", task2.getName());
```

当这两个任务完成时，第二个并行网关将加入两个执行，由于只有一个输出序列流，因此不会创建并发执行路径，只有存档订单任务将处于活动状态。

请注意，并行网关不需要平衡（即对应并行网关的传入/传出序列流的匹配数量）。并行网关将简单地等待所有传入的序列流，并为每个传出的序列流创建一个并发执行路径，不受流程模型中其他构造的影响。因此，以下过程在 BPMN 2.0 中是合法的：

![](http://rep.shaoteemo.com/activiti/bpmn.unbalanced.parallel.gateway.png)

#### 3.包含网关（Inclusive Gateway）

描述：

包含网关可以看作是排他网关和并行网关的组合。就像排他网关一样，您可以在传出序列流上定义条件，并且包含网关将评估它们。但主要区别在于包含网关可以采用多个序列流，就像并行网关一样。

包含网关的功能基于传入和传出序列流：

- **fork**：评估所有传出序列流条件，并且对于评估为true的序列流条件，流将并行执行，为每个序列流创建一个并行执行。
- **join**：到达包含网关的所有并行执行在网关中等待，直到每个具有进程令牌的传入序列流的执行到达。这是与并行网关的一个重要区别。因此换句话说，包含网关将只等待符合条件的传入序列流。加入后，该过程继续经过加入包含网关。

请注意，如果同一个包含网关有多个传入和传出序列流，则包含网关可以同时具有 fork 和 join 行为。在这种情况下，网关将首先加入所有具有进程令牌的传入序列流，然后将其拆分为多个并发执行路径，以用于具有评估为 true 的条件的传出序列流。

图形示例：

包容性网关可视化为内部带有圆形符号的网关（菱形）。

![](http://rep.shaoteemo.com/activiti/bpmn.inclusive.gateway.png)

XML示例：

定义一个包容性网关需要一行 XML：

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
        targetNamespace="包含网关演示">

    <process id="inclusive_gateway" name="inclusiveGateway">
        <!--定义包含网关-->
        <inclusiveGateway id="myInclusiveGateway" />
    </process>
</definitions>
```

实际行为（fork、join 或两者）由连接到包含网关的序列流定义。

例如，上面的模型归结为以下 XML：

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
        targetNamespace="包含网关演示">

    <process id="inclusive_gateway" name="inclusiveGateway">
        <startEvent id="theStart" />
        <sequenceFlow id="flow1" sourceRef="theStart" targetRef="fork" />

        <inclusiveGateway id="fork" />
        <sequenceFlow sourceRef="fork" targetRef="receivePayment" >
            <conditionExpression xsi:type="tFormalExpression">${paymentReceived == false}</conditionExpression>
        </sequenceFlow>
        <sequenceFlow sourceRef="fork" targetRef="shipOrder" >
            <conditionExpression xsi:type="tFormalExpression">${shipOrder == true}</conditionExpression>
        </sequenceFlow>

        <userTask id="receivePayment" name="Receive Payment" />
        <sequenceFlow sourceRef="receivePayment" targetRef="join" />

        <userTask id="shipOrder" name="Ship Order" />
        <sequenceFlow sourceRef="shipOrder" targetRef="join" />

        <inclusiveGateway id="join" />
        <sequenceFlow sourceRef="join" targetRef="archiveOrder" />

        <userTask id="archiveOrder" name="Archive Order" />
        <sequenceFlow sourceRef="archiveOrder" targetRef="theEnd" />

        <endEvent id="theEnd" />
    </process>
</definitions>
```

在上面的例子中，流程启动后，如果流程变量paymentReceived == false和shipOrder == true，则会创建两个任务。如果这些流程变量中只有一个等于 true，则只会创建一项任务。如果没有条件评估为true并抛出异常。这可以**通过指定默认的传出序列流来防止**。在以下示例中，将创建一项任务，即船舶订单任务：

```java
HashMap<String, Object> variableMap = new HashMap<String, Object>();
          variableMap.put("receivedPayment", true);
          variableMap.put("shipOrder", true);
          ProcessInstance pi = runtimeService.startProcessInstanceByKey("forkJoin");
TaskQuery query = taskService.createTaskQuery()
                         .processInstanceId(pi.getId())
                         .orderByTaskName()
                         .asc();

List<Task> tasks = query.list();
assertEquals(1, tasks.size());

Task task = tasks.get(0);
assertEquals("Ship Order", task.getName());
```

当此任务完成时，第二个包容网关将加入两次执行，并且由于只有一个传出序列流，因此不会创建并发执行路径，并且只有存档订单任务将处于活动状态。

请注意，包含网关不需要平衡（即对应包含网关的传入/传出序列流的匹配数量）。包含网关将简单地等待所有传入的序列流，并为每个传出的序列流创建一个并发执行路径，不受流程模型中其他构造的影响。

#### 4.事件网关（Event-based Gateway）

描述：

基于事件的网关允许根据事件做出决定。网关的每个传出序列流都需要连接到一个中间捕获事件。当流程执行到达基于事件的网关时，网关的行为类似于等待状态：执行被暂停。此外，对于每个传出序列流，都会创建一个事件订阅。

请注意，从基于事件的网关流出的序列流不同于普通的序列流。这些序列流从未真正“执行”过。相反，它们允许流程引擎确定到达基于事件的网关的执行需要订阅哪些事件。以下限制适用：

- 基于事件的网关必须有两个或多个传出序列流。
- 基于事件的网关只能连接到仅**intermediateCatchEvent**的元素。 （Activiti 不支持在基于事件的网关之后接收任务。）
- 连接到基于事件的网关的 **intermediateCatchEvent** 必须具有单个传入序列流。

图形示例：

一个基于事件的网关被可视化为一个菱形，就像其他 BPMN 网关一样，里面有一个特殊的图标。

![](http://rep.shaoteemo.com/activiti/bpmn.event.based.gateway.notation.png)

XML示例：

用于定义基于事件的网关的 XML 元素是 eventBasedGateway。

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
        targetNamespace="事件网关演示">

    <process id="event-based_gateway" name="event-basedGateway">
        <!--事件网关-->
        <eventBasedGateway id="eventBasedGateway"/>
    </process>
</definitions>
```

例子：

以下流程是基于事件的网关流程的示例。当执行到达基于事件的网关时，流程执行被暂停。此外，流程实例订阅警报信号事件并创建一个定时器，该定时器在 10 分钟后触发。这有效地导致流程引擎等待十分钟以等待信号事件。如果信号在 10 分钟内发生，则取消定时器并在信号后继续执行。如果未触发信号，则在计时器之后继续执行并取消信号订阅。

![](http://rep.shaoteemo.com/bpmn.event.based.gateway.example.png)

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
        targetNamespace="事件网关演示">

    <!--文档案例-->
    <signal id="alertSignal" name="alert" />

    <process id="catchSignal">

        <startEvent id="start" />

        <sequenceFlow sourceRef="start" targetRef="gw1" />

        <eventBasedGateway id="gw1" />

        <sequenceFlow sourceRef="gw1" targetRef="signalEvent" />
        <sequenceFlow sourceRef="gw1" targetRef="timerEvent" />

        <intermediateCatchEvent id="signalEvent" name="Alert">
            <signalEventDefinition signalRef="alertSignal" />
        </intermediateCatchEvent>

        <intermediateCatchEvent id="timerEvent" name="Alert">
            <timerEventDefinition>
                <timeDuration>PT10M</timeDuration>
            </timerEventDefinition>
        </intermediateCatchEvent>

        <sequenceFlow sourceRef="timerEvent" targetRef="exGw1" />
        <sequenceFlow sourceRef="signalEvent" targetRef="task" />

        <userTask id="task" name="Handle alert"/>

        <exclusiveGateway id="exGw1" />

        <sequenceFlow sourceRef="task" targetRef="exGw1" />
        <sequenceFlow sourceRef="exGw1" targetRef="end" />

        <endEvent id="end" />
    </process>
</definitions>
```

### Task

#### 1.用户任务（User Task）

描述：

用户任务用于对需要由人类参与者完成的工作进行建模。当流程执行到达这样的用户任务时，会在分配给该任务的用户或组的任务列表中创建一个新任务。

图形示例：

用户任务可视化为典型任务（圆角矩形），左上角有一个小用户图标。

![](http://rep.shaoteemo.com/bpmn.user.task.png)

XML示例：

用户任务在 XML 中定义如下。 id 属性是必需的，name 属性是可选的。

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
        targetNamespace="用户任务演示">

    <process id="user_task" name="userTask">
        <userTask id="theTask" name="Important task"/>
    </process>
</definitions>
```

用户任务也可以有描述。事实上，任何 BPMN 2.0 元素都可以有描述。描述是通过添加文档元素来定义的。

```xml
<userTask id="theTask" name="Schedule meeting" >
            <documentation>
                Schedule an engineering meeting for next week with the new hire.
            </documentation>
</userTask>
```

描述文本可以通过标准 Java 方式从任务中检索：

```java
task.getDescription();
```

到期日：

每个任务都有一个字段，指示该任务的截止日期。 Query API 可用于查询在特定日期之前、之前或之后(之间)到期的任务。

有一个活动扩展，它允许您在任务定义中指定一个表达式，以在创建任务时设置它的初始截止日期。该表达式应始终解析为 java.util.Date、java.util.String（ISO8601 格式）、ISO8601 持续时间（例如 PT50M）或 null。例如，您可以使用在流程中的先前（节点）表单中输入或在先前服务任务中计算的日期。如果使用 time-duration，到期日期将根据当前时间计算，增加给定的时间段。例如，当“PT30M”用作dueDate 时，任务将在30 分钟后到期。

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
        targetNamespace="用户任务演示">

    <process id="user_task" name="userTask">
        <!--具有过期时间的UserTask-->
        <userTask id="theTask3" name="Important task" activiti:dueDate="${dateVariable}"/>
    </process>
</definitions>
```

也可以使用 TaskService 或使用传递的 DelegateTask 在 TaskListeners 中更改任务的截止日期。

用户分配：

用户任务可以直接分配给用户。这是通过定义一个 humanPerformer 子元素来完成的。这样的**humanPerformer**定义需要一个实际定义用户的**resourceAssignmentExpression**。目前，仅支持**formalExpressions** 。

```xml
<!--指定分配用户-->
<userTask id='theTask4' name='important task' >
    <humanPerformer>
        <resourceAssignmentExpression>
            <formalExpression>kermit</formalExpression>
        </resourceAssignmentExpression>
    </humanPerformer>
</userTask>
```

只能将一名用户分配为任务的人工执行者。在 Activiti 术语中，此用户称为受让人。具有受托人的任务在其他人的任务列表中不可见，而可以在受托人的所谓个人任务列表中找到。

可以通过 TaskService 检索直接分配给用户的任务，如下所示：

```java
List<Task> tasks = taskService.createTaskQuery().taskAssignee("kermit").list();
```

任务也可以放在所谓的人员候选任务列表中。在这种情况下，必须使用potentialOwner 构造。用法类似于 humanPerformer 构造。请注意，形式表达式中的每个元素都需要定义，以指定它是用户还是组（引擎无法猜测）。

```xml
<!--指定候选人列表或组-->
<userTask id='theTask5' name='important task' >
    <potentialOwner>
        <resourceAssignmentExpression>
            <formalExpression>user(kermit), group(management)</formalExpression>
        </resourceAssignmentExpression>
    </potentialOwner>
</userTask>
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

用于任务分配的 Activiti 扩展：

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
  <!--任务候选组(只能是组)-->
  <userTask id="theTask8" name="my task" activiti:candidateGroups="management, accountancy"/>
  ```

  这与使用上述定义的potentialOwner 构造完全相同。请注意，不需要像潜在所有者构造那样使用组（管理）声明，因为该属性只能用于组。

- *CandidateUsers* 和*CandidateGroups* 都可以定义在同一个用户任务上。

注意：虽然 Activiti 提供了一个身份管理组件，它通过 IdentityService 管理，但是没有检查提供的用户是否被身份组件知道。这允许 Activiti 在嵌入到应用程序中时与现有的身份管理解决方案集成。

自定义身份链接类型（实验性）：

BPMN 标准支持单个分配的用户或 humanPerformer 或potentialOwners，这些用户形成potentialOwners中定义的潜在所有者池。此外，Activiti 为 User Task 定义了扩展属性元素，可以代表任务**assignee** 或**candidate owner**。

支持的 Activiti 身份链接类型有：

```java
package org.activiti.engine.task;
import org.activiti.engine.TaskService;
public class IdentityLinkType {
  /* Activiti native roles */
  public static final String ASSIGNEE = "assignee";
  public static final String CANDIDATE = "candidate";
  public static final String OWNER = "owner";
  public static final String STARTER = "starter";
  public static final String PARTICIPANT = "participant";
}
```

BPMN 标准和 Activiti 示例授权标识是**user** 和**group**。如上一节所述，Activiti 身份管理实现不用于生产用途，但应根据支持的授权方案进行扩展。

如果需要其他链接类型，可以使用以下语法将自定义资源定义为扩展元素：

```xml
<!--(实验性的)自定义扩展链接类型-->
<userTask id="theTask9" name="make profit">
    <extensionElements>
        <activiti:customResource activiti:name="businessAdministrator">
            <resourceAssignmentExpression>
                <formalExpression>user(kermit), group(management)</formalExpression>
            </resourceAssignmentExpression>
        </activiti:customResource>
    </extensionElements>
</userTask>
```

自定义链接表达式添加到 TaskDefinition 类：

```java
protected Map<String, Set<Expression>> customUserIdentityLinkExpressions =
      new HashMap<String, Set<Expression>>();
protected Map<String, Set<Expression>> customGroupIdentityLinkExpressions =
      new HashMap<String, Set<Expression>>();

public Map<String,
         Set<Expression>> getCustomUserIdentityLinkExpressions() {
  return customUserIdentityLinkExpressions;
}

public void addCustomUserIdentityLinkExpression(String identityLinkType,
      Set<Expression> idList)
  customUserIdentityLinkExpressions.put(identityLinkType, idList);
}

public Map<String,
       Set<Expression>> getCustomGroupIdentityLinkExpressions() {
  return customGroupIdentityLinkExpressions;
}

public void addCustomGroupIdentityLinkExpression(String identityLinkType,
       Set<Expression> idList) {
  customGroupIdentityLinkExpressions.put(identityLinkType, idList);
}
```

它们在运行时由 UserTaskActivityBehavior handleAssignments 方法填充。

最后，必须扩展 IdentityLinkType 类以支持自定义身份链接类型：

```Java
public class IdentityLinkType
    /*继承官方链接类型类以扩展类型*/
    extends org.activiti.engine.task.IdentityLinkType
{
    public static final String ADMINISTRATOR = "administrator";

    public static final String EXCLUDED_OWNER = "excludedOwner";
}
```

通过任务监听器（TaskListener）自定义分配：

如果前面的方法还不够，可以使用 create event上的[task listener](https://www.activiti.org/userguide/#taskListeners)委托自定义分配逻辑：

```xml
<userTask id="task1" name="My task" >
  <extensionElements>
    <activiti:taskListener event="create" class="org.activiti.MyAssignmentHandler" />
  </extensionElements>
</userTask>
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

描述：

脚本任务是一项自动活动。当流程执行到达脚本任务时，会执行相应的脚本。

图形示例：

脚本任务可视化为典型的 BPMN 2.0 任务（圆角矩形），矩形左上角有一个小脚本图标。

![](http://rep.shaoteemo.com/bpmn.scripttask.png)

XML示例：

脚本任务是通过指定脚本和脚本格式来定义的。

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
        targetNamespace="脚本任务">

    <process id="scriptTask" name="Script Task">
        <scriptTask id="theScriptTask" name="Execute script" scriptFormat="groovy">
            <script>
                sum = 0
                for ( i in inputArray ) {
                sum += i
                }
            </script>
        </scriptTask>
    </process>
</definitions>
```

scriptFormat 属性的值必须是与[JSR-223](http://jcp.org/en/jsr/detail?id=223)（Java 平台脚本）兼容的名称。默认情况下，每个 JDK 中都包含 JavaScript，因此不需要任何额外的 jar。如果您想使用另一个（与 JSR-223 兼容的）脚本引擎，将相应的 jar 添加到类路径并使用适当的名称就足够了。例如，Activiti 单元测试经常使用 Groovy，因为其语法与 Java 的语法非常相似。

请注意，Groovy 脚本引擎与 groovy-all jar 捆绑在一起。在 2.0 版之前，脚本引擎是常规 Groovy jar 的一部分。因此，现在必须添加以下依赖项：

```xml
<dependency>
      <groupId>org.codehaus.groovy</groupId>
      <artifactId>groovy-all</artifactId>
      <version>2.x.x<version>
</dependency>
```

脚本中的变量：

通过到达脚本任务的执行可访问的所有流程变量都可以在脚本中使用。在示例中，脚本变量“inputArray”实际上是一个过程变量（整数数组）。

```xml
<script>
    sum = 0
    for ( i in inputArray ) {
    sum += i
    }
</script>
```

也可以在脚本中设置流程变量，只需调用 execution.setVariable("variableName", variableValue)。默认情况下，不会自动存储任何变量（注意：在 Activiti 5.12 之前就是这种情况！）。通过将 scriptTask 上的 autoStoreVariables 属性设置为 true，可以自动存储脚本中定义的任何变量（例如上面示例中的 sum）。但是，最佳做法是不要这样做并使用显式 execution.setVariable() 调用，因为在某些最新版本的 JDK 上，自动存储变量对某些脚本语言不起作用。有关更多详细信息，请参阅此链接。

```xml
<!--设置自动存储变量。默认值：false-->
<scriptTask id="script" scriptFormat="JavaScript" activiti:autoStoreVariables="false"/>
```

该参数的默认值为false，意味着如果在脚本任务定义中省略该参数，则所有声明的变量将仅在脚本运行期间存在。

关于如何在脚本中设置变量的示例：

```xml
<script>
    def scriptVar = "test123"
    execution.setVariable("myVar", scriptVar)
</script>
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

安全：

当使用 javascript 作为脚本语言时，也可以使用安全脚本。请参阅安全脚本部分。

#### 3.Java服务任务（Java Service Task）

描述：

Java 服务任务用于调用外部 Java 类。

图形示例：

服务任务显示为左上角带有小齿轮图标的圆角矩形。

![](http://rep.shaoteemo.com/bpmn.java.service.task.png)

XML示例：

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

实现方式：

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

属性注入：

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

字段注入和线程安全：

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
- 每次解析委托表达式时，返回委托类的新实例。例如在使用 Spring 时，这意味着必须将 bean 的作用域设置为原型（例如通过在委托类中添加 @Scope(SCOPE_PROTOTYPE) 注解）（译著：以非单例的形式创建）

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

服务任务结果：

通过将流程变量名称指定为服务任务的“activiti:resultVariable”属性的文字值，可以将服务执行的返回值（仅适用于使用表达式的服务任务）分配给现有的或新的流程变量定义。特定流程变量的任何现有值都将被服务执行的结果值覆盖。当未指定结果变量名称时，服务执行结果值将被忽略。

```xml
<serviceTask id="aMethodExpressionServiceTask" activiti:expression="#{myService.doSomething()}" activiti:resultVariable="myVar" />
```

在上面的示例中，服务执行的结果（在流程变量或 Spring bean 中以名称“myService”提供的对象上的“doSomething()”方法调用的返回值）被设置服务执行完成后，转到名为“myVar”的流程变量。

异常处理：

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

异常映射：

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

异常序列流：

[[INTERNAL: non-public implementation classes\]](https://www.activiti.org/userguide/#internal)

