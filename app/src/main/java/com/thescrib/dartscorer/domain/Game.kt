package com.thescrib.dartscorer.domain

/**
 * Wie er aan de beurt is, de pijlen van de lopende beurt en de ronde.
 * Een ronde begint telkens weer bij [starter].
 */
data class TurnState(
    val current: Int = 0,
    val darts: List<Dart> = emptyList(),
    val round: Int = 1,
    val starter: Int = 0,
) {
    val isFull: Boolean get() = darts.size >= DARTS_PER_TURN
    val dartsLeft: Int get() = DARTS_PER_TURN - darts.size

    fun add(dart: Dart) = copy(darts = darts + dart)

    /** Geeft de beurt aan de volgende speler die nog meedoet volgens [active]. */
    fun next(players: Int, active: (Int) -> Boolean = { true }): TurnState {
        if ((0 until players).none(active)) return copy(darts = emptyList())
        var p = current
        var r = round
        do {
            p = (p + 1) % players
            if (p == starter) r++
        } while (!active(p))
        return TurnState(current = p, darts = emptyList(), round = r, starter = starter)
    }
}

/** De stand van een spel. Elk spel heeft zijn eigen spelersgegevens, maar deze velden delen ze allemaal. */
sealed interface GameState {
    val turn: TurnState

    /** De winnaar(s); leeg zolang het spel nog loopt. Bij Shanghai kan het gelijkspel zijn. */
    val winners: List<Int>

    val isOver: Boolean get() = winners.isNotEmpty()
}

/**
 * Spelregels als pure functie: de stand na een pijl hangt alleen af van de vorige stand en die pijl.
 * Zo kun je een spel altijd opnieuw afspelen vanaf de lijst pijlen, en is "ongedaan maken" simpelweg
 * de laatste pijl weglaten.
 */
abstract class GameRules<S : GameState> {
    abstract fun initial(players: Int): S
    abstract fun throwDart(state: S, dart: Dart): S

    fun replay(players: Int, darts: List<Dart>): S =
        darts.fold(initial(players)) { state, dart -> if (state.isOver) state else throwDart(state, dart) }
}

enum class GameType { X01, CRICKET, AROUND_THE_CLOCK, SHANGHAI, KILLER }

/** Instellingen van een spel; bepaalt welke regels gelden. */
sealed interface GameConfig {
    val type: GameType
    val rules: GameRules<*>
    val minPlayers: Int get() = 1
}

data class X01Config(
    val start: Int = 501,
    val doubleIn: Boolean = false,
    val doubleOut: Boolean = true,
    /** Aantal legs dat je moet winnen ("first to"). */
    val legs: Int = 1,
) : GameConfig {
    override val type get() = GameType.X01
    override val rules get() = X01Rules(this)
}

data object CricketConfig : GameConfig {
    override val type get() = GameType.CRICKET
    override val rules get() = CricketRules
}

data class AroundTheClockConfig(
    /** Dubbel telt voor twee vakjes vooruit, triple voor drie. */
    val multipliersSkip: Boolean = false,
) : GameConfig {
    override val type get() = GameType.AROUND_THE_CLOCK
    override val rules get() = AroundTheClockRules(this)
}

data class ShanghaiConfig(val rounds: Int = 7) : GameConfig {
    override val type get() = GameType.SHANGHAI
    override val rules get() = ShanghaiRules(this)
}

data class KillerConfig(
    val lives: Int = 3,
    /** Het nummer van elke speler, in spelersvolgorde. */
    val numbers: List<Int>,
) : GameConfig {
    override val type get() = GameType.KILLER
    override val rules get() = KillerRules(this)
    override val minPlayers get() = 2

    companion object {
        /** Geeft elke speler een eigen willekeurig nummer van 1 tot en met 20. */
        fun random(players: Int, lives: Int = 3, random: kotlin.random.Random = kotlin.random.Random) =
            KillerConfig(lives, (1..20).shuffled(random).take(players))
    }
}

/**
 * Een partij: de instellingen, de spelers en alle pijlen tot nu toe.
 * De stand wordt afgeleid door de pijlen opnieuw af te spelen.
 */
data class Match(
    val config: GameConfig,
    val players: List<String>,
    val darts: List<Dart> = emptyList(),
) {
    val state: GameState by lazy { config.rules.replay(players.size, darts) }

    val canUndo: Boolean get() = darts.isNotEmpty()

    fun throwDart(dart: Dart): Match = if (state.isOver) this else copy(darts = darts + dart)

    fun undo(): Match = if (darts.isEmpty()) this else copy(darts = darts.dropLast(1))

    /** Zelfde spelers en instellingen, opnieuw beginnen. */
    fun restart(): Match = copy(darts = emptyList())
}
