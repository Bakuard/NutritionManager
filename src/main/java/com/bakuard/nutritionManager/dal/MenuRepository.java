package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.MenuSort;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.model.exceptions.ValidateException;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для агрегата {@link Menu}.
 */
public interface MenuRepository {

    public boolean save(Menu menu);

    public Menu remove(UUID menuId);

    public Menu getById(UUID menuId);

    public List<Menu> getMenus(Pageable pageable,
                               User user,
                               MenuSort order);

    public int getNumberMenus(User user);

}
