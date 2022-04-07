package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.validation.ValidateException;

import java.util.UUID;

public interface UserRepository {

    /**
     * Сохраняет данные указанного пользователя в БД. Если в БД нет пользователя с таким идентификатором, то добавляет
     * его. Если в БД есть пользователь с таким идентификатором - то обновляет его. В обоих случаях проверяется - есть
     * ли в БД пользователь с таким именем, паролем или почтой и если да - генерирует исключение.
     * @param user сохраняемый пользователь.
     * @return true - если указанный пользователь отсутсвовал в БД или отличался от переданного, иначе - false.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если user имеет значение null.<br/>
     *         2. если пользователь с таким именем уже есть в БД.
     */
    public boolean save(User user);

    /**
     * Возвращает пользователя по его ID. Если пользователя с таким ID не существует - выбрасывет исключение.
     * @param userId уникальный идентификатор пользователя.
     * @return пользователя по его ID.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти пользователя с таким ID.<br/>
     *         2. если userId равен null.
     */
    public User tryGetById(UUID userId);

    /**
     * Возвращает пользователя по его имени. Если пользователя с таким именем не сущестует  - выбрасывает исключение.
     * @param name уникальное имя пользователя.
     * @return пользователя по его имени.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти пользователя с таким name.<br/>
     *         2. если name равен null.
     */
    public User tryGetByName(String name);

    /**
     * Возвращает пользователя по его почте. Если пользователя с такой почтой не сущестует  - выбрасывает исключение.
     * @param email уникальная почта пользователя.
     * @return пользователя по его почте.
     * @throws ValidateException если верно одно из следующих условий:<br/>
     *         1. если не удалось найти пользователя с таким email.<br/>
     *         2. если email равен null.
     */
    public User tryGetByEmail(String email);

}
