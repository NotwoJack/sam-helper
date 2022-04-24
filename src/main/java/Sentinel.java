import cn.hutool.core.util.RandomUtil;

import java.util.List;
import java.util.Map;

/**
 * 哨兵捡漏模式 可长时间运行。
 * 用于极速达捡漏，可以在UserConfig中设置下单目标金额
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
        int sleepMillisMax = 20000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 8;

        //60次以后长时间等待10分钟左右
        int longWaitCount = 0;

        Map<String, Map<String, Object>> init = Api.init(UserConfig.deliveryType);

        boolean first = true;
        while (!Api.context.containsKey("end")) {
            try {
                if (first) {
                    first = false;
                } else {
                    if (longWaitCount++ > 60) {
                        longWaitCount = 0;
                        sleep(RandomUtil.randomInt(50000, 70000));
                    } else {
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getCart(init.get("storeDetail"));
                    if (goodDtos == null){
                        sleep(RandomUtil.randomInt(500, 1000));
                    }
                }
                if (goodDtos == null) {
                    continue;
                }
                if ((Double)Api.context.get("amount") < UserConfig.targetAmount){
                    Api.print(false, "【失败】购物车未达到目标金额");
                    continue;
                }

                Map<String, Object> capacityData = null;
                for (int i = 0; i < loopTryCount && capacityData == null; i++) {
                    capacityData = Api.getCapacityData(init.get("storeDetail"));
                    if (capacityData == null){
                        sleep(RandomUtil.randomInt(500, 1000));
                    }
                }
                if (capacityData == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount; i++) {
                    if (Api.commitPay(goodDtos, capacityData, init.get("deliveryAddressDetail"), init.get("storeDetail"))) {
                        Api.play();
                    }
                    sleep(RandomUtil.randomInt(100, 500));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
