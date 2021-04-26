package me.dannly.party.party

import me.dannly.party.Main
import me.dannly.party.utils.Database
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.util.*

/**
 * The main class for creating and managing parties
 * @author Dannly
 */
class Party {

    /**
     * The list of UUIDs (players) of this party
     */
    val players = mutableListOf<UUID>()

    /**
     * The unique ID for this party
     */
    var id: UUID = UUID.randomUUID()

    /**
     * Constructor to create a party from the [players]
     */
    constructor(vararg players: OfflinePlayer) {
        this.players.addAll(players.map { it.uniqueId })
        setupScoreboard()
        Database.addOrUpdate(this)
    }

    /**
     * Internal use only, please refrain from using this
     */
    constructor(players: List<OfflinePlayer>) {
        this.players.addAll(players.map { it.uniqueId })
    }

    /**
     * Gets the currently online players of this party
     */
    val onlinePlayers: List<Player>
        get() {
            return players.map { Bukkit.getOfflinePlayer(it) }.mapNotNull { it.player }
        }

    /**
     * (Re)organizes the scoreboard for all currently online party members
     */
    fun setupScoreboard() {
        for (s in onlinePlayers) {
            var board: Scoreboard = s.scoreboard
            if (Bukkit.getOnlinePlayers().filter { it != s }.map { it.scoreboard }.contains(board))
                board = Bukkit.getScoreboardManager()!!.newScoreboard
            val objectiveName = "party"
            val sidebar = board.getObjective(DisplaySlot.SIDEBAR)
            if (sidebar?.name != "party")
                sidebar?.unregister()
            if (board.getObjective(objectiveName) == null) {
                val objective = board.registerNewObjective(objectiveName, "dummy", "Party")
                objective.displaySlot = DisplaySlot.SIDEBAR
            }
            s.scoreboard = board
            updatePlayers(s)
        }
    }

    private fun updatePlayers(player: Player) {
        val scoreboard = player.scoreboard
        val nearbyPlayers = getNearbyPlayers(player)
        scoreboard.entries.forEach { scoreboard.resetScores(it) }
        for (u in players) {
            val offlinePlayer = Bukkit.getOfflinePlayer(u)
            val prefix: String = if (!offlinePlayer.isOnline) {
                ChatColor.RED.toString()
            } else {
                val s = offlinePlayer.player
                val builder = StringBuilder()
                val contains = nearbyPlayers.contains(s)
                builder.append(if (contains) ChatColor.GREEN.toString() else ChatColor.RED.toString())
                if (s == player)
                    builder.append(ChatColor.YELLOW.toString())
                if (contains)
                    builder.append("+5% XP ")
                builder.toString()
            }
            scoreboard.getObjective("party")!!.getScore("$prefix${offlinePlayer.name}").score = players.indexOf(u)
        }
    }

    /**
     * Gets the players eligible for the additional XP system around the specified [player]
     */
    fun getNearbyPlayers(player: Player): List<Player> {
        return onlinePlayers.filter {
            it.location.distance(player.location) <= Main.instance.config.getDouble(
                "party-distance",
                100.0
            )
        }
    }

    /**
     * Gets the players eligible for the additional XP system based on a specific [location]
     */
    fun getNearbyPlayers(location: Location): List<Player> {
        return onlinePlayers.filter {
            it.location.distance(location) <= Main.instance.config.getDouble(
                "party-distance",
                100.0
            )
        }
    }

    /**
     * Adds [players] to this party
     */
    fun addPlayers(vararg players: OfflinePlayer) {
        this.players.addAll(players.map { it.uniqueId })
        setupScoreboard()
        if (players.isNotEmpty())
            Database.addOrUpdate(this)
    }

    /**
     * Remove [players] from this party
     */
    fun removePlayers(vararg players: OfflinePlayer) {
        this.players.removeAll(players.map { it.uniqueId })
        for (player in players.mapNotNull { it.player })
            player.scoreboard.getObjective("party")?.unregister()
        if (this.players.isEmpty()) {
            Database.clear(this)
            return
        } else {
            setupScoreboard()
        }
        if (players.isNotEmpty())
            Database.addOrUpdate(this)
    }

    companion object {

        /**
         * Gets the maximum party size
         */
        val maximumPartySize
            get() = Main.instance.config.getInt("party-maximum-size", 10)

        /**
         * A mutable list containing all parties for easier access and management
         */
        private val parties: List<Party>
            get() = Database.readParties

        /**
         * Retrieves the party associated with the specified [uuid]
         */
        fun getParty(uuid: UUID): Party? {
            return parties.find { it.players.contains(uuid) }
        }

        /**
         * Checks if there is a party containing the specified [uuid]
         */
        fun hasParty(uuid: UUID): Boolean {
            return parties.any { it.players.contains(uuid) }
        }
    }
}