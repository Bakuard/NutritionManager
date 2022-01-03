package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Constraint;
import com.bakuard.nutritionManager.model.filters.DishSort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import java.util.UUID;

/**
 * Репозиторий для агрегата {@link Dish}.
 */
public interface DishRepository {

    public boolean save(Dish dish);

    public Dish remove(UUID dishId);

    public Dish getById(UUID dishId);

    public Page<Dish> getDishes(Pageable pageable,
                                User user,
                                DishSort order);

    public Page<Dish> getDishes(Pageable pageable,
                                User user,
                                Constraint constraint,
                                DishSort order);

    public int getNumberDishes(User user);

    public int getNumberDishes(User user, Constraint constraint);

}
