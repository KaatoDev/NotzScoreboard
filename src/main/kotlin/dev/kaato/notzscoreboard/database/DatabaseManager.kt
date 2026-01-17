package dev.kaato.notzscoreboard.database

import dev.kaato.notzscoreboard.entities.ScoreboardE
import dev.kaato.notzscoreboard.entities.ScoreboardModel
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.util.*

object DatabaseManager {
    fun insertScoreboardDB(name: String, display: String, color: String, header: String, template: String, footer: String, visibleGroups: List<String>, players: List<UUID>): Int {
        val id = transaction {
            Scoreboards.insert {
                it[this.name] = name
                it[this.display] = display
                it[this.color] = color
                it[this.header] = header
                it[this.template] = template
                it[this.footer] = footer
                it[this.visibleGroups] = parseToJson(visibleGroups)
                it[this.players] = parseToJson2(players)
            } get Scoreboards.id
        }
        return id
    }

    fun getScoreboardDB(id: Int): ScoreboardModel {
        return transaction {
            Scoreboards.selectAll().where { Scoreboards.id eq id }.first().let {
                ScoreboardModel(
                    it[Scoreboards.id],
                    it[Scoreboards.name],
                    it[Scoreboards.display],
                    it[Scoreboards.color],
                    it[Scoreboards.header],
                    it[Scoreboards.template],
                    it[Scoreboards.footer],
                    parseGroupList(it[Scoreboards.visibleGroups]),
                    parsePlayersList(it[Scoreboards.players]),
                    it[Scoreboards.created],
                    it[Scoreboards.updated]
                )
            }
        }
    }

    fun updateScoreboardDB(scoreboard: ScoreboardE) {
        transaction {
            Scoreboards.update({ Scoreboards.id eq scoreboard.id }) {
                it[display] = scoreboard.getDisplay()
                it[header] = scoreboard.getHeader()
                it[template] = scoreboard.getTemplate()
                it[footer] = scoreboard.getFooter()
                it[color] = scoreboard.getColor()
                it[visibleGroups] = parseToJson(scoreboard.getVisibleGroups())
                it[players] = parseToJson2(scoreboard.getPlayers())
                it[updated] = LocalDateTime.now()
            }
        }
    }

    fun deleteScoreboardDB(id: Int): Boolean? {
        val res = transaction {
            Scoreboards.deleteWhere { Scoreboards.id eq id }
        }
        return when (res) {
            0 -> false
            1 -> true
            else -> {
                log("More Scoreboards were deleted!!!!! - $res")
                null
            }
        }
    }

    fun loadScoreboardsDB(): List<ScoreboardE> {
        return transaction { Scoreboards.select(Scoreboards.id).map { ScoreboardE(it[Scoreboards.id]) } }
    }

    fun containScoreboardDB(name: String): Boolean {
        return transaction {
            Scoreboards.selectAll().where { Scoreboards.name eq name }.toList().isNotEmpty()
        }
    }

    fun containScoreboardsDB(): Boolean {
        return transaction {
            Scoreboards.selectAll().toList().isNotEmpty()
        }
    }


    fun parseToJson(list: List<String>): String {
        val arr = JsonArray(list.map { JsonPrimitive(it) })
        return arr.toString()
    }

    fun parseToJson2(list: List<UUID>): String {
        val arr = JsonArray(list.map { JsonPrimitive(it.toString()) })
        return arr.toString()
    }

    fun parseGroupList(json: String): List<String> {
        return Json.decodeFromString(json)
    }

    fun parsePlayersList(json: String): List<UUID> {
        return Json.decodeFromString<List<String>>(json).map(UUID::fromString)
    }
}