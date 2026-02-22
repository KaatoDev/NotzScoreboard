package dev.kaato.notzscoreboard.entities

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getTemplate
import java.time.LocalDateTime
import java.util.*

data class ScoreboardModel(
    val id: Int,
    val name: String,
    val display: String,
    val color: String,
    val header: String,
    val template: String,
    val footer: String,
    val visibleGroups: List<String>,
    val players: List<UUID>,
    val created: LocalDateTime,
    val updated: LocalDateTime?,
)