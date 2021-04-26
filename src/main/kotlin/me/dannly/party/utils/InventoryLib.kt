package me.dannly.party.utils

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object InventoryLib {

    fun getItem(type: Material?, amount: Int = 1, name: String?,
                vararg lore: String): ItemStack {
        val item = ItemStack(type!!, amount)
        val im = item.itemMeta
        if (name != null) if (name.isNotEmpty()) im!!.setDisplayName(name) else im!!.setDisplayName(ChatColor.RESET.toString())
        if (lore[0].isNotEmpty()) im!!.lore = listOf(*lore)
        item.itemMeta = im
        return item
    }

}