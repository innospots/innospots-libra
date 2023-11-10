## 容器化构建

需提前构建好 innospots-base:latest 基础镜像，参考docs/docker/README.md

### 1.通过maven构建基础tar包

```$shell script
mvn clean install -DskipTests
```

TODO待验证--

```shell script
mvn docker:build
```

### 2.构建docker镜像

在当前目录下执行

```$shell script
docker build -t innospots-server-administration:版本号 .
```

示例：

```$shell script
[root@local innospots-server-administration]# docker build -t innospots-server-administration:1.1.0-SNAPSHOT .
Sending build context to Docker daemon    134MB
Step 1/6 : From innospots-base:latest
 ---> 53f3d21dfefc
Step 2/6 : ENV APPLICATION_NAME innospots-server-administration
 ---> Using cache
 ---> 7f079007bbbe
Step 3/6 : ENV SERVER_MAIN_CLASS io.innospots.administration.server.InnospotsAdministrationServer
 ---> Using cache
 ---> aece1431267d
Step 4/6 : ENV INNOSPOTS_HOME /innospots/$APPLICATION_NAME
 ---> Using cache
 ---> 0e755f953c17
Step 5/6 : ADD target/$APPLICATION_NAME.tar.gz /innospots/
 ---> Using cache
 ---> 4776cecef2c3
Step 6/6 : ENTRYPOINT ["sh","/innospots/bin/app_entrypoint.sh"]
 ---> Using cache
 ---> 350fbda3df04
Successfully built 350fbda3df04
Successfully tagged innospots-server-administration:1.1.0-SNAPSHOT
[root@hadoop100 innospots-server-administration]# 
```

执行完成后查看镜像，查看是否有innospots-server-administration镜像文件

```shell script
docker images
```

示例：

```shell script
[root@hadoop100 innospots-server-administration]# docker images
REPOSITORY                        TAG                 IMAGE ID            CREATED             SIZE
innospots-server-administration   1.1.0-SNAPSHOT      350fbda3df04        2 hours ago         283MB
innospots-base                    latest              53f3d21dfefc        2 hours ago         143MB
alpine                            latest              c059bfaa849c        12 months ago       5.59MB
```

### 3.启动容器

目前容器化启动支持两种数据库

- h2：已内置表结构和初始化数据，启动后，可直接登录系统，进行产品功能体验。
- mysql：需要提前准备一个mysql数据库实例，手动初始化表结构和基础数据。再参考下面命令启动容器。

#### 内置H2数据库启动

启动容器

```shell script
docker run \
--name innospots-server-administration \
-p 容器端口:宿主机端口 \
-e ENV_PROFILE=dockerH2 \
innospots-server-administration:版本号
```

示例：

```shell script
docker run \
--name innospots-server-administration \
-p 9800:9800 \
-e ENV_PROFILE=dockerH2 \
innospots-server-administration:1.1.0-SNAPSHOT


[root@hadoop100 innospots-server-administration]# docker run \
> --name innospots-server-administration \
> -p 9800:9800 \
> -e ENV_PROFILE=dockerH2 \
> innospots-server-administration:1.1.0-SNAPSHOT
================================================================================================================
Starting application io.innospots.administration.server.InnospotsAdministrationServer, working directory:/innospots/innospots-server-administration
/usr/lib/jvm/default-jvm/bin/java -Xms128m -Xmx1g -Xss256k -XX:MaxMetaspaceSize=192m -XX:MetaspaceSize=192m -Djava.ext.dirs=/usr/lib/jvm/default-jvm/jre/lib/ext:/usr/lib/jvm/default-jvm/lib/ext -Xloggc:/innospots/innospots-server-administration/logs/innospots_gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/innospots/innospots-server-administration/logs/java_heapdump.hprof -XX:-UseLargePages -Dspring.profiles.active=dockerH2 -cp /usr/lib/jvm/default-jvm/lib/dt.jar:/usr/lib/jvm/default-jvm/lib/tools.jar:/usr/lib/jvm/default-jvm/jre/lib/rt.jar:/innospots/innospots-server-administration/config:/innospots/innospots-server-administration/lib/* io.innospots.administration.server.InnospotsAdministrationServer

  ___ _   _ _   _  ___  ____  ____   ___ _____     _     ___ ____  ____      _
 |_ _| \ | | \ | |/ _ \/ ___||  _ \ / _ \_   _|   | |   |_ _| __ )|  _ \    / \
  | ||  \| |  \| | | | \___ \| |_) | | | || |_____| |    | ||  _ \| |_) |  / _ \
  | || |\  | |\  | |_| |___) |  __/| |_| || |_____| |___ | || |_) |  _ <  / ___ \
 |___|_| \_|_| \_|\___/|____/|_|    \___/ |_|     |_____|___|____/|_| \_\/_/   \_\

Spring Boot version: 2.7.2

2022-11-27T18:03:06,902 INFO  [main] org.springframework.boot.StartupInfoLogger logStarting(55): Starting InnospotAdministrationServer v1.1.0-SNAPSHOT using Java 1.8.0_345 on 715dde5a055a with PID 20 (/innospots/innospots-server-administration/lib/innospots-server-administration-1.1.0-SNAPSHOT.jar started by root in /innospots)
2022-11-27T18:03:06,915 DEBUG [main] org.springframework.boot.StartupInfoLogger logStarting(56): Running with Spring Boot v2.7.2, Spring v5.3.22
2022-11-27T18:03:06,920 INFO  [main] org.springframework.boot.SpringApplication logStartupProfileInfo(640): The following 2 profiles are active: "security", "dockerH2"
2022-11-27T18:03:09,603 INFO  [main] org.springframework.data.repository.config.RepositoryConfigurationDelegate registerRepositoriesIn(132): Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2022-11-27T18:03:09,638 INFO  [main] org.springframework.data.repository.config.RepositoryConfigurationDelegate registerRepositoriesIn(201): Finished Spring Data repository scanning in 15 ms. Found 0 JPA repository interfaces.
2022-11-27T18:03:11,368 INFO  [main] com.zaxxer.hikari.HikariDataSource <init>(80): innospots-console-hikari - Starting...
2022-11-27T18:03:11,935 INFO  [main] com.zaxxer.hikari.HikariDataSource <init>(82): innospots-console-hikari - Start completed.
2022-11-27T18:03:12,936 WARN  [main] io.undertow.websockets.jsr.Bootstrap handleDeployment(68): UT026010: Buffer pool was not set on WebSocketDeploymentInfo, the default pool will be used
2022-11-27T18:03:12,972 INFO  [main] io.undertow.servlet.spec.ServletContextImpl log(382): Initializing Spring embedded WebApplicationContext
2022-11-27T18:03:12,973 INFO  [main] org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext prepareWebApplicationContext(292): Root WebApplicationContext: initialization completed in 5882 ms
2022-11-27T18:03:13,347 INFO  [main] org.springframework.boot.autoconfigure.h2.H2ConsoleAutoConfiguration logDataSources(87): H2 console available at '/h2-console'. Database available at 'jdbc:h2:/innospots/innospots-server-administration/h2_db/innospots'
2022-11-27T18:03:16,188 INFO  [main] org.springframework.orm.jpa.AbstractEntityManagerFactoryBean buildNativeEntityManagerFactory(437): Initialized JPA EntityManagerFactory for persistence unit 'default'
2022-11-27T18:03:19,885 INFO  [main] org.quartz.core.SchedulerSignalerImpl <init>(61): Initialized Scheduler Signaller of type: class org.quartz.core.SchedulerSignalerImpl
2022-11-27T18:03:19,885 INFO  [main] org.quartz.core.QuartzScheduler <init>(229): Quartz Scheduler v.2.3.2 created.
2022-11-27T18:03:19,893 INFO  [main] org.quartz.simpl.RAMJobStore initialize(155): RAMJobStore initialized.
2022-11-27T18:03:19,896 INFO  [main] org.quartz.core.QuartzScheduler initialize(294): Scheduler meta-data: Quartz Scheduler (v2.3.2) 'SimpleQuartzScheduler' with instanceId 'SIMPLE_NON_CLUSTERED'
  Scheduler class: 'org.quartz.core.QuartzScheduler' - running locally.
  NOT STARTED.
  Currently in standby mode.
  Number of jobs executed: 0
  Using thread pool 'org.quartz.simpl.SimpleThreadPool' - with 8 threads.
  Using job-store 'org.quartz.simpl.RAMJobStore' - which does not support persistence. and is not clustered.
```

启动&进入容器，屏蔽容器默认entrypoint命令

```shell script
docker run \
--entrypoint=/bin/sh \
-it \
--name innospots-server-administration \
-e ENV_PROFILE=dockerH2 \
-p 9800:9800 \
innospots-server-administration:1.1.0-SNAPSHOT
```

后台启动，服务器重启后，容器自动重启

```shell script
docker run \
-d \
--restart always \
--name innospots-server-administration \
-p 9800:9800 \
-e ENV_PROFILE=dockerH2 \
innospots-server-administration:1.1.0-SNAPSHOT
```

本地测试执行，退出后运行容器销毁

```shell script
docker run \
--entrypoint=/bin/sh \
-it \
--rm \
--name innospots-server-administration \
-e ENV_PROFILE=dockerH2 \
-p 9800:9800 \
innospots-server-administration:1.1.0-SNAPSHOT
```

#### 外置MySQL数据库启动

初始化数据库TODO

启动容器

```shell script
docker run \
--name innospots-server-administration \
-p 容器端口:宿主机端口 \
-e ENV_PROFILE=docker \
-e innospots.db.jdbc-url="jdbc:mysql://ip:port/db_name?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=CONVERT_TO_NULL" \
-e innospots.db.username=xxxx \
-e innospots.db.password=xxxx \
innospots-server-administration:1.1.0-SNAPSHOT
```

示例：

```shell script
docker run \
--name innospots-server-administration \
-p 9800:9800 \
-e ENV_PROFILE=docker \
-e innospots.db.jdbc-url="jdbc:mysql://192.168.1.100:3306/innospots_dev?allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&zeroDateTimeBehavior=CONVERT_TO_NULL" \
-e innospots.db.username=root \
-e innospots.db.password=root \
innospots-server-administration:1.1.0-SNAPSHOT
```

通过容器启动传递的应用参数：

| 参数名称                                    | 参数说明    | 是否必传 | 默认值                      |
|-----------------------------------------|---------|------|--------------------------|
| server.port                             | 服务启动端口  | 否    | 9876                     |
| innospots.data.schemaCacheTimeoutSecond |         | 否    | 30                       |
| innospots.data.recordStoreType          |         | 否    | mysql                    |
| innospots.data.datasourceCode           |         | 否    | ch4k                     |
| innospots.db.drver-class-name           | 数据库驱动类  | 否    | com.mysql.cj.jdbc.Driver |
| innospots.db.jdbc-url                   | 数据库URL  | 是    |                          |
| innospots.db.username                   | 数据库用户   | 是    |                          |
| innospots.db.password                   | 数据库密码   | 是    |                          |
| innospots.db.maximum-pool-size          | 数据库最大线程 | 否    | 64                       |
| innospots.db.minimum-idle               | 数据库最小线程 | 否    | 4                        |
| innosports.workflow.server.port         | 工作流服务端口 | 否    | 19876                    |

### 4.页面访问

容器启动成功，访问宿主机地址，ip:port。
示例：http://192.168.1.100:9800/

### 5.常用命令

启动&进入容器，屏蔽容器默认entrypoint命令

```shell script
docker run \
--entrypoint=/bin/sh \
-it \
REPOSITORY:TAG
```

进入已启动的容器

```shell script
docker exec -it CONTAINER ID sh
```

停止容器

```shell script
docker stop CONTAINER ID
```

启动容器

```shell script
docker start CONTAINER ID
```