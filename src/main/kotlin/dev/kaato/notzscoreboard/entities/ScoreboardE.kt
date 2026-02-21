@file:Suppress("DEPRECATION")

package dev.kaato.notzscoreboard.entities

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.plugin
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.database.DatabaseManager.deleteScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.getScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.insertScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.updateScoreboardDB
import dev.kaato.notzscoreboard.manager.AnimationManager.getAnimation
import dev.kaato.notzscoreboard.manager.AnimationManager.hasAnimation
import dev.kaato.notzscoreboard.manager.MessageManager.getMessage
import dev.kaato.notzscoreboard.manager.PlaceholderManager.addPlaceholder
import dev.kaato.notzscoreboard.manager.PlaceholderManager.getPlaceholder
import dev.kaato.notzscoreboard.manager.PlaceholderManager.hasPlaceholderRegex
import dev.kaato.notzscoreboard.manager.PlaceholderManager.placeholderRegex
import dev.kaato.notzscoreboard.manager.PlayerManager.checkPlayerVersion
import dev.kaato.notzscoreboard.manager.ScoreboardManager
import dev.kaato.notzscoreboard.manager.ScoreboardManager.checkVisibleGroups
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getPlayerFromGroup
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getPlayersFromGroups
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getStaffStatus
import dev.kaato.notzscoreboard.manager.ScoreboardManager.multilineTime
import dev.kaato.notzscoreboard.utils.MessageUtil.c
import dev.kaato.notzscoreboard.utils.MessageUtil.join
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import dev.kaato.notzscoreboard.utils.MessageUtil.set
import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

/**
 * @param name Unique name to be used in commands.
 * @param display Displayname that will appear on messages.
 * @param header Header template of the scoreboard.
 * @param template Main template of the scoreboard.
 * @param footer Footer template of the scoreboard.
 * @param color The color that will be set at the start of each line.
 * @param visibleGroups List of scoreboard groups that's used for the {staff} placeholder.
 */
class ScoreboardE(val id: Int) {
    /**
     * @param name Unique name to be used in commands.
     * @param display Displayname that will appear on messages.
     */
    constructor(name: String, display: String, color: String = "&e", header: String = "", template: String = "player", footer: String = "staff-status", visibleGroups: MutableList<String> = mutableListOf(), players: MutableList<UUID> = mutableListOf()) : this(insertScoreboardDB(name, display, color, header, template, footer, visibleGroups, players))

    val name: String
    private var display: String
    private var header: String
    private var template: String
    private var footer: String
    private var color: String
    private val visibleGroups = mutableListOf<String>()
    private var players = mutableListOf<UUID>()
    val created: LocalDateTime
    private var updated: LocalDateTime?

    private var lines = mutableListOf<String>()
    private var suffixLines = hashMapOf<String, String>()
    private var onlinePlayers = mutableListOf<UUID>()
    private var isntDefault = true
    private var task: BukkitTask? = null
    private val multilineRegex = Regex("""--(-?)(.*?)""")
    private val multilines = hashMapOf<String, MutableList<String>>()
    private val multilinesInterval = hashMapOf<String, Int>()
    private val animatedLines = hashMapOf<String, String>()

    init {
        val sb = getScoreboardDB(id)
        name = sb.name
        display = sb.display
        header = sb.header
        template = sb.template
        footer = sb.footer
        color = sb.color
        visibleGroups.addAll(sb.visibleGroups)
        players.addAll(sb.players)
        created = sb.created
        updated = sb.updated

        start()
    }

    fun start() {
        updateLines()
        updatePlaceholder()
//        players.filter { Bukkit.getPlayer(it)?.isOnline ?: false }.forEach(this::addOnlinePlayer)
    }

    fun containsPlayer(playerUUID: UUID): Boolean = players.contains(playerUUID)


// -------------------
    // getters - start

    /** @return Scoreboard's displayname.*/
    fun getDisplay(): String {
        return display
    }

    /** @return Scoreboard's player list.*/
    fun getPlayers(): MutableList<UUID> {
        return players
    }

    /** @return Scoreboard's online player list.*/
    fun getOnlinePlayers(): MutableList<UUID> {
        return onlinePlayers
    }

    /** @return Scoreboard's visible groups.*/
    fun getVisibleGroups(): MutableList<String> {
        return visibleGroups
    }

    /** @return Scoreboard's header template.*/
    fun getHeader(): String {
        return header
    }

    /** @return Scoreboard's main template.*/
    fun getTemplate(): String {
        return template
    }

    /** @return Scoreboard's footer template.*/
    fun getFooter(): String {
        return footer
    }

    /** @return Scoreboard's color.*/
    fun getColor(): String {
        return color
    }

    /** @return If the scoreboard is set as default. */
    fun isDefault(): Boolean {
        return !isntDefault
    }

    /** Set the scoreboard on a player (use to view the scoreboard) */
    fun getScoreboard(playerUUID: UUID) {
        updatePlayer(playerUUID)
    }


    // getters - end
// -------------------
    // setters - start

    /**
     * @param header New header template to be set.
     * @param template New main template to be set.
     * @param footer New footer template to be set.
     * Insert any of the 3 parameters.
     */
    fun setTemplate(header: String? = null, template: String? = null, footer: String? = null) {
        this.header = header ?: this.header
        this.template = template ?: this.template
        this.footer = footer ?: this.footer
        updateLines()
        databaseUpdate()
    }

    /** @param color New color template to be set. */
    fun setColor(color: String) {
        this.color = color
        updateLines()
        databaseUpdate()
    }

    /** @param display New display to be set. */
    fun setDisplay(display: String) {
        this.display = display
        databaseUpdate()
    }

    /** @param default alter if the scoreboard is default. */
    fun setDefault(default: Boolean) {
        isntDefault = !default
    }

    // setters - end
// -------------------
    // adds - start

    fun addPlayer(playerUUID: UUID): Boolean {
        if (!players.contains(playerUUID)) {
            players.add(playerUUID)
            updatePlayers()
            databaseUpdate()
        }
        return addOnlinePlayer(playerUUID)
    }

    private fun addOnlinePlayer(playerUUID: UUID): Boolean {
        return if (!onlinePlayers.contains(playerUUID)) {
            if (onlinePlayers.isEmpty()) runTask()

            onlinePlayers.add(playerUUID)
            updatePlaceholder()
            updatePlayer(playerUUID)

            true
        } else false
    }

    fun addGroup(group: String): Boolean {
        return if (!visibleGroups.contains(group)) {
            visibleGroups.add(group)
            databaseUpdate()

            true
        } else false
    }

    fun addGroup(groups: MutableList<String>) {
        groups.forEach { addGroup(it) }
    }

    // adds - end
// -------------------
    // rems - start

    fun remPlayer(playerUUID: UUID): Boolean {
        if (players.contains(playerUUID)) {
            players.remove(playerUUID)
            databaseUpdate()
        }
        return remOnlinePlayer(playerUUID)
    }

    fun remOnlinePlayer(playerUUID: UUID): Boolean {
        return if (onlinePlayers.contains(playerUUID)) {
            onlinePlayers.remove(playerUUID)

            Bukkit.getPlayer(playerUUID)?.scoreboard = Bukkit.getScoreboardManager().newScoreboard
            updatePlaceholder()
            tryToCancelTask()

            true
        } else false
    }

    fun remGroup(group: String): Boolean {
        return if (visibleGroups.contains(group)) {
            visibleGroups.remove(group)
            databaseUpdate()

            true
        } else false
    }

    // rems - end
// -------------------
    // updaters - start

    /** Updates the {staff_(scoreboard)} and the {(scoreboard)_list} palceholders. */
    private fun updatePlaceholder() {
        val player = if (onlinePlayers.isNotEmpty()) Bukkit.getPlayer(onlinePlayers[Random.nextInt(onlinePlayers.size)])?.name ?: getMessage("status.offline") else getMessage("status.offline")

        addPlaceholder("staff_$name", player)
        addPlaceholder("${name}_list", onlinePlayers.size.toString())
    }

    /** Call the updatePlayer() method for each player. */
    fun updatePlayers() {
        if (onlinePlayers.isNotEmpty()) onlinePlayers.forEach(::updatePlayer)
    }

    /** Update the players' scoreboard or create a new scoreboard if necessary */
    private fun updatePlayer(playerUUID: UUID) {
        Bukkit.getPlayer(playerUUID).let {
            if (it == null) return@let
            if (it.scoreboard.getObjective(name) == null) scoreboardCreate(it)
            else if (footer == "staff-status" && !lines.containsAll(getStaffStatus(visibleGroups))) {
                updateLines()
                scoreboardCreate(it)
            }
            scoreboardUpdate(it)
        }
    }

    fun updateLines() {
        lines.clear()
        multilines.clear()
        suffixLines.clear()
        val removeLines = mutableListOf<String>()

        if (header.isNotBlank()) lines.addAll(ScoreboardManager.getTemplate(header, visibleGroups))
        if (template.isNotBlank()) lines.addAll(ScoreboardManager.getTemplate(template, visibleGroups))
        if (footer.isNotBlank()) lines.addAll(ScoreboardManager.getTemplate(footer, visibleGroups))

        lines = lines.map {
            if (it.contains(multilineRegex)) {
                val newLine = multilineRegex.replace(it, "")
                val ml = it.replace(newLine, "")

                if (multilines[ml].isNullOrEmpty()) {
                    multilines[ml] = mutableListOf(color + newLine)
                    ml
                } else {
                    multilines[ml]!!.add(color + newLine)
                    removeLines.add(it)
                    it
                }

            } else if (it.isNotEmpty() && it[0] != '&' && it[0] != '<') "$color$it"
            else it
        }.toMutableList()

        lines.removeAll(removeLines)
    }

    fun updateMultilines() {

    }

    // updaters - end
// -------------------
    // scoreboard - start

    /** Sets the scoreboard on the player */
    private fun scoreboardCreate(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        if (hasAnimation(placeholderRegex.replace(getPlaceholder("title")) { it.groupValues[1] }))
            animatedLines["title"] = placeholderRegex.replace(getPlaceholder("title")) { it.groupValues[1] }

        val objective = try {
            scoreboard.registerNewObjective(name, Criteria.DUMMY, c(set(getPlaceholder("title"))))
        } catch (ignore: NoClassDefFoundError) {
            scoreboard.registerNewObjective(name, "DUMMY", c(set(getPlaceholder("title"))))
        }

        try {
            objective.numberFormat(NumberFormat.blank())
        } catch (ignore: NoClassDefFoundError) {
        }
        objective.displaySlot = DisplaySlot.SIDEBAR

        lines.forEachIndexed { i, line ->
            val index = lines.size - i - 1
            var entry = ChatColor.entries[index].toString()

            val team = scoreboard.registerNewTeam("line_$index")

            var prefix = line

            if (line.contains(multilineRegex)) {
                multilinesInterval[line] = multilineTime + 1
                suffixLines[team.name] = line

            } else if (line.contains(":") || hasPlaceholderRegex(line)) {
                var suffix: String

                if (line.contains("::")) {
                    prefix = line.split("::")[0]
                    suffix = line.split("::")[1]
                } else if (line.contains(":")) {
                    prefix = line.split(":")[0] + ":"
                    suffix = join(line.split(":"), ":").substring(prefix.length)
                } else {
                    prefix = line.replace(placeholderRegex, "")
                    suffix = line.replace(prefix, "")
                }

                suffixLines[team.name] = suffix
            }

            prefix = set(prefix, player)

            if (checkPlayerVersion(player.uniqueId)) {
                prefix = LegacyComponentSerializer.legacySection().serialize(c(set(prefix)))
                if (prefix.length > 16 && checkPlayerVersion(player.uniqueId)) entry = prefix.substring(16)
                entry += ChatColor.WHITE

            }

            team.addEntry(entry)
            team.prefix(c(prefix))
            objective.getScore(entry).score = index
        }

        try {
            player.scoreboard = scoreboard
        } catch (e: NoClassDefFoundError) {
            throw e
        }
    }

    /** Updates the player's scoreboard. */
    private fun scoreboardUpdate(player: Player) {
        suffixLines.forEach {
            var suffix = it.value
            val team = player.scoreboard.getTeam(it.key)

            if (it.value.contains(multilineRegex)) {
                if (multilinesInterval[it.value]!! >= multilineTime) {
                    multilinesInterval[it.value] = 0

                    val newLine = multilines[it.value]?.random() ?: it.value
                    var prefix = newLine

                    if (newLine.contains(":") || hasPlaceholderRegex(newLine)) {
                        if (newLine.contains("::")) {
                            prefix = newLine.split("::")[0]
                            suffix = newLine.split("::")[1]
                        } else if (newLine.contains(":")) {
                            prefix = newLine.split(":")[0] + ":"
                            suffix = join(newLine.split(":"), ":").substring(prefix.length)
                        } else {
                            prefix = newLine.replace(placeholderRegex, "")
                            suffix = newLine.replace(prefix, "")
                        }
                    }

                    prefix = set(prefix, player)

                    if (checkPlayerVersion(player.uniqueId)) {
                        prefix = LegacyComponentSerializer.legacySection().serialize(c(set(prefix)))
                        if (prefix.length > 16 && checkPlayerVersion(player.uniqueId)) {
                            team?.entries?.forEach { et -> team.removeEntry(et) }
                            team?.addEntry(prefix.substring(16))
                        }
                    }
                    team?.prefix(c(prefix))
                } else {
                    multilinesInterval[it.value] = multilinesInterval[it.value]!! + 1
                    return@forEach
                }
            }

            var doReturn = false

            suffix = suffix.replace(placeholderRegex) { placeholders ->
                when (val placeholder = placeholders.groupValues[1]) {
                    "staff", "supstaff" -> staffLine(placeholder)

                    "staff_list" -> staffsLine().toString()

                    "player_list" -> getPlayersFromGroups(listOf(default_group)).size.toString()

                    else -> {
                        if (hasAnimation(placeholder)) {
                            if (!animatedLines.containsKey(it.key))
                                animatedLines[it.key] = placeholder
                            doReturn = true
                            ""
                        } else {
                            if (animatedLines.containsKey(it.key))
                                animatedLines.remove(it.key)
                            placeholders.groupValues[0]
                        }
                    }
                }
            }
            if (doReturn) return@forEach
            team?.suffix(c(set(suffix, player)))
        }

//        if (player.scoreboard.getTeam("line_${lines.size}") != null) {
//            player.scoreboard.getTeam("line_${lines.size}")!!.prefix(c("ScoreboardE_line-338-ish"))
//        }
    }

    fun animatePlayers() {
        animatedLines.forEach { (key, value) ->
            val suffix = getAnimation(value)
            val title = key == "title"
            onlinePlayers.forEach {
                Bukkit.getPlayer(it).let { player ->
                    if (title) {
                        player?.scoreboard?.getObjective(name)?.displayName(c(suffix))
                    } else {
                        val team = player?.scoreboard?.getTeam(key)
                        team?.suffix(c(suffix))
                    }
                }
            }
        }
    }

    // scoreboard - end
// -------------------
    // managers - start

    /** @return Return the {staff} placeholder of this scoreboard. */
    private fun staffLine(placeholder: String): String {
        return if (!checkVisibleGroups(visibleGroups)) {
            if (placeholder == "staff") getMessage("status.staff")
            else getMessage("status.supstaff")
        } else getPlayerFromGroup(visibleGroups)
    }

    /** @return Return the {staff_list} placeholder of this scoreboard. */
    private fun staffsLine(): Int {
        return if (visibleGroups.isEmpty()) 0
        else getPlayersFromGroups(visibleGroups).size
    }

    /** Update the scoreboard on the database. */
    fun databaseUpdate() {
        updateScoreboardDB(this)
    }

    /** clear the scoreboards of all players in the player's list. */
    fun shutdownSB() {
        onlinePlayers.forEach { Bukkit.getPlayer(it)?.scoreboard = Bukkit.getScoreboardManager().newScoreboard }
    }

    /** Stops the scoreboard and delete it from the database. */
    fun delete() {
        shutdownSB()
        onlinePlayers.clear()
        tryToCancelTask()
        deleteScoreboardDB(id)
    }

    // managers - end
// -------------------
    // task - start

    /** Run the self-update scoreboard task. */
    private fun runTask() {
        val time = (if (sf.config.contains("priority-time.$name")) sf.config.getLong("priority-time.$name") else 20) * 20

        task = object : BukkitRunnable() {
            override fun run() {
                updatePlayers()
            }
        }.runTaskTimer(plugin, 0, time)
    }

    /** Cancel the self-update scoreboard task */
    fun tryToCancelTask() {
        if (onlinePlayers.isEmpty() && isntDefault && task != null) task!!.cancel()
    }

    fun forceCancelTask() {
        try {
            task?.cancel()
            log("&a&lForcible cancellation of the &bscoreboard &l${name} &f(${display}&f) &a&ltask completed!!!")
        } catch (e: Exception) {
            log("&c&lFailed to force cancellation of the &bscoreboard &l${name} &f(${display}&f) &c&ltask!!!")
            throw e
        }
    }

    /**
     * @param minutes Time in minutes of the break.
     * @return If it has a task running.
     * Pause the self-update scoreboard task for N minutes
     */
    fun pauseTask(minutes: Int = 1): Boolean {
        return if (task != null) {
            task!!.cancel()

            object : BukkitRunnable() {
                override fun run() {
                    runTask()
                }
            }.runTaskLater(plugin, minutes * 60 * 20L)

            true
        } else false
    }

    // task - end
// -------------------
}