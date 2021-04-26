package me.dannly.party.utils

import me.dannly.party.Main
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.function.BiConsumer

object Invite {
    private val invites = HashMap<UUID, MutableList<UUID>>()

    fun invite(a: Player, b: Player, action: BiConsumer<Player, Player>? = null) {
        invites[a.uniqueId] = invites.getOrDefault(a.uniqueId, mutableListOf()).also { it.add(b.uniqueId) }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, {
            val i = invites.getOrDefault(a.uniqueId, mutableListOf()).also { it.remove(a.uniqueId) }.ifEmpty {
                invites.remove(a.uniqueId)
                mutableListOf()
            }
            if (i.isNotEmpty())
                invites[a.uniqueId] = i
            (a as Player?)?.let { (b as Player?)?.run { action?.accept(it, this) } }
        }, 20 * Main.instance.config.getLong("party-invite-expire-seconds", 15))
    }

    fun hasInvite(b: Player): Boolean {
        return invites.values.any { it.contains(b.uniqueId) }
    }

    fun removeInvite(b: Player) {
        invites.remove(getInviter(b))
    }

    fun getInviter(b: Player): UUID? {
        return invites.keys.find { invites[it]!!.contains(b.uniqueId) }
    }
}