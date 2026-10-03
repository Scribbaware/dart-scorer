package com.thescrib.dartscorer.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class X01Test {
    private fun t(n: Int) = Dart(n, 3)
    private fun d(n: Int) = Dart(n, 2)
    private fun s(n: Int) = Dart(n)

    private fun play(config: X01Config, players: Int, vararg darts: Dart) =
        Match(config, List(players) { "P$it" }, darts.toList()).state as X01State

    @Test
    fun subtractsAndPassesTurnAfterThreeDarts() {
        val state = play(X01Config(501), 2, t(20), t(20), t(20))
        assertEquals(321, state.players[0].remaining)
        assertEquals(1, state.turn.current)
        assertEquals(180.0, state.players[0].average, 0.001)
        assertEquals(listOf(t(20), t(20), t(20)), state.players[0].lastVisit)
    }

    @Test
    fun bustRestoresScoreAndEndsTurn() {
        // 301 - 180 = 121, dan 60 + 60 = 120 → 1 over: bust bij double out.
        val state = play(X01Config(301), 2, t(20), t(20), t(20), Dart.MISS, Dart.MISS, Dart.MISS, t(20), t(20))
        assertEquals(121, state.players[0].remaining)
        assertTrue(state.players[0].lastVisitBust)
        assertEquals(1, state.turn.current)
        // Busts tellen niet mee voor het gemiddelde, de pijlen wel.
        assertEquals(180, state.players[0].points)
        assertEquals(5, state.players[0].dartsThrown)
    }

    @Test
    fun mustFinishOnDoubleWithDoubleOut() {
        val config = X01Config(start = 40)
        assertTrue(play(config, 1, d(20)).isOver)
        val single = play(config, 1, s(20), s(20))
        assertFalse(single.isOver)
        assertEquals(40, single.players[0].remaining)
    }

    @Test
    fun anyDartFinishesWithoutDoubleOut() {
        val state = play(X01Config(start = 40, doubleOut = false), 1, s(20), s(20))
        assertTrue(state.isOver)
        assertEquals(listOf(0), state.winners)
    }

    @Test
    fun bullCountsAsDouble() {
        assertTrue(play(X01Config(start = 50), 1, Dart(Dart.BULL, 2)).isOver)
    }

    @Test
    fun doubleInIgnoresDartsUntilFirstDouble() {
        val state = play(X01Config(start = 301, doubleIn = true), 1, t(20), d(10), s(5))
        assertEquals(301 - 20 - 5, state.players[0].remaining)
    }

    @Test
    fun legsRotateStarterAndDecideMatch() {
        val config = X01Config(start = 40, legs = 2)
        val afterLeg1 = play(config, 2, d(20))
        assertFalse(afterLeg1.isOver)
        assertEquals(2, afterLeg1.leg)
        assertEquals(1, afterLeg1.players[0].legsWon)
        assertEquals(1, afterLeg1.turn.current) // speler 2 begint leg 2
        assertEquals(40, afterLeg1.players[0].remaining)

        val done = play(config, 2, d(20), Dart.MISS, Dart.MISS, Dart.MISS, d(20))
        assertEquals(listOf(0), done.winners)
    }

    @Test
    fun undoRemovesLastDart() {
        val match = Match(X01Config(), listOf("A")).throwDart(t(20)).throwDart(t(19))
        val undone = match.undo()
        assertEquals(441, (undone.state as X01State).players[0].remaining)
    }

    @Test
    fun checkoutSuggestions() {
        assertEquals(listOf(t(20), t(20), Dart(Dart.BULL, 2)), checkout(170, 3, true))
        assertEquals(listOf(d(20)), checkout(40, 3, true))
        assertEquals(listOf(t(20), d(20)), checkout(100, 3, true))
        assertNull(checkout(169, 3, true))
        assertNull(checkout(100, 1, true))
        // Elke voorgestelde route klopt en eindigt op een dubbel.
        for (score in 2..170) {
            val route = checkout(score, 3, true) ?: continue
            assertEquals(score, route.sumOf { it.score })
            assertTrue(route.last().isDouble)
        }
        assertEquals(listOf(t(20), t(20), t(20)), checkout(180, 3, false))
    }
}
