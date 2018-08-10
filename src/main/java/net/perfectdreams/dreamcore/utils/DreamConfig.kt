package net.perfectdreams.dreamcore.utils

import org.bukkit.Location

class DreamConfig(val serverName: String, val bungeeName: String) {
	var withoutPermission = "§cVocê não tem permissão para fazer isto!"
	var blacklistedWorldsTeleport: List<String> = mutableListOf()
	var blacklistedRegionsTeleport: List<String> = mutableListOf()
	var isStaffPermission = "perfectdreams.staff"
	var databaseName = "perfectdreams"
	var serverDatabaseName = "perfectdreams_survival"
	lateinit var spawn: Location
	lateinit var pantufaWebhook: String
	lateinit var pantufaInfoWebhook: String
	lateinit var pantufaErrorWebhook: String
	var socketPort = -1
}