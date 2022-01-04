package com.bakuard.nutritionManager.model.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

class ValidateExceptionTest {

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any field exceptions,
             validate exception doesn't contain nested validation exceptions
             => iterator#hasNext() return false
            """)
    public void iterator1() {
        ProductValidateException validateException = new ProductValidateException();

        Iterator<IncorrectFiledValueException> iterator = validateException.iterator();

        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any field exceptions
             => iterator#hasNext() return false
            """)
    public void iterator2() {
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(new ProductContextValidateException());
        validateException.addReason(new ProductContextValidateException());
        validateException.addReason(new ProductContextValidateException());

        Iterator<IncorrectFiledValueException> iterator = validateException.iterator();

        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterator#hasNext() return true
            """)
    public void iterator3() {
        ProductValidateException validateException = new ProductValidateException();
        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(new BlankValueException(getClass(), "some field"));
        nestedValidateException.addReason(new BlankValueException(getClass(), "some field"));
        nestedValidateException.addReason(new BlankValueException(getClass(), "some field"));
        validateException.addReason(nestedValidateException);
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(new BlankValueException(getClass(), "some field"));
        nestedValidateException3.addReason(new BlankValueException(getClass(), "some field"));
        nestedValidateException3.addReason(new BlankValueException(getClass(), "some field"));
        nestedValidateException2.addReason(nestedValidateException3);
        validateException.addReason(nestedValidateException2);

        Iterator<IncorrectFiledValueException> iterator = validateException.iterator();

        Assertions.assertTrue(iterator.hasNext());
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterator#next() return all field exceptions
            """)
    public void iterator4() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        Assertions.assertIterableEquals(expected, validateException);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any field exceptions
             => iterator#next() return all field exceptions
            """)
    public void iterator5() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);
        expected.forEach(validateException::addReason);

        Assertions.assertIterableEquals(expected, validateException);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterator#next() return all field exceptions
            """)
    public void iterator6() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        Assertions.assertIterableEquals(expected, validateException);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => number iteration = number of all field exceptions
            """)
    public void iterator7() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        Iterator<IncorrectFiledValueException> iterator = validateException.iterator();
        int number = 0;
        while(iterator.hasNext()) {
            iterator.next();
            ++number;
        }

        Assertions.assertEquals(expected.size(), number);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any field exceptions,
             validate exception doesn't contain nested validation exceptions
             => number iterations = 0
            """)
    public void forEach1() {
        ProductValidateException validateException = new ProductValidateException();

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertEquals(0, actual.size());
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any field exceptions
             => number iterations = 0
            """)
    public void forEach2() {
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(new ProductContextValidateException());
        validateException.addReason(new ProductContextValidateException());
        validateException.addReason(new ProductContextValidateException());;

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertEquals(0, actual.size());
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterate all fields
            """)
    public void forEach3() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any field exceptions
             => iterate all fields
            """)
    public void forEach4() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);
        expected.forEach(validateException::addReason);

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterate all fields
            """)
    public void forEach5() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains field exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some field exceptions
             => iterate all fields
            """)
    public void forEach6() {
        List<IncorrectFiledValueException> expected = List.of(
                new BlankValueException(getClass(), "some field"),
                new MissingValueException(getClass(), "some field"),
                new OutOfRangeException(getClass(), "some field"),
                new DuplicateTagException(getClass(), "some field"),
                new NegativeValueException(getClass(), "some field"),
                new NotPositiveValueException(getClass(), "some field")
        );

        ProductContextValidateException nestedValidateException = new ProductContextValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ProductContextValidateException nestedValidateException3 = new ProductContextValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ProductContextValidateException nestedValidateException2 = new ProductContextValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ProductValidateException validateException = new ProductValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<IncorrectFiledValueException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

}