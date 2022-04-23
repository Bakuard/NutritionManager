package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.Container;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
                 List<Entity.Builder<MenuItem>> items,
                 List<String> tags,
                 AppConfigData config) {
        Container<List<MenuItem>> menuItemsContainer = new Container<>();
        Container<List<Tag>> tagContainer = new Container<>();
        Container<URL> imageURlContainer = new Container<>();

        Validator.check(
                Rule.of("Menu.id").notNull(id),
                Rule.of("Menu.user").notNull(user),
                Rule.of("Menu.name").notNull(name).and(r -> r.notBlank(name)),
                Rule.of("Menu.description").notNull(description).and(r -> r.notBlank(description)),
                Rule.of("Menu.imageUrl").isNull(imageUrl).or(r -> r.isUrl(imageUrl, imageURlContainer)),
                Rule.of("Menu.items").doesNotThrows(items, Entity.Builder::tryBuild, menuItemsContainer).
                        and(r -> r.notContainsDuplicate(menuItemsContainer.get(), MenuItem::getDishName)),
                Rule.of("Menu.tags").doesNotThrows(tags, Tag::new, tagContainer),
                Rule.of("Menu.config").notNull(config)
        );

        this.id = id;
        this.user = user;
        this.name = name.trim();
        this.description = description.trim();
        this.imageUrl = imageURlContainer.get();
        this.items = menuItemsContainer.get();
        this.tags = tagContainer.get();
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

    /**
     * Возвращает все элементы данного меню в виде неизменяемого списка.
     * @return все элементы данного меню.
     */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Возвращает все теги данного меню в виде неизменяемого списка.
     * @return все теги данного меню.
     */
    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    /**
     * Возвращает данные о конкретном продукте для каждого ингрдиента блюда этого меню. Для каждого ингредиента
     * каждого блюда можно указать. Особые случаи:<br/>
     * 1. Если для данного меню не задано ни одно блюдо - возвращает пустой список. <br/>
     * 2. Если любое блюдо меню не имеет ни одноо ингредиента - возвращает пустой список. <br/>
     * 3. Если всем ингредиентам любого блюда этого меню не соответствует ни одного продукта - возвращает пустой
     *    список. <br/>
     * 4. Если некоторому ингредиенту некоторого блюда не соответствует ни один продукт - то для данного ингредиента
     *    НЕ будет добавлен элемент {@link MenuItemProduct} в итоговый список. <br/>
     * 5. Если для одного и того же ингредиента одного и того же блюда указанно несколько продуктов - будет
     *    выбран первый из указанных. <br/>
     * 6. Если для некоторого ингредиента некоторого блюда не указанно ни одного продукта - будет выбран самый
     *    дешевый из всех продуктов подходящих для данного ингредиента. <br/>
     * 7. Если для некоторого ингредиента некоторого блюда задан индекс продукта, который больше или равен кол-ву
     *    всех продуктов соответствующих данному ингредиенту - то для этого ингредиента будет выбран последний
     *    из всех соответствующих ему продуктов (все продукты упорядочены по цене в порядке возрастания). <br/>
     * @param quantity кол-во данного меню.
     * @param constraints ограничения задающие конкретные продукты для ингредиентов блюд этого меню.
     * @return данные об одном конкретном продукте для каждого ингрдиента блюда этого меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если quantity имеет значение null. <br/>
     *         2. Если constraints имеет значение null. <br/>
     *         3. Если один из элементов constraints имеет значение null. <br/>
     *         4. Если quantity меньше или равно нулю. <br/>
     */
    public List<MenuItemProduct> getMenuItemProducts(BigDecimal quantity,
                                                     List<ProductConstraint> constraints) {
        Validator.check(
                Rule.of("Menu.quantity").notNull(quantity).and(r -> r.positiveValue(quantity)),
                Rule.of("Menu.constraints").notNull(constraints).and(r -> r.notContainsNull(constraints))
        );

        ArrayList<MenuItemProduct> result = new ArrayList<>();
        for(MenuItem item : items) {
            Dish dish = item.getDish();
            for(int i = 0; i < dish.getIngredientNumber(); i++) {
                DishIngredient ingredient = dish.getIngredient(i).orElseThrow();
                BigDecimal necessaryQuantity = ingredient.getNecessaryQuantity(
                        item.getNecessaryQuantity(quantity)
                );

                final int ingredientIndex = i;
                Optional<Product> product = dish.getProduct(
                        ingredientIndex,
                        constraints.stream().
                                filter(c -> dish.getName().equals(c.dishName()) &&
                                        c.ingredientIndex() == ingredientIndex).
                                mapToInt(ProductConstraint::productIndex).
                                findFirst().
                                orElse(0)
                );

                if(product.isPresent()) {
                    MenuItemProduct itemProduct = new MenuItemProduct(
                            item,
                            product.get(),
                            necessaryQuantity,
                            config
                    );
                    result.add(itemProduct);
                }
            }
        }
        return result;
    }

    /**
     * Групирует элементы меню по продуктам для нахождения одних и тех же продуктов используемых для разных блюд.<br/>
     * Данный метод полагается, что передаваемый список был корректно сформирован вызывающим кодом, например,
     * вызовом метода {@link #getMenuItemProducts(BigDecimal, List)}.
     * @param menuItemProducts список продуктов, где каждый продукт соответствует одному ингредиенту одного
     *                         блюда этого меню.
     * @return элементы меню сгрупированные по продуктам.
     * @throws ValidateException если menuItemProducts является null.
     */
    public Map<Product, List<MenuItemProduct>> groupByProduct(List<MenuItemProduct> menuItemProducts) {
        return menuItemProducts.stream().
                collect(Collectors.groupingBy(MenuItemProduct::getProduct));
    }

    /**
     * Возвращает кол-во указанного продукта необходимого для приготовления всех блюд этого меню, в состав
     * которых входит этот продукт. Особые случаи: <br/>
     * 1. Если product не является ключом products - возвращает пустой Optional. <br/>
     * 2. Если products является пустым - возвращает пустой Optional. <br/>
     * Данный метод полагается, что передаваемый ассоциативный массив был корректно сформирован вызывающим кодом,
     * например, вызовом метода {@link #groupByProduct(List)}.
     * @param product продукт.
     * @param products элементы меню сгрупированные по продуктам.
     * @return кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если product имеет значение null. <br/>
     *         2. Если products имеет значение null. <br/>
     */
    public Optional<BigDecimal> getNecessaryQuantity(Product product, Map<Product, List<MenuItemProduct>> products) {
        Validator.check(
                Rule.of("Menu.product").notNull(product),
                Rule.of("Menu.products").notNull(products)
        );

        List<MenuItemProduct> menuItems = products.get(product);
        if(menuItems == null) return Optional.empty();
        return menuItems.stream().
                map(MenuItemProduct::getNecessaryQuantity).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает НЕДОСТАЮЩЕЕ кол-во указанного продукта (с учетом кол-ва, которое уже есть в наличии у пользователя)
     * необходимого для приготовления всех блюд этого меню, в состав которых этот продукт входит. Недостающее кол-во
     * рассчитывается с учетом фасовки продукта (размера упаковки) и представляет собой кол-во недостающих "упаковок"
     * продукта. Особые случаи:<br/>
     * 1. Если product не является ключом products - возвращает пустой Optional. <br/>
     * 2. Если products является пустым - возвращает пустой Optional. <br/>
     * Данный метод полагается, что передаваемый ассоциативный массив был корректно сформирован вызывающим кодом,
     * например, вызовом метода {@link #groupByProduct(List)}.
     * @param product продукт.
     * @param products элементы меню сгрупированные по продуктам.
     * @return недостающее кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если product имеет значение null. <br/>
     *         2. Если products имеет значение null. <br/>
     */
    public Optional<BigDecimal> getLackQuantity(Product product, Map<Product, List<MenuItemProduct>> products) {
        Validator.check(
                Rule.of("Menu.product").notNull(product),
                Rule.of("Menu.products").notNull(products)
        );

        return getNecessaryQuantity(product, products).
                map(v -> v.subtract(product.getQuantity()).max(BigDecimal.ZERO)).
                map(lackQuantity ->
                        lackQuantity.divide(product.getContext().getPackingSize(), config.getMathContext()).
                                setScale(0, RoundingMode.UP)
                );
    }

    /**
     * Возвращает общую стоимость недостающего кол-ва указанного продукта (с учетом кол-ва, которое уже есть в
     * наличии у пользователя) необходимого для приготовления всех блюд этого меню, в состав которых этот
     * продукт входит. Стоимость недостающего кол-ва рассчитывается с учетом фасовки продукта (размера упаковки).
     * Особые случаи:<br/>
     * 1. Если product не является ключом products - возвращает пустой Optional. <br/>
     * 2. Если products является пустым - возвращает пустой Optional. <br/>
     * Данный метод полагается, что передаваемый ассоциативный массив был корректно сформирован вызывающим кодом,
     * например, вызовом метода {@link #groupByProduct(List)}.
     * @param product продукт.
     * @param products элементы меню сгрупированные по продуктам.
     * @return недостающее кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если product имеет значение null. <br/>
     *         2. Если products имеет значение null. <br/>
     */
    public Optional<BigDecimal> getLackQuantityPrice(Product product, Map<Product, List<MenuItemProduct>> products) {
        return getLackQuantity(product, products).
                map(v -> v.multiply(product.getContext().getPrice()));
    }

    /**
     * Возвращает данные о блюде входящем в состав этого меню. Если среди блюд входящих в данное меню нет блюда
     * с указанным именем - возвращает пустой Optional.
     * @param dishName имя искомого блюда.
     * @return блюдо или пустой Optional.
     */
    public Optional<MenuItem> getMenuItem(String dishName) {
        return items.stream().
                filter(i -> i.getDishName().equals(dishName)).
                findAny();
    }

    /**
     * Возвращает стоимость данного меню с учетом выбранных продуктов для каждого ингредиента блюда и
     * кол-вом даного меню. Расчет стоимости ведется только для недостающего кол-ва продукта. Допускается,
     * что для некоторых ингредиентов некоторых блюд этого меню не указано ни одного продукта. Особые случаи:<br/>
     * 1. Если products пуст - возвращает пустой Optional. <br/>
     * Данный метод полагается, что передаваемый ассоциативный массив был корректно сформирован вызывающим кодом,
     * например, вызовом метода {@link #groupByProduct(List)}.
     * @param products ассоциативный массив продуктов, где каждый продукт соответствует одному ингредиенту одного
     *                 блюда этого меню.
     * @return стоимость данного меню или пустой Optional.
     * @throws ValidateException если products имеет значение null.
     */
    public Optional<BigDecimal> getLackProductsPrice(Map<Product, List<MenuItemProduct>> products) {
        Validator.check(
                Rule.of("Menu.products").notNull(products)
        );

        return products.keySet().stream().
                map(product -> getLackQuantityPrice(product, products)).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает минимально возможную стоимость данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного элемента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.
     * @return минимально возможная стоимость данного меню.
     */
    public Optional<BigDecimal> getMinPrice() {
        return items.stream().
                map(item -> item.getDish().getMinPrice()).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает максимальную возможную стоимость данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного элемента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете средней максимальной стоимости меню.
     * @return максимально возможная стоимость данного меню.
     */
    public Optional<BigDecimal> getMaxPrice() {
        return items.stream().
                map(item -> item.getDish().getMaxPrice()).
                filter(Optional::isPresent).
                map(Optional::get).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает среднюю стоимость для данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного элемента - возвращает пустой Optional.<br/>
     * 2. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 3. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете средней стоимости меню.
     * @return средняя стоимость данного меню.
     */
    public Optional<BigDecimal> getAveragePrice() {
        return getMinPrice().
                flatMap(min -> getMaxPrice().map(max -> max.add(min))).
                map(sum -> sum.divide(new BigDecimal(2), config.getMathContext()));
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
     * Используется для предоставлении данных о конкретном продукте соответствующих одному из ингредиентов
     * блюда входящего в это меню.
     */
    public static final class MenuItemProduct {

        private final MenuItem item;
        private final Product product;
        private final BigDecimal necessaryQuantity;

        /**
         * Создает новый объект MenuItemProduct.
         * @param item элемент этого меню (блюдо и кол-во, в котором оно входит в состав этого меню).
         * @param product один из продуктов соответствующих одному из ингредиентов этого элемента меню.
         * @param necessaryQuantity кол-во этого продукта необходимого для приготовления всех порций блюда
         *                          относящегося к указанному элементу (с учетом кол-ва меню).
         * @param config Общме настройки прилоежния.
         */
        public MenuItemProduct(MenuItem item, Product product, BigDecimal necessaryQuantity, AppConfigData config) {
            this.item = item;
            this.product = product;
            this.necessaryQuantity = necessaryQuantity.setScale(config.getNumberScale(), config.getRoundingMode());
        }

        public MenuItem getItem() {
            return item;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getNecessaryQuantity() {
            return necessaryQuantity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MenuItemProduct that = (MenuItemProduct) o;
            return item.equals(that.item) &&
                    product.equals(that.product) &&
                    necessaryQuantity.equals(that.necessaryQuantity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, product, necessaryQuantity);
        }

        @Override
        public String toString() {
            return "MenuItemProduct{" +
                    "item=" + item +
                    ", product=" + product +
                    ", necessaryQuantity=" + necessaryQuantity +
                    '}';
        }

    }


    /**
     * Используется при расчете цены меню. Позволяет указать конкретные продукты для ингредиентов
     * блюд входящих в меню.
     * @param dishName наименования блюда для ингредиента которого выбирается продукт.
     * @param ingredientIndex индекс ингредиента блюда для которого выбирается продукт. Принимает значения
     *                        в диапозоне [0, кол-во ингредиентов блюда - 1]
     * @param productIndex индекс одно из продуктов подходящих для данного ингредиента. Задаваемое значение
     *                     должно быть больше или равно нулю. Все продукты соответствующие данному ингредиенту
     *                     упорядоченны по цене в порядке возрастания.
     */
    public record ProductConstraint(String dishName, int ingredientIndex, int productIndex) {}


    public static class Builder implements Entity.Builder<Menu> {

        private UUID id;
        private User user;
        private String name;
        private String description;
        private String imageUrl;
        private List<Entity.Builder<MenuItem>> items;
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
            return this;
        }

        public Builder addItem(Entity.Builder<MenuItem> item) {
            items.add(item);
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
