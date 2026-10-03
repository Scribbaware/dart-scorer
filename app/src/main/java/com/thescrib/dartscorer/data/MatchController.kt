package com.thescrib.dartscorer.data

import com.thescrib.dartscorer.domain.Dart
import com.thescrib.dartscorer.domain.GameConfig
import com.thescrib.dartscorer.domain.Match
import com.thescrib.dartscorer.domain.MatchCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Houdt de lopende partij bij en bewaart hem na elke pijl, zodat je verder kunt waar je was,
 * ook als Android de app tussendoor afsluit.
 */
class MatchController(private val settings: SettingsRepository, private val scope: CoroutineScope) {
    private val _match = MutableStateFlow<Match?>(null)
    private val saveLock = Mutex()
    val match: StateFlow<Match?> = _match.asStateFlow()

    /** Laadt een bewaard spel; alleen als er nog geen spel in het geheugen staat. */
    suspend fun restore() {
        if (_match.value != null) return
        _match.value = settings.savedMatch()?.let(MatchCodec::decode)
    }

    fun start(config: GameConfig, players: List<String>) {
        update(Match(config, players))
        scope.launch { settings.setLastPlayers(players) }
    }

    fun throwDart(dart: Dart) = edit { it.throwDart(dart) }
    fun undo() = edit { it.undo() }
    fun restart() = edit { it.restart() }
    fun quit() = update(null)

    private fun edit(change: (Match) -> Match) {
        _match.value?.let { update(change(it)) }
    }

    private fun update(match: Match?) {
        _match.value = match
        // Altijd de nieuwste stand wegschrijven, ook als twee pijlen vlak na elkaar komen.
        scope.launch { saveLock.withLock { settings.setSavedMatch(_match.value?.let(MatchCodec::encode)) } }
    }
}
