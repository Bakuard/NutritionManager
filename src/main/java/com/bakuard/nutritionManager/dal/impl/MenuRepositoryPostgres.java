package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.dal.Criteria;
import com.bakuard.nutritionManager.dal.DishRepository;
import com.bakuard.nutritionManager.dal.MenuRepository;
import com.bakuard.nutritionManager.model.Menu;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.util.Page;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

public class MenuRepositoryPostgres implements MenuRepository {

    private JdbcTemplate statement;
    private AppConfigData appConfig;
    private DishRepository dishRepository;

    public MenuRepositoryPostgres(DataSource dataSource,
                                  AppConfigData appConfig,
                                  DishRepository dishRepository) {
        statement = new JdbcTemplate(dataSource);
        this.dishRepository = dishRepository;
        this.appConfig = appConfig;
    }

    @Override
    public boolean save(Menu menu) {
        return false;
    }

    @Override
    public Menu tryRemove(UUID userId, UUID menuId) {
        return null;
    }

    @Override
    public Optional<Menu> getById(UUID userId, UUID menuId) {
        return Optional.empty();
    }

    @Override
    public Optional<Menu> getByName(UUID userId, String name) {
        return Optional.empty();
    }

    @Override
    public Menu tryGetById(UUID userId, UUID menuId) {
        return null;
    }

    @Override
    public Menu tryGetByName(UUID userId, String name) {
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
