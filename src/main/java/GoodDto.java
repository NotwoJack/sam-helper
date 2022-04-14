import lombok.Getter;
import lombok.Setter;

/** 商品类DTO
 * @author zhangyibo
 */
@Getter
@Setter
public class GoodDto {
    String spuId;

    String storeId;

    String quantity;

    Boolean isSelected = true;

         }
