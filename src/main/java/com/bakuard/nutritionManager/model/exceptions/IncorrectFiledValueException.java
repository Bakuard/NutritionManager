package com.bakuard.nutritionManager.model.exceptions;

/**
 * Базовый тип исключения связанный со всеми
 */
public class IncorrectFiledValueException extends AbstractDomainException {

    private final Class<?> type;
    private final String fieldName;
    private Object rejectedValue;

    /**
     * Создает объект исключения с указанием типа объекта и поля/параметра для которого оно было вызвано.
     * @param type тип объкта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     */
    public IncorrectFiledValueException(Class<?> type,
                                        String fieldName) {
        this.type = type;
        this.fieldName = fieldName;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. сообщение описывающее данное исключение.<br/>
     * 2. тип объекта для которого было вызвано исключение.<br/>
     * 3. наименование поля/параметра для которого было вызвано это исключение.
     * @param message сообщение предоставляющее дополнительную информацию об этом исключении.
     * @param type тип объкта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которогобыло вызвано это исключение.
     */
    public IncorrectFiledValueException(String message,
                                        Class<?> type,
                                        String fieldName) {
        super(message);
        this.type = type;
        this.fieldName = fieldName;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. сообщение описывающее данное исключение.<br/>
     * 2. другое исключение являющееся причинной данного.<br/>
     * 3. тип объекта для которого было вызвано исключение.<br/>
     * 4. наименование поля/параметра для которого было вызвано это исключение.
     * @param message сообщение предоставляющее дополнительную информацию об этом исключении.
     * @param cause другое исключение являющееся причинной данного.
     * @param type тип объкта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которогобыло вызвано это исключение.
     */
    public IncorrectFiledValueException(String message,
                                        Throwable cause,
                                        Class<?> type,
                                        String fieldName) {
        super(message, cause);
        this.type = type;
        this.fieldName = fieldName;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. другое исключение являющееся причинной данного.<br/>
     * 2. тип объекта для которого было вызвано исключение.<br/>
     * 3. наименование поля/параметра для которого было вызвано это исключение.
     * @param cause другое исключение являющееся причинной данного.
     * @param type тип объкта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     */
    public IncorrectFiledValueException(Throwable cause,
                                        Class<?> type,
                                        String fieldName) {
        super(cause);
        this.type = type;
        this.fieldName = fieldName;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. тип объекта для которого было вызвано исключение.<br/>
     * 2. наименование поля/параметра для которого было вызвано это исключение.<br/>
     * 3. значение задаваемое полю/параметру ставшее причинной исключения.
     * @param type тип объекта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     * @param rejectedValue значение задаваемое полю/параметру ставшее причинной исключения.
     */
    public IncorrectFiledValueException(Class<?> type,
                                        String fieldName,
                                        Object rejectedValue) {
        this.type = type;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. сообщение описывающее данное исключение.<br/>
     * 2. тип объекта для которого было вызвано исключение.<br/>
     * 3. наименование поля/параметра для которого было вызвано это исключение.<br/>
     * 4. значение задаваемое полю/параметру ставшее причинной исключения.
     * @param message сообщение описывающее данное исключение.
     * @param type тип объекта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     * @param rejectedValue значение задаваемое полю/параметру ставшее причинной исключения.
     */
    public IncorrectFiledValueException(String message,
                                        Class<?> type,
                                        String fieldName,
                                        Object rejectedValue) {
        super(message);
        this.type = type;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. сообщение описывающее данное исключение.<br/>
     * 2. другое исключение являющееся причинной данного.<br/>
     * 3. тип объекта для которого было вызвано исключение.<br/>
     * 4. наименование поля/параметра для которого было вызвано это исключение.<br/>
     * 5. значение задаваемое полю/параметру ставшее причинной исключения.
     * @param message сообщение описывающее данное исключение.
     * @param cause другое исключение являющееся причинной данного.
     * @param type тип объекта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     * @param rejectedValue значение задаваемое полю/параметру ставшее причинной исключени.
     */
    public IncorrectFiledValueException(String message,
                                        Throwable cause,
                                        Class<?> type,
                                        String fieldName,
                                        Object rejectedValue) {
        super(message, cause);
        this.type = type;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Создает объект исключения содержащего следующие данные:<br/>
     * 1. другое исключение являющееся причинной данного.<br/>
     * 2. тип объекта для которого было вызвано исключение.<br/>
     * 3. наименование поля/параметра для которого было вызвано это исключение.<br/>
     * 4. значение задаваемое полю/параметру ставшее причинной исключения.
     * @param cause другое исключение являющееся причинной данного.
     * @param type тип объекта для которого было вызвано исключение.
     * @param fieldName наименование поля/параметра для которого было вызвано это исключение.
     * @param rejectedValue значение задаваемое полю/параметру ставшее причинной исключе
     */
    public IncorrectFiledValueException(Throwable cause,
                                        Class<?> type,
                                        String fieldName,
                                        Object rejectedValue) {
        super(cause);
        this.type = type;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Возвращает тип объекта для которого было вызвано данное исключение.
     * @return тип объекта для которого было вызвано данное исключение.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Возвращает наименование поля/параметра для которого было вызвано это исключение.
     * @return наименование поля/параметра для которого было вызвано это исключение.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Возвращает значение задаваемое полю/параметру ставшее причинной исключения.
     * @return значение задаваемое полю/параметру ставшее причинной исключения.
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }

    /**
     * Возвращаемое значение используется для идентификации объекта исключения при формировании очета об ошибке
     * пользователю.<br/>
     * IncorrectFiledValueException переопределяет этот метод используя для идентификации объекта исключения
     * следующие данные: тип исключения, тип объекта и наименование поля/параметра. Данные представляются в виде
     * одной строки в формате ТипИскючения.ТипОбъекта.полеИлиПараметр
     * @return контекстные данные исключения.
     */
    public String getMessageKey() {
        return getClass().getSimpleName() + "." + type.getSimpleName() + "." + fieldName;
    }

}
