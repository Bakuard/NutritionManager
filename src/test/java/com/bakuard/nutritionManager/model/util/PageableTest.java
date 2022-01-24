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

}