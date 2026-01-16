package dev.kaato.notzscoreboard

import dev.kaato.notzapi.NotzAPI
import dev.kaato.notzapi.NotzAPI.Companion.addPlugin
import dev.kaato.notzapi.NotzAPI.Companion.removePlugin
import dev.kaato.notzapi.apis.NotzYAML
import dev.kaato.notzapi.managers.ItemManager
import dev.kaato.notzapi.managers.MessageManager
import dev.kaato.notzapi.managers.NotzManager
import dev.kaato.notzapi.managers.PlaceholderManager
import dev.kaato.notzapi.utils.*
import dev.kaato.notzapi.utils.MessageU.Companion.c
import dev.kaato.notzscoreboard.commands.NScoreboardC
import dev.kaato.notzscoreboard.commands.NTestC
import dev.kaato.notzscoreboard.database.DAO
import dev.kaato.notzscoreboard.events.JoinLeaveE
import dev.kaato.notzscoreboard.manager.ScoreboardManager.load
import dev.kaato.notzscoreboard.manager.ScoreboardManager.shutdown
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.system.measureTimeMillis


class NotzScoreboard : JavaPlugin() {
    companion object {
        lateinit var pathRaw: String

        lateinit var cf: NotzYAML
        lateinit var msgf: NotzYAML
        lateinit var sf: NotzYAML

        lateinit var plugin: JavaPlugin
        var notzAPI: NotzAPI? = null
        lateinit var napi: NotzManager
        lateinit var itemManager: ItemManager
        lateinit var messageManager: MessageManager
        lateinit var placeholderManager: PlaceholderManager
        lateinit var eventU: EventU
        lateinit var mainU: MainU
        lateinit var menuU: MenuU
        lateinit var messageU: MessageU
        lateinit var othersU: OthersU
        lateinit var dao: DAO
    }

    override fun onEnable() {
        val load = measureTimeMillis {
            pathRaw = dataFolder.absolutePath
            plugin = this

            notzAPI = Bukkit.getServicesManager().load(NotzAPI::class.java)
            napi = addPlugin(plugin)
            napi.version = "3.4-pre2"

            messageManager = napi.messageManager
            itemManager = napi.itemManager
            placeholderManager = napi.placeholderManager
            eventU = napi.eventU
            mainU = napi.mainU
            menuU = napi.menuU
            messageU = napi.messageU
            othersU = napi.othersU

            cf = NotzYAML(this, "config")
            sf = NotzYAML(this, "scoreboard")
            msgf = messageManager.messageFile

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
                bStats()
                othersU.sendAdmin("&2NotzScoreboard &ainitialized! (${load / 1000.0}s)")
            }
        }.runTaskLater(this, 4 * 20L)
    }

    private fun start() {
        regCommands()
        regEvents()
        regTabs()

        letters()
    }

    private fun regCommands() {
        getCommand("nscoreboard")?.setExecutor(NScoreboardC())
        getCommand("test")?.setExecutor(NTestC())
    }

    private fun regEvents() {
        getPluginManager().registerEvents(JoinLeaveE(), this)
    }

    private fun regTabs() {
        getCommand("nscoreboard")?.tabCompleter = NScoreboardC()
    }

    private fun letters() {
        messageU.send(
            Bukkit.getConsoleSender(), """
                &2Inicializado com sucesso.
                &f┳┓    &2┏┓       ┓        ┓&f  &2┓┏┏┓
                &f┃┃┏┓╋┓&2┗┓┏┏┓┏┓┏┓┣┓┏┓┏┓┏┓┏┫&f━━&2┃┃ ┫
                &f┛┗┗┛┗┗&2┗┛┗┗┛┛ ┗ ┗┛┗┛┗┻┛ ┗┻&f  &2┗┛┗┛
                
                ${messageU.set("{prefix}")} &6Para mais plugins como este, acesse &bhttps://kaato.dev/plugins&6!!
                ${messageU.set("{prefix}")} &6For more plugins like this, visit &bhttps://kaato.dev/plugins&6!!
                
            """.trimIndent()
        )
        Bukkit.getOnlinePlayers().forEach {
            if (othersU.isAdmin(it)) {
                it.sendMessage(" ")
                it.sendMessage(c("${messageU.set("{prefix}")} &6For more plugins like this, visit &e&oour website&6! &&url[&b&okaato.dev/plugins](https://kaato.dev/plugins)"))
                it.sendMessage(" ")
            }
        }
    }

    fun bStats() {
        val pluginId = 28538
        Metrics(this, pluginId)
    }

    override fun onDisable() {
        removePlugin(plugin)
//        saveScoreboards()
        shutdown()
    }
}
