package com.thescrib.dartscorer.domain

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Zet een partij om naar tekst en terug, zodat een lopend spel bewaard blijft als Android de app
 * afsluit. Drie regels: instellingen, spelers (URL-gecodeerd) en de pijlen als "segment:multiplier".
 */
object MatchCodec {
    private const val UTF8 = "UTF-8"

    fun encode(match: Match): String = listOf(
        encodeConfig(match.config),
        match.players.joinToString(",") { URLEncoder.encode(it, UTF8) },
        match.darts.joinToString(",") { "${it.segment}:${it.multiplier}" },
    ).joinToString("\n")

    /** Geeft null als de tekst niet (meer) te lezen is, bijvoorbeeld na een update met een ander formaat. */
    fun decode(text: String): Match? = runCatching {
        val lines = text.split("\n")
        val config = decodeConfig(lines[0])
        val players = lines[1].split(",").map { URLDecoder.decode(it, UTF8) }
        val darts = lines.getOrNull(2).orEmpty().split(",").filter { it.isNotBlank() }.map {
            val (s, m) = it.split(":")
            Dart(s.toInt(), m.toInt())
        }
        if (players.size < config.minPlayers) null else Match(config, players, darts)
    }.getOrNull()

    private fun encodeConfig(config: GameConfig): String = when (config) {
        is X01Config -> "X01|${config.start}|${config.doubleIn.bit}|${config.doubleOut.bit}|${config.legs}"
        CricketConfig -> "CRICKET"
        is AroundTheClockConfig -> "CLOCK|${config.multipliersSkip.bit}"
        is ShanghaiConfig -> "SHANGHAI|${config.rounds}"
        is KillerConfig -> "KILLER|${config.lives}|${config.numbers.joinToString(";")}"
    }

    private fun decodeConfig(line: String): GameConfig {
        val f = line.split("|")
        return when (f[0]) {
            "X01" -> X01Config(f[1].toInt(), f[2] == "1", f[3] == "1", f[4].toInt())
            "CRICKET" -> CricketConfig
            "CLOCK" -> AroundTheClockConfig(f[1] == "1")
            "SHANGHAI" -> ShanghaiConfig(f[1].toInt())
            "KILLER" -> KillerConfig(f[1].toInt(), f[2].split(";").map { it.toInt() })
            else -> error("Onbekend spel ${f[0]}")
        }
    }

    private val Boolean.bit get() = if (this) "1" else "0"
}
