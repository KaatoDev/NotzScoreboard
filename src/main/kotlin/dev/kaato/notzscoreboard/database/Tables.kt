package dev.kaato.notzscoreboard.database

import org.jetbrains.annotations.NotNull
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.CurrentDateTime
import org.jetbrains.exposed.v1.javatime.datetime

const val max_varchar = 128

object Scoreboards : Table("scoreboards") {
    @NotNull val id = integer("id").autoIncrement()
    override val primaryKey = PrimaryKey(id)
    @NotNull val name = varchar("name",max_varchar).uniqueIndex()
    @NotNull val display = varchar("display",max_varchar)
    @NotNull val color = varchar("color",max_varchar).default("&e")
    @NotNull val header = varchar("header",max_varchar).default("")
    @NotNull val template = varchar("template",max_varchar).default("player")
    @NotNull val footer = varchar("footer",max_varchar).default("staff-status")
    @NotNull val visibleGroups = text("visibleGroups").default("[]")
    @NotNull val players = text("players").default("[]")
    @NotNull val created = datetime("created").defaultExpression(CurrentDateTime)
    @NotNull val updated = datetime("updated").nullable()
}