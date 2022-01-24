package com.bakuard.nutritionManager.model.filters;

import com.bakuard.nutritionManager.model.exceptions.ServiceException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Фильтр хранилища продуктов прденазначен для задания следующих ограничений: какие продукты из имеющихся у
 * пользователя в наличии и в каком кол-ве могут использоваться. Объекты даного класса являются неизменяемыми.
 */
public final class ProductStoreFilter {

    /**
     * Возвразает фильтр указывающий, что можно использовать любые продукты из имеющихся у пользователя
     * в наличии в любом доступном кол-ве.
     * @return фильтр задающий универсальное множество для продуктов имеющихся у пользователя в наличии.
     */
    public static ProductStoreFilter universalSet() {
        return null;
    }

    /**
     * Возвращает фильтр указывающий, что из имеющихся в наличии у пользователя продуктов ничего нельзя
     * использовать.
     * @return фильтр задающий пустое множество для продуктов имющихся у пользователя в наличии.
     */
    public static ProductStoreFilter emptySet() {
        return null;
    }


    private final Map<UUID, BigDecimal> products;
    private final boolean isEmptySet;

    /**
     * Созздает фильтр задающий универсальное множество для всех продуктов имеющихся у пользователя в
     * наличии.
     */
    public ProductStoreFilter() {
        products = Map.of();
        isEmptySet = false;
    }

    /**
     * Создает и возвращает новый объект фильтра отличающийся от текущего добавлением следующего ограничения:
     * из имеющихся в наличии у пользователя продуктов разрешенно использовать продукт с данным идентификатором и
     * в данном кол-ве. Остальные ограничения текущего фильтра остаются в силе и для нового. Если указываемый
     * продукт присутствует в текущем фильтре, то он остается в новом фильтре, но его кол-во будет равно
     * quantity.
     * @param productId идентификатор продукта.
     * @param quantity кол-во в котором разрещенно использовать указанный продукт.
     * @return новый объект, если указаный продукт отсутствует в текущем фильтре или указанное кол-во
     *         отличается от такового в текущем фильтре, иначе - этот же объект.
     * @throws ServiceException если productId или quantity имеют значение null.
     */
    public ProductStoreFilter putProduct(UUID productId, BigDecimal quantity) {
        return null;
    }

    /**
     * Возворащает кол-во в котором разрещенно использовать продукт с указанным идентификатором.
     * @param productId идентификатор искомого продукта.
     * @return кол-во в котором разрещенно использовать продукт с указанным идентификатором.
     */
    public BigDecimal getQuantity(UUID productId) {
        return null;
    }

    /**
     * Проверяет - задает ли данный фильтр универсальное множество для всех продуктов имеющихся в наличии
     * у пользователя.
     * @return true - если данный фильтр задает универсальное множество, иначе - false.
     */
    public boolean isUniversalSet() {
        return false;
    }

    /**
     * Проверяет - задает ли данный фильтр пустое множество для всех продуктов имеющихся в наличии у пользователя.
     * @return true - если данный фильтр задает пустое множество, иначе - false.
     */
    public boolean isEmptySet() {
        return false;
    }

    /**
     * Возвращает ассоциативный массив пар продукт-количество. Если данный фильтр представляет пустое или
     * универсальное множество - возвращает пустой ассоциативный массив.
     * @return ассоциативный массив пар продукт-количество.
     */
    public Map<UUID, BigDecimal> getProducts() {
        return products;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductStoreFilter that = (ProductStoreFilter) o;
        return isEmptySet == that.isEmptySet &&
                products.equals(that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(products, isEmptySet);
    }

    @Override
    public String toString() {
        return "ProductStoreFilter{" +
                "products=" + products +
                ", isEmptySet=" + isEmptySet +
                '}';
    }

}
