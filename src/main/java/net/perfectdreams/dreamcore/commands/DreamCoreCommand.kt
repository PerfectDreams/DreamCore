package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.withoutPermission
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

class DreamCoreCommand(val configFile: File) : AbstractCommand("dreamcore") {
	override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
		if (!p0.hasPermission("dreamcore.setup")) {
			p0.sendMessage(withoutPermission)
			return true
		}

		val arg0 = p3.getOrNull(0)
		val arg1 = p3.getOrNull(1)

		if (p0 is Player) {
			if (arg0 == "set_spawn") {
				DreamCore.dreamConfig.spawn = p0.location

				configFile.writeText(DreamUtils.gson.toJson(DreamCore.dreamConfig))

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
}