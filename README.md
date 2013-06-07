#Just eclipse - 基于eclipse的Module Define开发插件

##工具介绍
Just eclipse为基于eclipse的module define开发插件(目前主要基于交易just架构)，支持js模块文件快速创建，模块提示等功能。
   
##环境需求
支持aptana,eclipse wtp等 eclipse核心为 3.6及以上版本的环境安装。

* 推荐下载web开发利器WTP：[官方下载地址](http://www.eclipse.org/webtools/)
* 载aptana：[官方下载地址](http://www.aptana.com/)

##运行及安装
解压zip文件后，将plugins目录中的jar文件copy到eclipse或aptana安装目录下的dropins文件夹，重启eclipse即可
(其中com.alibaba.just.jsdt_XXX.jar为wtp中的jsdt插件，没有jsdt的可不安装，如果升级版本的话请删除上一个版本文件)

##使用说明

1. 导入分支或js代码：
 * 新建任意类型的项目，或用已存在的任意项目，导入js代码或分支（推荐使用linked folder方式导入, New -> Folder -> 点击Advanced -> 选择linked folder ）
2. 设置项目的module libraries以及root path：
 * 右击项目 -> 选择properties -> 选择Just Project Page -> 设置当前module libraries以及root path
 * Module Libraries：当前项目依赖的js module 文件路径 
 * Root Path：通过just eclipse 菜单新建js module文件时所对应的根目录（设置后对应的folder右上有红色标记，这路径也可直接通过导航中右键菜单设置）
3. 打开module视图：  
 * 菜单选择Window -> Show View -> Other... -> 在树形项目中选择Just Eclipse的Module View
 * Module View中可以显示当前编辑的js module文件相关信息
3. 通过模板新建module文件：
 * 方式1：菜单New -> Other... -> 选择Just Eclipse下的New module file
 * 方式2：左侧导航的邮件菜单 -> Just Eclipse -> New Module File
 * 按照UI提示新建即可，生成的module文件模板可在Preferences中的Just Eclipse项设置
4. 其他：
 * Just Eclipse 的全局设置：菜单选择Window -> Preferences -> Just Eclipse
 * 导入模块或生成merge文件可在当前编辑的文件中右键或使用对应的快捷键
 * 如果你使用的是WTP，在js的编辑器中支持代码助手提示，在string字段中使用(alt + /)即可
 * 其他有待以后补充......
 
##Change Log
  [2013-05-15]: 升级版本到1.0.5，优化UI，修改项目中module js导入存储格式，升级后请重新设置项目中lib路径