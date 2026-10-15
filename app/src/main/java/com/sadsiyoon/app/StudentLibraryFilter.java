package com.sadsiyoon.app;

/** Pure selection rules for the student's offline library. */
final class StudentLibraryFilter {
    private StudentLibraryFilter() {}

    static boolean includes(String filter, boolean playable, boolean favorite,
                            boolean completed, long savedPositionMs) {
        if ("done".equals(filter)) return completed;
        if ("continue".equals(filter))
            return playable && !completed && savedPositionMs > 0L;
        // Also handles older or unknown filter values safely.
        return favorite;
    }
}
