package com.bakuard.nutritionManager.validation;

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

        Iterator<Result> iterator = validateException.iterator();

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
        validateException.addReason(new ValidateException(getClass(), "someMethod"));
        validateException.addReason(new ValidateException(getClass(), "someMethod"));
        validateException.addReason(new ValidateException(getClass(), "someMethod"));

        Iterator<Result> iterator = validateException.iterator();

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
        nestedValidateException.addReason("some field", Constraint.NOT_BLANK);
        nestedValidateException.addReason("some field", Constraint.NOT_BLANK);
        nestedValidateException.addReason("some field", Constraint.NOT_BLANK);
        validateException.addReason(nestedValidateException);
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason("some field", Constraint.NOT_BLANK);
        nestedValidateException3.addReason("some field", Constraint.NOT_BLANK);
        nestedValidateException3.addReason("some field", Constraint.NOT_BLANK);
        nestedValidateException2.addReason(nestedValidateException3);
        validateException.addReason(nestedValidateException2);

        Iterator<Result> iterator = validateException.iterator();

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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
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
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        Iterator<Result> iterator = validateException.iterator();
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

        List<Result> actual = new ArrayList<>();
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
        validateException.addReason(new ValidateException(getClass(), "someMethod"));
        validateException.addReason(new ValidateException(getClass(), "someMethod"));
        validateException.addReason(new ValidateException(getClass(), "someMethod"));

        List<Result> actual = new ArrayList<>();
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
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
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<Result> actual = new ArrayList<>();
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);
        expected.forEach(validateException::addReason);

        List<Result> actual = new ArrayList<>();
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<Result> actual = new ArrayList<>();
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
        List<Result> expected = List.of(
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_BLANK, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NULL, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.RANGE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_CONTAINS_DUPLICATE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.NOT_NEGATIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null),
                new Result(getClass(), "someMethod", "some field",
                        Constraint.POSITIVE_VALUE, Result.State.FAIL, null,
                        "key", null, null)
        );

        ValidateException nestedValidateException = new ValidateException(getClass(), "someMethod");
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException(getClass(), "someMethod");
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException(getClass(), "someMethod");
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException(getClass(), "someMethod");
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<Result> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

}