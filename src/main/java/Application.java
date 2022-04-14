import cn.hutool.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抢菜主程序
 */
public class Application {


    public static final Map<String, Object> map = new ConcurrentHashMap<>();


    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }


    public static void main(String[] args) {

        //此为高峰期策略 通过同时获取或更新 购物车、配送、订单确认信息再进行高并发提交订单

        //一定要注意 并发量过高会导致被风控 请合理设置线程数、等待时间和执行时间 不要长时间的执行此程序（我配置的线程数和间隔 2分钟以内）
        //如果想等过高峰期后进行简陋 长时间执行 则将线程数改为1  间隔时间改为10秒以上 并发越小越像真人 不会被风控  要更真一点就用随机数（自己处理）

        //基础信息执行线程数
        int baseTheadSize = 2;

        //提交订单执行线程数
        int submitOrderTheadSize = 6;

        //请求间隔时间
        int sleepMillis = 100;

//        for (int i = 0; i < baseTheadSize; i++) {
//            new Thread(() -> {
//                while (!map.containsKey("end")) {
////                    Api.allCheck();
//                    Api.getCapacityData();
//                    //此接口作为补充使用 并不是一定需要 所以执行间隔拉大一点
//                    sleep(100);
//                }
//            }).start();
//        }

        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!map.containsKey("end")) {
                    List<GoodDto> goods = Api.getCart();
                    if (goods != null) {
                        map.put("goods", goods);
                    }
                    sleep(sleepMillis);
                }
            }).start();
        }
        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!map.containsKey("end")) {
                    sleep(sleepMillis);
                    if (map.get("goods") == null) {
                        continue;
                    }
                    Map<String, Object> time = Api.getCapacityData();
                    if (time != null) {
                        map.put("time", time);
                    }
                }
            }).start();
        }
        for (int i = 0; i < submitOrderTheadSize; i++) {
            new Thread(() -> {
                while (!map.containsKey("end")) {
                    if (map.get("goods") == null || map.get("time") == null) {
                        sleep(sleepMillis);
                        continue;
                    }
                    Api.commitPay((List<GoodDto>) map.get("goods"),(Map<String, Object>) map.get("time"));
                }
            }).start();
        }
    }
}
