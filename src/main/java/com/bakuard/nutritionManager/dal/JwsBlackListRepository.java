package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.exceptions.ValidateException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Представляет черный список токенов. Черный список используется для реализации быстрого logout.
 */
public interface JwsBlackListRepository {

    /**
     * Добавляет уникальный идентификатор токена и время истечения этого токена в черный список. Если
     * выполняется хотя бы одно из следующих условий, то идентификатор НЕ будет добавлен в черный список:<br/>
     * 1. Время истечения токена предшествует или совпадает с временем вызова этого метода. <br/>
     * 2. Если токен с таким идентификатором уже находится в черном списке на момент вызова этого метода
     * @param tokenId уникальный идентификатор токена
     * @param expired время истечения токена
     * @return true - если идентификатор токена был добавлен в черный список, false - в противном случае.
     * @throws ValidateException если верно одно из следующих утверждений:<br/>
     *         1. tokenId имеет значение null.
     *         2. expired имеет значение null.
     *
     */
    public boolean addToBlackList(UUID tokenId, LocalDateTime expired);

    /**
     * Проверяет - находится ли токен с указанным идентификатором в черном списке.
     * @param tokenId уникальный идентификатор токена
     * @return true - если токен с указанным идентификатором находится в черном списке, false - в противном случае.
     * @throws ValidateException если tokenId имеет значение null.
     */
    public boolean inBlackList(UUID tokenId);

    /**
     * Находит и удаляет из черного списка все токены, время жизни которых превыщает или равно указанной дате.
     * @param deadline время относительно которого принемется решение - какие токены удалить из черного списка.
     * @return кол-во удаленных токенов. Возвращаемое значение может равняться 0.
     * @throws ValidateException если deadline имеет значение null.
     */
    public int removeAllExpired(LocalDateTime deadline);

}
