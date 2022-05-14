package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.validation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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
     * 1. Если меню не содержит ни одного элемента - возвращает пустой список. <br/>
     * 2. Если ни одно блюдо этого меню не содержит ни одного ингредиента - возвращает пустой список. <br/>
     * 3. Если некоторое блюдо этого меню не содержит ни одного ингредиента - то для этого блюда не будет добавлен
     *    ни один элемент в итоговый список. <br/>
     * 4. Если {@link ProductConstraint#dishName()} одного из элементов указывает на блюдо, которого нет в данном
     *    меню - этот элемент будет отброшен и не будет принимать участия в формировании конечного резузльтата. <br/>
     * 5. Если {@link ProductConstraint#ingredientIndex()} некоторого элемента < 0 - этот элемент будет отброшен
     *    и не будет принимать участия в формировании конечного резузльтата. <br/>
     * 6. Если {@link ProductConstraint#ingredientIndex()} некоторого элемента >= кол-во всех ингредиентов
     *    соответствующего блюда - этот элемент будет отброшен и не будет принимать участия в формировании конечного
     *    резузльтата. <br/>
     * 7. Если {@link ProductConstraint#productIndex()} некоторого элемента < 0 - этот элемент будет отброшен
     *    и не будет принимать участия в формировании конечного резузльтата. <br/>
     * 8. Если {@link ProductConstraint#productIndex()} некоторого элемента >= кол-во всех продуктов соответствующего
     *    блюда и ингредиента, и при этом кол-во этих продуктов больше нуля - этот элемент будет отброшен и не будет
     *    принимать участия в формировании конечного резузльтата. <br/>
     * 10. Если некоторому ингредиенту некоторого блюда не соответствует ни один продукт - то для этого ингредиента
     *    будет добавлен элемент, метод {@link MenuItemProduct#product()} которого будет возвращаеть пустой
     *    Optional. <br/>
     * 11. Если для одного и того же ингредиента одного и того же блюда constraints содержит несколько элементов -
     *    будет выбран первый корректный из указанных. <br/>
     * 12. Если для некоторого ингредиента некоторого блюда не указанно ни одного элемента в constraints или все
     *    элементы содержащие такие ограничения были отброшены - будет выбран самый дешевый из всех продуктов
     *    подходящих для данного ингредиента. <br/>
     * @param constraints ограничения задающие конкретные продукты для ингредиентов блюд этого меню.
     * @return данные об одном конкретном продукте для каждого ингредиента блюда этого меню.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. Если constraints имеет значение null. <br/>
     *         2. Если один из элементов constraints имеет значение null. <br/>
     */
    public List<MenuItemProduct> getMenuItemProducts(List<ProductConstraint> constraints) {
        Validator.check(
                Rule.of("Menu.constrains").notNull(constraints).
                        and(r -> r.notContainsNull(constraints))
        );

        ArrayList<MenuItemProduct> result = new ArrayList<>();
        for(int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            Dish dish = items.get(itemIndex).getDish();
            for(int i = 0; i < dish.getIngredientNumber(); i++) {
                final int ingredientIndex = i;
                int productIndex = constraints.stream().
                        filter(c -> c.ingredientIndex() == ingredientIndex && dish.getName().equals(c.dishName())).
                        mapToInt(ProductConstraint::productIndex).
                        filter(pIndex -> pIndex >= 0 && pIndex < dish.getProductsNumber(ingredientIndex).orElseThrow()).
                        findFirst().
                        orElse(0);

                Dish.IngredientProduct product = dish.getProduct(ingredientIndex, productIndex).orElseThrow();

                MenuItemProduct menuItemProduct = new MenuItemProduct(
                        product.product(), itemIndex, ingredientIndex, productIndex);
                result.add(menuItemProduct);
            }
        }
        return result;
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
        Validator.check(
                Rule.of("Menu.product").notNull(product),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        BigDecimal result = null;

        if(product.product().isPresent()) {
            MenuItem item = items.get(product.itemIndex());
            DishIngredient ingredient = item.getDish().tryGetIngredient(product.ingredientIndex());
            result = ingredient.getNecessaryQuantity(item.getNecessaryQuantity(menuNumber));
            result = result.setScale(config.getNumberScale(), config.getRoundingMode());
        }

        return Optional.ofNullable(result);
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
     * используется один и тот же продукт и т.д.) полагая, что данный объект был сформирован правильно с помощью
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
        Validator.check(
                Rule.of("Menu.products").notNull(products),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        Map<Integer, List<MenuItemProduct>> productsByDishes = products.stream().
                filter(p -> p.product().isPresent()).
                collect(Collectors.groupingBy(MenuItemProduct::itemIndex));

        return productsByDishes.entrySet().stream().
                collect(Collectors.toMap(
                        pair -> items.get(pair.getKey()),
                        pair -> pair.getValue().stream().
                                    map(p -> getDishIngredientQuantity(p, menuNumber).orElseThrow()).
                                    reduce(BigDecimal::add).
                                    orElseThrow()
                ));
    }

    /**
     * Возвращает кол-во указанного продукта необходимого для приготовления всех блюд этого меню, в состав
     * которых входит этот продукт (все элементы products должны относится к одному продукту). Расчет ведется
     * с учетом кол-ва меню. <br/>
     * Особые случаи: <br/>
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
        Validator.check(
                Rule.of("Menu.products").notNull(products),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        return products.stream().
                filter(p -> p.product().isPresent()).
                map(p -> getDishIngredientQuantity(p, menuNumber).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает необходимое кол-во "упаковок" продукта необходимого для приготовления всех блюд этого меню,
     * в состав которых этот продукт входит. Все элементы products должны относится к одному продукту. Недостающее
     * кол-во рассчитывается с учетом фасовки продукта (размера упаковки). <br/>
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
     * @return недостающее кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getNecessaryPackageQuantity(List<MenuItemProduct> products,
                                                            BigDecimal menuNumber) {
        Validator.check(
                Rule.of("Menu.products").notNull(products),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        return getNecessaryQuantity(products, menuNumber).
                map(necessaryQuantityInUnits -> {
                    Product product = retrieveProduct(products);
                    return calculatePackagesNumber(product, necessaryQuantityInUnits);
                });
    }

    /**
     * Возвращает общую стоимость кол-ва "упаковок" указанного продукта необходимого для приготовления всех блюд
     * этого меню, в состав которых этот продукт входит. Все элементы products должны относится к одному продукту.
     * Стоимость недостающего кол-ва рассчитывается с учетом фасовки продукта (размера упаковки).<br/>
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
    public Optional<BigDecimal> getNecessaryPackageQuantityPrice(List<MenuItemProduct> products,
                                                                 BigDecimal menuNumber) {
        return getNecessaryPackageQuantity(products, menuNumber).
                map(necessaryPackageQuantity -> {
                    Product product = retrieveProduct(products);
                    return calculateProductPrice(product, necessaryPackageQuantity);
                });
    }

    /**
     * Возвращает НЕДОСТАЮЩЕЕ кол-во указанного продукта (с учетом кол-ва, которое уже есть в наличии у пользователя)
     * необходимого для приготовления всех блюд этого меню, в состав которых этот продукт входит. Все элементы
     * products должны относится к одному продукту. Недостающее кол-во рассчитывается с учетом фасовки продукта
     * (размера упаковки) и представляет собой кол-во недостающих "упаковок" продукта. <br/>
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
     * @return недостающее кол-во указанного продукта.
     * @throws ValidateException если выполняется хотя бы одно из следующих условий: <br/>
     *         1. если products имеет значение null. <br/>
     *         2. если menuNumber имеет значение null.<br/>
     *         3. если menuNumber меньше или равно нулю.<br/>
     */
    public Optional<BigDecimal> getLackPackageQuantity(List<MenuItemProduct> products,
                                                       BigDecimal menuNumber) {
        Validator.check(
                Rule.of("Menu.products").notNull(products),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        return getNecessaryQuantity(products, menuNumber).
                map(necessaryQuantityInUnits -> {
                    Product product = retrieveProduct(products);
                    BigDecimal lackQuantityInUnits = calculateLackQuantityInUnits(product, necessaryQuantityInUnits);
                    return calculatePackagesNumber(product, lackQuantityInUnits);
                });
    }

    /**
     * Возвращает общую стоимость недостающего кол-ва указанного продукта (с учетом кол-ва, которое уже есть в
     * наличии у пользователя) необходимого для приготовления всех блюд этого меню, в состав которых этот
     * продукт входит. Все элементы products должны относится к одному продукту. Стоимость недостающего кол-ва
     * рассчитывается с учетом фасовки продукта (размера упаковки).<br/>
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
        return getLackPackageQuantity(products, menuNumber).
                map(lackPackageQuantity -> {
                    Product product = retrieveProduct(products);
                    return calculateProductPrice(product, lackPackageQuantity);
                });
    }

    /**
     * Возвращает стоимость данного меню с учетом выбранных продуктов для каждого ингредиента блюда и
     * кол-вом даного меню. Расчет стоимости ведется только для недостающего кол-ва продукта. Допускается,
     * что для некоторых ингредиентов некоторых блюд этого меню не указано ни одного продукта.
     * Особые случаи:<br/>
     * 1. Если products является пустым - возвращает пустой Optional. <br/>
     * 2. Если {@link MenuItemProduct#product()} каждого элемента списка возвращает пустой Optional - этот метод
     *    также вернет пустой Optional. <br/>
     * 3. Если какой-либо из элементов списка {@link MenuItemProduct#product()} возвращает пустой Optional -
     *    то этот элемент списка не принимает участия в формировании итогового результата. <br/><br/>
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
        Validator.check(
                Rule.of("Menu.products").notNull(products),
                Rule.of("Menu.menuNumber").notNull(menuNumber).
                        and(r -> r.positiveValue(menuNumber))
        );

        Map<Product, List<MenuItemProduct>> groups = groupByProduct(products);

        return groups.values().stream().
                map(value -> getLackPackageQuantityPrice(value, menuNumber).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает минимально возможную стоимость данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного блюда - возвращает пустой Optional.<br/>
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
     * @return минимально возможная стоимость данного меню.
     */
    public Optional<BigDecimal> getMinPrice() {
        List<ProductConstraint> constraints = items.stream().
                flatMap(item -> {
                    String dishName = item.getDishName();
                    return IntStream.range(0, item.getDish().getIngredientNumber()).
                            mapToObj(ingredientIndex ->
                                    new Menu.ProductConstraint(dishName, ingredientIndex, 0));
                }).
                toList();

        List<MenuItemProduct> menuItems = getMenuItemProducts(constraints);

        return groupByProduct(menuItems).values().stream().
                map(values -> getNecessaryPackageQuantityPrice(values, BigDecimal.ONE).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает максимальную возможную стоимость данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного блюда - возвращает пустой Optional.<br/>
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
     * @return максимально возможная стоимость данного меню.
     */
    public Optional<BigDecimal> getMaxPrice() {
        List<MenuItemProduct> menuItems = new ArrayList<>();
        for(int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            Dish dish = items.get(itemIndex).getDish();
            for(int ingredientIndex = 0; ingredientIndex < dish.getIngredientNumber(); ingredientIndex++) {
                /*
                 * Если метод dish.getProducts(ingredientIndex, pageNumber) в качестве pageNumber получит значение,
                 * которое больше или равно кол-ву всех страниц на которые разбита исходная выборка - то метод вернет
                 * последнюю страницу выборки. Здесь мы берем значение, которое точно будет больше числа страниц любой
                 * выборки (вряд ли у какого-то пользователя наберется 30000 продуктов).
                 */
                int latsPageNumber = 10000;
                Page<Dish.IngredientProduct> page = dish.getProducts(ingredientIndex, latsPageNumber).
                        orElseThrow();
                int lastProductIndex = page.getMetadata().getTotalItems().
                        subtract(BigInteger.ONE).
                        intValueExact();
                Optional<Product> lastProduct = page.getByGlobalIndex(lastProductIndex).
                        flatMap(Dish.IngredientProduct::product);

                MenuItemProduct item = new MenuItemProduct(lastProduct, itemIndex, ingredientIndex, lastProductIndex);
                menuItems.add(item);
            }
        }

        return groupByProduct(menuItems).values().stream().
                map(values -> getNecessaryPackageQuantityPrice(values, BigDecimal.ONE).orElseThrow()).
                reduce(BigDecimal::add);
    }

    /**
     * Возвращает среднюю стоимость для данного меню. Особые случаи:<br/>
     * 1. Если для данного меню не было указанно ни одного блюда - возвращает пустой Optional.<br/>
     * 2. Если для любого блюда меню не задано ни одного ингредиента - возвращает пустой Optional.<br/>
     * 3. Если любому ингредиенту любого блюда этого меню не соответствует ни одного продукта -
     *    возвращает пустой Optional.<br/>
     * 4. Если какому-либо ингредиенту некоторого блюда не соответствует ни одного продукта - то он не принимает
     *    участия в рассчете минимальной стоимости меню.<br/>
     * @return средняя стоимость данного меню.
     */
    public Optional<BigDecimal> getAveragePrice() {
        Optional<BigDecimal> max = getMaxPrice();
        Optional<BigDecimal> min = getMinPrice();
        Optional<BigDecimal> sum = min.map(vMin -> vMin.add(max.orElseThrow()));
        return sum.map(vSum -> vSum.divide(new BigDecimal(2), config.getMathContext()));
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


    private Product retrieveProduct(List<MenuItemProduct> menuItemProducts) {
        return menuItemProducts.stream().
                filter(i -> i.product().isPresent()).
                map(i -> i.product().orElseThrow()).
                findAny().
                orElseThrow();
    }

    private BigDecimal calculateLackQuantityInUnits(Product product,
                                                    BigDecimal necessaryQuantity) {
        return necessaryQuantity.subtract(product.getQuantity()).max(BigDecimal.ZERO);
    }

    private BigDecimal calculatePackagesNumber(Product product, BigDecimal quantityInUnits) {
        if(quantityInUnits.signum() > 0) {
            quantityInUnits = quantityInUnits.
                    divide(product.getContext().getPackingSize(), config.getMathContext()).
                    setScale(0, RoundingMode.UP);
        }

        return quantityInUnits;
    }

    private BigDecimal calculateProductPrice(Product product, BigDecimal packagesNumber) {
        return product.getContext().getPrice().multiply(packagesNumber, config.getMathContext());
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
