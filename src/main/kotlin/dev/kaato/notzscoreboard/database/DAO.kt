package dev.kaato.notzscoreboard.database

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.cf
import dev.kaato.notzscoreboard.NotzScoreboard.Companion.pathRaw
import dev.kaato.notzscoreboard.utils.MessageUtil.log
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.SQLException

class DAO {
    private lateinit var database: Database
    private val useMysql = cf.config.getBoolean("useMySQL")
    private val sql = if (useMysql) "mysql" else "sqlite"
    private val host = cf.config.getString("mysql.host")
    private val port = cf.config.getString("mysql.port")
    private val mysqlDB = cf.config.getString("mysql.database")
    private val databaseName = if (useMysql) "//$host:$port/${mysqlDB}" else "$pathRaw/notzscoreboard.db"
    private val user = cf.config.getString("mysql.user") ?: ""
    private val password = cf.config.getString("mysql.password") ?: ""

    fun init() {
        try {
            if (useMysql) Class.forName("com.mysql.cj.jdbc.Driver") else Class.forName("org.sqlite.JDBC")

            database = if (useMysql) Database.connect("jdbc:$sql:$databaseName", "com.mysql.cj.jdbc.Driver", user, password) else Database.connect("jdbc:$sql:$databaseName", "org.sqlite.JDBC")

            transaction(database) {
                SchemaUtils.createMissingTablesAndColumns(Scoreboards)
            }

            log("&aSuccessfully initialized Database!")
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }

    fun getDatabase() = database
}