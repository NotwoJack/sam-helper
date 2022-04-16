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
        int sleepMillisMin = 3000;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 5000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 8;

        Map<String, Map<String, Object>> init = Api.init();

        while (!Api.context.containsKey("end")) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    sleep(RandomUtil.randomInt(100, 500));
                    goodDtos = Api.getCart(init.get("storeDetail"));
                }
                if (goodDtos == null) {
                    continue;
                }

                Map<String, Object> multiReserveTimeMap = null;
                for (int i = 0; i < loopTryCount && multiReserveTimeMap == null; i++) {
                    sleep(RandomUtil.randomInt(100, 500));
                    multiReserveTimeMap = Api.getCapacityData(init.get("storeDetail"));
                }
                if (multiReserveTimeMap == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount; i++) {
                    if (Api.commitPay(goodDtos, multiReserveTimeMap, init.get("deliveryAddressDetail"), init.get("storeDetail"))) {
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
