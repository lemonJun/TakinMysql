package com.lemonjun.mysql.orm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ho.yaml.Yaml;

public interface MessageAlert {
    public void sendMessage(String message);

    public static class Factory {

        private static String config;
        private static int port;
        private static String smsIp;
        private static String mobiles;

        private static String title;

        private static Map<String, Long> contentTimeMap = new ConcurrentHashMap<String, Long>();
        private static final Long interval = 5 * 60 * 1000L;

        public static void setConfig(String config) {
            Factory.config = config;
        }

        public static int getMaxThreadsPerDs() {
            int max_threads_per_ds = 5; /*每个数据源的最大线程数*/
            if (config == null) {
                return max_threads_per_ds;
            }

            String alertConfig = config + "/alert.config";
            File alertFile = new File(alertConfig);
            if (!alertFile.exists()) {
                return max_threads_per_ds;
            }

            try {
                HashMap<String, String> receiversPhones = (HashMap<String, String>) Yaml.loadType(alertFile, HashMap.class);
                max_threads_per_ds = receiversPhones.containsKey("max_threads_per_ds") ? Integer.parseInt(receiversPhones.get("max_threads_per_ds")) : 5;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return max_threads_per_ds;

        }

        @SuppressWarnings("unchecked")
        public static MessageAlert get() {

            if (config == null) {
                return noAlert;
            }

            String alertConfig = config + "/alert.config";
            File alertFile = new File(alertConfig);
            if (!alertFile.exists()) {
                return noAlert;
            }

            try {

                HashMap<String, String> receiversPhones = (HashMap<String, String>) Yaml.loadType(alertFile, HashMap.class);
                String phones[] = receiversPhones.get("receiver").split(",");

                smsIp = receiversPhones.get("smsIp");
                port = Integer.parseInt(receiversPhones.get("port"));
                title = receiversPhones.get("title");
                if (title == null)
                    title = "";

                StringBuilder sb = new StringBuilder();

                for (String phone : phones) {
                    sb.append("\"" + phone + "\",");
                }

                if (sb.length() > 0)
                    sb.deleteCharAt(sb.length() - 1);

                mobiles = sb.toString();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (mobiles.equals(""))
                return noAlert;

            return alert;
        }

        private static MessageAlert noAlert = new MessageAlert() {

            @Override
            public void sendMessage(String message) {
            }

        };

        private static MessageAlert alert = new MessageAlert() {

            @Override
            public void sendMessage(String message) {

                Long time = System.currentTimeMillis();
                Long lasttime = contentTimeMap.get(message);
                if (lasttime != null && (time - lasttime < interval))
                    return;

                contentTimeMap.put(message, time);

                if (mobiles == null || mobiles.length() < 5)
                    return;

                String sendDataStr = "{\"mobile\":[" + mobiles + "],\"content\":\"" + title + message + "\"}";

                //                try {
                //
                //                    UDPClient.sendMsg(sendDataStr, smsIp, port);
                //                } catch (Exception e) {
                //
                //                    e.printStackTrace();
                //                }
            }

        };
    }

}
