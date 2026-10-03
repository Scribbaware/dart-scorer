package com.thescrib.dartscorer.domain

data class X01Player(
    val remaining: Int,
    val legsWon: Int = 0,
    /** Met double-in tel je pas mee na je eerste dubbel. */
    val opened: Boolean = true,
    /** Totaal gescoorde punten in de hele partij (busts tellen niet mee); voor het gemiddelde. */
    val points: Int = 0,
    val dartsThrown: Int = 0,
    /** Pijlen van de vorige beurt, om op het scorebord te tonen. */
    val lastVisit: List<Dart> = emptyList(),
    val lastVisitBust: Boolean = false,
) {
    /** Gemiddelde per drie pijlen, zoals gebruikelijk bij darts. */
    val average: Double get() = if (dartsThrown == 0) 0.0 else points * 3.0 / dartsThrown
}

data class X01State(
    override val turn: TurnState,
    val players: List<X01Player>,
    /** Restant van de speler aan de beurt bij het begin van de beurt; daar ga je naar terug bij een bust. */
    val visitStart: Int,
    val leg: Int = 1,
    override val winners: List<Int> = emptyList(),
) : GameState {
    val current: X01Player get() = players[turn.current]
}

class X01Rules(private val config: X01Config) : GameRules<X01State>() {

    override fun initial(players: Int) = X01State(
        turn = TurnState(),
        players = List(players) { freshPlayer() },
        visitStart = config.start,
    )

    private fun freshPlayer(legsWon: Int = 0, points: Int = 0, darts: Int = 0) =
        X01Player(remaining = config.start, legsWon = legsWon, opened = !config.doubleIn, points = points, dartsThrown = darts)

    override fun throwDart(state: X01State, dart: Dart): X01State {
        val turn = state.turn.add(dart)
        val me = state.current.copy(dartsThrown = state.current.dartsThrown + 1)

        // Double-in: zolang je niet "open" bent, telt alleen een dubbel.
        if (!me.opened && !dart.isDouble) {
            return state.endOrContinue(turn, me)
        }
        val left = me.remaining - dart.score
        val bust = left < 0 ||
            (config.doubleOut && left == 1) ||
            (config.doubleOut && left == 0 && !dart.isDouble)

        if (bust) {
            val scoredThisVisit = state.visitStart - me.remaining
            val busted = me.copy(
                remaining = state.visitStart,
                points = me.points - scoredThisVisit,
                // Wie deze beurt pas "open" ging, staat na de bust weer op slot.
                opened = !config.doubleIn || state.visitStart != config.start,
                lastVisit = turn.darts,
                lastVisitBust = true,
            )
            return state.passTurn(turn, busted)
        }

        val scored = me.copy(remaining = left, opened = true, points = me.points + dart.score)
        if (left > 0) return state.endOrContinue(turn, scored)

        // Uitgegooid: leg gewonnen.
        val winner = scored.copy(legsWon = scored.legsWon + 1, lastVisit = turn.darts, lastVisitBust = false)
        val players = state.players.toMutableList().also { it[turn.current] = winner }
        if (winner.legsWon >= config.legs) {
            return state.copy(turn = turn, players = players, winners = listOf(turn.current))
        }
        // Nieuwe leg: iedereen terug naar de start, de volgende speler begint.
        val starter = (state.turn.starter + 1) % players.size
        return X01State(
            turn = TurnState(current = starter, starter = starter),
            players = players.map { freshPlayer(it.legsWon, it.points, it.dartsThrown).copy(lastVisit = it.lastVisit, lastVisitBust = it.lastVisitBust) },
            visitStart = config.start,
            leg = state.leg + 1,
        )
    }

    private fun X01State.endOrContinue(turn: TurnState, me: X01Player): X01State =
        if (turn.isFull) {
            passTurn(turn, me.copy(lastVisit = turn.darts, lastVisitBust = false))
        } else {
            copy(turn = turn, players = players.toMutableList().also { it[turn.current] = me })
        }

    private fun X01State.passTurn(turn: TurnState, me: X01Player): X01State {
        val updated = players.toMutableList().also { it[turn.current] = me }
        val next = turn.next(updated.size)
        return copy(turn = next, players = updated, visitStart = updated[next.current].remaining)
    }
}

/**
 * Uitgooi-advies: de kortste reeks van hoogstens [dartsLeft] pijlen die precies [remaining] wegneemt,
 * met een dubbel als laatste pijl bij [doubleOut]. Geeft null als uitgooien met deze pijlen niet kan.
 */
fun checkout(remaining: Int, dartsLeft: Int, doubleOut: Boolean): List<Dart>? {
    if (remaining <= 0 || dartsLeft <= 0) return null
    if (remaining > 60 * dartsLeft - if (doubleOut) 10 else 0) return null
    val finishers = Dart.ALL.filter { !doubleOut || it.isDouble }.sortedBy { finishRank(it) }
    // Opzet-pijlen: hoogste eerst, zodat de route zo kort en gangbaar mogelijk is.
    val setups = Dart.ALL.sortedWith(compareByDescending<Dart> { it.score }.thenByDescending { it.multiplier })

    for (n in 1..dartsLeft) {
        var best: List<Dart>? = null
        var bestRank = Int.MAX_VALUE
        for (last in finishers) {
            val rest = remaining - last.score
            val route = when (n) {
                1 -> if (rest == 0) emptyList() else null
                2 -> setups.firstOrNull { it.score == rest }?.let { listOf(it) }
                else -> setups.firstNotNullOfOrNull { first ->
                    setups.firstOrNull { it.score == rest - first.score }?.let { listOf(first, it) }
                }
            } ?: continue
            val rank = finishRank(last)
            if (rank < bestRank) {
                best = route + last
                bestRank = rank
            }
        }
        if (best != null) return best
    }
    return null
}

/** Favoriete dubbels om op uit te gooien (lager is beter): D20, D16, D8 enz. laten makkelijk een nieuwe dubbel over. */
private fun finishRank(dart: Dart): Int {
    if (!dart.isDouble) return 100 - dart.score
    val preferred = listOf(20, 16, 8, 18, 12, 10, 4, 14, 6, 2, 19, 17, 15, 13, 11, 9, 7, 5, 3, 1, Dart.BULL)
    return preferred.indexOf(dart.segment)
}
