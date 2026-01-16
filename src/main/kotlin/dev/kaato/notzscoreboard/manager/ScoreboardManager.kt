package dev.kaato.notzscoreboard.manager

import dev.kaato.notzapi.utils.MessageU.Companion.c
import dev.kaato.notzapi.utils.MessageU.Companion.join
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.messageU
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.placeholderManager
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.plugin
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.database.DatabaseManager.loadScoreboardsDB
import dev.kaato.notzscoreboard.entities.ScoreboardE
import dev.kaato.notzscoreboard.manager.PlayerManager.checkPlayer
import dev.kaato.notzscoreboard.manager.PlayerManager.initializePlayers
import dev.kaato.notzscoreboard.manager.PlayerManager.loadPlayers
import dev.kaato.notzscoreboard.manager.PlayerManager.players
import me.clip.placeholderapi.PlaceholderAPI.setPlaceholders
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

object ScoreboardManager {
    val blacklist = arrayOf("create", "delete", "remove", "list", "null", "players", "reload")
    val scoreboards = hashMapOf<String, ScoreboardE>()
    private val templates = hashMapOf<String, List<String>>()
    private val staffStatus = hashMapOf<Boolean, List<String>>()
    var default_group: String
    private var priorityList = hashMapOf<String, PriorityClass>()

    data class PriorityClass(var task: BukkitTask?, var time: Long)

    // -------------------

    init {
        default_group = sf.config.getString("default_group") ?: "player"
    }

    fun getDefaultScoreboard(): ScoreboardE? = scoreboards.values.find { it.isDefault() }
    fun getDefaultScoreboardId(): Int = getDefaultScoreboard()?.id ?: 0

    // scoreboard - start

    fun getScoreboardByID(id: Int): ScoreboardE? {
        return scoreboards.filterValues { it.id == id }.values.firstOrNull()
    }

    fun getScoreboard(name: String): ScoreboardE? {
        return scoreboards.filterKeys { it == name }.values.firstOrNull()
    }

    fun containScoreboard(id: Int): Boolean {
        return scoreboards.filterValues { it.id == id }.isNotEmpty()
    }

    fun createScoreboard(name: String, display: String, player: Player? = null): Boolean {
        return if (!scoreboards.containsKey(name)) {
            val scoreboard = ScoreboardE(name, display)
            scoreboards[name] = scoreboard

            if (name == (getDefaultScoreboard()?.name ?: "")) scoreboard.setDefault(true)
            if (player != null) {
                addPlayerTo(player, player, name)
                messageU.send(player, "createScoreboard", display)
            }

            true
        } else false
    }

    fun deleteScoreboard(scoreboard: String): Boolean? {
        return if (scoreboards.contains(scoreboard)) {
            if (scoreboard != default_group) {
                val score = scoreboards[scoreboard]!!
                score.getPlayers().forEach { checkPlayer(it, isDefault = score.isDefault()) }
                score.delete()
                scoreboards.remove(scoreboard)

                true
            } else null
        } else false
    }

    fun viewScoreboard(player: Player, scoreboard: String) {
        if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.getScoreboard(player)
            messageU.send(player, "viewScoreboard1", display(scoreboard))

        } else messageU.send(player, "viewScoreboard2")
    }

    fun pauseScoreboard(player: Player, scoreboard: String, minutes: Int = 1) {
        if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.pauseTask(minutes)
            messageU.send(player, "pauseScoreboard1", defaults = listOf(display(scoreboard), minutes.toString(), if (minutes > 1) "s" else ""))

        } else messageU.send(player, "pauseScoreboard2")
    }

    fun addPlayerTo(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addPlayer(player)) {
            messageU.send(sender, "addPlayerTo1", defaults = listOf(score.getDisplay(), player.name))
            checkPlayer(player, score)

            if (scoreboards[default_group]!!.getVisibleGroups().contains(scoreboard))
                scoreboards[default_group]!!.update()

        } else messageU.send(sender, "addPlayerTo2", player.name)
    }

    fun remPlayerFrom(sender: Player, player: Player, scoreboard: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remPlayer(player)) {
            messageU.send(sender, "remPlayerFrom1", defaults = listOf(score.getDisplay(), player.name))
            checkPlayer(player, isDefault = score.isDefault())

        } else messageU.send(sender, "remPlayerFrom2", defaults = listOf(player.name, if (players.containsKey(player.uniqueId)) players[player.uniqueId]!!.name else default_group))
    }

    fun addGroupTo(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.addGroup(group))
            messageU.send(player, "addGroupTo1", defaults = listOf(display(group), score.getDisplay()))
        else messageU.send(player, "addGroupTo2", defaults = listOf(display(group), score.getDisplay()))
    }

    fun remGroupFrom(player: Player, scoreboard: String, group: String) {
        val score = scoreboards[scoreboard]!!

        if (score.remGroup(group))
            messageU.send(player, "remGroupFrom1", defaults = listOf(display(group), score.getDisplay()))
        else messageU.send(player, "remGroupFrom2", defaults = listOf(display(group), score.getDisplay()))
    }

    fun setDisplay(player: Player, scoreboard: String, display: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getDisplay()

        if (display == temp) {
            score.setDisplay(display)
            messageU.send(player, "setDisplay1", defaults = listOf(scoreboard, temp, display))

        } else messageU.send(player, "setDisplay2", scoreboard)
    }

    fun setTemplate(scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setTemplate(player: Player, scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        val score = scoreboards[scoreboard]!!

        if (header != null) {
            if (header != score.getHeader())
                messageU.send(player, "setTemplate1", defaults = listOf("header", score.getDisplay(), score.getHeader(), header))
            else messageU.send(player, "setTemplate2", defaults = listOf("header", score.getDisplay()))
        }

        if (template != null) {
            if (template != score.getTemplate())
                messageU.send(player, "setTemplate1", defaults = listOf("template", score.getDisplay(), score.getTemplate(), template))
            else messageU.send(player, "setTemplate2", defaults = listOf("template", score.getDisplay()))
        }

        if (footer != null) {
            if (footer != score.getFooter())
                messageU.send(player, "setTemplate1", defaults = listOf("footer", score.getDisplay(), score.getFooter(), footer))
            else messageU.send(player, "setTemplate2", defaults = listOf("footer", score.getDisplay()))
        }

        if (header == null && template == null && footer == null)
            messageU.send(player, "setTemplate3")

        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setColor(player: Player, scoreboard: String, color: String) {
        val score = scoreboards[scoreboard]!!
        val temp = score.getColor()

        if (color != temp) {
            score.setColor(color)
            player.sendMessage("$color $temp")
            messageU.send(player, "setColor1", defaults = listOf(display(scoreboard), join(temp.map { temp + it }, ""), join(color.map { color + it }, "")))
        } else messageU.send(player, "setColor2", score.getDisplay())

    }

    fun display(scoreboard: String): String {
        return scoreboards[scoreboard]!!.getDisplay()
    }

    fun seeVisibleGroups(player: Player, scoreboard: String = "") {
        messageU.sendHeader(player, join(scoreboards[scoreboard]!!.getVisibleGroups(), prefix = "&e ⧽ &f$scoreboard&e: &f", separator = "&e, &f") {
            if (it == scoreboard) "&a$it" else it
        })
    }

    // scoreboard - end
// -------------------
    // geral - start

    fun reload() {
        plugin.server.pluginManager.disablePlugin(plugin)
        plugin.server.pluginManager.enablePlugin(plugin)
    }

    fun updateAllScoreboards(p: Player) {
        scoreboards.values.forEach { it.update() }
        messageU.send(p, "updateAllScoreboards")
    }

    fun getTemplate(template: String, visibleGroups: List<String>? = null): List<String> {
        return if (templates.containsKey(template))
            templates[template]!!
        else if (template == "staff-status" && visibleGroups != null)
            staffStatus[checkVisibleGroups(visibleGroups)]!!
        else listOf(template)
    }

    fun getPlayerFromGroup(visibleGroups: List<String>): String {
        val playerList = getPlayersFromGroups(visibleGroups)
        return playerList[Random.nextInt(playerList.size)].name
    }

    fun getPlayersFromGroups(visibleGroups: List<String>): List<Player> {
        return scoreboards.filterValues { visibleGroups.contains(it.name) }.flatMap {
            it.value.getPlayers()
        }
    }

    private fun checkVisibleGroups(visibleGroups: List<String>): Boolean {
        return visibleGroups.map { scoreboards.values.find { sb -> sb.name == it } }.any { it != null && it.getPlayers().isNotEmpty() }
    }

    fun checkVisibleGroupsBy(scoreboard: String) {
        if (scoreboard != default_group)
            scoreboards.values.forEach { if (it.getVisibleGroups().contains(scoreboard)) it.update() }
    }

    fun shutdown() {
        scoreboards.values.forEach {
            it.forceCancelTask()
            it.shutdownSB()
        }
    }

    // geral - end
// -------------------
    // loaders - start

    fun load() {
        loadPlaceholders()

        val templatesConfig = sf.config.getMapList("templates")
        default_group = sf.config.getString("default-group") ?: ""
        arrayOf("low", "medium", "high").forEach { priorityList[it] = PriorityClass(null, sf.config.getLong("priority-time.$it") * 20) }

        templatesConfig.forEach { map ->
            map.forEach {
                val scoreLines = mutableListOf<String>()

                (it.value as List<*>).forEach { l -> scoreLines.add(l.toString()) }
                (it.value.toString())

                templates[it.key.toString()] = scoreLines
            }
        }

        staffStatus[true] = sf.config.getStringList("staff-status.online")
        staffStatus[false] = sf.config.getStringList("staff-status.offline")

        loadScoreboards()
    }

    private fun loadPlaceholders() {
        sf.config.getMapList("placeholders").flatMap { it.entries }.forEach { placeholderManager.addPlaceholder("{${it.key.toString()}}", it.value.toString()) }

        placeholderManager.addPlaceholders(
            hashMapOf(
                "{title}" to { sf.config.getString("title") ?: "[NotzScoreboard]" },

                "{rank}" to { p: Any? ->
                    var rank = "&7Sem rank."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null) {
                        val player = p as Player
                        rank = setPlaceholders(player, "%yrankup_rank_tag%") + setPlaceholders(player, "%yrankup_rank_name%")

                        if (rank.contains("5")) rank = setPlaceholders(player, "%yrankup_rank_tag%") + "&l" + setPlaceholders(player, "%yrankup_rank_name%")
                        else if (!rank.contains("I")) rank = "&8[$rank&8]"
                    }
                    rank
                },

                "{status_rankup}" to { p: Any? ->
                    var status = "&7Sem status."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("yPlugins") != null)
                        status = setPlaceholders(p as Player, "%yrankup_rank_tag%") + setPlaceholders(p, "%yrankup_progressbar%")

                    status
                },

                "{clan}" to { p: Any? ->
                    var clan = "&7Sem clan."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_clan_name%").isNotEmpty()) {
                        val pa = setPlaceholders(p, "%simpleclans_clan_name%")
                        clan = c(pa).toString()
                    }
                    clan
                },

                "{clan_tag}" to { p: Any? ->
                    var clan = "&7Sem clan."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        val pa = setPlaceholders(p, "%simpleclans_tag_label%")
                        clan = c(pa.substring(4, pa.length - 5)).toString()
                    }
                    clan
                },

                "{clankdr}" to { p: Any? ->
                    var kdr = "&7Sem KDR."

                    if (p != null && Bukkit.getServer().pluginManager.getPlugin("simpleclans") != null && setPlaceholders(p as Player, "%simpleclans_tag_label%").isNotEmpty()) {
                        kdr = setPlaceholders(p, "%simpleclans_kdr%")
                        if (kdr == "0") kdr = "&fSem KDR."
                    }
                    kdr
                }
            ))
    }

    private fun loadScoreboards() {
        val scores = loadScoreboardsDB()
        loadPlayers()

//        if (scores == null) {
////            messageU.send(Bukkit.getConsoleSender(), "&cError smanager1")
////            return
////        }

        if (scores.isNotEmpty())
            scores.forEach { scoreboards[it.name] = it }
        else {
            createScoreboard("player", "&e&lPlayer")
            createScoreboard("helper", "&e&lHelper")
            createScoreboard("trial", "&d&lTrial")
            createScoreboard("mod", "&2&lMod")
            createScoreboard("admin", "&c&lAdmin")
            createScoreboard("manager", "&4&lManager")
            createScoreboard("owner", "&6&lOwner")

            scoreboards["helper"]?.setTemplate("", "player", "staff")
            scoreboards["helper"]?.addGroup(mutableListOf("helper", "trial", "mod"))
            scoreboards["helper"]?.setColor("&e")
            scoreboards["trial"]?.setTemplate("", "player", "staff")
            scoreboards["trial"]?.addGroup(mutableListOf("helper", "trial", "mod"))
            scoreboards["trial"]?.setColor("&d")
            scoreboards["mod"]?.setTemplate("", "player", "modstaff")
            scoreboards["mod"]?.addGroup(mutableListOf("admin"))
            scoreboards["mod"]?.setColor("&2")
            scoreboards["admin"]?.setTemplate("supstaff", "admin", "modstaff")
            scoreboards["admin"]?.addGroup(mutableListOf("manager"))
            scoreboards["admin"]?.setColor("&c")
            scoreboards["manager"]?.setTemplate("supstaff", "manager", "")
            scoreboards["manager"]?.addGroup(mutableListOf("helper", "trial", "mod", "admin", "manager", "owner"))
            scoreboards["manager"]?.setColor("&4")
            scoreboards["owner"]?.setTemplate("supstaff", "owner", "")
            scoreboards["owner"]?.addGroup(mutableListOf("helper", "trial", "mod", "admin", "manager", "owner"))
            scoreboards["owner"]?.setColor("&6")
        }

        initializePlayers()
    }

    // loaders - end

    fun saveScoreboards() {
        scoreboards.values.forEach { it.databaseUpdate() }
    }
// -------------------
}