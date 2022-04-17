package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для агрегата {@link Menu}.
 */
public interface MenuRepository {

    public boolean save(Menu menu);

    public Menu remove(UUID menuId);

    public Menu getById(UUID menuId);

    public Menu tryGetById(UUID menuId);

    public Page<Menu> getMenus(Criteria criteria);

    public Page<Tag> getTags(Criteria criteria);

    public Page<String> getNames(Criteria criteria);

    public int getNumberMenus(Criteria criteria);

    public int getTagsNumber(Criteria criteria);

    public int getNamesNumber(Criteria criteria);

}
