package com.thescrib.dartscorer.domain

// ---------------------------------------------------------------------------------------------
// Cricket: sluit 15 t/m 20 en de bull (3 hits per nummer). Extra hits op een nummer dat jij hebt
// gesloten en een tegenstander nog niet, leveren punten op. Wie alles sluit en minstens evenveel
// punten heeft als ieder ander, wint.
// ---------------------------------------------------------------------------------------------

val CRICKET_NUMBERS = listOf(20, 19, 18, 17, 16, 15, Dart.BULL)

data class CricketPlayer(
    /** Aantal hits per nummer (0–3), in de volgorde van [CRICKET_NUMBERS]. */
    val marks: List<Int> = List(CRICKET_NUMBERS.size) { 0 },
    val points: Int = 0,
) {
    val closedAll: Boolean get() = marks.all { it >= 3 }
}

data class CricketState(
    override val turn: TurnState,
    val players: List<CricketPlayer>,
    override val winners: List<Int> = emptyList(),
) : GameState

object CricketRules : GameRules<CricketState>() {
    override fun initial(players: Int) = CricketState(TurnState(), List(players) { CricketPlayer() })

    override fun throwDart(state: CricketState, dart: Dart): CricketState {
        val turn = state.turn.add(dart)
        val me = turn.current
        val players = state.players.toMutableList()
        val index = CRICKET_NUMBERS.indexOf(dart.segment)
        if (index >= 0) {
            val p = players[me]
            val total = p.marks[index] + dart.multiplier
            val extra = (total - 3).coerceAtLeast(0)
            val opponentOpen = players.indices.any { it != me && players[it].marks[index] < 3 }
            players[me] = p.copy(
                marks = p.marks.toMutableList().also { it[index] = total.coerceAtMost(3) },
                points = p.points + if (opponentOpen) extra * dart.segment else 0,
            )
        }
        val p = players[me]
        if (p.closedAll && players.indices.all { it == me || players[it].points <= p.points }) {
            return state.copy(turn = turn, players = players, winners = listOf(me))
        }
        return state.copy(turn = if (turn.isFull) turn.next(players.size) else turn, players = players)
    }
}

// ---------------------------------------------------------------------------------------------
// Around the Clock: gooi 1 t/m 20 op volgorde en tot slot de bull. Wie als eerste de bull raakt, wint.
// ---------------------------------------------------------------------------------------------

val CLOCK_TARGETS = (1..20).toList() + Dart.BULL

data class ClockState(
    override val turn: TurnState,
    /** Per speler de index in [CLOCK_TARGETS] van het volgende doel. */
    val progress: List<Int>,
    override val winners: List<Int> = emptyList(),
) : GameState {
    fun target(player: Int): Int = CLOCK_TARGETS[progress[player].coerceAtMost(CLOCK_TARGETS.lastIndex)]
}

class AroundTheClockRules(private val config: AroundTheClockConfig) : GameRules<ClockState>() {
    override fun initial(players: Int) = ClockState(TurnState(), List(players) { 0 })

    override fun throwDart(state: ClockState, dart: Dart): ClockState {
        val turn = state.turn.add(dart)
        val me = turn.current
        val progress = state.progress.toMutableList()
        if (!dart.isMiss && dart.segment == state.target(me)) {
            val steps = if (config.multipliersSkip && dart.segment != Dart.BULL) dart.multiplier else 1
            // Bij overslaan nooit voorbij de bull: die moet je altijd zelf raken.
            progress[me] = if (dart.segment == Dart.BULL) CLOCK_TARGETS.size
            else (progress[me] + steps).coerceAtMost(CLOCK_TARGETS.lastIndex)
        }
        if (progress[me] >= CLOCK_TARGETS.size) {
            return state.copy(turn = turn, progress = progress, winners = listOf(me))
        }
        return state.copy(turn = if (turn.isFull) turn.next(progress.size) else turn, progress = progress)
    }
}

// ---------------------------------------------------------------------------------------------
// Shanghai: in ronde 1 telt alleen de 1, in ronde 2 alleen de 2, enzovoort. Een "Shanghai"
// (enkel, dubbel en triple van het rondenummer in één beurt) wint direct. Anders wint na de
// laatste ronde de hoogste score.
// ---------------------------------------------------------------------------------------------

data class ShanghaiState(
    override val turn: TurnState,
    val scores: List<Int>,
    val rounds: Int,
    override val winners: List<Int> = emptyList(),
    /** True als het spel door een Shanghai is gewonnen. */
    val shanghai: Boolean = false,
) : GameState {
    val target: Int get() = turn.round.coerceAtMost(rounds)
}

class ShanghaiRules(private val config: ShanghaiConfig) : GameRules<ShanghaiState>() {
    override fun initial(players: Int) = ShanghaiState(TurnState(), List(players) { 0 }, config.rounds)

    override fun throwDart(state: ShanghaiState, dart: Dart): ShanghaiState {
        val turn = state.turn.add(dart)
        val me = turn.current
        val target = state.target
        val scores = state.scores.toMutableList()
        if (dart.segment == target) scores[me] += dart.score

        if (!turn.isFull) return state.copy(turn = turn, scores = scores)

        val hits = turn.darts.filter { it.segment == target }.map { it.multiplier }.toSet()
        if (hits == setOf(1, 2, 3)) {
            return state.copy(turn = turn, scores = scores, winners = listOf(me), shanghai = true)
        }
        val next = turn.next(scores.size)
        if (next.round > config.rounds) {
            val best = scores.max()
            return state.copy(turn = turn, scores = scores, winners = scores.indices.filter { scores[it] == best })
        }
        return state.copy(turn = next, scores = scores)
    }
}

// ---------------------------------------------------------------------------------------------
// Killer: iedereen heeft een eigen nummer. Raak de dubbel van je eigen nummer om "killer" te worden.
// Als killer kost elke dubbel van een tegenstander hem een leven (je eigen dubbel kost jou er één).
// Wie als laatste overblijft, wint.
// ---------------------------------------------------------------------------------------------

data class KillerPlayer(val number: Int, val lives: Int, val isKiller: Boolean = false) {
    val isAlive: Boolean get() = lives > 0
}

data class KillerState(
    override val turn: TurnState,
    val players: List<KillerPlayer>,
    override val winners: List<Int> = emptyList(),
) : GameState

class KillerRules(private val config: KillerConfig) : GameRules<KillerState>() {
    override fun initial(players: Int) = KillerState(
        TurnState(),
        List(players) { KillerPlayer(number = config.numbers.getOrElse(it) { it + 1 }, lives = config.lives) },
    )

    override fun throwDart(state: KillerState, dart: Dart): KillerState {
        val turn = state.turn.add(dart)
        val me = turn.current
        val players = state.players.toMutableList()
        val target = if (dart.isDouble) players.indexOfFirst { it.number == dart.segment && it.isAlive } else -1
        if (target == me) {
            val p = players[me]
            players[me] = if (p.isKiller) p.copy(lives = p.lives - 1) else p.copy(isKiller = true)
        } else if (target >= 0 && players[me].isKiller) {
            players[target] = players[target].copy(lives = players[target].lives - 1)
        }

        val alive = players.indices.filter { players[it].isAlive }
        if (alive.size <= 1) {
            return state.copy(turn = turn, players = players, winners = alive.ifEmpty { listOf(me) })
        }
        val turnOver = turn.isFull || !players[me].isAlive
        return state.copy(
            turn = if (turnOver) turn.next(players.size) { players[it].isAlive } else turn,
            players = players,
        )
    }
}
