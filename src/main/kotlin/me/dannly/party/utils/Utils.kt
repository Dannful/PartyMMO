package me.dannly.party.utils

import org.bukkit.command.CommandSender
import java.util.*

object Utils {
    fun getListForPlayer(sender: CommandSender, vararg elements: String): List<String> {
        val list: MutableList<String> = ArrayList()
        for (element in elements) {
            if (!element.contains(":")) {
                list.add(element)
                continue
            }
            val split = element.split(":".toRegex()).toTypedArray()
            if (split.size == 2 && sender.hasPermission(split[1])) list.add(split[0])
        }
        return list.sorted()
    }
}