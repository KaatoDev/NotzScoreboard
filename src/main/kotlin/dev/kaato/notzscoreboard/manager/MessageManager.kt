package dev.kaato.notzscoreboard.manager

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.msgf

object MessageManager {
    fun hasMessage(message: String): Boolean {
        return message.isNotBlank() && message != " " && msgf.config.contains("messages.$message")
    }

    fun getMessage(message: String): String {
        val path = "messages.$message"
        return if (hasMessage(message))
            msgf.get().getString(path) ?: message
        else message
    }
}