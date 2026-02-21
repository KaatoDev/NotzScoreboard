package dev.kaato.notzscoreboard.apis

import dev.kaato.notzscoreboard.NotzScoreboard.Companion.plugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Level

class NotzYAML(private val fileName: String) {
    companion object {
        private val yamlVersion: Int = 1
    }

    private val file = File(plugin.dataFolder, "$fileName.yml")
    lateinit var config: FileConfiguration

    init {
        ensureFile()
        reload()
    }

    fun get(): FileConfiguration = config

    fun reload() {
        config = YamlConfiguration.loadConfiguration(file)

        plugin.getResource("$fileName.yml")?.use { stream ->
            val defaults = YamlConfiguration.loadConfiguration(
                InputStreamReader(stream, Charsets.UTF_8)
            )
            config.setDefaults(defaults)
            config.options().copyDefaults(true)
        }

        if (config.contains("version")) {
            val yamlOldVersion = config.getInt("version", 0)
            if (yamlOldVersion != yamlVersion) {
                backupOldConfig(yamlOldVersion)
                config.set("version", yamlVersion)
            }
        }

        save()
    }

    private fun backupOldConfig(oldVersion: Int) {
        val backup = File(file.parentFile, "$fileName.old.v$oldVersion.yml")
        file.renameTo(backup)
        plugin.saveResource("$fileName.yml", true)
    }

    fun save() {
        try {
            config.save(file)
        } catch (e: IOException) {
            plugin.logger.log(
                Level.SEVERE,
                "Could not save config ${file.name}",
                e
            )
        }
    }

    fun delete(path: String) {
        config.set(path, null)
        save()
    }

    private fun ensureFile() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!file.exists()) {
            plugin.saveResource("$fileName.yml", false)
        }
    }
}