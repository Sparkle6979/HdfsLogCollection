import task.DataBackupCleanTask;
import task.DataCollectTask;

import java.util.Timer;

public class DataCollectionMain {
    public static void main(String[] args) {
    // 启动数据采集任务 定时调度
        // 使用ScheduledExecutorService代替Timer吧
        // 多线程并行处理定时任务时，Timer运行多个TimeTask时，只要其中之一没有捕获抛出的异常，
        // 其它任务便会自动终止运行，使用ScheduledExecutorService则没有这个问题
        Timer timer = new Timer();
        timer.schedule(new DataCollectTask(), 0, 60 * 1000L);

        // 启动一个超时备份清除任务定时调度
        timer.schedule(new DataBackupCleanTask(), 0, 60*60*1000L);
    }
}
