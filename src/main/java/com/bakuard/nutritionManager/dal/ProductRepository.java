package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.dal.projection.ProductField;
import com.bakuard.nutritionManager.dal.projection.ProductFields;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductContext;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.validation.ValidateException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для агрегата {@link Product}.
 */
public interface ProductRepository {

    /**
     * Сохраняет данные указанного продукта в БД. Если в БД нет продукта с таким идентификатором, то добавляет его.
     * Если в БД есть продукт с таким идентификатором - то обновляет его. В обоих случаях проверяется - есть ли
     * в БД у пользователя {@link Product#getUser()} продукт содержащий такой же {@link ProductContext} и если
     * ответ положительный - генерирует исключение.
     * @param product сохраняемый продукт.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если product имеет значение null.<br/>
     *         2. если в БД уже есть другой продукт с таким же контекстом ({@link ProductContext}).
     */
    public void save(Product product);

    /**
     * Удаляет из БД продукт, который принадлежит пользователю с идентификатором userId и имеет идентификатор
     * productId. Если у указанного пользователя нет продукта с таким идентификатором - выбрасывает исключение.<br/>
     * (Для однозначного определения удаляемого продукта достаточно только его ID, но необходимо проверять, что
     * пользователь выполняющий удаление действительно является владельцем продукта.)
     * @param productId идентификатор продукта.
     * @param userId идентификатор пользователя, которому принадлежит продукт.
     * @return возвращает удаленный продукт.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если у указанного пользователя нет продукта с таким идентификатором.<br/>
     *         2. если productId равен null.<br/>
     *         3. если userId равен null.
     */
    public Product tryRemove(UUID userId, UUID productId);

    /**
     * Возвращает продукт указанного пользователя по идентификатору этого продукта. Если у указанного
     * пользователя нет продукта с таким идентификатором - возвращает пустой Optional.<br/>
     * (Для однозначного определения возвращаемого продукта достаточно только его ID, но необходимо проверять, что
     * пользователь запрашивающий продукт действительно является его владельцем.)
     * @param productId идентификатор продукта.
     * @param userId идентификатор пользователя, которому принадлежит продукт.
     * @return объект Product.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если productId равен null.<br/>
     *         2. если userId равен null.
     */
    public Optional<Product> getById(UUID userId, UUID productId);

    /**
     * Возвращает продукт указанного пользователя по идентификатору этого продукта. Если у указанного
     * пользователя нет продукта с таким идентификатором - выбрасывает исключение.<br/>
     * (Для однозначного определения возвращаемого продукта достаточно только его ID, но необходимо проверять, что
     * пользователь запрашивающий продукт действительно является его владельцем.)
     * @param productId идентификатор продукта.
     * @param userId идентификатор пользователя, которому принадлежит продукт.
     * @return объект Product.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если у указанного пользователя нет продукта с таким идентификатором.<br/>
     *         2. если productId равен null.<br/>
     *         3. если userId равен null.
     */
    public Product tryGetById(UUID userId, UUID productId);

    /**
     * Возвращает упорядоченную выборку продуктов из множества всех продуктов с учетом заданных ограничений
     * в виде criteria (см. {@link Criteria}).
     * @param criteria критерий формирования выборки продуктов.
     * @return выборку продуктов удовлетворяющую ограничениям criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<Product> getProducts(Criteria criteria);

    /**
     * Возвращает выборку тегов удовлетворяющую ограничению criteria упорядоченную по значению({@link Tag#getValue()})
     * в порядке возрастания (см. {@link Criteria}).
     * @param criteria критерий формирования выборки тегов.
     * @return выборку тегов удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<Tag> getTags(Criteria criteria);

    /**
     * Возвращает выборку наименований магазинов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link Criteria}).
     * @param criteria критерий формирования выборки магазинов продуктов.
     * @return выборку из магазинов продуктов удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getShops(Criteria criteria);

    /**
     * Возвращает выборку наименований сортов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link Criteria}).
     * @param criteria критерий формирования выборки сортов продуктов.
     * @return выборку из сортов продуктов удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getGrades(Criteria criteria);

    /**
     * Возвращает выборку категорий продуктов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link Criteria}).
     * @param criteria критерий формирования выборки категорий продуктов.
     * @return выборку из категорий продуктов удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getCategories(Criteria criteria);

    /**
     * Возвращает выборку производителей продуктов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link Criteria}).
     * @param criteria критерий формирования выборки производителей продуктов.
     * @return выборку из производителей продуктов удовлетворяющую ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public Page<String> getManufacturers(Criteria criteria);

    /**
     * Возвращает выборку из всех значений указанного поля продуктов заданного пользователя, сгруппированную
     * по категориям. Все значения указанного поля будут упорядочены в лексикографическом порядке в
     * пределах каждой категории.
     * @param field искомое поле
     * @param userId идентификатор пользователя, из продуктов которого делается выборка.
     * @return выборку из всех значений заданного поля, сгруппированную по категориям.
     */
    public <T> Page<ProductField<T>> getFieldsGroupingByCategory(ProductFields field, UUID userId);

    /**
     * Возвращает кол-во всех продуктов удовлетворяющих ограничениям criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие продукты подсчитывать.
     * @return кол-во всех продуктов удовлетворяющих ограничениям criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getProductsNumber(Criteria criteria);

    /**
     * Возвращает сумму цен всех продуктов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * Если нет ни одного продукта удовлетворяющего ограничению criteria - возвращает пустой Optional.
     * @param criteria критерии указывающие какие продукты учитывать
     * @return сумму цен всех продуктов удовлетворяющих ограничению criteria
     * @throws ValidateException если criteria является null.
     */
    public Optional<BigDecimal> getProductsSum(Criteria criteria);

}
