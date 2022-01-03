package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.hash.Hashing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
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
        ProductContextValidateException validateException =
                new ProductContextValidateException("Fail to create product context");

        validateException.addReason(checkCategory(category));
        validateException.addReason(checkShop(shop));
        validateException.addReason(checkVariety(variety));
        validateException.addReason(checkManufacturer(manufacturer));
        validateException.addReason(checkUnit(unit));
        validateException.addReason(checkPrice(price));
        validateException.addReason(checkPackingSize(packingSize));
        validateException.addReason(checkAppConfig(config));

        List<Tag> validTags = null;
        try {
            validTags = tags.stream().map(Tag::new).toList();
            validateException.addReason(checkTags(validTags));
        } catch(IncorrectFiledValueException e) {
            validateException.addReason(e);
        }

        if(validateException.violatedConstraints()) throw validateException;

        this.category = category;
        this.shop = shop;
        this.variety = variety;
        this.manufacturer = manufacturer;
        this.unit = unit;
        this.price = price.setScale(config.getNumberScale(), config.getRoundingMode());
        this.packingSize = packingSize.setScale(config.getNumberScale(), config.getRoundingMode());
        this.tags = ImmutableSortedSet.copyOf(validTags);
        this.hashKey = calculateSha256();
    }

    /**
     * Создает и возвращает новый объект отличающийся от текущего указанной категорией. Задаваемое значение будет
     * сохраненно без начальных и конечных пробельных символов.
     * @param category задаваемая категория продукта.
     * @return новый объект, если задаваемое значение категории продукта отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws MissingValueException если указанное значение равняется null.
     * @throws BlankValueException если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setCategory(String category) {
        tryThrow(checkCategory(category));

        category = category.trim();

        if(this.category.equals(category)) return this;
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
     * Создает и возвращает новый объект отличающийся от текущего указанной точкой продажи. Задаваемое значение
     * будет сохраненно без начальных и конечных пробельных символов.
     * @param shop задаваемое наименование точки продажи.
     * @return новый объект, если задаваемое значение точки продажи продукта отличается от текущего, иначе
     *         возвращает этот же объект.
     * @throws MissingValueException если указанное значение равняется null.
     * @throws BlankValueException если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setShop(String shop) {
        tryThrow(checkShop(shop));

        shop = shop.trim();

        if(this.shop.equals(shop)) return this;
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
     * Создает и возвращает новый объект отличающийся от текущего указанным сортом. Задаваемое значение будет
     * сохраненно без начальных и конечных пробельных символов.
     * @param variety задаваемое наименование сорта продукта.
     * @return новый объект, если задаваемое значение сорта продукта отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws MissingValueException если указанное значение равняется null.
     * @throws BlankValueException если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setVariety(String variety) {
        tryThrow(checkVariety(variety));

        variety = variety.trim();

        if(this.variety.equals(variety)) return this;
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
     * Создает и возвращает новый объект отличающийся от текущего указанным производителем. Задаваемое значение
     * будет сохраненно без начальных и конечных пробельных символов.
     * @param manufacturer задаваемое наименование производителя продукта.
     * @return новый объект, если задаваемое значение производителя отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws MissingValueException если указанное значение равняется null.
     * @throws BlankValueException если указанное значение не содержит ни одного отображаемого символа.
     */
    public ProductContext setManufacturer(String manufacturer) {
        tryThrow(checkManufacturer(manufacturer));

        manufacturer = manufacturer.trim();

        if(this.manufacturer.equals(manufacturer)) return this;
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
     * Создает и возвращает новый объект отличающийся от текущего указанной единицей измерения кол-ва. Задаваемое
     * значение будет сохраненно без начальных и конечных пробельных символов.
     * @param unit единица измерения кол-ва данного продукта.
     * @return новый объект, если задаваемое значение единицы измерения кол-ва отличается от текущего, иначе
     *         возвращает этот же объект.
     * @throws MissingValueException если указанное значение unit имеет значение null.
     * @throws BlankValueException если указанное значение unit не содержит ни одного отображаемого символа.
     */
    public ProductContext setUnit(String unit) {
        tryThrow(checkUnit(unit));

        unit = unit.trim();

        if(this.unit.equals(unit)) return this;

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
     * Создает и возвращает новый объект отличающийся от текущего указанным значением цены продукта.
     * @param price задаваемое значение цены продукта.
     * @return новый объект, если задаваемое значение цены отличается от текущего, иначе возвращает
     *         этот же объект.
     * @throws MissingValueException если указанное значение цены продукта null.
     * @throws NegativeValueException если указанное значение цены меньше нуля.
     */
    public ProductContext setPrice(BigDecimal price) {
        tryThrow(checkPrice(price));

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
     * @throws MissingValueException если packingSize имеет значение null.
     * @throws OutOfRangeException если packingSize меньше или равен нулю.
     */
    public ProductContext setPackingSize(BigDecimal packingSize) {
        tryThrow(checkPackingSize(packingSize));

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
     * @throws MissingValueException если указанный объект tag имеет значение null.
     * @throws DuplicateTagException если указанный тег уже содержится в данном объекте.
     */
    public ProductContext addTag(Tag tag) {
        List<Tag> tags = new ArrayList<>();
        tags.add(tag);

        tryThrow(checkTags(tags));

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


    private void tryThrow(RuntimeException e) {
        if(e != null) throw e;
    }

    private IncorrectFiledValueException checkCategory(String category) {
        IncorrectFiledValueException exception = null;

        if(category == null) {
            exception = new MissingValueException("Product category can't be null", getClass(), "category");
        } else if(category.isBlank()) {
            exception = new BlankValueException("Product category can't be blank", getClass(), "category");
        }

        return exception;
    }

    private IncorrectFiledValueException checkShop(String shop) {
        IncorrectFiledValueException exception = null;

        if(shop == null) {
            exception = new MissingValueException("Product shop can't be null", getClass(), "shop");
        } else if(shop.isBlank()) {
            exception = new BlankValueException("Product shop can't be blank", getClass(), "shop");
        }

        return exception;
    }

    private IncorrectFiledValueException checkVariety(String variety) {
        IncorrectFiledValueException exception = null;

        if(variety == null) {
            exception = new MissingValueException("Product variety can't be null", getClass(), "variety");
        } else if(variety.isBlank()) {
            exception = new BlankValueException("Product variety can't be blank", getClass(), "variety");
        }

        return exception;
    }

    private IncorrectFiledValueException checkManufacturer(String manufacturer) {
        IncorrectFiledValueException exception = null;

        if(manufacturer == null) {
            exception = new MissingValueException("Product manufacturer can't be null", getClass(), "manufacturer");
        } else if(manufacturer.isBlank()) {
            exception = new BlankValueException("Product manufacturer can't be blank", getClass(), "manufacturer");
        }

        return exception;
    }

    private IncorrectFiledValueException checkUnit(String unit) {
        IncorrectFiledValueException exception = null;

        if(unit == null) {
            exception = new MissingValueException("Product unit can't be null.", getClass(), "unit");
        } else if(unit.isBlank()) {
            exception = new BlankValueException("Product unit can not be blank", getClass(), "unit");
        }

        return exception;
    }

    private IncorrectFiledValueException checkPrice(BigDecimal price) {
        IncorrectFiledValueException exception = null;

        if(price == null) {
            exception = new MissingValueException("Product price can't be null.", getClass(), "price");
        } else if(price.signum() < 0) {
            exception = new NegativeValueException("Product price can't be negative", getClass(), "price");
        }

        return exception;
    }

    private IncorrectFiledValueException checkPackingSize(BigDecimal packingSize) {
        IncorrectFiledValueException exception = null;

        if(packingSize == null) {
            exception = new MissingValueException("Product packingSize can't be null", getClass(), "packingSize");
        } else if(packingSize.signum() <= 0) {
            exception = new NotPositiveValueException("Product packingSize must be greater then 0",
                    getClass(),
                    "packingSize",
                    packingSize);
        }

        return exception;
    }

    private IncorrectFiledValueException checkTags(List<Tag> tags) {
        IncorrectFiledValueException exception = null;

        if(tags.stream().anyMatch(Objects::isNull)) {
            exception = new MissingValueException("Product tag can't be null.", getClass(), "tag");
        } else if(tags.stream().anyMatch(tag -> Collections.frequency(tags, tag) > 2)) {
            exception = new DuplicateTagException(
                    "This tag is already specified for the product.",
                    getClass(),
                    "tag");
        }

        return exception;
    }

    private IncorrectFiledValueException checkAppConfig(AppConfigData config) {
        IncorrectFiledValueException exception = null;
        if(config == null) {
            exception = new MissingValueException(
                    "AppConfiguration for productContext cant' be null", getClass(), "config");
        }
        return exception;
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
