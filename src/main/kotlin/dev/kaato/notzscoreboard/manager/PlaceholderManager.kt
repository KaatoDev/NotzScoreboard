package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.msgf
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.sf
import dev.kaato.notzscoreboard.utils.MessageUtil.formatMoney
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlaceholderManager {
    private val placeholders = hashMapOf<String, String>()
    val placeholderRegex = Regex("""[%{](.*?)[%}]""")

    init {
        placeholders["title"] = sf.get().getString("title") ?: "[title]"
        placeholders["prefix"] = msgf.get().getString("prefix") ?: "[prefix]"
        sf.config.getMapList("placeholders").flatMap { it.entries }.forEach { addPlaceholder(it.key.toString(), it.value.toString()) }
    }

    fun addPlaceholder(placeholder: String, value: String) {
        placeholders[placeholder] = value
    }

    fun remPlaceholder(placeholder: String) {
        if (placeholders.containsKey(placeholder))
            placeholders.remove(placeholder)
    }

    fun getPlaceholder(placeholder: String): String {
        return placeholders[placeholder] ?: "[$placeholder]"
    }

    fun getPlaceholder(placeholder: String, player: Player): String {
        return if (placeholder == "money")
            placeholderMoney(player)
        else if (hasPlaceholder(placeholder)) getPlaceholder(placeholder) else  PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
    }

    fun hasPlaceholder(placeholder: String): Boolean {
        return placeholder.contains(placeholderRegex)
    }

    fun placeholderMoney(player: Player): String {
        return try {
//            var placeholder = "&2$&a " + formatMoney(PlaceholderAPI.setPlaceholders(player as Player, "%vault_eco_balance_2dp%").toDouble())
            val pp: String = PlaceholderAPI.setPlaceholders(player, "%vault_eco_balance_formatted%")

            val placeholder = if (Bukkit.getServer().pluginManager.getPlugin("Vault") != null) {
                if (!pp.contains(",")) "&2$&a" + pp.replace("$", "&2")
                else "&2$&a" + pp.replace("$", "&2").replace(pp.substring(pp.indexOf("."), pp.indexOf(".") + 2), "")

            } else "&2$&a0"
            placeholder

        } catch (ignored: Exception) {
            "{money}"
        }
    }
}