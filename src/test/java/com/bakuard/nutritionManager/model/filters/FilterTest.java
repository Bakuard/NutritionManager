package com.bakuard.nutritionManager.model.filters;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static com.bakuard.nutritionManager.model.filters.Filter.Type.*;

class FilterTest {

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth = 0,
             actual match < matchNumber
             => return false
            """)
    public void containsExactly1() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(2, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth = 0,
             actual match = matchNumber
             => return true
            """)
    public void containsExactly2() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(1, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth = 0,
             actual match > matchNumber
             => return false
            """)
    public void containsExactly3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(0, 0, MANUFACTURER, OR, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth > 0,
             actual match < matchNumber
             => return false
            """)
    public void containsExactly4() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(2, 1, MANUFACTURER, CATEGORY, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth > 0,
             actual match = matchNumber
             => return true
            """)
    public void containsExactly5() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(2, 1, SHOPS, CATEGORY, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsExactly(matchNumber, maxDepth, types):
             maxDepth > 0,
             actual match > matchNumber
             => return false
            """)
    public void containsExactly6() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsExactly(2, 1, SHOPS, OR, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth = 0,
             actual match < minMatch
             => return false
            """)
    public void containsMin1() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(2, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth = 0,
             actual match = minMatch
             => return true
            """)
    public void containsMin2() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(1, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth = 0,
             actual match > minMatch
             => return true
            """)
    public void containsMin3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(0, 0, OR, CATEGORY, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth > 0,
             actual match < minMatch
             => return false
            """)
    public void containsMin4() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(2, 1, MANUFACTURER, CATEGORY, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth > 0,
             actual match = minMatch
             => return true
            """)
    public void containsMin5() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(2, 1, SHOPS, CATEGORY, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMin(minMatch, maxDepth, types):
             maxDepth > 0,
             actual match > minMatch
             => return true
            """)
    public void containsMin6() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMin(2, 1, SHOPS, OR, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth = 0,
             actual match < maxMatch
             => return true
            """)
    public void containsMax1() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(2, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth = 0,
             actual match = maxMatch
             => return true
            """)
    public void containsMax2() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(1, 0, SHOPS, OR, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth = 0,
             actual match > maxMatch
             => return false
            """)
    public void containsMax3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(0, 0, OR, CATEGORY, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth > 0,
             actual match < maxMatch
             => return false
            """)
    public void containsMax4() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(2, 1, MANUFACTURER, CATEGORY, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth > 0,
             actual match = maxMatch
             => return true
            """)
    public void containsMax5() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(2, 1, SHOPS, CATEGORY, AND);

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            containsMax(maxMatch, maxDepth, types):
             maxDepth > 0,
             actual match > maxMatch
             => return false
            """)
    public void containsMax6() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.containsMax(2, 1, SHOPS, OR, AND);

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            getAllFilters():
             filter tree contains only one item
             => return this item
            """)
    public void getAllFilters1() {
        Filter filter = Filter.user(toUUID(1));

        Stream<IterableFilter> actual = filter.getAllFilters();

        Assertions.assertThat(actual).
                containsExactly(
                        new IterableFilter(filter, 0)
                );
    }

    @Test
    @DisplayName("""
            getAllFilters():
             filter tree has several items
             => return this item
            """)
    public void getAllFilters2() {
        Filter user1 = Filter.user(toUUID(1));
        Filter greater = Filter.greater(BigDecimal.ZERO);
        Filter user2 = Filter.user(toUUID(2));
        Filter manufacturer = Filter.anyManufacturer("a", "b", "c", "d");
        Filter or1 = Filter.or(manufacturer, user2);
        Filter grade = Filter.anyGrade("a", "b", "c");
        Filter grade2 = Filter.anyGrade("a", "b", "c");
        Filter category = Filter.anyCategory("a", "b", "c");
        Filter and1 = Filter.and(grade2, category);
        Filter or2 = Filter.or(grade, and1);
        Filter and2 = Filter.and(user1, greater, or1, or2);
        Filter shop = Filter.anyShop("a", "b", "c");
        Filter filter = Filter.or(and2, shop);

        Stream<IterableFilter> actual = filter.getAllFilters();

        Assertions.assertThat(actual).
                containsExactly(
                        new IterableFilter(filter, 0),
                        new IterableFilter(and2, 1),
                        new IterableFilter(shop, 1),
                        new IterableFilter(user1, 2),
                        new IterableFilter(greater, 2),
                        new IterableFilter(or1, 2),
                        new IterableFilter(or2, 2),
                        new IterableFilter(manufacturer, 3),
                        new IterableFilter(user2, 3),
                        new IterableFilter(grade, 3),
                        new IterableFilter(and1, 3),
                        new IterableFilter(grade2, 4),
                        new IterableFilter(category, 4)
                );
    }

    @Test
    @DisplayName("""
            getDepth():
             filter consist of one item
             => return 0
            """)
    public void getDepth1() {
        Filter filter = Filter.user(toUUID(1));

        int actual = filter.getDepth();

        Assertions.assertThat(actual).isZero();
    }

    @Test
    @DisplayName("""
            getDepth():
             filter has 3 layer of nesting,
             all subtree has equal depth
             => return 2
            """)
    public void getDepth2() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO)
                ),
                Filter.and(
                        Filter.user(toUUID(2)),
                        Filter.greater(BigDecimal.TEN)
                ),
                Filter.and(
                        Filter.anyCategory("a", "b", "c"),
                        Filter.anyShop("a", "b", "c")
                )
        );

        int actual = filter.getDepth();

        Assertions.assertThat(actual).isEqualTo(2);
    }

    @Test
    @DisplayName("""
            getDepth():
             filter has 5 layer of nesting,
             all subtree has different depth
             => return 4
            """)
    public void getDepth3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        int actual = filter.getDepth();

        Assertions.assertThat(actual).isEqualTo(4);
    }


    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}