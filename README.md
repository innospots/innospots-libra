# Innospots开源企业级数字化智能决策解决方案
![alt 属性文本](https://github.com/innospots/innospots-assets/blob/main/images/innospots-logo.png?raw=true)

Innospots是一款开箱即用的开源企业数字化智能决策解决方案。融合了大数据分析BI引擎与工作流自动化引擎，致力于提高数据挖掘和决策应用的效能。这将有助于快速地将业务决策转化为自动化执行的决策流程，从而为企业数字化转型提供强大的支持和推动力。

[![License](https://img.shields.io/github/license/innospots/innospots)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](http://innospots.com/manual)



## 产品特色 

### 工作流自动化引擎
Innospots专注于为企业提供全方位的一站式流程解决方案，专为满足企业在各种业务流程场景下的多元需求，包括：应用自动化、决策引擎、工作流引擎及任务调度等。大幅推动企业提升业务流程效率和生产力，同时降低对IT资源的依赖，迈向智能化、高效化的企业决策新时代。

1、多种类型触发器类型  
   定时任务、Webhook、消息队列（Kafka)、IMAP邮件等  
2、丰富逻辑节点  
   条件节点、分支节点、计算节点、脚本节点、SQL、数据聚合、任务、消息提醒等  
3、多种数据源类型  
   数据库、消息队列、API、邮件等  
4、可视化工作流设计和调试   
   可进行整体工作流或单个节点运行调试，进行输入、输出数据检查和执行验证    

![](https://github.com/innospots/innospots-assets/blob/main/images/workflow-engine.png?raw=true)

### 可视化流程节点设计器
可视化节点设计器是一款低代码节点设计工具，其主要目的是为工作流引擎提供高效且灵活的节点创建解决方案，实现功能的高度可扩展性

1、多种节点类型    
   支持丰富的触发器以及应用节点类型   
2、节点参数表单设计器    
   基于表单引擎的可视化参数组件设计系统，提供了强大的参数管理和配置功能   
3、代码可视化调试器    
   通过图形界面实现节点表单参数设置、代码的联动执行和调试，大大提升便捷性

![](https://github.com/innospots/innospots-assets/blob/main/images/workflow-node-designer.png?raw=true)

### 大数据分析BI引擎
通过简单的拖拽轻松链接数据源、构建数据集以及创建出色的交互式报告和仪表板。快速总结关键性能指标，做出更明智的、数据驱动的决策。

![](https://github.com/innospots/innospots-assets/blob/main/images/bi-report.png?raw=true)

### 企业数字化运营基础开发框架
Innospots基于组件化和模块化的设计思想，实现了可插拔的组件式开发方式，同时提供了丰富的基础组件，旨在提升企业应用开发效率，使得企业能够基于Innospots轻松开发基于数据驱动的智能决策解决方案。

![](https://github.com/innospots/innospots-assets/blob/main/images/all-modules.png?raw=true)

## 功能清单

* **工作流自动化引擎**：充分利用计算机代替人工完成业务流程自动化，减少人工错误、确保零失误、提高效率、大幅度降低运营成本。
* **流程节点设计器**：可视化低代码编辑工具，旨在为流程引擎灵活创建新节点类型，实现功能的高度可扩展性。
* **大数据分析BI引擎**：通过拖拉拽的方式，快速完成数据模型制作、数据探索和报表的制作，生成一整套数据可视化解决方案。
* **多数据源**：灵活支持多种数据源接入，包括：文件数据源、内存数据源、批数据源、流数据源等。
* **用户&角色**：基于RBAC（基于角色）的权限设计模型，支持菜单访问授权、应用操作权限等各种灵活的资源权限管理。
* **工作台**：工作台是用户登录后的首页，为用户展现了关键数据信息，提供了重要功能的快捷入口；同时用户也可以进行组件配置，满足个性化需求。
* **菜单管理**：动态插拔式的菜单系统设计、支持资源、外部链接等类型菜单，同时还支持菜单鉴权以及国际化管理。
* **协同任务管理**：通过工作流自动化引擎，打通智能决策和与工作项任务管理，实现数据驱动工作任务分配。
* **消息中心**：平台支持基于角色的消息通知提醒，同时也支持多种渠道的消息推送：站内、短信、邮件、API等。
* **国际化**：内置可视化国际化管理模块，可进行多国语言、货币设置以及语言资源翻译管理。
* **日志管理**：支持登录和操作日志管理，管理平台可查询正常或异常的登录和操作日志。
* **任务调度**：支持可视化调度任务编排和管理操作，支持暂停/恢复/终止运行中任务以及在线查看调度执行结果和日志。


## 在线体验
在这个innospots演示站点中，您可以全面体验我们所提供的各项功能模块，深入了解我们产品的特点和优势。
请您务必注意，本站点的主要目的是为了展示和让用户体验产品功能，并不适用于进行实际的操作。为了确保站点的安全性和可用性，演示站点的所有内容将每小时自动重置一次，以及确保所有用户都能够顺畅地体验我们的产品。
> **DEMO地址**：[http://1.15.20.45:8686](http://1.15.20.45:8686)
> 
> **用户名**：admin 
> **密   码**：innospots*()

## 在线文档
**快速开始**：
[https://innospots.com/docs/quick-start](https://innospots.com/docs/quick-start])

## 社区支持
### 反馈问题
平台问题可以查看 [Issue](https://github.com/innospots/innospots/issues)

### 交流讨论
可以加入社区微信群参与讨论

<img src="https://github.com/innospots/innospots-assets/blob/main/images/smars-wx.png?raw=true" height="200" width="200" />


## License
Innosopots is under the Apache 2.0 license. See the [LICENSE](https://github.com/innospots/innospots/blob/master/LICENSE) file for details.