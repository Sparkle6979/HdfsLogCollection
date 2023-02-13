## 日志数据采集
### 需求说明


web系统将用户浏览行为记录在了多台web服务器的日志文件中，日志数据不断增长，而每台web服务器的磁盘容量有限，不能无限存储，此系统定期地将日志文件从本地磁盘目录中传输到HDFS集群。

---

### 设计分析
采集程序的核心关键点，设计如下：

- 程序运行在各台服务器上，读取本地文件，通过该客户端传输到HDFS集群
。
- 程序所采集的文件，传输到HDFS后应按照采集周期生成文件夹，文件名需要重命名。
- 程序采集完成的文件，在本地服务器上备份一段时间，以免后续流程丢失数据。
- 本地服务器上的备份文件应该定期清理，以免服务器的磁盘空间耗尽。

---
### 开发实现
1. 日志源目录及采集、备份过程中的各个文件夹路径，HDFS 连接地址，文件命名前缀等参数，应该设计成可配置参数，可以由用户在配置文件中配置修改，以便可以灵活维护及部署

    具体配置参数见： resources/datacollection.properties
   

2. 采集任务线程类的主要流程：

- 探测日志源目录，获取需要采集的文件，筛选出以 .log 结尾的日志文件；
- 将待采集的文件移动到一个待上传目录；
    ````java
    // step1
    File[] srcFiles = src_dir.listFiles(new FilenameFilter() {	
        @Override
        public boolean accept(File dir, String name) {
            if(name.startsWith(prop.getProperty(GlobalConstants.LOG_SRC_FILE_PREFIX))){
                return true;
            }
            return false;
        }
    });
    
    // step2
    Configuration conf = new Configuration();
    conf.set("fs.defaultFS", prop.getProperty(GlobalConstants.HDFS_URI));
    FileSystem fs = FileSystem.get(new URI(prop.getProperty(GlobalConstants.HDFS_URI)),conf,"root");
    // ...
    FileUtils.moveFileToDirectory(file, backupDir, true);
    ````

3. 备份日志清理任务主要工作流程：
- 遍历日志备份目录，获取其中的子文件夹，转换成备份文件夹的日期；
- 判断备份文件夹的备份时间距当前时间的时间差是否超出备份最长时限，如果超过最大允许时限，则删除该备份子目录；
    ```java
    if (date.getTime() - backDate.getTime() > 24 * 60 * 60 * 1000L) {
        // 如果超出24小时，则删除
        System.out.println("探测到需要清除的备份文件夹：" + dir.getPath());
        FileUtils.deleteQuietly(dir);
        System.out.println("成功删除");
    }
    ```
  
---
