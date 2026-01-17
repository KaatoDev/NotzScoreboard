package dev.kaato.notzscoreboard.commands

import dev.kaato.notzscoreboard.manager.CommandsManager.addGroupToCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.addPlayerToCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.clearfooterCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.clearheaderCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.cleartemplateCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.createScoreboardCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.deleteScoreboardCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.pauseScoreboardCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.remGroupFromCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.remPlayerFromCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.resetPlayerCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.scoreboardListCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.seePlayersCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.seeVisibleGroupsCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.setColorCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.setDisplayCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.setTemplateCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.updateAllScoreboardsCMD
import dev.kaato.notzscoreboard.manager.CommandsManager.viewScoreboardCMD
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboards
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboardsPlayers
import dev.kaato.notzscoreboard.utils.MessageUtil.getMessage
import dev.kaato.notzscoreboard.utils.MessageUtil.send
import dev.kaato.notzscoreboard.utils.MessageUtil.sendHeader
import dev.kaato.notzscoreboard.utils.OthersUtil.isntAdmin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*

class NScoreboardC : TabExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return false

        val player: Player = sender

        if (isntAdmin(player)) return true

        val a = args.map { it.lowercase() }
        val scoreboard = if (a.isNotEmpty() && scoreboards.containsKey(a[0])) a[0] else null

        when (a.size) {
            1 -> if (scoreboard == null) when (a[0]) {
                "list" -> scoreboardListCMD(player)
                "players" -> seePlayersCMD(player)
                "update" -> updateAllScoreboardsCMD(player)
                else -> help(player)

            } else help(player, scoreboard)

            2 -> if (scoreboard != null) when (a[1]) {
                "clearheader" -> clearheaderCMD(player, scoreboard)
                "clearfooter" -> clearfooterCMD(player, scoreboard)
                "cleartemplate" -> cleartemplateCMD(player, scoreboard)
                "pause" -> pauseScoreboardCMD(player, scoreboard)
                "players" -> seePlayersCMD(player, a[0])
                "view" -> viewScoreboardCMD(player, scoreboard)
                "visiblegroups" -> seeVisibleGroupsCMD(player, scoreboard)
                else -> help(player, scoreboard)

            } else when (a[0]) {
                "delete" -> deleteScoreboardCMD(player, a[1])
                "reset" -> resetPlayerCMD(player, a[1])
                "set" -> addPlayerToCMD(player, player.name, a[1])
                else -> help(player)
            }

            3 -> if (a[0] == "create") {
                createScoreboardCMD(player, a[1], args[2])
            } else if (scoreboard != null) when (a[1]) {
                "addplayer" -> addPlayerToCMD(player, a[2], scoreboard)
                "addgroup" -> addGroupToCMD(player, scoreboard, a[2])
                "pause" -> pauseScoreboardCMD(player, scoreboard, a[2])
                "remplayer" -> remPlayerFromCMD(player, a[2], scoreboard)
                "remgroup" -> remGroupFromCMD(player, scoreboard, a[2])
                "setcolor" -> setColorCMD(player, scoreboard, a[2])
                "setdisplay" -> setDisplayCMD(player, scoreboard, args[2])
                "setheader" -> setTemplateCMD(player, scoreboard, a[2])
                "setfooter" -> setTemplateCMD(player, scoreboard, footer = a[2])
                "settemplate" -> setTemplateCMD(player, scoreboard, template = a[2])

            } else help(player)

            4, 5, 6 -> if (a[0] == "create") createScoreboardCMD(player, a[1], args[2], a[3], a[4], a[5]) else helpCreate(player)
            else -> help(player, scoreboard)
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): MutableList<String> {
        val a = args.map { it.lowercase() }
        val scoreboard = if (a.isNotEmpty()) scoreboards.containsKey(a[0]) else false

        return when (a.size) {
            1 -> arrayOf("create", "delete", "list", "players", "reload", "reset", "set", "update").filter { it.contains(a[0]) }.toMutableList()

            2 -> if (scoreboard) arrayOf("addplayer", "addgroup", "clearheader", "clearfooter", "cleartemplate", "pause", "players", "remplayer", "remgroup", "setcolor", "setdisplay", "setheader", "setfooter", "settemplate", "view", "visiblegroups").filter { it.contains(a[1]) }.toMutableList() else when (a[0]) {
                "create" -> mutableListOf("<name>")
                "reset" -> scoreboardsPlayers.filter { it.key != default_group }.values.flatten().toMutableList()
                "set" -> scoreboards.keys.toMutableList()
                else -> Collections.emptyList()
            }

            3 -> if (scoreboard) when (a[1]) {
                "addplayer" -> scoreboardsPlayers.filter { it.key != a[0] }.values.flatten().toMutableList()
                "remplayer" -> scoreboardsPlayers.filter { it.key == a[0] }.values.flatten().toMutableList()
                "addgroup", "remgroup" -> scoreboards.keys.toMutableList()
                else -> Collections.emptyList()
            } else if (a[0] == "create") mutableListOf("<display>") else Collections.emptyList()


            4 -> if (a[0] == "create") mutableListOf("<header>") else Collections.emptyList()
            5 -> if (a[0] == "create") mutableListOf("<template>") else Collections.emptyList()
            6 -> if (a[0] == "create") mutableListOf("<footer>") else Collections.emptyList()


            else -> Collections.emptyList()
        }
    }

    private fun helpCreate(player: Player) {
        send(player, "&eUse &f/&enotzsb &ecreate &f<&ename&f> &f<&edisplay&f> (&eheader&f) (&etemplate&f) (&efooter&f)")
    }

    /**
     * @param player Player.
     * @param scoreboard Scoreboard.
     *
     * Send the commands' instructions to the player.
     */
    private fun help(player: Player, scoreboard: String? = null) {
        if (scoreboard == null) sendHeader(
            player, """
                ${getMessage("commands.notzscoreboard")} &f/&enotzscoreboard &7+
                &7+ &ecreate &f<&ename&f> &f<&edisplay&f> (&eheader&f) (&etemplate&f) (&efooter&f) &7- ${getMessage("commands.create")}
                &7+ &edelete &f<&escoreboard&f> &7- ${getMessage("commands.delete")}
                &7+ &elist &7- ${getMessage("commands.list")}
                &7+ &eplayers &7- ${getMessage("commands.players")}
                &7+ &ereload &7- ${getMessage("commands.reload")}
                &7+ &ereset &f<&eplayer&f> &7- ${getMessage("commands.reset")}
                &7+ &eset &f<&escoreboard&f> &7- ${getMessage("commands.set")}
                &7+ &eupdate &7- ${getMessage("commands.update")}
            """.trimIndent()
        )
        else sendHeader(
            player, """
            &f/&enotzsb &a${scoreboard} &7+
            &7+ &eaddplayer &f<&eplayer&f> &7- ${getMessage("commands.scoreboard.addplayer")}
            &7+ &eaddgroup &f<&egroup&f> &7- ${getMessage("commands.scoreboard.addgroup")}
            &7+ &eclearheader &7- ${getMessage("commands.scoreboard.clearheader")}
            &7+ &eclearfooter &7- ${getMessage("commands.scoreboard.clearfooter")}
            &7+ &ecleartemplate &7- ${getMessage("commands.scoreboard.cleartemplate")}
            &7+ &epause &f(&eminutes&f) &7- ${getMessage("commands.scoreboard.pause")}
            &7+ &eplayers &7- ${getMessage("commands.scoreboard.players")}
            &7+ &eremplayer &f<&eplayer&f> &7- ${getMessage("commands.scoreboard.remplayer")}
            &7+ &eremgroup &f<&egroup&f> &7- ${getMessage("commands.scoreboard.remgroup")}
            &7+ &esetcolor &f<&ecolor&f> &7- ${getMessage("commands.scoreboard.setcolor")}
            &7+ &esetdisplay &f<&edisplay&f> &7- ${getMessage("commands.scoreboard.setdisplay")}
            &7+ &esetheader &f<&etemplate&f> &7- ${getMessage("commands.scoreboard.setheader")}
            &7+ &esetfooter &f<&etemplate&f> &7- ${getMessage("commands.scoreboard.setfooter")}
            &7+ &esettemplate &f<&etemplate&f> &7- ${getMessage("commands.scoreboard.settemplate")}
            &7+ &eview &7- ${getMessage("commands.scoreboard.view")}
            &7+ &evisiblegroups &7- ${getMessage("commands.scoreboard.visiblegroups")}
        """.trimIndent()
        )
    }
}