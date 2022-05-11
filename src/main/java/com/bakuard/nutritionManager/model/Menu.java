package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        Container<List<MenuItem>> menuItemsContainer = new Container<>();
        Container<List<Tag>> tagContainer = new Container<>();
        Container<URL> imageURlContainer = new Container<>();

        Validator.check(
                Rule.of("Menu.id").notNull(id),
                Rule.of("Menu.user").notNull(user),
                Rule.of("Menu.name").notNull(name).and(r -> r.notBlank(name)),
                Rule.of("Menu.description").notNull(description).and(r -> r.notBlank(description)),
                Rule.of("Menu.imageUrl").isNull(imageUrl).or(r -> r.isUrl(imageUrl, imageURlContainer)),
                Rule.of("Menu.items").doesNotThrows(items, AbstractBuilder::tryBuild, menuItemsContainer).
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
     * Возвращает кол-во блюд входящих в данное меню.
     * @return кол-во блюд входящих в данное меню.
     */
    public int getMenuItemNumbers() {
        return items.size();
    }

    /**
     * Возвращает данные о блюде входящем в состав этого меню. Если среди блюд меню нет блюда с таким индексом -
     * выбрасывает исключение.
     * @param itemIndex индекс искомого блюда.
     * @return блюдо.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. itemIndex < 0 <br/>
     *         2. itemIndex >= кол-ву блюд меню. <br/>
     */
    public MenuItem tryGetItem(int itemIndex) {
        Validator.check(
                Rule.of("Menu.itemIndex").range(itemIndex, 0, items.size() - 1)
        );

        return items.get(itemIndex);
    }

    /**
     * Возвращает данные о блюде входящем в состав этого меню. Если среди блюд входящих в данное меню нет блюда
     * с указанным именем - выбрасывает исключение.
     * @param dishName имя искомого блюда.
     * @return блюдо.
     * @throws ValidateException если среди блюд этого меню нет блюда с указанным именем.
     */
    public MenuItem tryGetItem(String dishName) {
        return getMenuItem(dishName).
                orElseThrow(
                        () -> new ValidateException().
                                addReason(Rule.of("Menu.dishName").failure(Constraint.CONTAINS_ITEM))
                );
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
     * Возвращает данные о конкретном продукте для каждого ингредиента каждого блюда этого меню. Особые случаи:<br/>
     * 1. Если некоторому ингредиенту некоторого блюда не соответствует ни один продукт - то для этого ингредиента
     *    будет добавлен элемент, метод {@link MenuItemProduct#product()} которого будет возвращаеть пустой
     *    Optional. <br/>
     * 2. Если некоторое блюдо этого меню не содержит ни одного ингредиента - то для этого блюда не будет добавлен
     *    ни один элемент в итоговый список. <br/>
     * 3. Если ни одно блюдо этого меню не содержит ни одного ингредиента - возвращает пустой список. <br/>
     * 4. Если меню не содержит ни одного элемента - возвращает пустой список. <br/>
     * 5. Если для одного и того же ингредиента одного и того же блюда указанно несколько продуктов - будет
     *    выбран первый из указанных. <br/>
     * 6. Если для некоторого ингредиента некоторого блюда не указанно ни одного продукта - будет выбран самый
     *    дешевый из всех продуктов подходящих для данного ингредиента. <br/>
     * 7. Если для некоторого ингредиента некоторого блюда задан индекс продукта, который больше или равен кол-ву
     *    всех продуктов соответствующих данному ингредиенту - то для этого ингредиента будет выбран последний
     *    из всех соответствующих ему продуктов (все продукты упорядочены по цене в порядке возрастания). <br/>
     * @param constraints ограничения задающие конкретные продукты для ингредиентов блюд этого меню.
     * @return данные об одном конкретном продукте для каждого ингредиента блюда этого меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если constraints имеет значение null. <br/>
     *         2. Если один из элементов constraints имеет значение null. <br/>
     *         3. Если ingredientIndex у одного из ProductConstraint меньше нуля. <br/>
     *         4. Если ingredientIndex у одного из ProductConstraint больше или равен кол-ву ингредиентво, и при
     *            этом меню содержит как минимум одно блюдо, которое содержит как минимум один ингредиент.<br/>
     *         5. Если productIndex у одного из ProductConstraint меньше нуля. <br/>
     *         6. Если dishName у одного из ProductConstraint равен null. <br/>
     */
    public List<MenuItemProduct> getMenuItemProducts(List<ProductConstraint> constraints) {
        Validator.check(
                Rule.of("Menu.constrains").notNull(constraints).
                        and(r -> r.notContainsNull(constraints))
        );

        return null;
    }

    /**
     * Групирует элементы меню по продуктам для нахождения одних и тех же продуктов используемых для разных блюд.
     * Если {@link MenuItemProduct#product()} какого-то элемента входного списка возвращает пустой Optional -
     * он не будет участвовать в формировании итогового результата.<br/><br/>
     * Данный метод полагается, что передаваемый список был корректно сформирован вызывающим кодом, например,
     * вызовом метода {@link #getMenuItemProducts(List)}.
     * @param menuItemProducts список продуктов, где каждый продукт соответствует одному ингредиенту одного
     *                         блюда этого меню.
     * @return элементы меню сгрупированные по продуктам.
     * @throws ValidateException если menuItemProducts является null.
     */
    public Map<Product, List<MenuItemProduct>> groupByProduct(List<MenuItemProduct> menuItemProducts) {
        return menuItemProducts.stream().
                filter(i -> i.product().isPresent()).
                collect(Collectors.groupingBy(i -> i.product().orElseThrow()));
    }

    /**
     * Возвращает кол-во указанного ингредиента необходимого для приготовления всех порцию указанного блюда
     * входящего в данное меню, с учетом кол-ва меню. Особые случаи: <br/>
     * 1. Если метод {@link MenuItemProduct#product()} аргумента product возвращает пустой Optional -
     *    метод также возвращает пустой Optional.
     * <br/><br/>
     * Метод не проверят содержимое объекта product (не выходят ли индексы за допустимые пределы, действительно
     * ли указанный продукт соответствует указанном ингредиенту) полагая, что данный объект был сформирован правильно
     * с помощью метода {@link #getMenuItemProducts(List)}.
     * @param product данные о продукте используемом для некоторого ингредиента некоторого блюда этого меню.
     * @param menuNumber кол-во данного меню.
     * @return кол-во указанного ингредиента необходимого для приготовления всех порцию указанного блюда.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий:<br/>
     *              1. menuNumber является null.<br/>
     *              2. menuNumber <= 0 <br/>
     *              3. product является null. <br/>
     */
    public Optional<BigDecimal> getDishIngredientQuantity(MenuItemProduct product,
                                                          BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает ассоциативный массив, каждый элемент которого указывает кол-во заданного продукта (все элементы
     * products должны относится к одному продукту) необходимого для приготовления одного из блюд этого меню.
     * Особые случаи: <br/>
     * 1. Если products является пустым - возвращает пустой ассоциативный массив. <br/>
     * 2. Если {@link MenuItemProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    вернет пустой ассоциативный массив. <br/>
     * 3. Если какой-либо из элементов списка {@link MenuItemProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/><br/>
     * Метод не проверят содержимое объекта products (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода {@link #groupByProduct(List)}.
     * @param products данные о продукте используемом для нескольких ингредиентов нескольких блюд.
     * @param menuNumber кол-во данного меню.
     * @return кол-во указанного продукта необходимого для приготвления каждого блюда этого меню для которых этот
     *         продукт используется.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Map<MenuItem, BigDecimal> getProductQuantityForDishes(List<MenuItemProduct> products,
                                                                 BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает кол-во указанного продукта необходимого для приготовления всех блюд этого меню, в состав
     * которых входит этот продукт. Расчет ведется с учетом кол-ва меню. Особые случаи: <br/>
     * 1. Если products является пустым - возвращает пустой Optional. <br/>
     * 2. Если {@link MenuItemProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 3. Если какой-либо из элементов списка {@link MenuItemProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/><br/>
     * Метод не проверят содержимое объекта products (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода {@link #groupByProduct(List)}.
     * @param products данные о продукте используемом для нескольких ингредиентов нескольких блюд.
     * @param menuNumber кол-во данного меню.
     * @return кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getNecessaryQuantity(List<MenuItemProduct> products,
                                                     BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает НЕДОСТАЮЩЕЕ кол-во указанного продукта (с учетом кол-ва, которое уже есть в наличии у пользователя)
     * необходимого для приготовления всех блюд этого меню, в состав которых этот продукт входит. Недостающее кол-во
     * рассчитывается с учетом фасовки продукта (размера упаковки) и представляет собой кол-во недостающих "упаковок"
     * продукта. Особые случаи:<br/>
     * 1. Если products является пустым - возвращает пустой Optional. <br/>
     * 2. Если {@link MenuItemProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 3. Если какой-либо из элементов списка {@link MenuItemProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/><br/>
     * Метод не проверят содержимое объекта products (список не содержит null, для всех элементов списка
     * испльзуется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода {@link #groupByProduct(List)}.
     * @param products данные о продукте используемом для нескольких ингредиентов нескольких блюд.
     * @param menuNumber кол-во данного меню.
     * @return недостающее кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getLackPackageQuantity(List<MenuItemProduct> products,
                                                       BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает общую стоимость недостающего кол-ва указанного продукта (с учетом кол-ва, которое уже есть в
     * наличии у пользователя) необходимого для приготовления всех блюд этого меню, в состав которых этот
     * продукт входит. Стоимость недостающего кол-ва рассчитывается с учетом фасовки продукта (размера упаковки).
     * Особые случаи:<br/>
     * 1. Если products является пустым - возвращает пустой Optional. <br/>
     * 2. Если {@link MenuItemProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 3. Если какой-либо из элементов списка {@link MenuItemProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/><br/>
     * Метод не проверят содержимое объекта products (список не содержит null, для всех элементов списка
     * используется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
     * метода {@link #groupByProduct(List)}.
     * @param products данные о продукте используемом для нескольких ингредиентов нескольких блюд.
     * @param menuNumber кол-во данного меню.
     * @return стоимость недостающего кол-ва указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getLackPackageQuantityPrice(List<MenuItemProduct> products,
                                                            BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает стоимость данного меню с учетом выбранных продуктов для каждого ингредиента блюда и
     * кол-вом даного меню. Расчет стоимости ведется только для недостающего кол-ва продукта. Допускается,
     * что для некоторых ингредиентов некоторых блюд этого меню не указано ни одного продукта. Особые случаи:<br/>
     * 1. Если products пуст - возвращает пустой Optional. <br/><br/>
     * Данный метод полагается, что передаваемый список был корректно сформирован вызывающим кодом, например,
     * вызовом метода {@link #getMenuItemProducts(List)}.
     * @param products данные об одном из продуктов каждого ингредиента каждого блюда этого меню.
     * @param menuNumber кол-во данного меню.
     * @return стоимость данного меню или пустой Optional.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getLackProductsPrice(List<MenuItemProduct> products,
                                                     BigDecimal menuNumber) {
        return null;
    }

    /**
     * Возвращает минимально возможную стоимость данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного элемента - возвращает пустой Optional.<br/>
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
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
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
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
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
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
    public record MenuItemProduct(Optional<Product> product, int itemIndex, int ingredientIndex, int productIndex) {}


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


    public static class Builder implements AbstractBuilder<Menu> {

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
            return this;
        }

        public Builder addItem(MenuItem.Builder item) {
            items.add(item);
            return this;
        }

        public Builder addTag(String tag) {
            tags.add(tag);
            return this;
        }

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

        public String getImageUrl() {
            return imageUrl;
        }

        public List<MenuItem.Builder> getItems() {
            return items;
        }

        public List<String> getTags() {
            return tags;
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
