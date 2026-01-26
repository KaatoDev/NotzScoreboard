package dev.kaato.notzscoreboard.utils

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.prefix
import dev.kaato.notzscoreboard.manager.MessageManager.getMessage
import dev.kaato.notzscoreboard.manager.PlaceholderManager.getPlaceholder
import dev.kaato.notzscoreboard.manager.PlaceholderManager.hasPlaceholderRegex
import dev.kaato.notzscoreboard.manager.PlaceholderManager.placeholderRegex
import dev.kaato.notzscoreboard.utils.OthersUtil.isAdmin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal

object MessageUtil {
    fun c(message: String): Component {
        return MiniMessage.miniMessage().deserialize(formatMessage(message))
    }

    fun log(message: String, sender: ConsoleCommandSender? = null) {
        val console = sender ?: Bukkit.getConsoleSender()
        val msg = set("$prefix $message")
        console.sendMessage(c(msg))
    }

    fun sendAdmin(message: String) {
        Bukkit.getOnlinePlayers().forEach {
            if (isAdmin(it)) send(it, message)
        }
    }

    fun formatMoney(amount: Number, isInt: Boolean = false): String {
        val ext = "K M B T q Q s S O N D Ud Dd Td qd Qd sd Sd Od Nd Vg".split(' ')
        var am = extendMoney(amount, isInt)

        if (am.contains(',') && am.length > 6) am = am.substringBeforeLast(',')

        if (am.contains('.') && am.length > 8) {
            val d = am.filter { it == '.' }.length
            am = "${am.substring(0, am.indexOf('.') + 2)} ${ext[d - 1]}"
        }

        return am
    }

    fun extendMoney(amount: Number, isInt: Boolean = false): String {
        var am = BigDecimal(amount.toString()).toString()
        val fl = if (am.contains('.')) "." else if (am.contains(',')) "," else ""
        var dec = ""
        var fm = ""

        if (fl.isNotBlank()) {
            dec = am.substringAfterLast(fl)
            if (dec.length > 2) dec = dec.substring(0, 2)
            else if (dec.length < 2) dec += '0'

            am = am.substringBeforeLast(fl)
        }

        if (am.length % 3 > 0 && am.length > 3) {
            fm = am.substring(0, am.length % 3)
            am = am.substring(am.length % 3)
        }

        am = am.mapIndexed { i, it -> if ((i + 1) % 3 == 1 && i > 2) ".$it" else it }.joinToString("")
        if (fm.isNotBlank()) am = "$fm.$am"

        return if (isInt) am else "$am,$dec"
    }

    fun join(text: List<String>, separator: String = ", ", prefix: String = "", postfix: String = "", transform: ((String) -> CharSequence)? = null): String {
        return text.joinToString(prefix = prefix, postfix = postfix, separator = separator, transform = transform)
    }

    fun formatMessage(rawMessage: String): String {
        var message = rawMessage

        if (message.contains(chatEventRegex)) while (message.contains(chatEventRegex)) {
            message = chatEventRegex.replace(message) {
                val items = chatInteract[it.groupValues[1]] ?: arrayOf("<hover:show_text:", "</hover>")
                "${items[0]}${it.groupValues[3]}>${it.groupValues[2]}${items[1]}"
            }
        }

        if (message.contains(gradientRegexSimple)) {
            message = gradientRegexSimple.replace(message) {
                val colors = it.groupValues[1].map { c -> colorsDeco[c] }.joinToString(":")
                val text = it.groupValues[2]
                "<gradient:$colors>$text</gradient>"
            }
        }

        if (message.contains(rainbowRegex)) {
            message = rainbowRegex.replace(message) {
                val rainbow = it.groupValues[1].let { c -> if (c.isNotBlank()) ":$c" else "" }
                val text = it.groupValues[2]
                "<rainbow$rainbow>$text</rainbow>"
            }
        }

        if (message.contains(colorRegex)) {
            message = colorRegex.replace(message) {
                "<${colorsDeco[it.groupValues[2].lowercase()[0]]}>"
            }
        }

        if (message.contains(gradientHexRegex)) {
            message = gradientHexRegex.replace(message) {
                val colors = it.groupValues[1].split(':').joinToString(":") { c ->
                    when (c.length) {
                        1 -> colorsDeco[c[0]] ?: c
                        3 -> "#${c[0]}${c[0]}${c[1]}${c[1]}${c[2]}${c[2]}"
                        6 -> "#$c"
                        else -> c
                    }
                }
                "<gradient:$colors>${it.groupValues[2]}</gradient>"
            }
        }

        if (message.contains(prideRegex)) {
            message = prideRegex.replace(message) {
                val flag = it.groupValues[2].let { c -> if (c.isNotBlank()) ":$c" else "" }
                val text = it.groupValues[3]
                "<pride$flag>$text</pride>"
            }
        }

        message = message.replace("\\n", "<newline>")
        message = message.replace("&.", "")

        return message
    }

    val colorRegex = Regex("""([&§])([0-9a-fk-orA-FK-OR])""")
    val chatEventRegex = Regex("""&&(copy|url|run|sug|suggest|hover|item|entity)\[(.*?)]\((.*?)\)""")
    const val END_REGEX = """<|&|&\.|$"""
    val gradientHexRegex = Regex("""&g\[(.*?)](.*?)(?=$END_REGEX)""")
    val prideRegex = Regex("""&p(\[(.*?)])?(.*?)(?=$END_REGEX)""")
    val gradientRegexSimple = Regex("""&g([0-9a-fA-F]{2,})(.*?)(?=$END_REGEX)""")
    val rainbowRegex = Regex("""&!(!?[0-9]?)(.*?)(?=$END_REGEX)""")
    val defaultsRegex = Regex("""\{default([0-9])?}""")

    val colorsDeco = hashMapOf(
        '1' to "dark_blue",
        '2' to "dark_green",
        '3' to "dark_aqua",
        '4' to "dark_red",
        '5' to "dark_purple",
        '6' to "gold",
        '7' to "gray",
        '8' to "dark_gray",
        '9' to "blue",
        '0' to "black",
        'a' to "green",
        'b' to "aqua",
        'c' to "red",
        'd' to "light_purple",
        'e' to "yellow",
        'f' to "white",
        'r' to "reset",
        'k' to "obfuscated",
        'l' to "bold",
        'm' to "strikethrough",
        'n' to "underlined",
        'o' to "italic",
    )

    val chatInteract = hashMapOf(
        "copy" to arrayOf("<click:copy_to_clipboard:", "</click>"),
        "url" to arrayOf("<click:open_url:", "</click>"),
        "run" to arrayOf("<click:run_command:", "</click>"),
        "suggest" to arrayOf("<click:suggest_command:", "</click>"),
        "sug" to arrayOf("<click:suggest_command:", "</click>"),
        "hover" to arrayOf("<hover:show_text:", "</hover>"),
        "item" to arrayOf("<hover:show_item:", "</hover>"),
        "entity" to arrayOf("<hover:show_entity:", "</hover>"),
    )

    fun prepareMessage(player: Player, message: String, default: String? = null, defaults: List<String> = listOf()): String {
        val msg = getMessage(message)

        return set(msg, player, default, defaults)
    }

    fun send(player: Player, message: String, default: String? = null, defaults: List<String> = listOf()) {
        val msg = prepareMessage(player, message, default, defaults)
        player.sendMessage(c("$prefix $msg"))
    }

    fun sendHeader(player: Player, message: String, default: String? = null, defaults: List<String> = listOf()) {
        var msg = prepareMessage(player, message, default, defaults)

        msg = """
            &r
            &f-=-=-=-&b= $prefix &b=&f-=-=-=-
            $msg
            &r
        """.replace("            ", "")

        player.sendMessage(c(msg))
    }

    fun set(text: String, player: Player? = null, default: String? = null, defaults: List<String> = listOf()): String {
        var txt = text

        if ((default != null || defaults.isNotEmpty()) && txt.contains(defaultsRegex)) txt = replaceDefaults(txt, default, defaults)

        if (hasPlaceholderRegex(txt)) txt = replacePlaceholders(txt, player)

        return txt
    }

    fun replacePlaceholders(text: String, player: Player? = null): String {
        var txt = text
        txt = txt.replace(placeholderRegex) {
            if (player != null) getPlaceholder(it.groupValues[1], player)
            else getPlaceholder(it.groupValues[1])
        }
        return txt
    }

    private fun replaceDefaults(text: String, default: String?, defaults: List<String>): String {
        var txt = text
        txt = txt.replace(defaultsRegex) {
            default ?: it.groupValues[1].toInt().let { index -> if (defaults.size > index) defaults[index] else "[default$index]" }
        }
        return txt
    }

    fun letters() {
        log(
            """
                &2Initialized successfully
                &f┳┓    &2┏┓       ┓        ┓&f  &2┓┏┏
                &f┃┃┏┓╋┓&2┗┓┏┏┓┏┓┏┓┣┓┏┓┏┓┏┓┏┫&f━━&2┃┃┃┃
                &f┛┗┗┛┗┗&2┗┛┗┗┛┛ ┗ ┗┛┗┛┗┻┛ ┗┻&f  &2┗┛┗╋
                
                $prefix &6Para mais plugins como este, acesse &bhttps://kaato.dev/plugins&6!!
                $prefix &6For more plugins like this, visit &bhttps://kaato.dev/plugins&6!!
                
            """.trimIndent()
        )

        Bukkit.getOnlinePlayers().forEach {
            if (isAdmin(it)) {
                it.sendMessage(" ")
                it.sendMessage(c("$prefix &6For more plugins like this, visit &e&oour website&6! &&url[&b&okaato.dev/plugins](https://kaato.dev/plugins)"))
                it.sendMessage(" ")
            }
        }
    }
}