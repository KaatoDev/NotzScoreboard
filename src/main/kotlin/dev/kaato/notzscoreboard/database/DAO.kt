package dev.kaato.notzscoreboard.database

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.cf
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.pathRaw
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

class DAO {
    private lateinit var db: Database
    private val useMysql = cf.config.getBoolean("useMySQL")
    private val sql = if (useMysql) "mysql" else "sqlite"
    private val host = cf.config.getString("mysql.host")
    private val port = cf.config.getString("mysql.port")
    private val mysqlDB = cf.config.getString("mysql.database")
    private val database = if (useMysql) "//$host:$port/${mysqlDB}" else "$pathRaw/notzscoreboard.db"
    private val user = cf.config.getString("mysql.user") ?: ""
    private val password = cf.config.getString("mysql.password") ?: ""

    fun init() {
        try {
            if (useMysql) Class.forName("com.mysql.cj.jdbc.Driver") else Class.forName("org.sqlite.JDBC")

            db = if (useMysql) Database.connect("jdbc:$sql:$database", "com.mysql.cj.jdbc.Driver", user, password) else Database.connect("jdbc:$sql:$database", "org.sqlite.JDBC")

            transaction {
                SchemaUtils.createMissingTablesAndColumns(Scoreboards)
            }

            log("&aSuccessfully initialized Database!")
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun database() = db
}