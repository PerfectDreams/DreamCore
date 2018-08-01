package net.perfectdreams.dreamcore

import co.aikar.commands.PaperCommandManager
import com.comphenix.attribute.NbtFactory
import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.commands.DreamCoreCommand
import net.perfectdreams.dreamcore.listeners.EntityListener
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.socket.SocketServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.concurrent.thread

class DreamCore : JavaPlugin() {
	companion object {
		var dreamConfig = DreamConfig()
	}

	override fun onEnable() {
		val configFile = File(dataFolder, "config.json")

		if (!configFile.exists()) {
			println("config.json não existe! Desligando servidor... :(")
			Bukkit.shutdown()
			return
		}

		dreamConfig = DreamUtils.gson.fromJson(configFile.readText())

		if (dreamConfig.socketPort != -1) {
			thread { SocketServer(dreamConfig.socketPort).start() }
		}

		PhoenixScoreboard.init()
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null)
			SignGUIUtils.registerSignGUIListener()

		Bukkit.getPluginManager().registerEvents(EntityListener(), this)

		// Iniciar funções do Vault dentro de um try ... catch
		// É necessário ficar dentro de um try ... catch para caso o servidor não tenha algum
		// hook do Vault (por exemplo, não possuir um hook para o chat)
		try { VaultUtils.setupChat() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupEconomy() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupPermissions() } catch (e: NoClassDefFoundError) {}

		val manager = PaperCommandManager(this)
		manager.registerCommand(DreamCoreCommand(configFile))

		ArmorStandHologram.loadArmorStandsIdsMarkedForRemoval()
	}

	override fun onDisable() {
	}
}

fun main(args: Array<String>) {
	val nbtFactory = NbtFactory()
}

class DreamConfig {
	lateinit var serverName: String
	lateinit var bungeeName: String
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