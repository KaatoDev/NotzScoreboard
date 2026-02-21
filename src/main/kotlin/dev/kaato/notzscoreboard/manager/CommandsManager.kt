package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.manager.PlayerManager.resetPlayer
import dev.kaato.notzscoreboard.manager.ScoreboardManager.addGroupTo
import dev.kaato.notzscoreboard.manager.ScoreboardManager.addPlayerTo
import dev.kaato.notzscoreboard.manager.ScoreboardManager.createScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.deleteScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.display
import dev.kaato.notzscoreboard.manager.ScoreboardManager.isBlacklisted
import dev.kaato.notzscoreboard.manager.ScoreboardManager.pauseScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.remGroupFrom
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboards
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setColor
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setDisplay
import dev.kaato.notzscoreboard.manager.ScoreboardManager.setTemplate
import dev.kaato.notzscoreboard.manager.ScoreboardManager.updateAllScoreboards
import dev.kaato.notzscoreboard.manager.ScoreboardManager.viewScoreboard
import dev.kaato.notzscoreboard.utils.MessageUtil.join
import dev.kaato.notzscoreboard.utils.MessageUtil.send
import dev.kaato.notzscoreboard.utils.MessageUtil.sendHeader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.text.ParseException

object CommandsManager {
    //    fun createScoreboardCMD(player: Player, name: String, display: String, header: String? = null, template: String? = null, footer: String? = null) {
    fun createScoreboardCMD(player: Player, name: String, display: String, header: String? = null, template: String? = null, footer: String? = null) {
        if (!isBlacklisted(name)) {
            if (createScoreboard(name, display, player)) {
                setTemplateCMD(player, name, header, template, footer)
                send(player, "createScoreboard", name)
            } else send(player, "create1", name)
        } else send(player, "create2")
    }

    fun deleteScoreboardCMD(player: Player, scoreboard: String) {
        when (deleteScoreboard(scoreboard)) {
            true -> send(player, "delete1", scoreboard)
            false -> send(player, "delete2")
            null -> send(player, "delete3", scoreboard)
        }
    }

    fun viewScoreboardCMD(player: Player, scoreboard: String) {
        if (viewScoreboard(player, scoreboard)) send(player, "viewScoreboard", display(scoreboard))
        else send(player, "notFound2")
    }

    fun pauseScoreboardCMD(player: Player, scoreboard: String, minutes: String = "1") {
        try {
            if (pauseScoreboard(scoreboard, minutes.toInt())) send(player, "pauseScoreboard", defaults = listOf(display(scoreboard), minutes, if (minutes.toInt() > 1) "s" else ""))
            else send(player, "notFound2")
        } catch (e: ParseException) {
            send(player, "pause")
        }
    }

    fun addPlayerToCMD(sender: Player, target: String, scoreboard: String) {
        val player = Bukkit.getPlayerExact(target)
        if (player == null) send(sender, "notFound2")
        else if (scoreboards.containsKey(scoreboard)) {
            if (addPlayerTo(player, scoreboard)) send(sender, "addPlayerTo1", defaults = listOf(display(scoreboard), player.name))
            else send(sender, "addPlayerTo2", player.name)
        } else send(player, "notFound1")
    }

    fun addGroupToCMD(player: Player, scoreboard: String, group: String) {
        if (!scoreboards.containsKey(group)) send(player, "notFound3")
        else if (addGroupTo(scoreboard, group)) send(player, "addGroupTo1", defaults = listOf(display(group), display(scoreboard)))
        else send(player, "addGroupTo2", defaults = listOf(display(group), display(scoreboard)))
    }

    fun remPlayerFromCMD(sender: Player, target: String, scoreboard: String) {
        val player = Bukkit.getPlayerExact(target)
        if (player == null) send(sender, "notFound1")
        else resetPlayerCMD(sender, target)

    }

    fun remGroupFromCMD(player: Player, scoreboard: String, group: String) {
        if (!scoreboards.containsKey(group)) send(player, "notFound3")
        else if (remGroupFrom(scoreboard, group)) send(player, "remGroupFrom1", defaults = listOf(display(group), display(scoreboard)))
        else send(player, "remGroupFrom2", defaults = listOf(display(group), display(scoreboard)))
    }

    fun setDisplayCMD(player: Player, scoreboard: String, display: String) {
        val oldDisplay = display(scoreboard)
        if (setDisplay(scoreboard, display)) send(player, "setDisplay1", defaults = listOf(scoreboard, oldDisplay, display))
        else send(player, "setDisplay2", scoreboard)
    }

    fun setTemplateCMD(player: Player, scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        val score = scoreboards[scoreboard]!!

        if (header != null) {
            if (header != score.getHeader()) send(player, "setTemplate1", defaults = listOf("header", score.getDisplay(), score.getHeader(), header))
            else send(player, "setTemplate2", defaults = listOf("header", score.getDisplay()))
        }

        if (template != null) {
            if (template != score.getTemplate()) send(player, "setTemplate1", defaults = listOf("template", score.getDisplay(), score.getTemplate(), template))
            else send(player, "setTemplate2", defaults = listOf("template", score.getDisplay()))
        }

        if (footer != null) {
            if (footer != score.getFooter()) send(player, "setTemplate1", defaults = listOf("footer", score.getDisplay(), score.getFooter(), footer))
            else send(player, "setTemplate2", defaults = listOf("footer", score.getDisplay()))
        }

//        if (header == null && template == null && footer == null) send(player, "setTemplate3")

        setTemplate(scoreboard, header, template, footer)
    }

    fun setColorCMD(player: Player, scoreboard: String, color: String) {
        setColor(scoreboard, color).let {
            if (color.isNotBlank()) send(player, "setColor1", defaults = listOf(display(scoreboard), join(it.map { txt -> it + txt }, ""), join(color.map { txt -> color + txt }, "")))
            else send(player, "setColor2", display(scoreboard))
        }
    }

    fun seeVisibleGroupsCMD(player: Player, scoreboard: String = "") {
        sendHeader(player, join(scoreboards[scoreboard]!!.getVisibleGroups(), prefix = "&e ⧽ &f$scoreboard&e: &f", separator = "&e, &f") {
            if (it == scoreboard) "&a$it" else it
        })
    }

// scoreboard - end
// -------------------
// geral - start

    fun updateAllScoreboardsCMD(player: Player) {
        updateAllScoreboards()
        send(player, "updateAllScoreboards")
    }

    fun resetPlayerCMD(sender: Player, target: String) {
        val player = Bukkit.getPlayerExact(target)

        if (player == null) send(sender, "notFound")
        else if (resetPlayer(player)) send(sender, "resetPlayer1", player.name)
        else send(sender, "resetPlayer2", player.name)
    }

    fun seePlayersCMD(player: Player, scoreboard: String = "") {
        val all = scoreboard.isBlank()

        val scores = scoreboards.values.filter { (if (all) it.name != default_group else it.name == scoreboard) }

        if (scores.isNotEmpty()) {
            val scorePlayers = mutableListOf<String>()

            scores.forEach {
                val pls = it.getPlayers().map { uid -> Bukkit.getPlayer(uid)?.name ?: "null" }
                scorePlayers.add("&f${it.getDisplay()}&e: &f${join(pls)}")
            }

            sendHeader(player, join(scorePlayers.toList(), separator = "\n"))

        } else send(player, "seePlayers")
    }

    fun scoreboardListCMD(player: Player) {
        sendHeader(
            player, "&6⧽ &eScoreboards:\n" + join(scoreboards.values.mapIndexed { index, it ->
                val str = if (scoreboards.size == 1) "⧽"
                else if (index == 0) "⎧"
                else if (index == scoreboards.size - 1) "⎩"
                else "⎜"
                "&e$str &f${it.name}&e: &f${it.getDisplay()}\n"
            }, separator = "")
        )
    }

    fun clearheaderCMD(player: Player, scoreboard: String) {
        setTemplateCMD(player, scoreboard, "")
        send(player, "clearHeader", display(scoreboard))
    }

    fun clearfooterCMD(player: Player, scoreboard: String) {
        setTemplateCMD(player, scoreboard, footer = "")
        send(player, "clearFooter", display(scoreboard))
    }

    fun cleartemplateCMD(player: Player, scoreboard: String) {
        setTemplateCMD(player, scoreboard, template = "")
        send(player, "clearTemplate", display(scoreboard))
    }
}