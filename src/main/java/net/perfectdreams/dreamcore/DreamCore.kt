package net.perfectdreams.dreamcore

import net.perfectdreams.dreamcore.commands.DreamCoreCommand
import net.perfectdreams.dreamcore.eventmanager.DreamEventManager
import net.perfectdreams.dreamcore.listeners.EntityListener
import net.perfectdreams.dreamcore.listeners.SocketListener
import net.perfectdreams.dreamcore.network.socket.SocketServer
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.tables.Users
import net.perfectdreams.dreamcore.utils.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.concurrent.thread

class DreamCore : JavaPlugin() {
	companion object {
		lateinit var dreamConfig: DreamConfig
		val INSTANCE
			get() = Bukkit.getPluginManager().getPlugin("DreamCore") as DreamCore
	}

	val dreamEventManager = DreamEventManager()
	lateinit var dreamScriptManager: DreamScriptManager

	override fun onEnable() {
		saveDefaultConfig()

		if (!config.contains("server-name")) {
			logger.severe { "Você esqueceu de colocar o \"server-name\" na configuração! Desligando servidor... :(" }
			Bukkit.shutdown()
			return
		}

		// Carregar configuração
		dreamConfig = DreamConfig(config.getString("server-name"), config.getString("bungee-name")).apply {
			this.withoutPermission = config.getString("without-permission", "§cVocê não tem permissão para fazer isto!")
			this.blacklistedWorldsTeleport = config.getStringList("blacklisted-worlds-teleport")
			this.blacklistedRegionsTeleport = config.getStringList("blacklisted-regions-teleport")
			this.isStaffPermission = config.getString("staff-permission", "perfectdreams.staff")
			this.databaseName = config.getString("database-name", "perfectdreams")
			this.mongoDbIp = config.getString("mongodb-ip", "10.0.0.3")
			this.serverDatabaseName = config.getString("server-database-name", "dummy")
			this.postgreSqlIp = config.getString("postgresql-ip", "10.0.0.6")
			this.postgreSqlPort = config.getInt("postgresql-port", 5432)
			this.postgreSqlUser = config.getString("postgresql-user", "postgres")
			this.postgreSqlPassword = config.getString("postgresql-password", "")
			this.enablePostgreSql = config.getBoolean("enable-postgresql", true)
			if (config.contains("spawn-location"))
				this.spawn = config.getSerializable("spawn-location", Location::class.java)
			this.pantufaWebhook = config.getString("webhooks.warn")
			this.pantufaInfoWebhook = config.getString("webhooks.info")
			this.pantufaErrorWebhook = config.getString("webhooks.error")
			this.socketPort = config.getInt("socket-port", -1)
		}

		if (dreamConfig.socketPort != -1) {
			thread { SocketServer(dreamConfig.socketPort).start() }
			Bukkit.getPluginManager().registerEvents(SocketListener(), this)
		}

		if (dreamConfig.enablePostgreSql) {
			transaction(Databases.databaseNetwork) {
				SchemaUtils.createMissingTablesAndColumns(Users)
			}
		} else {
			logger.warning { "Suporte ao PostgreSQL está desativado!" }
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

		DreamCoreCommand(this).register()

		dreamScriptManager = DreamScriptManager(this)
		dreamScriptManager.loadScripts()

		ArmorStandHologram.loadArmorStandsIdsMarkedForRemoval()
		dreamEventManager.startEventsTask()
	}

	override fun onDisable() {
	}
}