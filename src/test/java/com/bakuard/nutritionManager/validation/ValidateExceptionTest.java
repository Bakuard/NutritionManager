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
        ValidateException validateException = new ValidateException();

        Iterator<RuleException> iterator = validateException.iterator();

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
        ValidateException validateException = new ValidateException();
        validateException.addReason(new ValidateException());
        validateException.addReason(new ValidateException());
        validateException.addReason(new ValidateException());

        Iterator<RuleException> iterator = validateException.iterator();

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
        ValidateException validateException = new ValidateException();
        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        nestedValidateException.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        nestedValidateException.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        validateException.addReason(nestedValidateException);
        ValidateException nestedValidateException2 = new ValidateException();
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        nestedValidateException3.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        nestedValidateException3.addReason(Rule.of("someRule").failure(Constraint.NOT_BLANK).check());
        nestedValidateException2.addReason(nestedValidateException3);
        validateException.addReason(nestedValidateException2);

        Iterator<RuleException> iterator = validateException.iterator();

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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        ValidateException nestedValidateException3 = new ValidateException();
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        Iterator<RuleException> iterator = validateException.iterator();
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
        ValidateException validateException = new ValidateException();

        List<RuleException> actual = new ArrayList<>();
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
        ValidateException validateException = new ValidateException();
        validateException.addReason(new ValidateException());
        validateException.addReason(new ValidateException());
        validateException.addReason(new ValidateException());

        List<RuleException> actual = new ArrayList<>();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(0));
        nestedValidateException.addReason(expected.get(1));
        nestedValidateException.addReason(expected.get(2));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(3));
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<RuleException> actual = new ArrayList<>();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        ValidateException nestedValidateException3 = new ValidateException();
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);
        expected.forEach(validateException::addReason);

        List<RuleException> actual = new ArrayList<>();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<RuleException> actual = new ArrayList<>();
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
        List<RuleException> expected = List.of(
                Rule.of("rule1").failure(Constraint.NOT_BLANK).check(),
                Rule.of("rule2").failure(Constraint.NOT_NULL).check(),
                Rule.of("rule3").failure(Constraint.RANGE).check(),
                Rule.of("rule4").failure(Constraint.NOT_CONTAINS_DUPLICATE).check(),
                Rule.of("rule5").failure(Constraint.NOT_NEGATIVE_VALUE).check(),
                Rule.of("rule6").failure(Constraint.POSITIVE_VALUE).check()
        );

        ValidateException nestedValidateException = new ValidateException();
        nestedValidateException.addReason(expected.get(2));
        nestedValidateException.addReason(expected.get(3));
        ValidateException nestedValidateException3 = new ValidateException();
        nestedValidateException3.addReason(expected.get(4));
        nestedValidateException3.addReason(expected.get(5));
        ValidateException nestedValidateException2 = new ValidateException();
        nestedValidateException2.addReason(nestedValidateException3);
        ValidateException validateException = new ValidateException();
        validateException.addReason(expected.get(0));
        validateException.addReason(expected.get(1));
        validateException.addReason(nestedValidateException);
        validateException.addReason(nestedValidateException2);

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertTrue(expected.containsAll(actual) && actual.containsAll(expected));
    }

}