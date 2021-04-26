package me.dannly.party

import me.dannly.party.party.PartyCommand
import me.dannly.party.party.PartyEvents
import me.dannly.party.utils.Database
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()
        getCommand("party")!!.setExecutor(PartyCommand())
        getCommand("party")!!.tabCompleter = PartyCommand()
        server.pluginManager.registerEvents(PartyEvents(), this)
        Database.createConnection()
    }

    override fun onDisable() {
    }

    companion object {
        val instance: Main
            get() = getPlugin(Main::class.java)

        fun getConfig(key: String): String {
            return ChatColor.translateAlternateColorCodes('&', instance.config.getString(key, "")!!)
        }
    }
}