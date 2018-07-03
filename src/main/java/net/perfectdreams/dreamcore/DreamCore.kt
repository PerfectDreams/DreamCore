package net.perfectdreams.dreamcore

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.commands.DreamCoreCommand
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.socket.SocketServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
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
		// Iniciar funções do Vault dentro de um try ... catch
		// É necessário ficar dentro de um try ... catch para caso o servidor não tenha algum
		// hook do Vault (por exemplo, não possuir um hook para o chat)
		try { VaultUtils.setupChat() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupEconomy() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupPermissions() } catch (e: NoClassDefFoundError) {}

		DreamCoreCommand(configFile).register()

		ArmorStandHologram.loadArmorStandsUniqueIds()
		ArmorStandHologram.killAllStoredArmorStands()
	}

	override fun onDisable() {
	}
}

class DreamConfig {
	lateinit var serverName: String
	lateinit var bungeeName: String
	var withoutPermission = "§cVocê não tem permissão para fazer isto!"
	var blacklistedWorldsTeleport: List<String> = mutableListOf()
	var blacklistedRegionsTeleport: List<String> = mutableListOf()
	var isStaffPermission = "perfectdreams.staff"
	lateinit var spawn: Location
	lateinit var pantufaWebhook: String
	lateinit var pantufaInfoWebhook: String
	lateinit var pantufaErrorWebhook: String
	var socketPort = -1
}