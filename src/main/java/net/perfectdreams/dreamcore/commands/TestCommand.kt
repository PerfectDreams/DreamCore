package net.perfectdreams.dreamcore.commands

import net.perfectdreams.dreamcore.utils.DreamConfig
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

class TestCommand : AbstractCommand("test") {
	@Subcommand
	fun test(player: Player?, test: DreamConfig) {
		println(test.blacklistedRegionsTeleport)
	}

	@Subcommand(["face"])
	fun face(player: Player?, @InjectArgument(ArgumentType.CUSTOM_ARGUMENT) face: BlockFace) {
		println("Face: $face")
	}
}