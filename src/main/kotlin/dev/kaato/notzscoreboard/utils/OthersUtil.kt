package dev.kaato.notzscoreboard.utils

import dev.kaato.notzscoreboard.utils.MessageUtil.send
import org.bukkit.entity.Player

object OthersUtil {
    fun hasPermission(player: Player, permission: String): Boolean = player.hasPermission("notzscoreboard.$permission")

    fun isAdmin(player: Player): Boolean {
        return hasPermission(player, "admin")
    }

    fun isntAdmin(player: Player): Boolean {
        val isntAdmin = !isAdmin(player)
        if (isntAdmin) send(player, "no-perm")
        return isntAdmin
    }
}