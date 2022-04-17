package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * Представляет собой меню - группу из неповторяющихся блюд, где для каждого блюда указанно его кол-во.
 */
public class Menu implements Entity<Menu> {

    private final UUID id;
    private final User user;
    private String name;
    private String description;
    private URL imageUrl;
    private List<MenuItem> items;
    private List<Tag> tags;
    private final AppConfigData config;

    public Menu(Menu other) {
        id = other.id;
        user = new User(other.user);
        name = other.name;
        description = other.description;
        imageUrl = other.imageUrl;
        items = new ArrayList<>(other.items);
        tags = new ArrayList<>(other.tags);
        config = other.config;
    }

    private Menu(UUID id,
                 User user,
                 String name,
                 String description,
                 String imageUrl,
                 List<MenuItem.Builder> items,
                 List<String> tags,
                 AppConfigData config) {

        this.id = id;
        this.user = user;
        this.config = config;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Возвращает стоимость данного меню с учетом выбранных продуктов для каждого ингредиента блюда и
     * кол-вом даного меню. Особые случаи:<br/>
     * 1. Если меню не содержит ни одного блюда - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту любого блюда не соответствует ни один продукт - возвращает пустой Optional.<br/>
     * 3. Если для какого-либо ингредиента блюда не соответствует ни один продукт - то он не принимает участия
     *    в рассчете цены меню.<br/>
     * 4. Если для какого-либо ингредиента некоторого блюда не выбран продукт - то в качестве значения по
     *    умолчанию будет выбран самый дешевый продукт соответствующий данному ингредиенту данного блюда.
     * @param quantity кол-во данного меню.
     * @param constraints набор ограничений задающий конкретные продукты для ингредиентов блюд этого меню.
     * @return стоимость данного меню или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *         1. Если quantity имеет значение null. <br/>
     *         2. Если quantity меньше или равно нулю. <br/>
     *         3. Если constraints имеет значение null. <br/>
     *         4. Если хотя бы один из элементов constraints имеет значение null. <br/>
     *         5. Если хотя бы для одного из элементов ProductConstraint.dishName() возвращает null. <br/>
     *         6. Если хотя бы для одного из элементов ProductConstraint.ingredientIndex() меньше нуля. <br/>
     *         7. Если хотя бы для одного из элементов ProductConstraint.ingredientIndex() больше или равен кол-ву
     *            ингредиентов соответствующего блюда. <br/>
     *         8. Если хотя бы для одного из элементов ProductConstraint.productIndex() меньше нуля.
     */
    public Optional<BigDecimal> getPrice(BigDecimal quantity,
                                         List<ProductConstraint> constraints) {
        return Optional.empty();
    }

    /**
     * Возвращает среднеарифметическую цену для данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного элемента - возвращает пустой Optional.<br/>
     * 2. Если любому элементу меню не соответствует ин одного элемента - возвращает пустой Optional.<br/>
     * 3. Если для какому-либо элементу не соответствует ни одного продукта - то он не принимает участия
     *    в рассчете среднеарифметической цены меню.
     * @return среднеарифметическая цена данного блюда.
     */
    public Optional<BigDecimal> getAveragePrice() {
        return Optional.empty();
    }

    @Override
    public boolean equalsFullState(Menu other) {
        return id.equals(other.id) &&
                user.equalsFullState(other.user) &&
                name.equals(other.name) &&
                Objects.equals(description, other.description) &&
                Objects.equals(imageUrl, other.imageUrl) &&
                items.equals(other.items) &&
                tags.equals(other.tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return id.equals(menu.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", user=" + user +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl=" + imageUrl +
                ", items=" + items +
                ", tags=" + tags +
                '}';
    }


    /**
     * Используется при расчете цены меню. Позволяет указать конкретные продукты для ингредиентов
     * блюд входящих в меню.
     */
    public record ProductConstraint(String dishName, int ingredientIndex, int productIndex) {}


    public static class Builder implements Entity.Builder<Menu> {

        private UUID id;
        private User user;
        private String name;
        private String description;
        private String imageUrl;
        private List<MenuItem.Builder> items;
        private List<String> tags;
        private AppConfigData config;

        public Builder() {
            items = new ArrayList<>();
            tags = new ArrayList<>();
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setConfig(AppConfigData config) {
            this.config = config;

            items.forEach(item -> item.setConfig(config));

            return this;
        }

        public Builder addItem(MenuItem.Builder item) {
            items.add(item);
            return this;
        }

        public Builder addItem(String dishName, BigDecimal quantity) {
            items.add(
                    new MenuItem.Builder().
                            setDishName(dishName).
                            setQuantity(quantity).
                            setConfig(config)
            );
            return this;
        }

        public Builder addTag(String tag) {
            tags.add(tag);
            return this;
        }

        @Override
        public Menu tryBuild() throws ValidateException {
            return new Menu(
                    id,
                    user,
                    name,
                    description,
                    imageUrl,
                    items,
                    tags,
                    config
            );
        }

    }

}
