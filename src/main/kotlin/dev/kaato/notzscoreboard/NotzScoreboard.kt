package dev.kaato.notzscoreboard

import dev.kaato.notzscoreboard.apis.NotzYAML
import dev.kaato.notzscoreboard.commands.NScoreboardC
import dev.kaato.notzscoreboard.database.DAO
import dev.kaato.notzscoreboard.events.JoinLeaveE
import dev.kaato.notzscoreboard.manager.ScoreboardManager.load
import dev.kaato.notzscoreboard.manager.ScoreboardManager.shutdown
import dev.kaato.notzscoreboard.utils.MessageUtil.letters
import dev.kaato.notzscoreboard.utils.MessageUtil.sendAdmin
import dev.kaato.notzscoreboard.utils.MessageUtil.set
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.system.measureTimeMillis

class NotzScoreboard : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String
        lateinit var prefix: String

        lateinit var cf: NotzYAML
        lateinit var msgf: NotzYAML
        lateinit var sf: NotzYAML

        lateinit var plugin: JavaPlugin
        lateinit var dao: DAO
    }

    override fun onEnable() {
        val load = measureTimeMillis {
            pathRaw = dataFolder.absolutePath
            plugin = this

            cf = NotzYAML( "config")
            sf = NotzYAML( "scoreboard")
            msgf = NotzYAML( "messages")
            prefix = set("{prefix}")

            try {
                dao = DAO()
                dao.init()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        object : BukkitRunnable() {
            override fun run() {
                load()
                start()
                sendAdmin("&2NotzScoreboard &ainitialized! (${load / 1000.0}s)")
            }
        }.runTaskLater(this, 4 * 20L)
    }
    
    private fun start() {   
        getCommand("nscoreboard")?.setExecutor(NScoreboardC())
        getCommand("nscoreboard")?.tabCompleter = NScoreboardC()
        getPluginManager().registerEvents(JoinLeaveE(), this)
        letters()
        bStats()
    } 
    
    fun bStats() {
        val pluginId = 28538
        Metrics(this, pluginId)
    }
    
    override fun onDisable() {
        shutdown()
    }
}
