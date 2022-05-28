package reggie.dto;


import lombok.Data;
import reggie.pojo.Setmeal;
import reggie.pojo.SetmealDish;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
