package net.perfectdreams.dreamcore

import com.github.salomonbrys.kotson.fromJson
import net.perfectdreams.dreamcore.utils.*
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.socket.SocketServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
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

		dreamConfig = DreamUtils.gson.fromJson<DreamConfig>(configFile.readText())

		if (dreamConfig.socketPort != -1) {
			thread { SocketServer(dreamConfig.socketPort).start() }
		}

		PhoenixScoreboard.init()
		SignGUIUtils.registerSignGUIListener()
		// Iniciar funções do Vault dentro de um try ... catch
		// É necessário ficar dentro de um try ... catch para caso o servidor não tenha algum
		// hook do Vault (por exemplo, não possuir um hook para o chat)
		try { VaultUtils.setupChat() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupEconomy() } catch (e: NoClassDefFoundError) {}
		try { VaultUtils.setupPermissions() } catch (e: NoClassDefFoundError) {}

		object: AbstractCommand("dreamcore") {
			override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
				if (!p0.hasPermission("dreamcore.setup")) {
					p0.sendMessage(withoutPermission)
					return true
				}

				val arg0 = p3.getOrNull(0)
				val arg1 = p3.getOrNull(1)

				if (p0 is Player) {
					if (arg0 == "set_spawn") {
						dreamConfig.spawn = p0.location

						configFile.writeText(DreamUtils.gson.toJson(dreamConfig))

						p0.sendMessage("§aSpawn atualizado!")
						return true
					}
				}

				if (arg0 == "softreload" && arg1 != null) {
					val plugin = Bukkit.getPluginManager().getPlugin(arg1)

					if (plugin == null) {
						p0.sendMessage("§cPlugin §e${arg1}§c não existe! Talvez você queria usar §6/plugman reload§c?")
						return true
					}

					if (plugin !is KotlinPlugin) {
						p0.sendMessage("§cPlugin §e${arg1}§c não extende KotlinPlugin!")
						return true
					}

					p0.sendMessage("§7\"Soft\" desativando §e${plugin.name}§7...")
					plugin.softDisable()
					p0.sendMessage("§7\"Soft\" ativando §e${plugin.name}§7...")
					plugin.softEnable()
					p0.sendMessage("§e${plugin.name}§a foi \"soft\" recarregado com sucesso!")
					return true
				}

				if (arg0 == "load" && arg1 != null) {
					val plugin = Bukkit.getPluginManager().getPlugin(arg1)

					if (plugin == null) {
						p0.sendMessage("§cPlugin §e${arg1}§c não existe! Talvez você queria usar §6/plugman load§c?")
						return true
					}

					if (plugin !is KotlinPlugin) {
						p0.sendMessage("§cPlugin §e${arg1}§c não extende KotlinPlugin!")
						return true
					}

					p0.sendMessage("§7\"Soft\" ativando §e${plugin.name}§7...")
					plugin.softEnable()
					p0.sendMessage("§e${plugin.name}§a foi \"soft\" ativado com sucesso!")
					return true
				}

				if (arg0 == "unload" && arg1 != null) {
					val plugin = Bukkit.getPluginManager().getPlugin(arg1)

					if (plugin == null) {
						p0.sendMessage("§cPlugin §e${arg1}§c não existe! Talvez você queria usar §6/plugman unload§c?")
						return true
					}

					if (plugin !is KotlinPlugin) {
						p0.sendMessage("§cPlugin §e${arg1}§c não extende KotlinPlugin!")
						return true
					}

					p0.sendMessage("§7\"Soft\" dtivando §e${plugin.name}§7...")
					plugin.softEnable()
					p0.sendMessage("§e${plugin.name}§a foi \"soft\" ativado com sucesso!")
					return true
				}

				p0.sendMessage("§e/dreamcore set_spawn")
				p0.sendMessage("§e/dreamcore softreload")
				p0.sendMessage("§e/dreamcore softload")
				p0.sendMessage("§e/dreamcore softunload")
				return true
			}
		}.register()
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