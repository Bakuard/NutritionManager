package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.model.exceptions.BlankValueException;
import com.bakuard.nutritionManager.model.exceptions.MissingValueException;

/**
 * Теги используются для уточнения пользвателем данных о продуктах, блюдах и меню, а также для последующей
 * фильтрации по заданным тегам.<br/>
 * Объекты данного класса являются не изменяемыми.
 */
public final class Tag implements Comparable<Tag> {

    private final String value;

    /**
     * Создает тег с указанным значением. Все начальные и конечные пробельные символы тега будут удалены.
     * @param value значение тега.
     * @throws MissingValueException если value являются null.
     * @throws BlankValueException если value не содержит ни одного отображаемого символа.
     */
    public Tag(String value) {
        tryThrow(checkValue(value));

        this.value = value.trim();
    }

    /**
     * Возвращает значение тега.
     * @return значение тега.
     */
    public String getValue() {
        return value;
    }

    /**
     * Проводит упорядочевающее сравнение тегов по значению.
     * @param o тег с которым проводится сравнение.
     * @return отрицательное число - если текущий тег меньше указанного,
     *         0 - если теги равны,
     *         положительное число - если текущий тег больше указанного.
     */
    @Override
    public int compareTo(Tag o) {
        return value.compareTo(o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag that = (Tag) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "ProductTag{" +
                "value='" + value + '\'' +
                '}';
    }


    private void tryThrow(RuntimeException e) {
        if(e != null) throw e;
    }

    private RuntimeException checkValue(String value) {
        RuntimeException exception = null;

        if(value == null) {
            exception = new MissingValueException("Tag value cant' be null", getClass(), "tagValue");
        } else if(value.isBlank()) {
            exception = new BlankValueException("Tag value can not be blank", getClass(), "tagValue");
        }

        return exception;
    }

}
