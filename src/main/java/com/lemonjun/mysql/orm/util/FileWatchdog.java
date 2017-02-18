package com.lemonjun.mysql.orm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件状态看门狗
 * 
 * @author renjun
 * 
 */
public class FileWatchdog {

    /**
     * 默认扫描时间60秒
     */
    static final public long DEFAULT_DELAY = 60000;
    /**
     * 根据文件名称存储Watchdog每个文件名称只能存在一dog，初始化允许10dog
     */

    private static Map<String, Watchdog> dogMap = new HashMap<String, Watchdog>(10);
    /**
     * 根据文件名称存储每个文件的ObserverList每个文件可以有多个FileObserver初始化允许同时监控10个文件
     */
    private static Map<String, List<FileObserver>> observerMap = new HashMap<String, List<FileObserver>>(10);
    /**
     * 
     */
    private static FileWatchdog filedog;

    public static FileWatchdog singleton() {
        if (filedog == null)
            filedog = new FileWatchdog();
        return filedog;
    }

    private FileWatchdog() {

    }

    /**
     * 增加对一个文件的监控 如果有多个Observer对一个文件，以最短时间间隔为标准
     * 
     * @param filename
     *            被监控的文件名
     * @param observer
     *            观察者
     */
    public void addFileObserver(String filename, FileObserver observer) {
        addFileObserver(filename, observer, DEFAULT_DELAY);

    }

    /**
     * 增加对一个文件的监控 如果有多个Observer对一个文件，以最短时间间隔为标准
     * 
     * @param filename
     *            被监控的文件名
     * @param observer
     *            观察者
     * @param deplay
     *            时延
     */
    public void addFileObserver(String filename, FileObserver observer, long delay) {

        Watchdog dog = dogMap.get(filename);
        List<FileObserver> observeList;
        if (dog != null) {

            if (dog.delay > delay)
                dog.setDelay(delay);

            observeList = observerMap.get(filename);
            if (observeList != null) {
                observeList.add(observer);
                return;
            }
            observeList = new ArrayList<FileObserver>();
            observeList.add(observer);
            observerMap.put(filename, observeList);
            return;
        }

        Watchdog newdog = new Watchdog(this, filename, delay);
        observeList = new ArrayList<FileObserver>();
        observeList.add(observer);
        Thread dogthread = new Thread(newdog);
        dogthread.start();

        dogMap.put(filename, newdog);
        observerMap.put(filename, observeList);

    }

    /**
     * 移除文件监控
     * 
     * @param filename
     * @param observer
     */
    public void removeFileObserver(String filename, FileObserver observer) {

        List<FileObserver> observerList = observerMap.get(filename);
        for (int i = 0; i < observerList.size(); i++) {
            FileObserver ob = observerList.get(i);
            if (observer.equals(ob))
                observerList.remove(ob);
            if (observerList.size() == 0)
                destroy(filename);
        }
    }

    /**
     * 根据文件名称摧毁线程
     * @param filename
     */
    public void destroy(String filename) {

        dogMap.get(filename).interrupted = true;
        dogMap.remove(filename);

    }

    /**
     * 文件发生变化通知文件的所有观察者
     * @param filename
     */
    public void fileHasChanged(String filename) {

        List<FileObserver> observerList = observerMap.get(filename);
        if (observerList != null) {
            for (int i = 0; i < observerList.size(); i++) {
                FileObserver ob = observerList.get(i);
                ob.fileHasChanged(filename);
            }
        }
    }

    /**
     * 文件状态的观察者
     * 
     * @author renjun
     * 
     */
    public interface FileObserver {

        /**
         * 通知已经有变化
         * 
         */
        public void fileHasChanged(String filename);

    }

}
