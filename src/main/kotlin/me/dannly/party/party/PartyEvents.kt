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

package me.dannly.party.party

import me.dannly.party.Main
import me.dannly.party.utils.InventoryLib
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class PartyEvents : Listener {
    @EventHandler
    fun move(event: PlayerMoveEvent) {
        val to = event.to
        if (to != null)
            updatePlayer(event.from, to, event.player)
    }

    @EventHandler
    fun drop(event: PlayerDropItemEvent) {
        val itemStack = arrayOf(
            Main.instance.config
                .getItemStack("party-invite-item", invite), Main.instance.config
                .getItemStack("party-request-item", request)
        )
        val itemStack1 = event.itemDrop.itemStack
        if (itemStack.contains(itemStack1)) {
            event.player.inventory.removeAll { it === itemStack1 }
            event.itemDrop.remove()
        }
    }

    @EventHandler
    fun click(event: InventoryClickEvent) {
        val itemStack = arrayOf(
            Main.instance.config
                .getItemStack("party-invite-item", invite), Main.instance.config
                .getItemStack("party-request-item", request)
        )
        if (itemStack.contains(event.cursor)) {
            val whoClicked = event.whoClicked
            if (event.clickedInventory != event.view.bottomInventory) {
                event.isCancelled = true
                if (whoClicked is Player)
                    whoClicked.updateInventory()
            }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        Bukkit.getScheduler()
            .runTask(Main.instance, Consumer { Party.getParty(event.player.uniqueId)?.setupScoreboard() })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun interact(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        if (event.rightClicked !is Player) return
        if (event.hand != EquipmentSlot.HAND) return
        val config = Main.instance.config
        val invite: ItemStack = config
            .getItemStack("party-invite-item", invite)!!
        val request: ItemStack = config
            .getItemStack("party-request-item", request)!!
        val b = player.inventory.itemInMainHand == invite
        if (b || player.inventory.itemInMainHand == request) {
            val rightClicked = event.rightClicked as Player
            event.isCancelled = true
            player.chat("/party " + (if (b) "invite" else "request") + " ${rightClicked.name}")
        }
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        Party.getParty(event.player.uniqueId)?.setupScoreboard()
    }

    private fun updatePlayer(from: Location, to: Location, player: Player) {
        val party: Party? = Party.getParty(player.uniqueId)
        if (party != null) {
            if (party.getNearbyPlayers(from) != party.getNearbyPlayers(to)) {
                Bukkit.getScheduler().runTask(Main.instance, Runnable { party.setupScoreboard() })
            }
        }
    }

    @EventHandler
    fun teleport(event: PlayerTeleportEvent) {
        val to = event.to
        if (to != null)
            updatePlayer(event.from, to, event.player)
    }

    @EventHandler
    fun entityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer
        if (killer != null) {
            val p: Party? = Party.getParty(killer.uniqueId)
            if (p != null) {
                val nearbyPlayers = p.getNearbyPlayers(killer)
                if (nearbyPlayers.isNotEmpty()) {
                    event.droppedExp = (event.droppedExp * (1 + nearbyPlayers.size * 0.05)).toInt()
                }
            }
        }
    }

    companion object {
        val invite = InventoryLib.getItem(Material.PLAYER_HEAD, 1, ChatColor.RESET.toString() + "Invite", "")
        val request = InventoryLib.getItem(Material.PLAYER_HEAD, 1, ChatColor.RESET.toString() + "Request", "")
    }
}