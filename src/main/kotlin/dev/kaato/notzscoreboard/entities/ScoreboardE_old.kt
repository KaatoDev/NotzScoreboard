package dev.kaato.notzscoreboard.entities

import dev.kaato.notzapi.utils.MessageU.Companion.c
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.messageU
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.placeholderManager
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.plugin
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.database.DatabaseManager.deleteScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.getScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.insertScoreboardDB
import dev.kaato.notzscoreboard.database.DatabaseManager.updateScoreboardDB
import dev.kaato.notzscoreboard.manager.ScoreboardManager
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getPlayerFromGroup
import dev.kaato.notzscoreboard.manager.ScoreboardManager.getPlayersFromGroups
import dev.kaato.notzscoreboard.manager.ScoreboardManager.scoreboards
import io.papermc.paper.scoreboard.numbers.NumberFormat
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.time.LocalDateTime
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
class ScoreboardE_old(val id: Int) {
    /**
     * @param name Unique name to be used in commands.
     * @param display Displayname that will appear on messages.
     */
    constructor(name: String, display: String, color: String = "&e", header: String = "", template: String = "player", footer: String = "staff-status", visibleGroups: MutableList<String> = mutableListOf()) : this(insertScoreboardDB(name, display, color, header, template, footer, visibleGroups))

    val name: String
    private var display: String
    private var header: String
    private var template: String
    private var footer: String
    private var color: String
    private val visibleGroups = mutableListOf<String>()
    val created: LocalDateTime
    private var updated: LocalDateTime?

    private var linesList = mutableListOf<String>()
    private var players = mutableListOf<Player>()
    private var isntDefault = true
    private var task: BukkitTask? = null

    init {
        val sb = getScoreboardDB(id)
        name = sb.name
        display = sb.display
        header = sb.header
        template = sb.template
        footer = sb.footer
        color = sb.color
        visibleGroups.addAll(sb.visibleGroups)
        created = sb.created
        updated = sb.updated

        update()
    }

// -------------------
    // getters - start

    /** @return Scoreboard's displayname.*/
    fun getDisplay(): String {
        return display
    }

    /** @return Scoreboard's player list.*/
    fun getPlayers(): MutableList<Player> {
        return players
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

    /** Set the scoreboard on a player (used to view the scoreboard) */
    fun getScoreboard(player: Player) {
        scoreboardCreate(player)
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
        println("header = [${header}], template = [${template}], footer = [${footer}]")
        this.header = header ?: this.header
        this.template = template ?: this.template
        this.footer = footer ?: this.footer
        update()
        databaseUpdate()
        println("header = [${header}], template = [${template}], footer = [${footer}]")
    }

    /** @param color New color template to be set. */
    fun setColor(color: String) {
        this.color = color
        update()
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

    /**
     * @param player New player to be added to the player list.
     * @return If contains the player on the list or not.
     */
    fun addPlayer(player: Player): Boolean {
        return if (!players.contains(player)) {
            if (players.isEmpty())
                runTask()

            players.add(player)
            update()
            updatePlayer(player)

            true
        } else false
    }

    /**
     * @param group New group to be added to the visible groups.
     * @return If contains the group on the list or not.
     */
    fun addGroup(group: String): Boolean {
        return if (!visibleGroups.contains(group)) {
            visibleGroups.add(group)
            databaseUpdate()
            update()

            true
        } else false
    }

    fun addGroup(groups: MutableList<String>) {
        groups.forEach { addGroup(it) }
    }

    // adds - end
// -------------------
    // rems - start

    /**
     * @param player The player to be removed from the player list.
     * @return If contains the player on the list or not.
     */
    fun remPlayer(player: Player): Boolean {
        return if (players.contains(player)) {
            players.remove(player)
            player.scoreboard = Bukkit.getScoreboardManager().newScoreboard
            update()
            cancelTask()

            true
        } else false
    }

    /**
     * @param group The group to be removed from the visible groups.
     * @return If contains the group on the list or not.
     */
    fun remGroup(group: String): Boolean {
        return if (visibleGroups.contains(group)) {
            visibleGroups.remove(group)
            databaseUpdate()
            update()

            true
        } else false
    }

    // rems - end
// -------------------
    // updaters - start

    /** Updates the {staff_(scoreboard)} and the {(scoreboard)_list} palceholders. */
    private fun updatePlaceholder() {
        val player = if (players.isNotEmpty()) players[Random.nextInt(players.size)].name else messageU.getMessage("status.offline")

        placeholderManager.addPlaceholder("{staff_$name}", player)
        placeholderManager.addPlaceholder("{${name}_list}", players.size.toString())
    }

    /** Call the updatePlayer() method for each player. */
    fun updatePlayers() {
        if (players.isNotEmpty())
            players.forEach(::updatePlayer)
    }

    /** Update the players' scoreboard or create a new scoreboard if necessary */
    private fun updatePlayer(player: Player) {
        if (player.scoreboard.getObjective(name) == null)
            scoreboardCreate(player)
        else scoreboardUpdate(player)

    }

    /** Update the scoreboard's lines and placeholders and the players' scoreboards. */
    fun update() {
        if (linesList.isNotEmpty())
            linesList.clear()

        if (header.isNotBlank()) linesList.addAll(ScoreboardManager.getTemplate(header))
        if (template.isNotBlank()) linesList.addAll(ScoreboardManager.getTemplate(template))
        if (footer.isNotBlank()) linesList.addAll(ScoreboardManager.getTemplate(footer, visibleGroups))

        var blanks = ""

        linesList = linesList.map {
            if (it.isBlank() || it == " ") {
                blanks += " "
                it + blanks

            } else if (it[0] == '&') it
            else color + it
        }.toMutableList()

        updatePlaceholder()
        shutdownSB()
        updatePlayers()
    }

    // updaters - end
// -------------------
    // scoreboard - start

    /** Sets the scoreboard on the player */
    private fun scoreboardCreate(player: Player) {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        val objective = scoreboard.registerNewObjective(name, Criteria.DUMMY, c(placeholderManager.set(sf.config.getString("title") ?: "")))

        objective.numberFormat(NumberFormat.blank())
        objective.displaySlot = DisplaySlot.SIDEBAR

        linesList.forEachIndexed { i, line ->
            val r = if (line.contains("{")) 0 else if (line.contains("%")) 1 else null
            var l = line
            val index = linesList.size - i - 1

            if (line.contains("#"))
                l = l.replaceFirst("#", "")


            if (r != null) {
                val team = scoreboard.registerNewTeam(name + index)

                var prefix = l.substring(0, l.indexOf(if (r == 0) "{" else "%"))
                var suffix = placeholderManager.set(l.removePrefix(prefix), player)
                println("prefix - $prefix")
                println("suffix - $suffix")

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", staffLine("{staff}"))
                if (suffix.contains("{supstaff}"))
                    suffix = suffix.replace("{supstaff}", staffLine("{supstaff}"))
                if (suffix.contains("{staff_list}"))
                    suffix = suffix.replace("{staff_list}", staffsLine().toString())

                if (suffix.contains("{player_list}"))
                    suffix = suffix.replace("{player_list}", scoreboards[default_group]!!.getPlayers().size.toString())

                if (prefix.length > 30)
                    prefix = "&cLine $index "
                if (suffix.length > 16)
                    suffix = "Suffix too large"

                team.prefix(c(prefix))
                team.addEntry("&r")
                team.suffix(c(suffix))

                objective.getScore(prefix).score = index

            } else if (l.length > 38)
                objective.getScore(c("&cLine $index is too large").toString()).score = index
            else if (l.length > 2 && l[2] == '#')
                objective.getScore(l.replaceFirst("#", "")).score = index
            else objective.getScore(l).score = index
        }

        try {
            player.scoreboard = scoreboard
        } catch (e: NoClassDefFoundError) {
            throw e
        }
    }

    /** Updates the player's scoreboard. */
    private fun scoreboardUpdate(player: Player) {
        linesList.forEachIndexed { i, line ->

            if ((line.contains("{") || line.contains("%")) && !line.contains('#')) {
                val index = linesList.size - i - 1

                val prefix = line.substring(0, line.indexOf(if (line.contains("{")) "{" else "%"))
                var suffix = placeholderManager.set(line.removePrefix(prefix), player)

                if (suffix.contains("{staff}"))
                    suffix = suffix.replace("{staff}", staffLine("{staff}"))
                else if (suffix.contains("{supstaff}"))
                    suffix = suffix.replace("{supstaff}", staffLine("{supstaff}"))
                else if (suffix.contains("{staff_list}"))
                    suffix = suffix.replace("{staff_list}", staffsLine().toString())

                if (suffix.contains("{player_list}"))
                    suffix = suffix.replace("{player_list}", scoreboards[default_group]!!.getPlayers().size.toString())

//                if (prefix.length > 30)
//                    prefix = c("&cLine $index ")
                if (suffix.length > 16)
                    suffix = "Suffix too large"

//                println(suffix)
//                println(c(suffix))

                player.scoreboard.getTeam(name + index)?.suffix(c(suffix))
            }
        }
    }

    // scoreboard - end
// -------------------
    // managers - start

    /** @return Return the {staff} placeholder of this scoreboard. */
    private fun staffLine(placeholder: String): String {
        return if (getPlayersFromGroups(visibleGroups).isEmpty()) {
            if (placeholder == "{staff}") messageU.getMessage("status.staff")
            else messageU.getMessage("status.supstaff")
        } else getPlayerFromGroup(visibleGroups)
    }

    /** @return Return the {staff_list} placeholder of this scoreboard. */
    private fun staffsLine(): Int {
        return if (visibleGroups.isEmpty()) 0
        else getPlayersFromGroups(visibleGroups).size
    }

    /** Update the scoreboard on the database. */
    fun databaseUpdate() {
//        updateScoreboardDB(this)
    }

    /** clear the scoreboards of all players in the player's list. */
    fun shutdownSB() {
        players.forEach { it.scoreboard = Bukkit.getScoreboardManager().newScoreboard }
    }

    /** Stops the scoreboard and delete it from the database. */
    fun delete() {
        shutdownSB()
        players.clear()
        cancelTask()
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
    fun cancelTask() {
        if (players.isEmpty() && isntDefault && task != null)
            task!!.cancel()
    }

    fun forceCancelTask() {
        try {
            task?.cancel()
            messageU.send(Bukkit.getConsoleSender(), "&a&lForcible cancellation of the &bscoreboard &l${name} &f(${display}&f) &a&ltask completed!!!")
        } catch (e: Exception) {
            messageU.send(Bukkit.getConsoleSender(), "&c&lFailed to force cancellation of the &bscoreboard &l${name} &f(${display}&f) &c&ltask!!!")
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