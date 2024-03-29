package com.bakuard.nutritionManager.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.bakuard.nutritionManager.validation.Rule.failure;

class ValidateExceptionTest {

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any rule exceptions,
             validate exception doesn't contain nested validation exceptions
             => return empty Iterator
            """)
    public void iterator1() {
        ValidateException validateException = new ValidateException();

        Iterator<RuleException> iterator = validateException.iterator();

        Assertions.assertThat(iterator).toIterable().isEmpty();
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any rule exceptions
             => return empty Iterator
            """)
    public void iterator2() {
        ValidateException validateException = new ValidateException().
                addReason(new ValidateException()).
                addReason(new ValidateException()).
                addReason(new ValidateException());

        Iterator<RuleException> iterator = validateException.iterator();

        Assertions.assertThat(iterator).toIterable().isEmpty();
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterator#hasNext() return true
            """)
    public void iterator3() {
        ValidateException validateException = new ValidateException().
                addReason(
                        new ValidateException().
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK))).
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK))).
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK)))
                ).
                addReason(
                        new ValidateException().
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK))).
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK))).
                                addReason(Rule.of("someRule", failure(Constraint.NOT_BLANK)))
                );

        Iterator<RuleException> iterator = validateException.iterator();

        Assertions.assertThat(iterator).hasNext();
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception doesn't contain any rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterator#next() return all rule exceptions
            """)
    public void iterator4() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        Assertions.assertThat((Iterable<RuleException>) validateException).
                containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any rule exceptions
             => iterator#next() return all rule exceptions
            """)
    public void iterator5() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(
                        new ValidateException()
                ).
                addReason(
                        new ValidateException().
                                addReason(new ValidateException())
                ).
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(expected.get(2)).
                addReason(expected.get(3)).
                addReason(expected.get(4)).
                addReason(expected.get(5));

        Assertions.assertThat((Iterable<RuleException>) validateException).
                containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterator#next() return all rule exceptions
            """)
    public void iterator6() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        Assertions.assertThat((Iterable<RuleException>) validateException).
                containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => number iteration = number of all rule exceptions
            """)
    public void iterator7() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        Iterator<RuleException> iterator = validateException.iterator();
        int number = 0;
        while(iterator.hasNext()) {
            iterator.next();
            ++number;
        }

        Assertions.assertThat(number).isEqualTo(expected.size());
    }

    @Test
    @DisplayName("""
            iterator():
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions,
             nested rule exceptions contains some validate exceptions
             => iterate all fields
            """)
    public void iterator8() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check(),
                Rule.of("rule7", failure(Constraint.IS_TRUE)).check(),
                Rule.of("rule8", failure(Constraint.ANY_MATCH)).check()
        );

        RuleException ruleException = expected.get(6);
        ruleException.addSuppressed(
                new ValidateException().
                        addReason(expected.get(7))
        );
        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                ).
                addReason(ruleException);

        Assertions.assertThat((Iterable<RuleException>) validateException).
                containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any rule exceptions,
             validate exception doesn't contain nested validation exceptions
             => number iterations = 0
            """)
    public void forEach1() {
        ValidateException validateException = new ValidateException();

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any rule exceptions
             => number iterations = 0
            """)
    public void forEach2() {
        ValidateException validateException = new ValidateException()
                .addReason(new ValidateException())
                .addReason(new ValidateException())
                .addReason(new ValidateException());

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception doesn't contain any rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterate all fields
            """)
    public void forEach3() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions don't contain any rule exceptions
             => iterate all fields
            """)
    public void forEach4() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterate all fields
            """)
    public void forEach5() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions
             => iterate all fields
            """)
    public void forEach6() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check()
        );

        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                );

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            forEach(action):
             validate exception contains rule exceptions,
             validate exception contains nested validation exceptions,
             nested validation exceptions contains some rule exceptions,
             nested rule exceptions contains some validate exceptions
             => iterate all fields
            """)
    public void forEach7() {
        List<RuleException> expected = List.of(
                Rule.of("rule1", failure(Constraint.NOT_BLANK)).check(),
                Rule.of("rule2", failure(Constraint.NOT_NULL)).check(),
                Rule.of("rule3", failure(Constraint.RANGE_CLOSED)).check(),
                Rule.of("rule4", failure(Constraint.NOT_CONTAINS_DUPLICATE)).check(),
                Rule.of("rule5", failure(Constraint.NOT_NEGATIVE_VALUE)).check(),
                Rule.of("rule6", failure(Constraint.POSITIVE_VALUE)).check(),
                Rule.of("rule7", failure(Constraint.IS_TRUE)).check(),
                Rule.of("rule8", failure(Constraint.ANY_MATCH)).check()
        );

        RuleException ruleException = expected.get(6);
        ruleException.addSuppressed(
                new ValidateException().
                        addReason(expected.get(7))
        );
        ValidateException validateException = new ValidateException().
                addReason(expected.get(0)).
                addReason(expected.get(1)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(2))
                ).
                addReason(expected.get(3)).
                addReason(
                        new ValidateException().
                                addReason(expected.get(4)).
                                addReason(expected.get(5))
                ).
                addReason(ruleException);

        List<RuleException> actual = new ArrayList<>();
        validateException.forEach(actual::add);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

}