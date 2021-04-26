package me.dannly.party.party

import me.dannly.party.Main
import me.dannly.party.utils.Invite
import me.dannly.party.utils.Logging
import me.dannly.party.utils.Utils
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.util.stream.Collectors

class PartyCommand : CommandExecutor, TabCompleter {

    private val permission: String = Main.instance.config.getString("party-set-item-permission", "party.admin")!!

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            Logging.log(sender, "@player-only@")
            return true
        }
        if (label.equals("party", ignoreCase = true)) {
            if (args.isEmpty()) {
                sender.chat("/party help")
                return true
            }
            if (args[0].equals("help", ignoreCase = true)) {
                sender.sendMessage(
                    Logging.getMessage("@help-command@").replace(
                        "%c".toRegex(), Logging.getMessage(
                            "party create <player>\nparty getinvite\nparty help\nparty invite <player>\nparty join\nparty leave${
                                if (sender.hasPermission(
                                        permission
                                    )
                                ) "\nparty setinvite\nparty setrequest" else ""
                            }"
                        )
                    )
                )
            } else {
                val config: FileConfiguration = Main.instance.config
                if (args[0].equals("create", ignoreCase = true)) {
                    if (Party.hasParty(sender.uniqueId)) {
                        Logging.log(sender, "@already-in-party@")
                        return true
                    }
                    Party(sender)
                    Logging.log(sender, "@party-created@")
                } else if (args[0].equals("getinvite", true)) {
                    val itemStack = config
                        .getItemStack("party-invite-item", PartyEvents.invite)
                    if (!sender.inventory.contains(itemStack))
                        sender.inventory.addItem(
                            itemStack
                        )
                    else
                        sender.sendMessage(Logging.getMessage("@party-invite-item-already-possess@"))
                } else if (args[0].equals("setinvite", ignoreCase = true)) {
                    if (!sender.hasPermission(permission)) return true
                    if (sender.inventory.itemInMainHand.type == Material.AIR) {
                        Logging.log(sender, "@party-set-item-invalid@")
                        return true
                    }
                    val instance: Main = Main.instance
                    instance.config["party-invite-item"] =
                        sender.inventory.itemInMainHand
                    instance.saveConfig()
                    instance.reloadConfig()
                    Logging.log(sender, "@party-set-item-success@")
                } else if (args[0].equals("setrequest", ignoreCase = true)) {
                    if (!sender.hasPermission(permission)) return true
                    if (sender.inventory.itemInMainHand.type == Material.AIR) {
                        Logging.log(sender, "@party-set-item-invalid@")
                        return true
                    }
                    val instance: Main = Main.instance
                    instance.config["party-request-item"] =
                        sender.inventory.itemInMainHand
                    instance.saveConfig()
                    instance.reloadConfig()
                    Logging.log(sender, "@party-set-item-success@")
                } else if (args[0].equals("getrequest", true)) {
                    val itemStack = config
                        .getItemStack("party-request-item", PartyEvents.request)
                    if (!sender.inventory.contains(itemStack))
                        sender.inventory.addItem(
                            itemStack
                        )
                    else
                        sender.sendMessage(Logging.getMessage("@party-invite-item-already-possess@"))
                } else if (args[0].equals("accept", true)) {
                    val inviter = Invite.getInviter(sender)
                    if (inviter == null) {
                        Logging.log(sender, "@no-invitation@")
                        return true
                    }
                    val party = Party.getParty(sender.uniqueId) ?: Party(sender)
                    party.addPlayers(Bukkit.getOfflinePlayer(inviter))
                    Invite.removeInvite(sender)
                } else if (args[0].equals("join", ignoreCase = true)) {
                    val uuid = Invite.getInviter(sender)
                    if (uuid == null) {
                        Logging.log(sender, "@no-invitation@")
                        return true
                    }
                    val party: Party = Party.getParty(uuid)!!
                    party.addPlayers(sender)
                    Invite.removeInvite(sender)
                } else if (args[0].equals("leave", ignoreCase = true)) {
                    val party = Party.getParty(sender.uniqueId)
                    if (party == null) {
                        Logging.log(sender, "@not-in-party@")
                        return true
                    }
                    party.removePlayers(sender)
                } else {
                    if (args[0].equals("invite", ignoreCase = true)) {
                        if (args.size < 2) {
                            sender.chat("/party help")
                            return true
                        }
                        var party = Party.getParty(sender.uniqueId)
                        if (party == null)
                            party = Party(sender)
                        if (party.players.size == Party.maximumPartySize) {
                            Logging.log(sender, "@party-full@")
                            return true
                        }
                        val target = Bukkit.getPlayer(args[1])
                        if (target != null) {
                            if (target == sender) {
                                Logging.log(sender, "@party-invite-oneself@")
                                return true
                            }
                            if (Invite.hasInvite(target)) {
                                Logging.log(sender, "@has-invite@")
                                return true
                            }
                            if (Party.hasParty(target.uniqueId)) {
                                Logging.log(sender, "@party-invite-already-in@")
                                return true
                            }
                            Invite.invite(sender, target)
                            Logging.log(sender, "@party-invite-sent@")
                            target.spigot()
                                .sendMessage(
                                    *ComponentBuilder().appendLegacy(
                                        Main.getConfig("party-invite-received")
                                            .replace("%p".toRegex(), sender.name)
                                            .replace("%c".toRegex(), "/party join")
                                    )
                                        .event(
                                            HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Text(Main.getConfig("party-join-party"))
                                            )
                                        )
                                        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join")).create()
                                )
                        } else {
                            sender.sendMessage(
                                Logging.getMessage(
                                    Logging.getMessage("@invalid-player@").replace("%p".toRegex(), "%" + args[1] + "%")
                                )
                            )
                        }
                    } else if (args[0].equals("request", true)) {
                        if (args.size < 2) {
                            sender.chat("/party help")
                            return true
                        }
                        if (Party.hasParty(sender.uniqueId)) {
                            Logging.log(sender, "@already-in-party@")
                            return true
                        }
                        val player = Bukkit.getPlayer(args[1])
                        if (player == null) {
                            sender.sendMessage(
                                Logging.getMessage(
                                    Logging.getMessage("@invalid-player@").replace("%p".toRegex(), "%" + args[1] + "%")
                                )
                            )
                            return true
                        }
                        player.spigot().sendMessage(
                            *ComponentBuilder().appendLegacy(
                                Main.getConfig("party-request-message").replace("%p".toRegex(), player.name)
                            )
                                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept")).event(
                                    HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Text(Main.getConfig("party-request-chat-hover"))
                                    )
                                ).create()
                        )
                        Logging.log(sender, "@party-request-message-sent@")
                        Invite.invite(sender, player)
                    }
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String>? {
        if (label.equals("party", ignoreCase = true)) {
            if (args.size == 1) {
                return Utils.getListForPlayer(
                    sender,
                    "accept", "create",
                    "getinvite",
                    "getrequest",
                    "help",
                    "invite",
                    "join",
                    "leave",
                    "request",
                    "setinvite:$permission",
                    "setrequest:$permission"
                ).stream().filter { s: String -> s.startsWith(args[0].toLowerCase()) }.collect(Collectors.toList())
            } else if (args.size == 2) {
                if (args[0].equals("invite", ignoreCase = true)) {
                    return ArrayList(Bukkit.getOnlinePlayers()).filter { it != sender }
                        .map { it.name }
                        .filter { it.toLowerCase().startsWith(args[1].toLowerCase()) }
                } else if (args[0].equals("request", true)) {
                    return ArrayList(Bukkit.getOnlinePlayers()).filter { it != sender }.map { it.name }
                        .filter { it.toLowerCase().startsWith(args[1].toLowerCase()) }
                }
            }
        }
        return listOf()
    }
}