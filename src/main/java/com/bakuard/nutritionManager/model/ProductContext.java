package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.validation.*;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Представляет контекстные данные о продукте. Под контекстными здесь подразумевается сововкупность
 * уточняющих данных о продукте, таких как категория, производитель, место продажи, сорт и т.д.
 * Контекстные данные различающиеся хотя бы одним пунктом в уточняющих данных будут представлены
 * как разные объекты ContextProduct.<br/><br/>
 * Объекты даного класса не изменяемы.
 */
public class ProductContext {

    private final String hashKey;
    private final String category;
    private final String shop;
    private final String variety;
    private final String manufacturer;
    private final String unit;
    private final BigDecimal price;
    private final BigDecimal packingSize;
    private final ImmutableSortedSet<Tag> tags;

    private ProductContext(String category,
                           String shop,
                           String variety,
                           String manufacturer,
                           String unit,
                           BigDecimal price,
                           BigDecimal packingSize,
                           ImmutableSortedSet<Tag> tags) {
        this.category = category;
        this.shop = shop;
        this.variety = variety;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.price = price;
        this.packingSize = packingSize;
        this.tags = tags;
        this.hashKey = calculateSha256();
    }

    private ProductContext(String category,
                           String shop,
                           String variety,
                           String manufacturer,
                           String unit,
                           BigDecimal price,
                           BigDecimal packingSize,
                           List<String> tags,
                           AppConfigData config) {
        Container<List<Tag>> container = new Container<>();

        ValidateException.check(
                Rule.of("Product.category").notNull(category).and(r -> r.notBlank(category)),
                Rule.of("Product.shop").notNull(shop).and(v -> v.notBlank(shop)),
                Rule.of("Product.variety").notNull(variety).and(v -> v.notBlank(variety)),
                Rule.of("Product.manufacturer").notNull(manufacturer).and(v -> v.notBlank(manufacturer)),
                Rule.of("Product.unit").notNull(unit).and(v -> v.notBlank(unit)),
                Rule.of("Product.price").notNull(price).and(v -> v.notNegative(price)),
                Rule.of("Product.packingSize").notNull(packingSize).and(v -> v.positiveValue(packingSize)),
                Rule.of("Product.config").notNull(config),
                Rule.of("Product.tags").doesNotThrow(tags, Tag::new, container).and(v -> v.notContainsDuplicate(container.get()))
        );

        this.category = category;
        this.shop = shop;
        this.variety = variety;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.price = price.setScale(config.getNumberScale(), config.getRoundingMode());
        this.packingSize = packingSize.setScale(config.getNumberScale(), config.getRoundingMode());
        this.tags = ImmutableSortedSet.copyOf(container.get());
        this.hashKey = calculateSha256();
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанной категорией. Задаваемое значение будет
     * сохраненно без начальных и конечных пробельных символов.
     * @param category задаваемая категория продукта.
     * @return новый объект, если задаваемое значение категории продукта отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setCategory(String category) {
        ValidateException.check(
                Rule.of("Product.category").notNull(category).and(r -> r.notBlank(category))
        );

        String resultCategory = category.trim();

        if(this.category.equals(resultCategory)) return this;
        return new ProductContext(
                resultCategory,
                shop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанной точкой продажи. Задаваемое значение
     * будет сохраненно без начальных и конечных пробельных символов.
     * @param shop задаваемое наименование точки продажи.
     * @return новый объект, если задаваемое значение точки продажи продукта отличается от текущего, иначе
     *         возвращает этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setShop(String shop) {
        ValidateException.check(
                Rule.of("Product.shop").notNull(shop).and(v -> v.notBlank(shop))
        );

        String resultShop = shop.trim();

        if(this.shop.equals(resultShop)) return this;
        return new ProductContext(
                category,
                resultShop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанным сортом. Задаваемое значение будет
     * сохраненно без начальных и конечных пробельных символов.
     * @param variety задаваемое наименование сорта продукта.
     * @return новый объект, если задаваемое значение сорта продукта отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setVariety(String variety) {
        ValidateException.check(
                Rule.of("Product.variety").notNull(variety).and(v -> v.notBlank(variety))
        );

        String resultVariety = variety.trim();

        if(this.variety.equals(resultVariety)) return this;
        return new ProductContext(
                category,
                shop,
                resultVariety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанным производителем. Задаваемое значение
     * будет сохраненно без начальных и конечных пробельных символов.
     * @param manufacturer задаваемое наименование производителя продукта.
     * @return новый объект, если задаваемое значение производителя отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setManufacturer(String manufacturer) {
        ValidateException.check(
                Rule.of("Product.manufacturer").notNull(manufacturer).and(v -> v.notBlank(manufacturer))
        );

        String resultManufacturer = manufacturer.trim();

        if(this.manufacturer.equals(resultManufacturer)) return this;
        return new ProductContext(
                category,
                shop,
                variety,
                resultManufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанной единицей измерения кол-ва. Задаваемое
     * значение будет сохраненно без начальных и конечных пробельных символов.
     * @param unit единица измерения кол-ва данного продукта.
     * @return новый объект, если задаваемое значение единицы измерения кол-ва отличается от текущего, иначе
     *         возвращает этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setUnit(String unit) {
        ValidateException.check(
                Rule.of("Product.unit").notNull(unit).and(v -> v.notBlank(unit))
        );

        String resultUnit = unit.trim();

        if(this.unit.equals(resultUnit)) return this;

        return new ProductContext(
                category,
                shop,
                variety,
                manufacturer,
                resultUnit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанным значением цены продукта.
     * @param price задаваемое значение цены продукта.
     * @return новый объект, если задаваемое значение цены отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение цены меньше нуля.
     */
    public ProductContext setPrice(BigDecimal price) {
        ValidateException.check(
                Rule.of("Product.price").notNull(price).and(v -> v.notNegative(price))
        );

        if(this.price.equals(price)) return this;

        return new ProductContext(
                category,
                shop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвразает новый объект отличающийся от текущего указанным значением размера упаковки. Например,
     * питьевая вода может продаваться в бутылках по 0.5 л., 1 л. или 2 л.
     * @param packingSize размер упаковки, которыми продается продукт.
     * @return новый объект, если указанный размер упаковки отличается от текущего, иначе - этот же объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если packingSize меньше или равен нулю.
     */
    public ProductContext setPackingSize(BigDecimal packingSize) {
        ValidateException.check(
                Rule.of("Product.packingSize").notNull(packingSize).and(v -> v.positiveValue(packingSize))
        );

        if(this.packingSize.equals(packingSize)) return this;

        return new ProductContext(
                category,
                shop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags
        );
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего добавленным тегом переданным в данный метод.
     * @param tag добавляемый тег.
     * @return новый объект.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанный тег уже содержится в данном объекте.
     */
    public ProductContext addTag(Tag tag) {
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        ValidateException.check(
                Rule.of("Product.tag").notNull(tag),
                Rule.of("Product.tags").notContainsItem(tags, tag)
        );

        return new ProductContext(
                category,
                shop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                ImmutableSortedSet.copyOf(tags)
        );
    }

    /**
     * Создает и возвращавет новый объект отличающийся от текщуего удаленным тегом, равным указанному.
     * @param tag удаляемый тег.
     * @return новый объект, если указанный тег присутствует в текущем объекте, или этот же объект, если
     *         указанный тег отсутствует в текущем объекте или имеет значение null.
     */
    public ProductContext removeTag(Tag tag) {
        if(!tags.contains(tag)) return this;

        return new ProductContext(
                category,
                shop,
                variety,
                manufacturer,
                unit,
                price,
                packingSize,
                tags.headSet(tag)
        );
    }

    /**
     * Возвращает хеш-сумму полученную от данного объекта с использованием алгоритма SHA-256.
     * @return хеш-сумма полученная от данного объекта с использованием алгоритма SHA-256.
     */
    public String hashKey() {
        return hashKey;
    }

    /**
     * Возвращает категорию продукта.
     * @return категорию продукта.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Возвращает наименование магазина.
     * @return наименование магазина.
     */
    public String getShop() {
        return shop;
    }

    /**
     * Возвращает сорт продукта.
     * @return сорт продукта.
     */
    public String getVariety() {
        return variety;
    }

    /**
     * Возвращает производителя продукта.
     * @return производитель продукта.
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Возвращает наименование единицы измерение кол-ва продукта.
     * @return наименование единицы измерение кол-ва продукта.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Возвращает цену продукта.
     * @return цена продукта.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Возвращает стоимость одной единицы кол-ва продукта. Это значение определяется соотношением
     * размера упаковки к её цене. Например, стоимость литра питьевой воды для бутылки в 0.5 л., 1 л. и
     * 2 л. скорее всего будет отличаться.
     * @param mc параметры округления и ограничения точности вычислений.
     * @return стоимость одной единицы кол-ва данного продукта.
     */
    public BigDecimal getUnitPrice(MathContext mc) {
        return packingSize.divide(price, mc);
    }

    /**
     * Возвращает размер упаковки для продукта.
     * @return размер упаковки для продукта.
     */
    public BigDecimal getPackingSize() {
        return packingSize;
    }

    /**
     * Проверяет - содердится ли уквазанный тег в данном объекте.
     * @param tag искомый тег.
     * @return true - если указанный тег содержится в данном продукте, иначе - false.
     */
    public boolean containsTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Возвращает набор всех тегов данного продукта доступный только для чтения.
     * @return набор всех тегов данного продукта.
     */
    public ImmutableSortedSet<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductContext context = (ProductContext) o;
        return category.equals(context.category) &&
                shop.equals(context.shop) &&
                variety.equals(context.variety) &&
                manufacturer.equals(context.manufacturer) &&
                unit.equals(context.unit) &&
                price.equals(context.price) &&
                packingSize.equals(context.packingSize) &&
                tags.equals(context.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, shop, variety, manufacturer, unit, price, packingSize, tags);
    }

    @Override
    public String toString() {
        return "ProductContext{" +
                "hashKey='" + hashKey + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", variety='" + variety + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", unit='" + unit + '\'' +
                ", price=" + price +
                ", packingSize=" + packingSize +
                ", tags=" + tags +
                '}';
    }


    private String calculateSha256() {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
            ObjectOutputStream writer = new ObjectOutputStream(buffer);

            writer.writeObject(category);
            writer.writeObject(shop);
            writer.writeObject(variety);
            writer.writeObject(unit);
            writer.writeObject(price);
            writer.writeObject(packingSize);
            for(Tag tag : tags) writer.writeObject(tag.getValue());
            writer.flush();

            return Hashing.
                    sha256().
                    hashBytes(buffer.toByteArray()).
                    toString();
        } catch(IOException e) {
            throw new RuntimeException("Unexpected exception.", e);
        }
    }


    public static class Builder implements AbstractBuilder<ProductContext> {

        private String category;
        private String shop;
        private String variety;
        private String manufacturer;
        private String unit;
        private BigDecimal price;
        private BigDecimal packingSize;
        private final List<String> tags;
        private AppConfigData appConfigData;

        public Builder() {
            tags = new ArrayList<>();
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setShop(String shop) {
            this.shop = shop;
            return this;
        }

        public Builder setVariety(String variety) {
            this.variety = variety;
            return this;
        }

        public Builder setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
            return this;
        }

        public Builder setUnit(String unit) {
            this.unit = unit;
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder setPackingSize(BigDecimal packingSize) {
            this.packingSize = packingSize;
            return this;
        }

        public Builder setAppConfiguration(AppConfigData appConfigData) {
            this.appConfigData = appConfigData;
            return this;
        }

        public Builder addTag(String tagValue) {
            tags.add(tagValue);
            return this;
        }

        @Override
        public ProductContext tryBuild() throws ValidateException {
            return new ProductContext(category, shop, variety, manufacturer, unit,
                    price, packingSize, tags, appConfigData);
        }

    }

}
