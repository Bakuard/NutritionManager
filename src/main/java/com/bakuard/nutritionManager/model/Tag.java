package com.bakuard.nutritionManager.model;

import com.bakuard.nutritionManager.validation.ValidateException;
import com.bakuard.nutritionManager.validation.Validator;

import static com.bakuard.nutritionManager.validation.Rule.notBlank;
import static com.bakuard.nutritionManager.validation.Rule.notNull;


/**
 * Теги используются для уточнения пользвателем данных о продуктах, блюдах и меню, а также для последующей
 * фильтрации по заданным тегам.<br/>
 * Объекты данного класса являются не изменяемыми.
 */
public class Tag implements Comparable<Tag> {

    private final String value;

    /**
     * Создает тег с указанным значением. Все начальные и конечные пробельные символы тега будут удалены.
     * @param value значение тега.
     * @throws ValidateException в следующих случаях:<br/>
     *         1. если указанное значение равняется null<br/>
     *         2. если указанное значение не содержит ни одного отображаемого символа.
     */
    public Tag(String value) {
        Validator.check("Tag.value", notNull(value).and(() -> notBlank(value)));

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

}
