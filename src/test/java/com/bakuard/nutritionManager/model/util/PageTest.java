package com.bakuard.nutritionManager.model.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

class PageTest {

    @Test
    @DisplayName("getTotalPages(): totalItems is 0 => return 0")
    public void getTotalPages1() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 0).
                createPageMetadata(0, 200);
        
        Assertions.assertThat(metadata.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("""
                getTotalPages():
                 totalItems isn't 0,
                 totalItems less than expectedMaxPageSize
                 => return 1
                """)
    public void getTotalPages2() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 0).
                createPageMetadata(9, 200);

        Assertions.assertThat(metadata.getTotalPages()).isEqualTo(BigInteger.ONE);
    }

    @Test
    @DisplayName("""
                getTotalPages():
                 totalItems isn't 0,
                 totalItems equal expectedMaxPageSize
                 => return 1
                """)
    public void getTotalPages3() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 0).
                createPageMetadata(10, 200);

        Assertions.assertThat(metadata.getTotalPages()).isEqualTo(BigInteger.ONE);
    }

    @Test
    @DisplayName("""
            getTotalPages():
             totalItems isn't 0,
             expectedMaxPageSize is min
             => return totalItems
            """)
    public void getTotalPage4() {
        Page.Metadata metadata = PageableByNumber.
                of(1, 0).
                createPageMetadata(10, 200);

        Assertions.assertThat(metadata.getTotalPages()).isEqualTo(BigInteger.TEN);
    }

    @Test
    @DisplayName("""
            getTotalPages():
             totalItems isn't 0,
             totalItems greater than expectedMaxPageSize
             => return correct positive value
            """)
    public void getTotalPage5() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 0).
                createPageMetadata(101, 200);

        Assertions.assertThat(metadata.getTotalPages()).isEqualTo(BigInteger.valueOf(11));
    }

    @Test
    @DisplayName("""
            getActualNumber():
             expectedPageNumber < 0
             => actualNumber = 0
            """)
    public void getActualNumber1() {
        Page.Metadata metadata = PageableByNumber.
                of(10, -1).
                createPageMetadata(1, 200);

        Assertions.assertThat(metadata.getActualNumber()).isZero();
    }

    @Test
    @DisplayName("""
            getActualNumber():
             expectedPageNumber > maxPageNumber,
             => actualNumber = maxPageNumber
            """)
    public void getActualNumber2() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 1000).
                createPageMetadata(101, 200);

        Assertions.assertThat(metadata.getActualNumber()).isEqualTo(BigInteger.TEN);
    }

    @Test
    @DisplayName("""
            getActualNumber():
             totalItems is 0,
             => actualNumber = 0
            """)
    public void getActualNumber3() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 1).
                createPageMetadata(0, 200);

        Assertions.assertThat(metadata.getActualNumber()).isZero();
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize = 0
             => actualSize = 1
            """)
    public void getActualSize1() {
        Page.Metadata metadata = PageableByNumber.
                of(0, 0).
                createPageMetadata(10, 200);

        Assertions.assertThat(metadata.getActualSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("""
            getActualSize():
             totalItems = 0
             => actualSize = 0
            """)
    public void getActualSize2() {
        Page.Metadata metadata = PageableByNumber.
                of(10, 1).
                createPageMetadata(0, 200);

        Assertions.assertThat(metadata.getActualSize()).isZero();
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize greater than 200,
             expectedPageSize equal totalItems
             => actualSize = 200
            """)
    public void getActualSize3() {
        Page.Metadata metadata = PageableByNumber.
                of(1000, 1).
                createPageMetadata(1000, 200);

        Assertions.assertThat(metadata.getActualSize()).isEqualTo(200);
    }

    @Test
    @DisplayName("""
            getActualSize():
             expectedPageSize equal 200,
             expectedPageSize less totalItems
             => actualSize = totalItems
            """)
    public void getActualSize4() {
        Page.Metadata metadata = PageableByNumber.
                of(1000, 1).
                createPageMetadata(100, 200);

        Assertions.assertThat(metadata.getActualSize()).isEqualTo(100);
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
        Page.Metadata metadata = PageableByNumber.
                of(10, 10).
                createPageMetadata(101, 200);

        Assertions.assertThat(metadata.getActualSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("""
            getOffset():
             totalItems is 0
             => offset is 0
            """)
    public void getOffset1() {
        Page.Metadata metadata = PageableByNumber.
                of(1, 0).
                createPageMetadata(0, 200);

        Assertions.assertThat(metadata.getOffset()).isZero();
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
        Page.Metadata metadata = PageableByNumber.
                of(10, 0).
                createPageMetadata(100, 200);

        Assertions.assertThat(metadata.getOffset()).isZero();
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
        Page.Metadata metadata = PageableByNumber.
                of(10, 5).
                createPageMetadata(100, 200);

        Assertions.assertThat(metadata.getOffset()).isEqualTo(BigInteger.valueOf(50));
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
        Page.Metadata metadata = PageableByNumber.
                of(10, 9).
                createPageMetadata(100, 200);

        Assertions.assertThat(metadata.getOffset()).isEqualTo(BigInteger.valueOf(90));
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
        Page.Metadata metadata = PageableByNumber.
                of(10, 9).
                createPageMetadata(91, 200);

        Assertions.assertThat(metadata.getOffset()).isEqualTo(BigInteger.valueOf(90));
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             page contains items,
             globalIndex < 0
             => return empty Optional
            """)
    public void getByGlobalIndex1() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 0).
                createPageMetadata(100, 200).
                createPage(createFullList(0, 10));

        Optional<Integer> actual = page.getByGlobalIndex(-1);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             page contains items,
             globalIndex = total items number
             => return empty Optional
            """)
    public void getByGlobalIndex2() {
        Page<Integer> lastPage = PageableByNumber.
                ofIndex(10, 99).
                createPageMetadata(100, 200).
                createPage(createFullList(0, 10));

        Optional<Integer> actual = lastPage.getByGlobalIndex(100);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             page contains items,
             globalIndex > total items number
             => return empty Optional
            """)
    public void getByGlobalIndex3() {
        Page<Integer> lastPage = PageableByNumber.
                ofIndex(10, 99).
                createPageMetadata(100, 200).
                createPage(createFullList(0, 10));

        Optional<Integer> actual = lastPage.getByGlobalIndex(101);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             item with globalIndex out of page bottom line
             => return empty Optional
            """)
    public void getByGlobalIndex4() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<Integer> actual = page.getByGlobalIndex(9);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             item with globalIndex out of page top line
             => return empty Optional
            """)
    public void getByGlobalIndex5() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<Integer> actual = page.getByGlobalIndex(20);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             item with globalIndex is first page item
             => return first page item
            """)
    public void getByGlobalIndex6() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<Integer> actual = page.getByGlobalIndex(10);
        Assertions.assertThat(actual).
                isPresent().
                contains(10);
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             item with globalIndex is last page item
             => return last page item
            """)
    public void getByGlobalIndex7() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<Integer> actual = page.getByGlobalIndex(19);
        Assertions.assertThat(actual).
                isPresent().
                contains(19);
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             item with globalIndex is middle page item
             => return middle page item
            """)
    public void getByGlobalIndex8() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<Integer> actual = page.getByGlobalIndex(15);
        Assertions.assertThat(actual).
                isPresent().
                contains(15);
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             page is empty
             => return empty Optional
            """)
    public void getByGlobalIndex9() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(0, 200).
                createPage(List.of());

        Optional<Integer> actual = page.getByGlobalIndex(15);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getByGlobalIndex(globalIndex):
             globalIndex is null
             => exception
            """)
    public void getByGlobalIndex10() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(0, 200).
                createPage(List.of());

        Assertions.assertThatNullPointerException().isThrownBy(() -> page.getByGlobalIndex(null));
    }

    @Test
    @DisplayName("""
            getGlobalIndexFor(predicate):
             predicate is null
             => exception
            """)
    public void getGlobalIndexFor1() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(0, 200).
                createPage(List.of());

        Assertions.assertThatNullPointerException().isThrownBy(() -> page.getGlobalIndexFor(null));
    }

    @Test
    @DisplayName("""
            getGlobalIndexFor(predicate):
             page is empty
             => return empty Optional
            """)
    public void getGlobalIndexFor2() {
        Page<Integer> page = Page.empty();

        Optional<BigInteger> actual = page.getGlobalIndexFor(i -> i == 15);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getGlobalIndexFor(predicate):
             page isn't empty,
             page don't contains item that matches the predicate
             => return empty Optional
            """)
    public void getGlobalIndexFor3() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<BigInteger> actual = page.getGlobalIndexFor(i -> i == -100);
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            getGlobalIndexFor(predicate):
             page isn't empty,
             page contains one item that matches the predicate
             => return correct result
            """)
    public void getGlobalIndexFor4() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<BigInteger> actual = page.getGlobalIndexFor(i -> i == 12);
        Assertions.assertThat(actual).
                isPresent().
                contains(BigInteger.valueOf(12));
    }

    @Test
    @DisplayName("""
            getGlobalIndexFor(predicate):
             page isn't empty,
             page contains several items that matches the predicate
             => return correct result for first item
            """)
    public void getGlobalIndexFor5() {
        Page<Integer> page = PageableByNumber.
                ofIndex(10, 15).
                createPageMetadata(100, 200).
                createPage(createFullList(10, 10));

        Optional<BigInteger> actual = page.getGlobalIndexFor(i -> (i % 2) != 0);
        Assertions.assertThat(actual).
                isPresent().
                contains(BigInteger.valueOf(11));
    }


    private List<Integer> createFullList(int from, int itemsNumber) {
        return IntStream.range(from, from + itemsNumber).boxed().toList();
    }

}