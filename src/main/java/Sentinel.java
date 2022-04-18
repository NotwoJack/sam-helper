import cn.hutool.core.util.RandomUtil;

import java.util.List;
import java.util.Map;

/**
 * 哨兵捡漏模式 可长时间运行
 */
public class Sentinel {

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //执行任务请求间隔时间最小值
        int sleepMillisMin = 10000;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 30000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 5;

        //60次以后长时间等待10分钟左右
        int longWaitCount = 0;

        Map<String, Map<String, Object>> init = Api.init();

        boolean first = true;
        while (!Api.context.containsKey("end")) {
            try {
                if (first) {
                    first = false;
                } else {
                    if (longWaitCount++ > 60) {
                        longWaitCount = 0;
                        System.out.println("执行60次循环后，休息10分钟左右再继续");
                        sleep(RandomUtil.randomInt(50000, 70000));
                    } else {
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    sleep(RandomUtil.randomInt(200, 1000));
                    goodDtos = Api.getCart(init.get("storeDetail"));
                }
                if (goodDtos == null) {
                    continue;
                }

                Map<String, Object> capacityData = null;
                for (int i = 0; i < loopTryCount && capacityData == null; i++) {
                    sleep(RandomUtil.randomInt(500, 1000));
                    capacityData = Api.getCapacityData(init.get("storeDetail"));
                }
                if (capacityData == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount; i++) {
                    if (Api.commitPay(goodDtos, capacityData, init.get("deliveryAddressDetail"), init.get("storeDetail"))) {
                        System.out.println("铃声持续1分钟，终止程序即可，如果还需要下单再继续运行程序");
                        Api.play();
                        break;
                    }
                    sleep(RandomUtil.randomInt(100, 500));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
