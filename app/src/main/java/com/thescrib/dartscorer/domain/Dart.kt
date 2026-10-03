package com.thescrib.dartscorer.domain

/**
 * Eén geworpen pijl. [segment] is 1–20, [BULL] (25) of 0 voor een misser;
 * [multiplier] is 1 (enkel), 2 (dubbel) of 3 (triple). Dubbel 25 is de bull (50 punten).
 */
data class Dart(val segment: Int, val multiplier: Int = 1) {
    init {
        require(segment == MISS_SEGMENT || segment in 1..20 || segment == BULL) { "Ongeldig segment $segment" }
        require(multiplier in 1..3) { "Ongeldige multiplier $multiplier" }
        require(!(segment == BULL && multiplier == 3)) { "Er bestaat geen triple bull" }
        require(!(segment == MISS_SEGMENT && multiplier != 1)) { "Een misser heeft geen multiplier" }
    }

    val score: Int get() = segment * multiplier
    val isMiss: Boolean get() = segment == MISS_SEGMENT
    val isDouble: Boolean get() = multiplier == 2 && !isMiss

    /** Korte notatie zoals op een scorebord: T20, D16, 25, BULL of een streepje voor een misser. */
    val label: String
        get() = when {
            isMiss -> "–"
            segment == BULL -> if (multiplier == 2) "BULL" else "25"
            multiplier == 3 -> "T$segment"
            multiplier == 2 -> "D$segment"
            else -> "$segment"
        }

    companion object {
        const val BULL = 25
        const val MISS_SEGMENT = 0
        val MISS = Dart(MISS_SEGMENT)

        /** Alle pijlen die punten kunnen opleveren (dus zonder misser). */
        val ALL: List<Dart> = buildList {
            for (m in 1..3) for (s in 1..20) add(Dart(s, m))
            add(Dart(BULL, 1))
            add(Dart(BULL, 2))
        }
    }
}

/** Maximaal aantal pijlen per beurt. */
const val DARTS_PER_TURN = 3
