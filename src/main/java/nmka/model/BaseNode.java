package nmka.model;

import java.util.List;

/**
 * Created by GYH on 2021/9/26
 */
public interface BaseNode {
    Integer getType();

    List<? extends BaseValue> getValues();
}
