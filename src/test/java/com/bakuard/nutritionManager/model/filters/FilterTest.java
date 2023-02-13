package com.bakuard.nutritionManager.model.filters;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
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
            bfs():
             filter tree contains only one item
             => return this item
            """)
    public void bfs1() {
        Filter filter = Filter.user(toUUID(1));

        Stream<IterableFilter> actual = filter.bfs();

        Assertions.assertThat(actual).
                containsExactly(
                        new IterableFilter(filter, 0)
                );
    }

    @Test
    @DisplayName("""
            bfs():
             filter tree has several items
             => return this items in BFS order
            """)
    public void bfs2() {
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

        Stream<IterableFilter> actual = filter.bfs();

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

    @Test
    @DisplayName("""
            isDnf():
             filter has single item
             => return true
            """)
    public void isDnf1() {
        Filter filter = Filter.user(toUUID(1));

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            isDnf():
             filter has several items,
             filter has AND and OR filters,
             filter logical form is not DNF
             => return false
            """)
    public void isDnf2() {
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

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            isDnf():
             filter has several items,
             filter has AND and OR filters,
             filter logical form is DNF
             => return true
            """)
    public void isDnf3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.and(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.and(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c"),
                Filter.or(
                        Filter.anyManufacturer("a", "b", "c", "d"),
                        Filter.user(toUUID(3)),
                        Filter.anyCategory("a", "b"),
                        Filter.or(
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                ),
                                Filter.or(
                                        Filter.anyManufacturer("a", "b", "c", "d"),
                                        Filter.user(toUUID(2))
                                )
                        )
                )
        );

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            isDnf():
             filter has several items,
             filter has only AND filters
             => return true
            """)
    public void isDnf4() {
        Filter filter = Filter.and(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.and(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.and(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            isDnf():
             filter has several items,
             filter has only OR filters
             => return true
            """)
    public void isDnf5() {
        Filter filter = Filter.or(
                Filter.or(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.or(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("""
            isDnf():
             filter has several items,
             filter logical form is KNF
             => return false
            """)
    public void isDnf6() {
        Filter filter = Filter.and(
                Filter.user(toUUID(1)),
                Filter.and(
                        Filter.anyManufacturer("a"),
                        Filter.and(
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.and(
                                        Filter.anyCategory("a"),
                                        Filter.and(
                                                Filter.user(toUUID(2)),
                                                Filter.or(
                                                        Filter.user(toUUID(3)),
                                                        Filter.anyGrade("a", "b", "c")
                                                )
                                        )
                                )
                        )
                )
        );

        boolean actual = filter.isDnf();

        Assertions.assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has single item
             => return same filter
            """)
    public void toDnf1() {
        Filter filter = Filter.user(toUUID(1));

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(filter).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has AND and OR filters,
             filter logical form is not DNF
             => return new filter in DNF form
            """)
    public void toDnf2() {
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
                                        Filter.anyGrade("d", "e", "f"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual.dfs()).
                map(iterableFilter -> iterableFilter.filter().getType()).
                containsExactly(
                        OR,
                            AND,
                                USER,
                                MIN_QUANTITY,
                                MANUFACTURER,
                                GRADES,
                            AND,
                                USER,
                                MIN_QUANTITY,
                                MANUFACTURER,
                                GRADES,
                                CATEGORY,
                            AND,
                                USER,
                                MIN_QUANTITY,
                                USER,
                                GRADES,
                            AND,
                                USER,
                                MIN_QUANTITY,
                                USER,
                                GRADES,
                                CATEGORY,
                            SHOPS
                );
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has AND and OR filters,
             filter logical form is DNF
             => return the equivalent filter with open brackets
            """)
    public void toDnf3() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.and(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.and(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c"),
                Filter.or(
                        Filter.anyManufacturer("a", "b", "c", "d"),
                        Filter.user(toUUID(3)),
                        Filter.anyCategory("a", "b"),
                        Filter.or(
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                ),
                                Filter.or(
                                        Filter.anyManufacturer("a", "b", "c", "d"),
                                        Filter.user(toUUID(2))
                                )
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.greater(BigDecimal.ZERO),
                                        Filter.anyManufacturer("a", "b", "c", "d"),
                                        Filter.user(toUUID(2)),
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                ),
                                Filter.anyShop("a", "b", "c"),
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(3)),
                                Filter.anyCategory("a", "b"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                ),
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has only AND filters
             => return the equivalent filter with open brackets
            """)
    public void toDnf4() {
        Filter filter = Filter.and(
                Filter.and(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.and(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.and(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.and(
                                        Filter.anyGrade("a", "b", "c"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.and(
                                Filter.user(toUUID(1)),
                                Filter.greater(BigDecimal.ZERO),
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2)),
                                Filter.anyGrade("a", "b", "c"),
                                Filter.anyGrade("a", "b", "c"),
                                Filter.anyCategory("a", "b", "c"),
                                Filter.anyShop("a", "b", "c")
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has only AND filters,
             filter tree is degenerate
             => return the equivalent filter with open brackets
            """)
    public void toDnf5() {
        Filter filter = Filter.and(
                Filter.user(toUUID(1)),
                Filter.and(
                        Filter.anyManufacturer("a"),
                        Filter.and(
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.and(
                                        Filter.anyCategory("a"),
                                        Filter.and(
                                                Filter.user(toUUID(2)),
                                                Filter.and(
                                                        Filter.user(toUUID(3)),
                                                        Filter.anyGrade("a", "b", "c")
                                                )
                                        )
                                )
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.and(
                                Filter.user(toUUID(1)),
                                Filter.anyManufacturer("a"),
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.anyCategory("a"),
                                Filter.user(toUUID(2)),
                                Filter.user(toUUID(3)),
                                Filter.anyGrade("a", "b", "c")
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has only OR filters
             => return the equivalent filter with open brackets
            """)
    public void toDnf6() {
        Filter filter = Filter.or(
                Filter.or(
                        Filter.user(toUUID(1)),
                        Filter.greater(BigDecimal.ZERO),
                        Filter.or(
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2))
                        ),
                        Filter.or(
                                Filter.anyGrade("a", "b", "c"),
                                Filter.or(
                                        Filter.anyGrade("d", "e", "f"),
                                        Filter.anyCategory("a", "b", "c")
                                )
                        )
                ),
                Filter.anyShop("a", "b", "c")
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.user(toUUID(1)),
                                Filter.greater(BigDecimal.ZERO),
                                Filter.anyManufacturer("a", "b", "c", "d"),
                                Filter.user(toUUID(2)),
                                Filter.anyGrade("a", "b", "c"),
                                Filter.anyGrade("d", "e", "f"),
                                Filter.anyCategory("a", "b", "c"),
                                Filter.anyShop("a", "b", "c")
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has only OR filters,
             filter tree is degenerate
             => return the equivalent filter with open brackets
            """)
    public void toDnf7() {
        Filter filter = Filter.or(
                Filter.user(toUUID(1)),
                Filter.or(
                        Filter.anyManufacturer("a"),
                        Filter.or(
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.or(
                                        Filter.anyCategory("a"),
                                        Filter.or(
                                                Filter.user(toUUID(2)),
                                                Filter.or(
                                                        Filter.user(toUUID(3)),
                                                        Filter.anyGrade("a", "b", "c")
                                                )
                                        )
                                )
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.user(toUUID(1)),
                                Filter.anyManufacturer("a"),
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.anyCategory("a"),
                                Filter.user(toUUID(2)),
                                Filter.user(toUUID(3)),
                                Filter.anyGrade("a", "b", "c")
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter logical form is KNF
             => return new filter in DNF form
            """)
    public void toDnf8() {
        Filter filter = Filter.and(
                Filter.user(toUUID(1)),
                Filter.and(
                        Filter.anyManufacturer("a"),
                        Filter.and(
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.or(
                                        Filter.anyCategory("a"),
                                        Filter.anyGrade("a", "b", "c")
                                )
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyManufacturer("a"),
                                        Filter.anyShop("a", "b", "c", "d"),
                                        Filter.anyCategory("a")
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyManufacturer("a"),
                                        Filter.anyShop("a", "b", "c", "d"),
                                        Filter.anyGrade("a", "b", "c")
                                )
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter logical form is KNF,
             filter has single AND filter
             => return new filter in DNF form
            """)
    public void toDnf9() {
        Filter filter = Filter.and(
                Filter.user(toUUID(1)),
                Filter.or(
                        Filter.anyManufacturer("a"),
                        Filter.or(
                                Filter.anyShop("a", "b", "c", "d"),
                                Filter.or(
                                        Filter.anyCategory("a"),
                                        Filter.or(
                                                Filter.user(toUUID(2)),
                                                Filter.or(
                                                        Filter.user(toUUID(3)),
                                                        Filter.anyGrade("a", "b", "c")
                                                )
                                        )
                                )
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyManufacturer("a")
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyShop("a", "b", "c", "d")
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyCategory("a")
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.user(toUUID(2))
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.user(toUUID(3))
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyGrade("a", "b", "c")
                                )
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            toDnf():
             filter has several items,
             filter has AND and OR filters,
             filter logical form is not DNF,
             filter tree is symmetrical
             => return new filter in DNF form
            """)
    public void toDnf10() {
        Filter filter = Filter.or(
                Filter.and(
                        Filter.or(
                                Filter.user(toUUID(1)),
                                Filter.anyShop("a", "b", "c")
                        ),
                        Filter.or(
                                Filter.user(toUUID(2)),
                                Filter.anyGrade("a", "b", "c")
                        )
                ),
                Filter.and(
                        Filter.or(
                                Filter.user(toUUID(3)),
                                Filter.anyShop("d", "e", "f")
                        ),
                        Filter.or(
                                Filter.user(toUUID(4)),
                                Filter.anyGrade("d", "e", "f")
                        )
                )
        );

        Filter actual = filter.toDnf();

        Assertions.assertThat(actual).
                isEqualTo(
                        Filter.or(
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.user(toUUID(2))
                                ),
                                Filter.and(
                                        Filter.user(toUUID(1)),
                                        Filter.anyGrade("a", "b", "c")
                                ),
                                Filter.and(
                                        Filter.anyShop("a", "b", "c"),
                                        Filter.user(toUUID(2))
                                ),
                                Filter.and(
                                        Filter.anyShop("a", "b", "c"),
                                        Filter.anyGrade("a", "b", "c")
                                ),
                                Filter.and(
                                        Filter.user(toUUID(3)),
                                        Filter.user(toUUID(4))
                                ),
                                Filter.and(
                                        Filter.user(toUUID(3)),
                                        Filter.anyGrade("d", "e", "f")
                                ),
                                Filter.and(
                                        Filter.anyShop("d", "e", "f"),
                                        Filter.user(toUUID(4))
                                ),
                                Filter.and(
                                        Filter.anyShop("d", "e", "f"),
                                        Filter.anyGrade("d", "e", "f")
                                )
                        )
                ).
                extracting(Filter::isDnf, InstanceOfAssertFactories.BOOLEAN).
                isTrue();
    }

    @Test
    @DisplayName("""
            dfs():
             filter tree contains only one item
             => return this item
            """)
    public void dfs1() {
        Filter filter = Filter.user(toUUID(1));

        Stream<IterableFilter> actual = filter.dfs();

        Assertions.assertThat(actual).
                containsExactly(
                        new IterableFilter(filter, 0)
                );
    }

    @Test
    @DisplayName("""
            dfs():
             filter tree has several items
             => return this items in DFS order
            """)
    public void dfs2() {
        Filter leaf1 = Filter.user(toUUID(1));
        Filter leaf2 = Filter.greater(BigDecimal.ZERO);
        Filter leaf3 = Filter.anyManufacturer("a", "b", "c", "d");
        Filter leaf4 = Filter.user(toUUID(2));
        Filter or1 = Filter.or(leaf3, leaf4);
        Filter leaf5 = Filter.anyGrade("a", "b", "c");
        Filter leaf6 = Filter.anyGrade("a", "b", "c");
        Filter leaf7 = Filter.anyCategory("a", "b", "c");
        Filter and1 = Filter.and(leaf6, leaf7);
        Filter or2 = Filter.or(leaf5, and1);
        Filter and2 = Filter.and(leaf1, leaf2, or1, or2);
        Filter leaf8 = Filter.anyShop("a", "b", "c");
        Filter filter = Filter.or(and2, leaf8);

        Stream<IterableFilter> actual = filter.dfs();

        Assertions.assertThat(actual).
                containsExactly(
                        new IterableFilter(filter, 0),
                        new IterableFilter(and2, 1),
                        new IterableFilter(leaf1, 2),
                        new IterableFilter(leaf2, 2),
                        new IterableFilter(or1, 2),
                        new IterableFilter(leaf3, 3),
                        new IterableFilter(leaf4, 3),
                        new IterableFilter(or2, 2),
                        new IterableFilter(leaf5, 3),
                        new IterableFilter(and1, 3),
                        new IterableFilter(leaf6, 4),
                        new IterableFilter(leaf7, 4),
                        new IterableFilter(leaf8, 1)
                );
    }

    @Test
    @DisplayName("""
            findFirstSibling(type):
             filter is root of filter tree,
             filter has not this type
             => return empty Optional
            """)
    public void findFirstSibling1() {
        Filter filter = Filter.user(toUUID(1));

        Optional<Filter> actual = filter.findFirstSibling(MANUFACTURER);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findFirstSibling(type):
             filter is root of filter tree,
             filter has this type
             => return this filter
            """)
    public void findFirstSibling2() {
        Filter filter = Filter.user(toUUID(1));

        Optional<Filter> actual = filter.findFirstSibling(USER);

        Assertions.assertThat(actual).contains(filter);
    }

    @Test
    @DisplayName("""
            findFirstSibling(type):
             filter has not sibling with such type,
             filter has not this type
             => return empty Optional
            """)
    public void findFirstSibling3() {
        Filter filter = Filter.user(toUUID(1));
        Filter.and(
                filter,
                Filter.anyGrade("a", "b", "c"),
                Filter.anyShop("a", "b", "c"),
                Filter.anyManufacturer("a", "b", "c")
        );

        Optional<Filter> actual = filter.findFirstSibling(CATEGORY);

        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("""
            findFirstSibling(type):
             filter has sibling with such type,
             filter has not this type
             => return empty Optional
            """)
    public void findFirstSibling4() {
        Filter filter = Filter.user(toUUID(1));
        Filter.and(
                filter,
                Filter.anyGrade("a", "b", "c"),
                Filter.anyShop("a", "b", "c"),
                Filter.anyShop("d", "e", "f"),
                Filter.anyManufacturer("a", "b", "c")
        );

        Optional<Filter> actual = filter.findFirstSibling(SHOPS);

        Assertions.assertThat(actual).contains(Filter.anyShop("a", "b", "c"));
    }



    private UUID toUUID(int number) {
        return UUID.fromString("00000000-0000-0000-0000-" + String.format("%012d", number));
    }

}