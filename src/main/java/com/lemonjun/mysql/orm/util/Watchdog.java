package com.lemonjun.mysql.orm.util;

import java.io.File;

import org.apache.log4j.Logger;

public class Watchdog extends Thread {

    /**
    *  被监控的文件名称
    */
    protected String filename;
    /**
    * 文件监控dog,当监测到文件发生变化时执行dog的fileHasChanged()事件
    */
    final FileWatchdog dog;

    /**每次检查的间隔时间默认为 set {@link
    *   #DEFAULT_DELAY}. 
    */
    protected long delay;
    /**
    * 被监控的文件
    */
    File file;
    /**
    * 是否被修改过
    */
    long lastModif = 0;
    /**
    * 是否已经进行过通知
    */
    boolean warnedAlready = false;
    /**
    * 当前线程是否执行标识位
    */
    boolean interrupted = false;
    /**
    * Log日志
    */

    private static Logger logger = Logger.getLogger(Watchdog.class.getName());

    /**
     * 初始化Watchdog,Watchdog为被观察者,初始化时要传入观察他的观察者FileWatchdog作为参数
     * @param dog
     * @param filename
     * @param delay
     */
    public Watchdog(FileWatchdog dog, String filename, long delay) {
        super(filename);
        this.dog = dog;
        this.filename = filename;
        this.delay = delay;
        file = new File(filename);
        setDaemon(true);
        checkAndConfigure();
    }

    /**
    * 扫描时间间隔
    */
    public void setDelay(long delay) {
        this.delay = delay;
    }

    /**
     * 对文件进行监测如果发生变化则调用观察者的fileHasChanged()方法
     */
    protected void checkAndConfigure() {
        boolean fileExists;
        try {
            fileExists = file.exists();
        } catch (SecurityException e) {
            logger.warn("被观察的文件不存在, 文件名:[" + filename + "].");
            interrupted = true; // there is no point in continuing
            return;
        }
        if (fileExists) {
            long l = file.lastModified(); // this can also throw a SecurityException
            if (l > lastModif) { // however, if we reached this point this
                lastModif = l; // is very unlikely.
                dog.fileHasChanged(filename);
                warnedAlready = false;
            }
        } else {
            if (!warnedAlready) {
                logger.debug("被观察的文件不存在, 文件名:[" + filename + "].");
                warnedAlready = true;
            }
        }
    }

    public void run() {
        while (!interrupted) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            checkAndConfigure();
        }
    }
}
