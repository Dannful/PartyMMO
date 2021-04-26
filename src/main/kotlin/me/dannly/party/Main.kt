/*
 * Copyright 2021 Dannly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to use
 * and distribute it as a library or an Application Programming Interface without
 * billing purposes.
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

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

    companion object {
        val instance: Main
            get() = getPlugin(Main::class.java)

        fun getConfig(key: String): String {
            return ChatColor.translateAlternateColorCodes('&', instance.config.getString(key, "")!!)
        }
    }
}