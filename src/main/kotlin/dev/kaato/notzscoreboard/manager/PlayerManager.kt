package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.manager.ScoreboardManager.addPlayerTo
import dev.kaato.notzscoreboard.manager.ScoreboardManager.default_group
import dev.kaato.notzscoreboard.manager.ScoreboardManager.registerPlayerToScoreboard
import dev.kaato.notzscoreboard.manager.ScoreboardManager.unregisterPlayerFromScoreboard
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.IOException

object PlayerManager {
    fun joinPlayer(player: Player) {
        when (registerPlayerToScoreboard(player)) {
            true -> log("&eAssigned a scoreboard to the player &f${player.name}&e.")
            false -> log("&cUnable to assign a scoreboard to the player &f${player.name}&c. Error: pmanager1")
        }
    }

    fun leavePlayer(player: Player) {
        if (!unregisterPlayerFromScoreboard(player))
            log("&cUnable to remove/assign a scoreboard to the player &f${player.name}&c. Error: pmanager2")
    }

    fun resetPlayer(player: Player): Boolean {
        return addPlayerTo(player, default_group)
    }

    fun initializePlayers() {
        Bukkit.getOnlinePlayers().forEach(::joinPlayer)
    }
}