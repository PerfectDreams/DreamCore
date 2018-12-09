package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.io.File

class DreamCoreCommand(val m: DreamCore) : AbstractCommand("dreamcore", permission = "dreamcore.setup") {
	@Subcommand(["set_spawn"])
	fun setSpawn(player: Player) {
		DreamCore.dreamConfig.spawn = player.location
		m.config.set("spawn-location", player.location)
		m.saveConfig()

		player.sendMessage("§aSpawn atualizado!")
	}

	@Subcommand(["reload"])
	fun reloadScripts(executor: CommandSender, fileName: String) {
		if (fileName == "all") {
			executor.sendMessage("§aRecarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			m.dreamScriptManager.loadScripts()
			executor.sendMessage("§aProntinho! ${m.dreamScriptManager.scripts.size} scripts foram carregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			if (script == null) {
				executor.sendMessage("§cO script ${fileName} não existe! (Ou não está carregado, vai que né)")
				return
			}

			executor.sendMessage("§aRecarregando script $fileName!")
			m.dreamScriptManager.unloadScript(script)
			m.dreamScriptManager.loadScript(File(m.dataFolder, "scripts/$fileName"))
			executor.sendMessage("§aProntinho! $fileName foi carregado com sucesso!")
		}
		// m.dreamScriptManager
	}

	@Subcommand(["unload"])
	fun unloadScripts(executor: CommandSender, fileName: String) {
		if (fileName == "all") {
			executor.sendMessage("§Descarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			executor.sendMessage("§aProntinho! Todos os scripts foram descarregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			if (script == null) {
				executor.sendMessage("§cO script ${fileName} existe! Use reload seu tosco!")
				return
			}

			executor.sendMessage("§aDescarregando script $fileName!")
			m.dreamScriptManager.unloadScript(script)
			executor.sendMessage("§aProntinho! $fileName foi descarregado com sucesso!")
		}
		// m.dreamScriptManager
	}
}