package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.database.DatabaseManager.loadScoreboardsDB
import dev.kaato.notzscoreboard.entities.ScoreboardE
import dev.kaato.notzscoreboard.manager.PlayerManager.initializePlayers
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import kotlin.random.Random

object ScoreboardManager {
    val blacklist = arrayOf("create", "delete", "remove", "list", "null", "players", "reload")
    val scoreboards = hashMapOf<String, ScoreboardE>()
    val scoreboardsPlayers = hashMapOf<String, MutableList<String>>()
    private val templates = hashMapOf<String, List<String>>()
    private val staffStatus = hashMapOf<Boolean, List<String>>()
    var default_group: String
    private var priorityList = hashMapOf<String, PriorityClass>()

    data class PriorityClass(var task: BukkitTask?, var time: Long)

    // -------------------

    init {
        default_group = sf.config.getString("default_group") ?: "player"
    }

    // scoreboard - start

    fun getDefaultScoreboard(): ScoreboardE? = scoreboards.values.find { it.isDefault() }
    fun getDefaultScoreboardId(): Int = getDefaultScoreboard()?.id ?: 0

    fun isBlacklisted(name: String): Boolean = blacklist.contains(name)

    fun registerPlayerToScoreboard(player: Player): Boolean {
        val playerScores = scoreboards.values.filter {
            it.getPlayers().contains(player.uniqueId)
        }

        return if (playerScores.isEmpty())
            addPlayerTo(player, default_group)
        else addPlayerTo(player, playerScores.first())
    }

    fun unregisterPlayerFromScoreboard(player: Player): Boolean {
        return (scoreboards.values.find { it.getOnlinePlayers().contains(player.uniqueId) }?.let {
            scoreboardsPlayers[it.name]?.remove(player.name)
            it.remOnlinePlayer(player.uniqueId)
        }) ?: false
    }

    fun createScoreboard(name: String, display: String, player: Player? = null): Boolean {
        if (scoreboards.containsKey(name)) return false

        val scoreboard = ScoreboardE(name, display)
        scoreboards[name] = scoreboard
        scoreboardsPlayers[name] = mutableListOf()

        if (name == (getDefaultScoreboard()?.name ?: "")) scoreboard.setDefault(true)
        if (player != null) addPlayerTo(player, name)

        return true
    }

    fun deleteScoreboard(scoreboard: String): Boolean? {
        return if (scoreboards.contains(scoreboard)) {
            if (scoreboard != default_group) {
                val score = scoreboards[scoreboard]!!
                scoreboards.remove(scoreboard)
                scoreboardsPlayers.remove(scoreboard)
                score.delete()

                true
            } else null
        } else false
    }

    fun viewScoreboard(player: Player, scoreboard: String): Boolean {
        return if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.getScoreboard(player)
            true
        } else false
    }

    fun pauseScoreboard(scoreboard: String, minutes: Int = 1): Boolean {
        return if (scoreboards.contains(scoreboard)) {
            scoreboards[scoreboard]!!.pauseTask(minutes)
            true
        } else false
    }

    fun addPlayerTo(player: Player, scoreboard: String): Boolean {
        val score = scoreboards[scoreboard] ?: return false
        return addPlayerTo(player, score)
    }

    fun addPlayerTo(player: Player, scoreboard: ScoreboardE): Boolean {
        return if (scoreboard.addPlayer(player.uniqueId)) {
            remPlayerFromExcept(player, scoreboard.name)
            scoreboardsPlayers[scoreboard.name]?.add(player.name)
            true
        } else false
    }

    fun remPlayerFromExcept(player: Player, exceptScoreboard: String) {
        scoreboards.values.filter { it.name != exceptScoreboard && it.containsPlayer(player.uniqueId) }.forEach {
            it.remPlayer(player.uniqueId)
            scoreboardsPlayers[it.name]?.remove(player.name)
        }
    }

    fun addGroupTo(scoreboard: String, group: String): Boolean {
        val score = scoreboards[scoreboard]!!
        return score.addGroup(group)
    }

    fun remGroupFrom(scoreboard: String, group: String): Boolean {
        val score = scoreboards[scoreboard]!!
        return score.remGroup(group)
    }

    fun setDisplay(scoreboard: String, display: String): Boolean {
        val score = scoreboards[scoreboard]!!
        val temp = score.getDisplay()

        return if (display == temp) {
            score.setDisplay(display)
            true
        } else false
    }

    fun setTemplate(scoreboard: String, header: String? = null, template: String? = null, footer: String? = null) {
        scoreboards[scoreboard]!!.setTemplate(header, template, footer)
    }

    fun setColor(scoreboard: String, color: String): String {
        val score = scoreboards[scoreboard]!!

        return score.getColor().let {
            if (it != color) {
                score.setColor(color)
                it
            } else ""
        }
    }

    fun display(scoreboard: String): String {
        return scoreboards[scoreboard]!!.getDisplay()
    }

// scoreboard - end
// -------------------
// geral - start

    fun updateAllScoreboards() {
        scoreboards.values.forEach { it.updatePlayers() }
    }

    fun getTemplate(template: String, visibleGroups: List<String>? = null): List<String> {
        return if (templates.containsKey(template)) templates[template]!!
        else if (template == "staff-status" && visibleGroups != null) staffStatus[checkVisibleGroups(visibleGroups)]!!
        else listOf(template)
    }

    fun getPlayerFromGroup(visibleGroups: List<String>): String {
        val playerList = getPlayersFromGroups(visibleGroups)
        return playerList[Random.nextInt(playerList.size)]
    }

    fun getPlayersFromGroups(visibleGroups: List<String>): List<String> {
        return scoreboardsPlayers.filter { visibleGroups.contains(it.key) }.flatMap { it.value }
    }

    fun checkVisibleGroups(visibleGroups: List<String>): Boolean {
        return getPlayersFromGroups(visibleGroups).isNotEmpty()
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

    fun loadScoreboardManager() {
        default_group = sf.config.getString("default-group") ?: ""
        arrayOf("low", "medium", "high").forEach { priorityList[it] = PriorityClass(null, sf.config.getLong("priority-time.$it") * 20) }
        val templatesConfig = sf.config.getMapList("templates")

        templatesConfig.forEach { map ->
            map.forEach {
                val scoreLines = mutableListOf<String>()

                (it.value as List<*>).forEach { l -> scoreLines.add(l.toString()) }
//                (it.value.toString())

                templates[it.key.toString()] = scoreLines
            }
        }

        staffStatus[true] = sf.config.getStringList("staff-status.online")
        staffStatus[false] = sf.config.getStringList("staff-status.offline")

        loadScoreboards()
    }

    private fun loadScoreboards() {
        val scores = loadScoreboardsDB()

        if (scores.isNotEmpty()) scores.forEach { scoreboards[it.name] = it }
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
}