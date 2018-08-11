package net.perfectdreams.dreamcore

import co.aikar.commands.PaperCommandManager
import com.comphenix.attribute.NbtFactory
import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.commands.DreamCoreCommand
import net.perfectdreams.dreamcore.listeners.EntityListener
import net.perfectdreams.dreamcore.listeners.SocketListener
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.socket.SocketServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.concurrent.thread

class DreamCore : JavaPlugin() {
	companion object {
		lateinit var dreamConfig: DreamConfig
	}

	override fun onEnable() {
		saveDefaultConfig()

		if (!config.contains("server-name")) {
			logger.severe { "Você esqueceu de colocar o \"server-name\" na configuração! Desligando servidor... :(" }
			Bukkit.shutdown()
			return
		}

		// Carregar configuração
		val dreamConfig = DreamConfig(config.getString("server-name"), config.getString("bungee-name")).apply {
			dreamConfig.withoutPermission = config.getString("without-permission", "§cVocê não tem permissão para fazer isto!")
			dreamConfig.blacklistedWorldsTeleport = config.getStringList("blacklisted-worlds-teleport")
			dreamConfig.blacklistedRegionsTeleport = config.getStringList("blacklisted-regions-teleport")
			dreamConfig.isStaffPermission = config.getString("staff-permission", "perfectdreams.staff")
			dreamConfig.databaseName = config.getString("database-name", "perfectdreams")
			dreamConfig.serverDatabaseName = config.getString("server-database-name", "dummy")
			dreamConfig.spawn = config.getSerializable("spawn-location", Location::class.java)
			dreamConfig.pantufaWebhook = config.getString("webhooks.warn")
			dreamConfig.pantufaInfoWebhook = config.getString("webhooks.info")
			dreamConfig.pantufaErrorWebhook = config.getString("webhooks.error")
			dreamConfig.socketPort = config.getInt("socket-port", -1)
		}

		if (dreamConfig.socketPort != -1) {
			thread { SocketServer(dreamConfig.socketPort).start() }
			Bukkit.getPluginManager().registerEvents(SocketListener(), this)
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
		manager.registerCommand(DreamCoreCommand(this))

		ArmorStandHologram.loadArmorStandsIdsMarkedForRemoval()
	}

	override fun onDisable() {
	}
}