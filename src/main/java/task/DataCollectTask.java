package task;
import config.GlobalConstant;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import util.PropHolder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import java.util.UUID;

public class DataCollectTask extends TimerTask {
//
//    // 日志源路径
//    public static final String LOG_SRC_PATH = "LOG_SRC_PATH";
//    // 日志备份路径
//    public static final String LOG_BAK_BASE_PATH = "LOG_BAK_BASE_PATH";
//    // 日志文件名前缀
//    public static final String LOG_SRC_FILE_PREFIX = "LOG_SRC_FILE_PREFIX";
//    // 日志文件待上传临时存储路径
//    public static final String LOG_TOUPLOAD = "LOG_TOUPLOAD";
//    // 目标HDFS集群URI
//    public static final String HDFS_URI = "HDFS_URI";
//    // 日志文件目标存储路径
//    public static final String LOG_HDFS_BASE_PATH = "LOG_HDFS_BASE_PATH";
//    // 日志文件名目标存储名前缀
//    public static final String LOG_HDFS_FILE_PREFIX = "LOG_HDFS_FILE_PREFIX";
//    // 日志文件名目标存储名后缀
//    public static final String LOG_HDFS_FILE_SUFFIX = "LOG_HDFS_FILE_SUFFIX";

    @Override
    public void run() {
        System.out.println("采集线程启动............");
        try {
            Properties properties = PropHolder.getProperties();

            File srcDir = new File(properties.getProperty(GlobalConstant.LOG_SRC_PATH));
            System.out.println(srcDir.getAbsolutePath());
            File[] srcFiles = srcDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if(name.endsWith(properties.getProperty(GlobalConstant.LOG_SRC_FILE_PREFIX))){
                        return true;
                    }
                    return false;
                }
            });
//            File[] srcFiles = srcDir.listFiles();
//            System.out.println(srcFiles);
//            for(File f:srcFiles){
//                System.out.println(f.getName());
//            }
            // 将采集到的文件备份到另一文件夹内
            File toUploadDir = new File(properties.getProperty(GlobalConstant.LOG_TOUPLOAD));

            for(File file:srcFiles){
                FileUtils.moveFileToDirectory(file,toUploadDir,true);
            }

            // 加载 hdfs 客户端
            Configuration conf = new Configuration();
            conf.set("fs.defaultFS",properties.getProperty(GlobalConstant.HDFS_URI));
            FileSystem fs = FileSystem.get(new URI(properties.getProperty(GlobalConstant.HDFS_URI)),conf,"sparkle6979l");

            // 获取待上传路径的所有文件
            File[] files = toUploadDir.listFiles();


            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
            String daytime = sdf.format(date);
            String dayString = daytime.substring(0, daytime.lastIndexOf("-"));

            // 准备上传
            for(File file:files){
                String dstName = properties.getProperty(GlobalConstant.LOG_HDFS_FILE_PREFIX) + UUID.randomUUID() + properties.getProperty(GlobalConstant.LOG_HDFS_FILE_SUFFIX);

                // 拼接hdfs上的完整的目标存储路径
                Path destPath = new Path(properties.getProperty(GlobalConstant.LOG_HDFS_BASE_PATH) + dayString + "/" + dstName);

                // 上传该文件
                System.out.println("准备上传：" + file.getPath());
                fs.copyFromLocalFile(new Path(file.getPath()), destPath);
                System.out.println("上传完毕");

                // 生成备份目录路径： 备份文件按小时分文件夹存放
                File backupDir = new File(properties.getProperty(GlobalConstant.LOG_BAK_BASE_PATH) + daytime);
                System.out.println("即将备份到：" + backupDir.getPath());

                // 移动到备份目录
                FileUtils.moveFileToDirectory(file, backupDir, true);
                System.out.println("备份完毕：" + backupDir.getPath());

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
