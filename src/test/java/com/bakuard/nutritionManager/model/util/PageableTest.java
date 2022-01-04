package com.bakuard.nutritionManager.model.util;

import com.bakuard.nutritionManager.model.exceptions.NegativePageTotalItemsException;

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
                NegativePageTotalItemsException.class,
                () -> Pageable.
                        of(10, 0).
                        createPageMetadata(-1)
        );
    }

}