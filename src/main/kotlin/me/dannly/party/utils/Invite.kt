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
            val i = invites.getOrDefault(a.uniqueId, mutableListOf()).also { it.remove(a.uniqueId) }
            if (i.isEmpty())
                invites.remove(a.uniqueId)
            else
                invites[a.uniqueId] = i
            (a to b).let { action?.accept(it.first, it.second) }
        }, 20 * Main.instance.config.getInt("party-invite-expire-seconds", 15).toLong())
    }

    fun hasInvited(a: Player): Boolean {
        return invites.containsKey(a.uniqueId)
    }

    fun hasInvite(b: Player): Boolean {
        return invites.values.find { it.contains(b.uniqueId) } != null
    }

    fun removeInvite(b: Player) {
        invites.remove(getInviter(b))
    }

    fun getInviter(b: Player): UUID? {
        return invites.keys.find { invites[it]!!.contains(b.uniqueId) }
    }
}