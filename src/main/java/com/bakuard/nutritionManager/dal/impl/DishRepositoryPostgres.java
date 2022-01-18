package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.model.Dish;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Filter;
import com.bakuard.nutritionManager.model.filters.DishSort;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.UUID;

public class DishRepositoryPostgres implements DishRepository {

    private JdbcTemplate statement;

    public DishRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean save(Dish dish) {
        return false;
    }

    @Override
    public Dish remove(UUID dishId) {
        return null;
    }

    @Override
    public Dish getById(UUID dishId) {
        return null;
    }

    @Override
    public Page<Dish> getDishes(Pageable pageable, User user, DishSort order) {
        return null;
    }

    @Override
    public Page<Dish> getDishes(Pageable pageable, User user, Filter filter, DishSort order) {
        return null;
    }

    @Override
    public int getNumberDishes(User user) {
        return 0;
    }

    @Override
    public int getNumberDishes(User user, Filter filter) {
        return 0;
    }

}
