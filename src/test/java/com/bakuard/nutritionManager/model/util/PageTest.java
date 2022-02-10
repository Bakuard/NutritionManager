package com.bakuard.nutritionManager.model.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

class PageTest {

    @Test
    @DisplayName("getTotalPages(): totalItems is 0 => return 0")
    public void getTotalPages1() {
        Page.Info info = Pageable.
                of(10, 0).
                createPageMetadata(0);

        Assertions.assertEquals(BigInteger.ZERO, info.getTotalPages());
    }

    @Test
    @DisplayName("""
                getTotalPages():
                 totalItems isn't 0,
                 totalItems less than expectedMaxPageSize
                 => return 1
                """)
    public void getTotalPages2() {
        Page.Info info = Pageable.
                of(10, 0).
                createPageMetadata(9);

        Assertions.assertEquals(BigInteger.ONE, info.getTotalPages());
    }

    @Test
    @DisplayName("""
                getTotalPages():
                 totalItems isn't 0,
                 totalItems equal expectedMaxPageSize
                 => return 1
                """)
    public void getTotalPages3() {
        Page.Info info = Pageable.
                of(10, 0).
                createPageMetadata(10);

        Assertions.assertEquals(BigInteger.ONE, info.getTotalPages());
    }

    @Test
    @DisplayName("""
            getTotalPages():
             totalItems isn't 0,
             expectedMaxPageSize is min
             => return totalItems
            """)
    public void getTotalPage4() {
        Page.Info info = Pageable.
                of(1, 0).
                createPageMetadata(10);

        Assertions.assertEquals(BigInteger.TEN, info.getTotalPages());
    }

    @Test
    @DisplayName("""
            getTotalPages():
             totalItems isn't 0,
             totalItems greater than expectedMaxPageSize
             => return correct positive value
            """)
    public void getTotalPage5() {
        Page.Info info = Pageable.
                of(10, 0).
                createPageMetadata(101);

        Assertions.assertEquals(BigInteger.valueOf(11), info.getTotalPages());
    }

    @Test
    @DisplayName("""
            getActualNumber():
             expectedPageNumber < 0
             => actualNumber = 0
            """)
    public void getActualNumber1() {
        Page.Info info = Pageable.
                of(10, -1).
                createPageMetadata(1);

        Assertions.assertEquals(BigInteger.ZERO, info.getActualNumber());
    }

    @Test
    @DisplayName("""
            getActualNumber():
             expectedPageNumber > maxPageNumber,
             => actualNumber = maxPageNumber
            """)
    public void getActualNumber2() {
        Page.Info info = Pageable.
                of(10, 1000).
                createPageMetadata(101);

        Assertions.assertEquals(BigInteger.valueOf(10), info.getActualNumber());
    }

    @Test
    @DisplayName("""
            getActualNumber():
             totalItems is 0,
             => actualNumber = 0
            """)
    public void getActualNumber3() {
        Page.Info info = Pageable.
                of(10, 1).
                createPageMetadata(0);

        Assertions.assertEquals(BigInteger.ZERO, info.getActualNumber());
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize = 0
             => actualSize = 1
            """)
    public void getActualSize1() {
        Page.Info info = Pageable.
                of(0, 0).
                createPageMetadata(10);

        Assertions.assertEquals(1, info.getActualSize());
    }

    @Test
    @DisplayName("""
            getActualSize():
             totalItems = 0
             => actualSize = 0
            """)
    public void getActualSize2() {
        Page.Info info = Pageable.
                of(10, 1).
                createPageMetadata(0);

        Assertions.assertEquals(0, info.getActualSize());
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize greater than 200,
             expectedPageSize equal totalItems
             => actualSize = 200
            """)
    public void getActualSize3() {
        Page.Info info = Pageable.
                of(1000, 1).
                createPageMetadata(1000);

        Assertions.assertEquals(200, info.getActualSize());
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize equal 200,
             expectedPageSize less totalItems
             => actualSize = totalItems
            """)
    public void getActualSize4() {
        Page.Info info = Pageable.
                of(1000, 1).
                createPageMetadata(100);

        Assertions.assertEquals(100, info.getActualSize());
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize equal 200,
             expectedPageSize less totalItems,
             expectedPageNumber is last
             => actualSize = totalItems
            """)
    public void getActualSize5() {
        Page.Info info = Pageable.
                of(10, 10).
                createPageMetadata(101);

        Assertions.assertEquals(1, info.getActualSize());
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems is 0
             => offset is 0
            """)
    public void getOffset1() {
        Page.Info info = Pageable.
                of(1, 0).
                createPageMetadata(0);

        Assertions.assertEquals(BigInteger.ZERO, info.getOffset());
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems isn't 0,
             actualNumber is first,
             actualNumber isn't last
             => correct offset
            """)
    public void getOffset2() {
        Page.Info info = Pageable.
                of(10, 0).
                createPageMetadata(100);

        Assertions.assertEquals(BigInteger.ZERO, info.getOffset());
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems isn't 0,
             actualNumber isn't first,
             actualNumber isn't last
             => correct offset
            """)
    public void getOffset3() {
        Page.Info info = Pageable.
                of(10, 5).
                createPageMetadata(100);

        Assertions.assertEquals(BigInteger.valueOf(50), info.getOffset());
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems isn't 0,
             actualNumber isn't first,
             actualNumber is last,
             actualNumber is full
             => correct offset
            """)
    public void getOffset4() {
        Page.Info info = Pageable.
                of(10, 9).
                createPageMetadata(100);

        Assertions.assertEquals(BigInteger.valueOf(90), info.getOffset());
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems isn't 0,
             actualNumber isn't first,
             actualNumber is last,
             actualNumber isn't full
             => correct offset
            """)
    public void getOffset5() {
        Page.Info info = Pageable.
                of(10, 9).
                createPageMetadata(91);

        Assertions.assertEquals(BigInteger.valueOf(90), info.getOffset());
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             globalIndex < 0
             => exception
            """)
    public void get1() {
        Page<Integer> page = Pageable.
                ofIndex(10, 9).
                createPageMetadata(100).
                createPage(createFullList(0, 10));

        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> page.get(-1));
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             item with globalIndex out of page bottom line
             => return null
            """)
    public void get2() {
        Page<Integer> page = Pageable.
                ofIndex(10, 15).
                createPageMetadata(100).
                createPage(createFullList(10, 10));

        Assertions.assertNull(page.get(9));
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             item with globalIndex out of page top line
             => return null
            """)
    public void get3() {
        Page<Integer> page = Pageable.
                ofIndex(10, 15).
                createPageMetadata(100).
                createPage(createFullList(10, 10));

        Assertions.assertNull(page.get(20));
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             item with globalIndex is first page item
             => return first page item
            """)
    public void get4() {
        Page<Integer> page = Pageable.
                ofIndex(10, 15).
                createPageMetadata(100).
                createPage(createFullList(10, 10));

        Assertions.assertEquals(10, page.get(10));
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             item with globalIndex is last page item
             => return last page item
            """)
    public void get5() {
        Page<Integer> page = Pageable.
                ofIndex(10, 15).
                createPageMetadata(100).
                createPage(createFullList(10, 10));

        Assertions.assertEquals(19, page.get(19));
    }

    @Test
    @DisplayName("""
            get(globalIndex):
             item with globalIndex is middle page item
             => return middle page item
            """)
    public void get6() {
        Page<Integer> page = Pageable.
                ofIndex(10, 15).
                createPageMetadata(100).
                createPage(createFullList(10, 10));

        Assertions.assertEquals(15, page.get(15));
    }


    private List<Integer> createFullList(int from, int itemsNumber) {
        return IntStream.range(from, from + itemsNumber).boxed().toList();
    }

}