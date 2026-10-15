package com.sadsiyoon.app;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression tests for local-only library filters (no device or server required). */
public class StudentLibraryFilterTest {
    @Test public void favoritesAreIndependentOfProgress() {
        assertTrue(StudentLibraryFilter.includes("fav", false, true, false, 0L));
        assertFalse(StudentLibraryFilter.includes("fav", true, false, true, 8000L));
    }

    @Test public void completedOnlyShowsMarkedLessons() {
        assertTrue(StudentLibraryFilter.includes("done", true, false, true, 0L));
        assertFalse(StudentLibraryFilter.includes("done", true, false, false, 9000L));
    }

    @Test public void continueRequiresSavedPlayableUnfinishedVideo() {
        assertTrue(StudentLibraryFilter.includes("continue", true, false, false, 9000L));
        assertFalse(StudentLibraryFilter.includes("continue", false, false, false, 9000L));
        assertFalse(StudentLibraryFilter.includes("continue", true, false, true, 9000L));
        assertFalse(StudentLibraryFilter.includes("continue", true, false, false, 0L));
        assertFalse(StudentLibraryFilter.includes("continue", true, false, false, -100L));
    }

    @Test public void unknownFilterFallsBackToFavorites() {
        assertTrue(StudentLibraryFilter.includes("unknown", false, true, false, 0L));
        assertFalse(StudentLibraryFilter.includes("unknown", true, false, false, 200L));
    }
}
