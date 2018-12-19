package net.perfectdreams.commands.bukkit

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginIdentifiableCommand
import org.bukkit.plugin.Plugin

class BukkitCommandWrapper(val commandManager: BukkitCommandManager, val sparklyCommand: SparklyCommand) : Command(
		sparklyCommand.labels.first(), // Label
		"", // Description (nobody cares)
		"/${sparklyCommand.labels.first()}", // Usage Message (nobody cares²)
		sparklyCommand.labels.drop(0) // Aliases, vamos retirar a primeira (que é a label) e vlw flw
), PluginIdentifiableCommand {
	override fun getPlugin(): Plugin {
		return commandManager.plugin
	}

	override fun execute(p0: CommandSender, p1: String, p2: Array<String>): Boolean {
		commandManager.dispatch(p0, sparklyCommand, p1, p2)
		return true
	}
}