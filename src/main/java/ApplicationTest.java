import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢菜测试程序
 */
public class ApplicationTest {

    public static void main(String[] args) throws InterruptedException {
        // 此为单次执行模式  用于在非高峰期测试下单  也必须满足3个前提条件  1.有收货地址  2.购物车有商品 3.能选择配送信息
        //先初始化 获得必要的参数
        Map<String, Map<String, Object>> init = Api.init();
        List<GoodDto> goodDtos = Api.getCart(init.get("storeDetail"));
        //        Map<String, Object> capacityData = Api.getCapacityData(init.get("storeDetail"));
        while (goodDtos != null && !Api.context.containsKey("end")){
            Map<String, Object> time = new HashMap<>();
            time.put("startRealTime","");
            time.put("endRealTime","1650265200");
            Api.commitPay(goodDtos, time, init.get("deliveryAddressDetail"), init.get("storeDetail"));
            Thread.sleep(300);
        }
       }
}
