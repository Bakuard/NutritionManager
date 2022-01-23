package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.config.AppConfigData;
import com.bakuard.nutritionManager.model.exceptions.*;
import com.bakuard.nutritionManager.model.util.AbstractBuilder;

import java.math.BigDecimal;
import java.util.*;

/**
 * Представляет данные об одном конкретном типе продуктов в сочетании с уточняющей информацией ({@link ProductContext});
 */
public class Product {

    private final UUID id;
    private final User user;
    private ProductContext context;
    private BigDecimal quantity;
    private String description;
    private String imagePath;

    public Product(Product other) {
        id = other.id;
        user = new User(other.user);
        context = other.context;
        quantity = other.quantity;
        description = other.description;
        imagePath = other.imagePath;
    }

    private Product(UUID id,
                    User user,
                    String description,
                    BigDecimal quantity,
                    String imagePath,
                    ProductContext.Builder contextBuilder,
                    AppConfigData config) {
        Checker.Container<ProductContext> container = Checker.container();

        Checker.of(getClass(), "constructor").
                nullValue("id", id).
                nullValue("user", user).
                nullValue("quantity", quantity).
                negativeValue("quantity", quantity).
                nullValue("config", config).
                nullValue("context", contextBuilder).
                tryBuild(contextBuilder, container).
                checkWithValidateException("Fail to create product");

        this.id = id;
        this.user = user;
        this.context = container.get();
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.description = description;
        this.imagePath = imagePath;
    }

    /**
     * Устанавливает для данного продукта указанные контекстные данные.
     * @param context контекстные данные данного продукта ({@link ProductContext}).
     * @throws ValidateException если указанное значение равняется null
     */
    public void setContext(ProductContext context) {
        Checker.of(getClass(), "setContext").
                nullValue("context", context).
                checkWithValidateException("Fail to set product context");

        this.context = context;
    }

    /**
     * Увеличевает кол-во данного продукта имеющегося в распоряжении у пользователя на указанное значение.
     * @param quantity значение на которое будет увеличенно кол-во данного продукта имеющегося в распряжении у
     *                 пользвателя.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение меньше нуля.
     */
    public void addQuantity(BigDecimal quantity) {
        Checker.of(getClass(), "addQuantity").
                nullValue("quantity", quantity).
                negativeValue("quantity", quantity).
                checkWithValidateException("Fail to add product quantity");

        this.quantity = this.quantity.add(quantity);
    }

    /**
     * Пытается уменьшить кол-во данного продукта имеющееся в распоряжении у пользователя на заданное значение и
     * возращает значение на которое фактически удалось уменьшить кол-во данного продукта. Если кол-во продукта
     * имеющееся в распоряжении у пользователя меньше указанного значения, то метод вернет значение равное кол-ву
     * этого продукта имеющегося у пользователя. Если же кол-во продукта у пользваотеля больше или равно quantity,
     * тогда метод вернет значение равное quantity.
     * @param quantity снимаемое кол-во продукта.
     * @return кол-во продукта, которое удалось снять.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение меньше нуля.
     */
    public BigDecimal take(BigDecimal quantity) {
        Checker.of(getClass(), "take").
                nullValue("quantity", quantity).
                negativeValue("quantity", quantity).
                checkWithValidateException("Fail to take product quantity");

        BigDecimal remain = this.quantity.min(quantity);
        this.quantity = this.quantity.subtract(remain);
        return remain;
    }

    /**
     * Устанавливает описание для текущего продукта. Метод модет принимать значение null.
     * @param description описание продукта.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Устанавливает значение пути к изображению данного продукта. Путь не обязательно может быть путем в
     * файловой системе. Метод может принимать значение null.
     * @param imagePath путь изображения данного продукта.
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Возвращает уникальный идентификатор для данного продукта.
     * @return уникальный идентификатор для данного продукта.
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает пользователя, с которым связан данный продукт.
     * @return уникальный идентификатор пользователя, с которым связан данный продукт.
     */
    public User getUser() {
        return user;
    }

    /**
     * Возвращает контекстные данные({@link ProductContext}) этого продукта.
     * @return контекстные данные этого продукта.
     */
    public ProductContext getContext() {
        return context;
    }

    /**
     * Возвращает кол-во данного продукта имеющеей с распоряжении у пользователя.
     * @return кол-во данного продукта имеющеей с распоряжении у пользователя.
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Возвращает описание к данноыму продукту.
     * @return описание данного продукта.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает путь к избражению данного продукта.
     * @return путь к избражению данного продукта.
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Выполняет сравнение на равенство двух продуктов с учетом всех их данных.
     * @param other продукт с которым выполняется сравнение.
     * @return true - если все поля двух продуктов соответственно равны, false - в противном случае.
     */
    public boolean equalsFullState(Product other) {
        if(this == other) return true;
        if(getClass() != other.getClass()) return false;
        return id.equals(other.id) &&
                user.equals(other.user) &&
                quantity.equals(other.quantity) &&
                context.equals(other.context) &&
                Objects.equals(description, other.description) &&
                Objects.equals(imagePath, other.imagePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", user=" + user +
                ", context=" + context +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }


    /**
     * Предоставляет удобный способ инициализации агрегата Product (включая входящие в него value-object).
     * Соответствие задаваемых значений бизнес правилам проверяется только в момент вызова метода build().
     * Объекты данного класса не устанавливают никаких значений по умолчанию для конструирования объектов
     * Product.
     */
    public static class Builder implements AbstractBuilder<Product> {

        private UUID id;
        private User user;
        private final ProductContext.Builder contextBuilder;
        private BigDecimal quantity;
        private String description;
        private String imagePath;
        private AppConfigData appConfigData;

        public Builder() {
            contextBuilder = new ProductContext.Builder();
        }

        public Builder generateId() {
            this.id = UUID.randomUUID();
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setCategory(String category) {
            contextBuilder.setCategory(category);
            return this;
        }

        public Builder setShop(String shop) {
            contextBuilder.setShop(shop);
            return this;
        }

        public Builder setVariety(String variety) {
            contextBuilder.setVariety(variety);
            return this;
        }

        public Builder setManufacturer(String manufacturer) {
            contextBuilder.setManufacturer(manufacturer);
            return this;
        }

        public Builder setUnit(String unit) {
            contextBuilder.setUnit(unit);
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            contextBuilder.setPrice(price);
            return this;
        }

        public Builder setPackingSize(BigDecimal packingSize) {
            contextBuilder.setPackingSize(packingSize);
            return this;
        }

        public Builder setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setImagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder setAppConfiguration(AppConfigData appConfigData) {
            this.appConfigData = appConfigData;
            contextBuilder.setAppConfiguration(appConfigData);
            return this;
        }

        public Builder addTag(String tagValue) {
            contextBuilder.addTag(tagValue);
            return this;
        }

        public Product tryBuild() {
            return new Product(
                    id,
                    user,
                    description,
                    quantity,
                    imagePath,
                    contextBuilder,
                    appConfigData
            );
        }

    }

}
