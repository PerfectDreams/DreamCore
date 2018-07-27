package net.perfectdreams.dreamcore.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.DreamUtils
import org.bukkit.entity.Player
import java.io.File

@CommandPermission("dreamcore.setup")
class DreamCoreCommand(val configFile: File) : BaseCommand() {
	@Subcommand("set_spawn")
	fun setSpawn(player: Player) {
		DreamCore.dreamConfig.spawn = player.location

		configFile.writeText(DreamUtils.gson.toJson(DreamCore.dreamConfig))

		player.sendMessage("§aSpawn atualizado!")
	}
}