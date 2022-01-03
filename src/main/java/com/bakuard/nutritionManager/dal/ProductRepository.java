package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.dal.criteria.*;
import com.bakuard.nutritionManager.model.Product;
import com.bakuard.nutritionManager.model.ProductContext;
import com.bakuard.nutritionManager.model.Tag;
import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.filters.Constraint;
import com.bakuard.nutritionManager.model.util.Page;
import com.bakuard.nutritionManager.model.util.Pageable;
import com.bakuard.nutritionManager.model.util.Pair;
import com.bakuard.nutritionManager.model.exceptions.*;

import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
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
     * @throws DataAccessException если возникла ошибка связанная с обращением к БД.
     * @throws MissingValueException если product имеет значение null.
     * @throws ProductAlreadyExistsException если в БД уже есть другой продукт с таким же контекстом
     *                                       ({@link ProductContext}).
     */
    public boolean save(Product product);

    /**
     * Удаляет из БД продукт идентификатор которого равен productId. Если в БД нет продукта с таким
     * идентификатором - выбрасывает исключение.
     * @param productId идентификатор продукта.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     * @throws UnknownProductException если productId имеет значение null или в БД нет продукта с таким ID.
     */
    public Product remove(UUID productId);

    /**
     * Возвращает продукт по его идентификатору. Если в БД нет продукта с таким идентификатором -
     * выбрасывает исключение.
     * @param productId идентификатор продукта.
     * @return объект Product или null.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     * @throws UnknownProductException если не удалось найти продукт с таким идентификатором или productId равен null.
     */
    public Product getById(UUID productId);

    /**
     * Возвращает упорядоченную выборку пар(продукт, кол-во) из множества всех продуктов на учете пользователя и
     * удовлетворяющих ограничениям constraint, где кол-во - это кол-во продукта купленного упаковками размером
     * ({@link ProductContext#getPackingSize()}). Возвращаемое подмножество продуктов удовлетворяющее указанному
     * огрничению определяется параметром pageable. Возвращаемое множество продуктов упорядочеваются по суммарной
     * цене с учетом необходимого кол-ва продукта и размера упаковки. Продукты с одинаковой суммарной ценой
     * упорядочеваюся по идентификатору. Особые случаи:<br/>
     * 1. Если в БД нет ни одного продукта соответствующего указанным ограничениям, то метод вернет пустую
     * выборку.<br/>
     * 2. Возвращаемое подмножество из множества всех продуктов удовлетворяющих заданным ограничениям определяется
     * параметром pageable (а также зависит от сортировки). Подробнее об исключительных ситуациях смотри
     * {@link Pageable}.<br/>
     * 3. Если в БД есть указанный пользователь, но у него нет ни одного продукта - метод вернет пустую выборку.
     * @param pageable содержит данные о номере и размере страницы используемых для пагинации (подробнее см.
     *                 {@link Pageable}).
     * @param user пользователь из продуктов которого составляется выборка.
     * @param necessaryQuantity минимальное необходимое кол-во продукта.
     * @param constraint ограничения на продукты в выборке.
     * @return выборку из продуктов на учете пользователя.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     * @throws MissingValueException если user, constraint, pageable или necessaryQuantity имеют значение null.
     * @throws NotPositiveValueException если necessaryQuantity <= 0
     */
    public Page<Pair<Product, BigDecimal>> getProducts(Pageable pageable,
                                                       User user,
                                                       BigDecimal necessaryQuantity,
                                                       Constraint constraint);

    /**
     * Возвращает упорядоченную выборку продуктов из множества всех продуктов с учетом заданных ограничений
     * в виде criteria (см. {@link ProductCriteria}).
     * @param criteria критерий формирования выборки продуктов.
     * @return выборку продуктов удовлетворяющую ограничениям criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<Product> getProducts(ProductCriteria criteria);

    /**
     * Возвращает выборку тегов удовлетворяющую ограничению criteria упорядоченную по значению({@link Tag#getValue()})
     * в порядке возрастания (см. {@link ProductFieldCriteria}).
     * @param criteria критерий формирования выборки тегов.
     * @return выборку тегов удовлетворяющую ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<Tag> getTags(ProductFieldCriteria criteria);

    /**
     * Возвращает выборку наименований магазинов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link ProductFieldCriteria}).
     * @param criteria критерий формирования выборки магазинов продуктов.
     * @return выборку из магазинов продуктов удовлетворяющую ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<String> getShops(ProductFieldCriteria criteria);

    /**
     * Возвращает выборку наименований сортов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link ProductFieldCriteria}).
     * @param criteria критерий формирования выборки сортов продуктов.
     * @return выборку из сортов продуктов удовлетворяющую ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<String> getVarieties(ProductFieldCriteria criteria);

    /**
     * Возвращает выборку категорий продуктов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link ProductCategoryCriteria}).
     * @param criteria критерий формирования выборки категорий продуктов.
     * @return выборку из категорий продуктов удовлетворяющую ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<String> getCategories(ProductCategoryCriteria criteria);

    /**
     * Возвращает выборку производителей продуктов удовлетворяющую ограничению criteria упорядоченную в порядке
     * возрастания (см. {@link ProductFieldCriteria}).
     * @param criteria критерий формирования выборки производителей продуктов.
     * @return выборку из производителей продуктов удовлетворяющую ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public Page<String> getManufacturers(ProductFieldCriteria criteria);

    /**
     * Возвращает кол-во всех продуктов удовлетворяющих ограничениям criteria (см. {@link ProductsNumberCriteria}).
     * @param criteria критерии указывающие какие продукты подсчитывать.
     * @return кол-во всех продуктов удовлетворяющих ограничениям criteria.
     * @throws MissingValueException если criteria является null.
     */
    public int getProductsNumber(ProductsNumberCriteria criteria);

    /**
     * Возвращает кол-во всех тегов удовлетворяющих ограничению criteria (см. {@link ProductFieldNumberCriteria}).
     * @param criteria критерии указывающие какие теги подсчитывать.
     * @return кол-во всех тегов удовлетворяющих ограничению criteria.
     * @throws MissingValueException если criteria является null.
     */
    public int getTagsNumber(ProductFieldNumberCriteria criteria);

    /**
     * Возвращает кол-во магазинов удовлетворяющих ограничению criteria (см. {@link ProductFieldNumberCriteria}).
     * @param criteria критерии указывающие какие магазины подсчитывать.
     * @return выборку из магазинов продуктов.
     * @throws MissingValueException если criteria является null.
     */
    public int getShopsNumber(ProductFieldNumberCriteria criteria);

    /**
     * Возвращает кол-во сортов удовлетворяющих ограничению criteria (см. {@link ProductFieldNumberCriteria}).
     * @param criteria критерии указывающие какие сорта подсчитывать.
     * @return выборку из сортов продуктов.
     * @throws MissingValueException если criteria является null.
     */
    public int getVarietiesNumber(ProductFieldNumberCriteria criteria);

    /**
     * Возвращает кол-во категорий продуктов удовлетворяющих ограничению criteria (см. {@link ProductCategoryNumberCriteria}).
     * @param criteria критерии указывающие какие категории подсчитывать.
     * @return выборку из категорий продуктов.
     * @throws MissingValueException если criteria является null.
     */
    public int getCategoriesNumber(ProductCategoryNumberCriteria criteria);

    /**
     * Возвращает кол-во производителей продуктов удовлетворяющих ограничению criteria (см. {@link ProductFieldNumberCriteria}).
     * @param criteria критерии указывающие каких производителей подсчитывать.
     * @return выборку из производителей продуктов.
     * @throws MissingValueException если criteria является null.
     */
    public int getManufacturersNumber(ProductFieldNumberCriteria criteria);

}
