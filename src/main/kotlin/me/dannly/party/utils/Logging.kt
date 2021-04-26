package me.dannly.party.utils

import me.dannly.party.Main
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

object Logging {
    fun getMessage(raw: String): String {
        var s = raw
        run {
            val pattern = Pattern.compile("@.*@", Pattern.DOTALL)
            val matcher = pattern.matcher(s)
            while (matcher.find()) {
                val group = matcher.group()
                s = s.replace(group.toRegex(), Main.getConfig(group.replace("@".toRegex(), "")))
            }
        }
        return s
    }

    fun log(sender: CommandSender, raw: String) {
        sender.sendMessage(getMessage(raw))
    }

    fun console(message: String, disablePlugin: Boolean = false) {
        println("${ChatColor.YELLOW}[${Main.instance.description.name}] $message")
        if (disablePlugin)
            Bukkit.getPluginManager().disablePlugin(Main.instance)
    }
}