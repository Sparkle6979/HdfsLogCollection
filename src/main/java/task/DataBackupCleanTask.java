package task;

import config.GlobalConstant;
import org.apache.commons.io.FileUtils;
import util.PropHolder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;

public class DataBackupCleanTask extends TimerTask {
    @Override
    public void run() {
        Date date = new Date();
        System.out.println("备份处理线程启动，当前时间： " + date);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
        try {
            Properties properties = PropHolder.getProperties();
            File backupBaseDir = new File(properties.getProperty(GlobalConstant.LOG_BAK_BASE_PATH));
            File[] backupDirs = backupBaseDir.listFiles();

            for(File file:backupDirs){
                Date backDate = sdf.parse(file.getName());

                if(backDate.getTime() - date.getTime() > 24 * 60 * 60 * 1000L){
                    System.out.println("探测到需要清除的备份文件夹" + file.getPath());
                    FileUtils.deleteQuietly(file);
                    System.out.println("成功删除");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
