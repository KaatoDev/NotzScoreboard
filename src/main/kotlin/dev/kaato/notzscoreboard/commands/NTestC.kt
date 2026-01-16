package dev.kaato.notzscoreboard.commands

import dev.kaato.notzapi.utils.MessageU.Companion.c
import dev.kaato.notzapi.utils.MessageU.Companion.formatMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NTestC : CommandExecutor{
    override fun onCommand(p: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (p !is Player) return false
        
        p.sendMessage(c(args.joinToString(" ")))
        Bukkit.getConsoleSender().sendMessage(c(args.joinToString(" ")))
        
        return true
    }
}