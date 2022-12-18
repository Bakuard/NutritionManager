package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.configData.ConfigData;
import com.bakuard.nutritionManager.validation.Container;
import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bakuard.nutritionManager.validation.Rule.*;

/**
 * Представляет контекстные данные о продукте. Под контекстными здесь подразумевается совокупность
 * уточняющих данных о продукте, таких как категория, производитель, место продажи, сорт и т.д.
 * ContextProduct различающиеся хотя бы одним полем считаются разными объектами.<br/><br/>
 * Объекты данного класса не изменяемы.
 */
public class ProductContext {

    private final String hashKey;
    private final String category;
    private final String shop;
    private final String grade;
    private final String manufacturer;
    private final String unit;
    private final BigDecimal price;
    private final BigDecimal packingSize;
    private final ImmutableList<Tag> tags;

    private ProductContext(String category,
                           String shop,
                           String grade,
                           String manufacturer,
                           String unit,
                           BigDecimal price,
                           BigDecimal packingSize,
                           List<String> tags,
                           ConfigData config) {
        Container<List<Tag>> container = new Container<>();

        Validator.check(
                "ProductContext.category", notNull(category).and(() -> notBlank(category)),
                "ProductContext.shop", notNull(shop).and(() -> notBlank(shop)),
                "ProductContext.grade", notNull(grade).and(() -> notBlank(grade)),
                "ProductContext.manufacturer", notNull(manufacturer).and(() -> notBlank(manufacturer)),
                "ProductContext.unit", notNull(unit).and(() -> notBlank(unit)),
                "ProductContext.price", notNull(price).and(() -> notNegative(price)),
                "ProductContext.packingSize", notNull(packingSize).and(() -> positiveValue(packingSize)),
                "ProductContext.config", notNull(config),
                "ProductContext.tags", doesNotThrows(tags, Tag::new, container).and(() -> notContainsDuplicate(container.get()))
        );

        this.category = category;
        this.shop = shop;
        this.grade = grade;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.price = price.setScale(config.decimal().numberScale(), config.decimal().roundingMode());
        this.packingSize = packingSize.setScale(config.decimal().numberScale(), config.decimal().roundingMode());
        this.tags = ImmutableList.copyOf(container.get());
        this.hashKey = calculateSha256();
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
    public String getGrade() {
        return grade;
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
     * Проверяет - содержится ли указанный тег в данном объекте.
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
    public ImmutableList<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductContext context = (ProductContext) o;
        return category.equals(context.category) &&
                shop.equals(context.shop) &&
                grade.equals(context.grade) &&
                manufacturer.equals(context.manufacturer) &&
                unit.equals(context.unit) &&
                price.equals(context.price) &&
                packingSize.equals(context.packingSize) &&
                tags.equals(context.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, shop, grade, manufacturer, unit, price, packingSize, tags);
    }

    @Override
    public String toString() {
        return "ProductContext{" +
                "hashKey='" + hashKey + '\'' +
                ", category='" + category + '\'' +
                ", shop='" + shop + '\'' +
                ", grade='" + grade + '\'' +
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
            writer.writeObject(grade);
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
        private String grade;
        private String manufacturer;
        private String unit;
        private BigDecimal price;
        private BigDecimal packingSize;
        private final List<String> tags;
        private ConfigData configData;

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

        public Builder setGrade(String grade) {
            this.grade = grade;
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

        public Builder setAppConfiguration(ConfigData configData) {
            this.configData = configData;
            return this;
        }

        public Builder addTag(String tagValue) {
            tags.add(tagValue);
            return this;
        }

        @Override
        public ProductContext tryBuild() throws ValidateException {
            return new ProductContext(category, shop, grade, manufacturer, unit,
                    price, packingSize, tags, configData);
        }

    }

}
