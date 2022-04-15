import cn.hutool.core.util.RandomUtil;

import java.util.ArrayList;
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
        //最小订单成交金额 举例如果设置成50 那么订单要超过50才会下单
        double minOrderPrice = 0;

        //执行任务请求间隔时间最小值
        int sleepMillisMin = 3000;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 5000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 8;

        while (!Api.context.containsKey("end")) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    sleep(RandomUtil.randomInt(100, 500));
                    goodDtos = Api.getCart();
                }
                if (goodDtos == null) {
                    continue;
                }

                Map<String, Object> multiReserveTimeMap = null;
                for (int i = 0; i < loopTryCount && multiReserveTimeMap == null; i++) {
                    sleep(RandomUtil.randomInt(100, 500));
                    multiReserveTimeMap = Api.getCapacityData();
                }
                if (multiReserveTimeMap == null) {
                    continue;
                }

                //繁忙 无时间 无货
                for (int i = 0; i < loopTryCount; i++) {
                    if (Api.commitPay(goodDtos, multiReserveTimeMap)) {
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
