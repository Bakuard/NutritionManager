package com.bakuard.nutritionManager.dal;

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
     * @return true - если указанный продукт отсутсвовал в БД или отличался от переданного, иначе - false.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если product имеет значение null.<br/>
     *         2. если в БД уже есть другой продукт с таким же контекстом ({@link ProductContext}).
     */
    public boolean save(Product product);

    /**
     * Удаляет из БД продукт идентификатор которого равен productId. Если в БД нет продукта с таким
     * идентификатором - выбрасывает исключение.
     * @param productId идентификатор продукта.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти продукт с таким ID.<br/>
     *         2. если productId равен null.
     */
    public Product remove(UUID productId);

    /**
     * Возвращает продукт по его идентификатору. Если в БД нет продукта с таким идентификатором -
     * выбрасывает исключение.
     * @param productId идентификатор продукта.
     * @return объект Product или null.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти продукт с таким ID.<br/>
     *         2. если productId равен null.
     */
    public Product getById(UUID productId);

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
    public Page<String> getVarieties(Criteria criteria);

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
     * Возвращает кол-во всех продуктов удовлетворяющих ограничениям criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие продукты подсчитывать.
     * @return кол-во всех продуктов удовлетворяющих ограничениям criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getProductsNumber(Criteria criteria);

    /**
     * Возвращает кол-во всех тегов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие теги подсчитывать.
     * @return кол-во всех тегов удовлетворяющих ограничению criteria.
     * @throws ValidateException если criteria является null.
     */
    public int getTagsNumber(Criteria criteria);

    /**
     * Возвращает кол-во магазинов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие магазины подсчитывать.
     * @return выборку из магазинов продуктов.
     * @throws ValidateException если criteria является null.
     */
    public int getShopsNumber(Criteria criteria);

    /**
     * Возвращает кол-во сортов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие сорта подсчитывать.
     * @return выборку из сортов продуктов.
     * @throws ValidateException если criteria является null.
     */
    public int getVarietiesNumber(Criteria criteria);

    /**
     * Возвращает кол-во категорий продуктов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие какие категории подсчитывать.
     * @return выборку из категорий продуктов.
     * @throws ValidateException если criteria является null.
     */
    public int getCategoriesNumber(Criteria criteria);

    /**
     * Возвращает кол-во производителей продуктов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * @param criteria критерии указывающие каких производителей подсчитывать.
     * @return выборку из производителей продуктов.
     * @throws ValidateException если criteria является null.
     */
    public int getManufacturersNumber(Criteria criteria);

    /**
     * Возвращает сумму цен всех продуктов удовлетворяющих ограничению criteria (см. {@link Criteria}).
     * Если нет ни одного продукта удовлетворяющего ограничению criteria - возвращает пустой Optional.
     * @param criteria критерии указывающие какие продукты учитывать
     * @return сумму цен всех продуктов удовлетворяющих ограничению criteria
     * @throws ValidateException если criteria является null.
     */
    public Optional<BigDecimal> getProductsSum(Criteria criteria);

}
