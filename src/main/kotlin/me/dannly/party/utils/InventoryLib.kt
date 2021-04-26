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

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object InventoryLib {

    fun getItem(
        type: Material?, amount: Int = 1, name: String?,
        vararg lore: String): ItemStack {
        val item = ItemStack(type!!, amount)
        val im = item.itemMeta
        if (name != null) if (name.isNotEmpty()) im!!.setDisplayName(name) else im!!.setDisplayName(ChatColor.RESET.toString())
        if (lore[0].isNotEmpty()) im!!.lore = listOf(*lore)
        item.itemMeta = im
        return item
    }

}