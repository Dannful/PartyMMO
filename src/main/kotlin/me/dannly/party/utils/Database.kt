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

import me.dannly.party.Main
import me.dannly.party.party.Party
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import java.sql.*
import java.util.*

object Database {

    private lateinit var deletePreparedStatement: PreparedStatement
    private lateinit var updatePreparedStatement: PreparedStatement
    private lateinit var selectPreparedStatement: PreparedStatement

    private val playersColumnSize = Party.maximumPartySize * 36 + Party.maximumPartySize - 1

    fun createConnection() {
        val config: FileConfiguration = Main.instance.config
        val username = config.getString("username").orEmpty()
        val password = config.getString("password").orEmpty()
        val ip = config.getString("host").orEmpty()
        val database = config.getString("database-name").orEmpty()
        val url = "jdbc:mysql://$ip/$database"
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(
                url,
                username,
                password
            )
            Logging.console("Successfully connected to the parties database!")
            val stmt = connection.createStatement()
            Logging.console("Attempting to create and/or connect to the table...")
            stmt.execute("create table if not exists parties (id VARCHAR(36) NOT NULL, players VARCHAR($playersColumnSize) NOT NULL, PRIMARY KEY (id))")
            stmt.close()
            Logging.console("Connection to the table established.")
            Logging.console("Preparing SQL statements...")
            deletePreparedStatement = connection.prepareStatement("delete from parties where id = ?")
            updatePreparedStatement =
                connection.prepareStatement("insert into parties (id, players) values (?, ?) on duplicate key update players = ?")
            selectPreparedStatement = connection.prepareStatement("select * from parties")
            Logging.console("Statements prepared.")
        } catch (e: ClassNotFoundException) {
            Logging.console("Failed to connect to MySQL, disabling plugin.", true)
        } catch (e: SQLException) {
            Logging.console("Failed to process the database, disabling plugin.", true)
        }
    }

    private lateinit var connection: Connection

    private const val errorMessage = "The table has been altered and thus can't be used properly. Disabling the plugin."

    fun get(): ResultSet? {
        if (!tableReady) {
            Logging.console(errorMessage, true)
            return null
        }
        return selectPreparedStatement.executeQuery()
    }

    fun clear(party: Party) {
        if (!tableReady) {
            Logging.console(errorMessage, true)
            return
        }
        deletePreparedStatement.setString(1, party.id.toString())
        deletePreparedStatement.executeUpdate()
    }

    fun addOrUpdate(party: Party) {
        if (!tableReady) {
            Logging.console(errorMessage, true)
            return
        }
        val players = party.players.joinToString(separator = ",", transform = { it.toString() })
        updatePreparedStatement.setString(1, party.id.toString())
        updatePreparedStatement.setString(2, players)
        updatePreparedStatement.setString(3, players)
        updatePreparedStatement.executeUpdate()
    }

    private val tableReady: Boolean
        get() {
            val table = connection.metaData.getColumns(null, null, "parties", null) ?: return false
            if (!table.next()) {
                table.close()
                return false
            }
            table.close()

            val primaryKeys = connection.metaData.getPrimaryKeys(null, null, "parties") ?: return false
            if (!primaryKeys.next()) {
                primaryKeys.close()
                return false
            }
            if (!primaryKeys.getString("COLUMN_NAME").equals("id")) {
                primaryKeys.close()
                return false
            }
            primaryKeys.close()

            val idColumn = connection.metaData.getColumns(null, null, "parties", "id") ?: return false
            if (!idColumn.next()) {
                idColumn.close()
                return false
            }
            if (idColumn.getInt("DATA_TYPE") != Types.VARCHAR) {
                idColumn.close()
                return false
            }
            if (idColumn.getInt("COLUMN_SIZE") < 36) {
                idColumn.close()
                return false
            }
            if (idColumn.getString("IS_NULLABLE").equals("yes", true)) {
                idColumn.close()
                return false
            }
            idColumn.close()

            val playersColumn = connection.metaData.getColumns(null, null, "parties", "players") ?: return false
            if (!playersColumn.next()) {
                playersColumn.close()
                return false
            }
            if (playersColumn.getInt("DATA_TYPE") != Types.VARCHAR) {
                playersColumn.close()
                return false
            }
            if (playersColumn.getInt("COLUMN_SIZE") < playersColumnSize) {
                playersColumn.close()
                return false
            }
            if (playersColumn.getString("IS_NULLABLE").equals("yes", true)) {
                playersColumn.close()
                return false
            }
            playersColumn.close()

            return true
        }

    val readParties: List<Party>
        get() {
            val parties = mutableListOf<Party>()
            if (!tableReady) {
                Logging.console(errorMessage, true)
                return parties
            }
            val resultSet = get() ?: return parties
            while (resultSet.next()) {
                val s = resultSet.getString("players")
                val element = Party(s.split(",").map { UUID.fromString(it) }
                    .map { Bukkit.getOfflinePlayer(it) })
                element.id = UUID.fromString(resultSet.getString("id"))
                parties.add(element)
            }
            resultSet.close()
            return parties
        }
}