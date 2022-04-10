package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Sort;
import com.bakuard.nutritionManager.model.util.Pageable;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

public class MenuRepositoryPostgres implements MenuRepository {

    private JdbcTemplate statement;

    public MenuRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean save(Menu menu) {
        return false;
    }

    @Override
    public Menu remove(UUID menuId) {
        return null;
    }

    @Override
    public Menu getById(UUID menuId) {
        return null;
    }

    @Override
    public Menu tryGetById(UUID menuId) {
        return null;
    }

    @Override
    public List<Menu> getMenus(Pageable pageable, User user, Sort order) {
        return null;
    }

    @Override
    public int getNumberMenus(User user) {
        return 0;
    }

}
