package com.metrolist.shared.db

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ListeningStreakTest {
    @Test
    fun currentStreakIncludesTodayWhenPlayed() {
        val result = calculateListeningStreak(
            playedDates = setOf(LocalDate.parse("2026-11-12"), LocalDate.parse("2026-11-13"), LocalDate.parse("2026-11-14")),
            today = LocalDate.parse("2026-11-14"),
        )

        assertEquals(3, result.currentDays)
        assertEquals(3, result.longestDays)
        assertTrue(result.listenedToday)
    }

    @Test
    fun currentStreakCanContinueFromYesterdayBeforeFirstPlayToday() {
        val result = calculateListeningStreak(
            playedDates = setOf(LocalDate.parse("2026-11-12"), LocalDate.parse("2026-11-13")),
            today = LocalDate.parse("2026-11-14"),
        )

        assertEquals(2, result.currentDays)
        assertFalse(result.listenedToday)
    }

    @Test
    fun breaksCurrentStreakButPreservesLongestStreak() {
        val result = calculateListeningStreak(
            playedDates = setOf(
                LocalDate.parse("2026-11-01"),
                LocalDate.parse("2026-11-02"),
                LocalDate.parse("2026-11-03"),
                LocalDate.parse("2026-11-07"),
            ),
            today = LocalDate.parse("2026-11-07"),
        )

        assertEquals(1, result.currentDays)
        assertEquals(3, result.longestDays)
    }
}
