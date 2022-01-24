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
        ValidateException validateException = new ValidateException(getClass(), "someMethod");

        Iterator<Constraint> iterator = validateException.iterator();

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
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));

        Iterator<Constraint> iterator = validateException.iterator();

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
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        nestedValidateException.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        nestedValidateException.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        validateException.addExcReason(nestedValidateException);
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        nestedValidateException3.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        nestedValidateException3.addReason(new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE));
        nestedValidateException2.addExcReason(nestedValidateException3);
        validateException.addExcReason(nestedValidateException2);

        Iterator<Constraint> iterator = validateException.iterator();

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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);
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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

        Iterator<Constraint> iterator = validateException.iterator();
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
        ValidateException validateException = new ValidateException(getClass(), "someMethod");

        List<Constraint> actual = new ArrayList<>();
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
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));
        validateException.addExcReason(new ValidateException(getClass(), "someMethod"));

        List<Constraint> actual = new ArrayList<>();
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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

        List<Constraint> actual = new ArrayList<>();
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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);
        expected.forEach(validateException::addReason);

        List<Constraint> actual = new ArrayList<>();
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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

        List<Constraint> actual = new ArrayList<>();
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
        List<Constraint> expected = List.of(
                new Constraint(getClass(), "some field", ConstraintType.BLANK_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.MISSING_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.OUT_OF_RANGE),
                new Constraint(getClass(), "some field", ConstraintType.DUPLICATE_TAG),
                new Constraint(getClass(), "some field", ConstraintType.NEGATIVE_VALUE),
                new Constraint(getClass(), "some field", ConstraintType.NOT_POSITIVE_VALUE)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addExcReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addExcReason(nestedValidateException);
        validateException.addExcReason(nestedValidateException2);

        List<Constraint> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

}