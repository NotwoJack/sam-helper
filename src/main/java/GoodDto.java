import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o){
        return o instanceof GoodDto && Objects.equals(this.spuId, ((GoodDto) o).spuId);
    }

         }
