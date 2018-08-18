package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.entity.Player

class DreamCoreCommand(val m: DreamCore) : AbstractCommand("dreamcore", permission = "dreamcore.setup") {
	@Subcommand(["set_spawn"])
	fun setSpawn(player: Player) {
		DreamCore.dreamConfig.spawn = player.location
		m.config.set("spawn-location", player.location)
		m.saveConfig()

		player.sendMessage("§aSpawn atualizado!")
	}
}