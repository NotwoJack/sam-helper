import cn.hutool.core.util.RandomUtil;

import java.util.*;


/**
 * 保供套餐抢购模式 可长时间运行
 */
@Deprecated
public class GuaranteeSentinel {

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public static void main(String[] args) {

        //执行任务请求间隔时间最小值
        int sleepMillisMin = 500;
        //执行任务请求间隔时间最大值
        int sleepMillisMax = 1000;

        //单轮轮询时请求异常（服务器高峰期限流策略）尝试次数
        int loopTryCount = 10;

        //11点20分00秒时间触发 保供套餐
        while (!Api.timeTrigger("11:20:00")) {
        }

        Api.init("2");

        AddressDto addressDto = null;
        while (addressDto == null) {
            List<AddressDto> addressDtoList = Api.getAddress();
            if (addressDtoList != null) {
                System.out.println("请输入收货地址序号，并回车");
                Scanner scanner = new Scanner(System.in);
                addressDto = addressDtoList.get(scanner.nextInt());
            }
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        List<CouponDto> couponList = null;
        while (couponList == null){
            couponList = Api.getCouponList();
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        Map<String, Object> storeDetail = null;
        while (storeDetail == null) {
            storeDetail = Api.getMiniUnLoginStoreList(Double.parseDouble(addressDto.getLatitude()), Double.parseDouble(addressDto.getLongitude()));
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }
        Map<String, Object> capacityData = null;
        while (capacityData == null) {
            capacityData = Api.getCapacityData(storeDetail);
            sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
        }

        while (true) {
            try {
                sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));

                List<GoodDto> goodDtos = null;
                for (int i = 0; i < loopTryCount && goodDtos == null; i++) {
                    goodDtos = Api.getPageData(storeDetail);
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                }
                if (goodDtos == null) {
                    continue;
                } else if (Api.limitGood.containsAll(goodDtos)) {
                    System.out.println("全部商品都已经下单");
                    continue;
                }

                Boolean addFlag = null;
                goodDtos.removeAll(Api.limitGood);
                if (!goodDtos.isEmpty()) {
                    for (int i = 0; i < loopTryCount && addFlag == null; i++) {
                        addFlag = Api.addCartGoodsInfo(goodDtos);
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }
                if (addFlag == null) {
                    continue;
                }

                for (int i = 0; i < loopTryCount; i++) {
                    for (GoodDto goodDto : goodDtos) {
                        if (Api.commitPay(Arrays.asList(goodDto), capacityData, addressDto, storeDetail, couponList)) {
                            Api.play("保供套餐，下单成功，下单金额：" + Api.context.get("amount"));
                            Api.limitGood.add(goodDto);
                            goodDtos.remove(goodDto);
                        }
                        sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
