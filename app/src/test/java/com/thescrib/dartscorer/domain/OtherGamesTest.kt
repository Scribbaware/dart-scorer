package com.thescrib.dartscorer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtherGamesTest {
    private val miss = Dart.MISS
    private fun players(n: Int) = List(n) { "P$it" }

    @Test
    fun cricketMarksAndPoints() {
        val m = Match(CricketConfig, players(2), listOf(Dart(20, 3), Dart(20, 2), Dart(19)))
        val s = m.state as CricketState
        assertEquals(3, s.players[0].marks[0])
        assertEquals(40, s.players[0].points)
        assertEquals(1, s.players[0].marks[1])
        assertEquals(1, s.turn.current)
    }

    @Test
    fun cricketNoPointsWhenEveryoneClosed() {
        val darts = listOf(Dart(20, 3), miss, miss, Dart(20, 3), Dart(20, 3), miss)
        val s = Match(CricketConfig, players(2), darts).state as CricketState
        assertEquals(0, s.players[1].points)
    }

    @Test
    fun cricketWinnerClosesAllWithEnoughPoints() {
        val closeAll = listOf(20, 19, 18, 17, 16, 15).map { Dart(it, 3) } + Dart(Dart.BULL, 2) + Dart(Dart.BULL, 1)
        // Speler 2 mist steeds; speler 1 sluit alles in drie beurten.
        val darts = closeAll.take(3) + listOf(miss, miss, miss) + closeAll.drop(3).take(3) + listOf(miss, miss, miss) + closeAll.drop(6)
        val s = Match(CricketConfig, players(2), darts).state as CricketState
        assertEquals(listOf(0), s.winners)
    }

    @Test
    fun aroundTheClockAdvancesAndWinsOnBull() {
        val hits = (1..20).map { Dart(it) } + Dart(Dart.BULL)
        val s = Match(AroundTheClockConfig(), players(1), hits).state as ClockState
        assertEquals(listOf(0), s.winners)

        val wrong = Match(AroundTheClockConfig(), players(1), listOf(Dart(2))).state as ClockState
        assertEquals(1, wrong.target(0))
    }

    @Test
    fun aroundTheClockSkip() {
        val s = Match(AroundTheClockConfig(multipliersSkip = true), players(1), listOf(Dart(1, 3))).state as ClockState
        assertEquals(4, s.target(0))
        val nearEnd = Match(AroundTheClockConfig(true), players(1), (1..18).map { Dart(it) } + Dart(19, 3)).state as ClockState
        assertEquals(Dart.BULL, nearEnd.target(0))
    }

    @Test
    fun shanghaiScoresOnlyTargetAndEndsAfterRounds() {
        val darts = listOf(Dart(1, 3), Dart(2), miss) + listOf(Dart(2, 2), miss, miss)
        val s = Match(ShanghaiConfig(rounds = 2), players(1), darts).state as ShanghaiState
        assertTrue(s.isOver)
        assertEquals(3 + 4, s.scores[0])
        assertFalse(s.shanghai)
    }

    @Test
    fun shanghaiWinsInstantly() {
        val s = Match(ShanghaiConfig(), players(2), listOf(Dart(1), Dart(1, 2), Dart(1, 3))).state as ShanghaiState
        assertTrue(s.shanghai)
        assertEquals(listOf(0), s.winners)
    }

    @Test
    fun shanghaiDraw() {
        val darts = listOf(Dart(1), miss, miss, Dart(1), miss, miss)
        val s = Match(ShanghaiConfig(rounds = 1), players(2), darts).state as ShanghaiState
        assertEquals(listOf(0, 1), s.winners)
    }

    @Test
    fun killerFlow() {
        val config = KillerConfig(lives = 1, numbers = listOf(5, 10, 15))
        // P0 wordt killer en raakt dan D10 → P1 is uit. Daarna is P2 aan de beurt.
        val s = Match(config, players(3), listOf(Dart(5, 2), Dart(10, 2), miss)).state as KillerState
        assertTrue(s.players[0].isKiller)
        assertFalse(s.players[1].isAlive)
        assertEquals(2, s.turn.current)
        // P2 mist alles, P0 schakelt P2 uit en wint.
        val done = Match(config, players(3), listOf(Dart(5, 2), Dart(10, 2), miss, miss, miss, miss, Dart(15, 2))).state as KillerState
        assertEquals(listOf(0), done.winners)
    }

    @Test
    fun killerOnlyKillersTakeLives() {
        val config = KillerConfig(lives = 3, numbers = listOf(5, 10))
        val s = Match(config, players(2), listOf(Dart(10, 2))).state as KillerState
        assertEquals(3, s.players[1].lives)
    }

    @Test
    fun codecRoundTrip() {
        val configs = listOf(
            X01Config(701, doubleIn = true, doubleOut = false, legs = 3),
            CricketConfig,
            AroundTheClockConfig(true),
            ShanghaiConfig(10),
            KillerConfig(5, listOf(3, 7)),
        )
        for (config in configs) {
            val match = Match(config, listOf("Anne-Marie", "Jan, de 2e"), listOf(Dart(20, 3), Dart.MISS, Dart(Dart.BULL, 2)))
            assertEquals(match, MatchCodec.decode(MatchCodec.encode(match)))
        }
        assertEquals(null, MatchCodec.decode("rommel"))
    }
}
