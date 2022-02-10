package com.bakuard.nutritionManager.model.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageableTest {

    @Test
    @DisplayName("""
            getPageMetadata(BigInteger totalItems):
             totalItems is negative
             => exception
            """)
    public void getPageMetadata1() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> Pageable.
                        of(10, 0).
                        createPageMetadata(-1)
        );
    }

    @Test
    @DisplayName("""
            ofIndex(expectedMaxPageSize, productIndex):
             productIndex < 0
             => correct result
            """)
    public void ofIndex1() {
        Pageable actual = Pageable.ofIndex(1, -1);

        Pageable expected = Pageable.of(1, 0);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedMaxPageSize, productIndex):
             productIndex = 0
             => correct result
            """)
    public void ofIndex2() {
        Pageable actual = Pageable.ofIndex(5, 0);

        Pageable expected = Pageable.of(5, 0);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedMaxPageSize, productIndex):
             productIndex > 0
             => correct result
            """)
    public void ofIndex3() {
        Pageable actual = Pageable.ofIndex(10, 15);

        Pageable expected = Pageable.of(10, 1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedMaxPageSize, productIndex):
             expectedMaxPageSize < 1
             => correct result
            """)
    public void ofIndex4() {
        Pageable actual = Pageable.ofIndex(0, 10);

        Pageable expected = Pageable.of(1, 10);
        Assertions.assertEquals(expected, actual);
    }

}