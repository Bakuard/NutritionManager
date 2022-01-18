package com.bakuard.nutritionManager.dal;

import com.bakuard.nutritionManager.model.User;
import com.bakuard.nutritionManager.model.exceptions.UnknownUserException;
import com.bakuard.nutritionManager.model.exceptions.UserAlreadyExistsException;

import org.springframework.dao.DataAccessException;

import java.util.UUID;

public interface UserRepository {

    /**
     * Сохраняет данные указанного пользователя в БД. Если в БД нет пользователя с таким идентификатором, то добавляет
     * его. Если в БД есть пользователь с таким идентификатором - то обновляет его. В обоих случаях проверяется - есть
     * ли в БД пользователь с таким именем, паролем или почтой и если да - генерирует исключение.
     * @param user сохраняемый пользователь.
     * @return true - если указанный пользователь отсутсвовал в БД или отличался от переданного, иначе - false.
     * @throws DataAccessException если возникла ошибка связанная с обращением к БД.
     * @throws MissingValueException если user имеет значение null.
     * @throws UserAlreadyExistsException если пользователь с таким именем уже есть в БД.
     */
    public boolean save(User user);

    /**
     * Возвращает пользователя по его ID. Если пользователя с таким ID не существует - выбрасывет исключение.
     * @param userId уникальный идентификатор пользователя.
     * @return пользователя по его ID.
     * @throws UnknownUserException если не удалось найти пользователя с таким ID или userId равен null.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     */
    public User getById(UUID userId);

    /**
     * Возвращает пользователя по его имени. Если пользователя с таким именем не сущестует  - выбрасывает исключение.
     * @param name уникальное имя пользователя.
     * @return пользователя по его имени.
     * @throws UnknownUserException если не удалось найти пользователя с таким именем или name равен null.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     */
    public User getByName(String name);

    /**
     * Возвращает пользователя по его почте. Если пользователя с такой почтой не сущестует  - выбрасывает исключение.
     * @param email уникальная почта пользователя.
     * @return пользователя по его почте.
     * @throws UnknownUserException если не удалось найти пользователя с такой почтой или email равен null.
     * @throws DataAccessException если при выполнении данной операции возникла ошибка при обращении к БД.
     */
    public User getByEmail(String email);

}
