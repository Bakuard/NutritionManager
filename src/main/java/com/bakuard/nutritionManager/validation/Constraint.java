package com.bakuard.nutritionManager.validation;

public enum Constraint {

    NOT_NULL,
    MUST_BE_NULL,
    NOT_BLANK,
    NONE_MATCH,
    NOT_CONTAINS_NULL,
    NOT_CONTAINS_DUPLICATE,
    NOT_CONTAINS_ITEM,
    CONTAINS_THE_SAME_ITEMS,
    ANY_MATCH,
    IS_EMPTY_COLLECTION,
    NOT_EMPTY_COLLECTION,
    STRING_LENGTH,
    NOT_NEGATIVE_VALUE,
    POSITIVE_VALUE,
    RANGE,
    RANGE_CLOSED,
    MIN,
    MAX,
    EQUAL,
    LESS_THEN,
    GREATER_THEN,
    IS_URL,
    IS_BIG_DECIMAL,
    IS_LONG,
    IS_INTEGER,
    IS_TRUE,
    DOES_NOT_THROW,
    DIFFERENT_SIGNS,
    CORRECT_JWS,
    CORRECT_CREDENTIALS,
    ENTITY_MUST_EXISTS_IN_DB,
    ENTITY_MUST_BE_UNIQUE_IN_DB,
    SUCCESSFUL_MAIL_SENDING,
    SUCCESSFUL_UPLOAD

}
