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

package me.dannly.party.utils

import org.bukkit.command.CommandSender

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