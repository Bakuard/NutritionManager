package com.bakuard.nutritionManager.model.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageableByNumberTest {

    @Test
    @DisplayName("""
            getPageMetadata(totalItems, maxPageSize):
             totalItems is negative
             => exception
            """)
    public void getPageMetadata1() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PageableByNumber.
                        of(10, 0).
                        createPageMetadata(-1, 1)
        );
    }

    @Test
    @DisplayName("""
            getPageMetadata(totalItems, maxPageSize):
             maxPageSize < 1
             => exception
            """)
    public void getPageMetadata2() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PageableByNumber.
                        of(10, 0).
                        createPageMetadata(1, 0)
        );
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, itemIndex):
             itemIndex < 0
             => correct result
            """)
    public void ofIndex1() {
        PageableByNumber actual = PageableByNumber.ofIndex(1, -1);

        PageableByNumber expected = PageableByNumber.of(1, 0);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, itemIndex):
             itemIndex = 0
             => correct result
            """)
    public void ofIndex2() {
        PageableByNumber actual = PageableByNumber.ofIndex(5, 0);

        PageableByNumber expected = PageableByNumber.of(5, 0);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, itemIndex):
             itemIndex > 0
             => correct result
            """)
    public void ofIndex3() {
        PageableByNumber actual = PageableByNumber.ofIndex(10, 15);

        PageableByNumber expected = PageableByNumber.of(10, 1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, itemIndex):
             expectedPageSize < 1
             => correct result
            """)
    public void ofIndex4() {
        PageableByNumber actual = PageableByNumber.ofIndex(0, 10);

        PageableByNumber expected = PageableByNumber.of(1, 10);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, itemIndex):
             itemIndex is null
             => exception
            """)
    public void ofIndex5() {
        Assertions.assertThrows(NullPointerException.class,
                () -> PageableByNumber.ofIndex(20, null)
        );
    }

    @Test
    @DisplayName("""
            ofIndex(expectedPageSize, expectedPageNumber):
             expectedPageNumber is null
             => exception
            """)
    public void of1() {
        Assertions.assertThrows(NullPointerException.class,
                () -> PageableByNumber.of(20, null)
        );
    }

}