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
        ProductValidateException validateException = new ProductValidateException("Fail to create product.");

        validateException.addReason(checkId(id));
        validateException.addReason(checkUser(user));
        validateException.addReason(checkQuantity(quantity));
        validateException.addReason(checkAppConfig(config));

        try {
            this.context = contextBuilder.tryBuild();
        } catch(ValidateException e) {
            validateException.addReason(e);
        }

        if(validateException.violatedConstraints()) throw validateException;

        this.id = id;
        this.user = user;
        this.quantity = quantity.setScale(config.getNumberScale(), config.getRoundingMode());
        this.description = description;
        this.imagePath = imagePath;
    }

    /**
     * Устанавливает для данного продукта указанные контекстные данные.
     * @param context контекстные данные данного продукта ({@link ProductContext}).
     */
    public void setContext(ProductContext context) {
        tryThrow(checkContext(context));

        this.context = context;
    }

    /**
     * Увеличевает кол-во данного продукта имеющегося в распоряжении у пользователя на указанное значение.
     * @param quantity значение на которое будет увеличенно кол-во данного продукта имеющегося в распряжении у
     *                 пользвателя.
     * @throws NegativeValueException если quantity меньше нуля.
     * @throws NullPointerException если quantity имеет значение null.
     */
    public void addQuantity(BigDecimal quantity) {
        tryThrow(checkQuantity(quantity));

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
     * @throws NegativeValueException если quantity меньше нуля.
     * @throws NullPointerException если quantity имеет значение null.
     */
    public BigDecimal take(BigDecimal quantity) {
        tryThrow(checkQuantity(quantity));

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


    private void tryThrow(RuntimeException e) {
        if(e != null) throw e;
    }

    private IncorrectFiledValueException checkId(UUID id) {
        MissingValueException checkResult = null;
        if(id == null) checkResult = new MissingValueException("Product id can't be null", getClass(), "id");
        return checkResult;
    }

    private IncorrectFiledValueException checkUser(User user) {
        MissingValueException checkResult = null;
        if(user == null) checkResult = new MissingValueException("Product user can't be null", getClass(), "user");
        return checkResult;
    }

    private IncorrectFiledValueException checkQuantity(BigDecimal quantity) {
        IncorrectFiledValueException checkResult = null;

        if(quantity == null) {
            checkResult = new MissingValueException("Product quantity can not be null.", getClass(), "quantity");
        } else if(quantity.signum() < 0) {
            checkResult = new NegativeValueException("Product quantity can't be negative.", getClass(), "quantity");
        }

        return checkResult;
    }

    private IncorrectFiledValueException checkContext(ProductContext context) {
        MissingValueException checkResult = null;
        if(context == null) checkResult = new MissingValueException("Product id can't be null", getClass(), "context");
        return checkResult;
    }

    private IncorrectFiledValueException checkAppConfig(AppConfigData config) {
        IncorrectFiledValueException exception = null;
        if(config == null) {
            exception = new MissingValueException("AppConfiguration for product cant' be null", getClass(), "config");
        }
        return exception;
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
