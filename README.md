# EasyDependency

一个帮助提高组件化开发效率的gradle插件，提供的功能包括：

1. 发布模块的构件都远程maven仓库
2. 动态更换依赖配置：对模块使用源码依赖或者maven仓库的构件（aar/jar）依赖

## 使用方法

1. 在根项目的`build.gradle`文件中添加依赖及插件使用的配置

   ```groovy
   // 引入插件，注意这个插件只能在根项目的gradle文件中引入
   // 内部会完成根项目下的各个子模块的配置
   apply plugin: 'easy-dependency'
   
   buildscript {
       repositories {
           ...
           // 添加仓库地址
           maven{
               url='https://dl.bintray.com/easily/easilytech'
           }
       }
       dependencies {
           ...
           //添加依赖
           classpath 'tech.easily:EasyDependencyPlugin:1.0.1'
           // NOTE: Do not place your application dependencies here; they belong
           // in the individual module build.gradle files
       }
   }
   ```

2. 配置发布构件到maven仓库

   在各个需要发布构件到module的`build.gradle`文件中添加以下配置

   ```groovy
   mavenPublish{
       version='0.0.1-SNAPSHOT' // 如果不包含SNAPSHOT，则发布到release的仓库
       groupId='your group id'
       artifactId='your artifact id' // 如果不配置，则使用模块的名称作为其默认值
       userName="your maven repo user's name"
       password="your maven repo password"
       releaseRepo="your release maven repo address"
       snapshotRepo="your snapshot maven repo address"
   }
   ```

   根据使用的maven的仓库，选择性配置以上的各项内容

   在需要发布构件到远程仓库的时候，执行模块中的`uploadArchives`的gradle任务即可

3. 配置动态替换依赖

   在各个需要进行动态依赖替换的module的`build.gradle`文件中添加以下配置

   ```groovy
   dependencies {
       implementation project(':testLib')
   }
   dynamicDependency{
       //这个key对应的是本地模块的名称（注意这个本地模块应该在依赖当中有配置,如上所示）
       testLib{
           debuggable=true //如果是true，则使用本地模块作为依赖参与编译，否则使用下面的配置获取远程的构件作为依赖参与编译
           groupId="target archive's group id"
           artifactId="target archive's archive id" // 默认使用模块的名称作为其值
           version="target archive's version"
       }
       // 如果有更多的模块需要进行动态依赖配置，则继续添加对应的配置快在里面即可
       ...
   }
   ```

   