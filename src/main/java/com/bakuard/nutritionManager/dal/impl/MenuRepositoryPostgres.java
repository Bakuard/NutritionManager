package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.util.Page;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
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
    public Page<Menu> getMenus(Criteria criteria) {
        return null;
    }

    @Override
    public Page<Tag> getTags(Criteria criteria) {
        return null;
    }

    @Override
    public Page<String> getNames(Criteria criteria) {
        return null;
    }

    @Override
    public int getNumberMenus(Criteria criteria) {
        return 0;
    }

    @Override
    public int getTagsNumber(Criteria criteria) {
        return 0;
    }

    @Override
    public int getNamesNumber(Criteria criteria) {
        return 0;
    }

}
