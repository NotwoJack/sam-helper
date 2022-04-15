import cn.hutool.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抢菜测试程序
 */
public class ApplicationTest {
    //三个接口
    //1.获取下单商品接口，用于获取下单商品单id
    //2.获取下单时间接口
    //3.下单接口，组合前两个接口的返回值

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("startRealTime","1650006000000");
        map.put("endRealTime","1650027600000");
        List<GoodDto> goods = Api.getCart();
        Api.commitPay(goods,map);

        //todo git fork clone 对403处理

//        if (UserConfig.addressId.length() == 0) {
//            System.err.println("请先执行UserConfig获取配送地址id");
//            return;
//        }

        // 此为单次执行模式  用于在非高峰期测试下单  也必须满足3个前提条件  1.有收货地址  2.购物车有商品 3.能选择配送信息
//        List<GoodDto> goodDtos = Api.getCart();
//        if (goodDtos == null || goodDtos.isEmpty()) {
//            return;
//        }
//        Map<String, Object> capacityData = Api.getCapacityData();
//        if (capacityData == null) {
//            return;
//        }
//        Api.commitPay(goodDtos,capacityData);
    }
}
