package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.annotation.Subcommand
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
		commandManager.dispatchBlocking(p0, sparklyCommand, p1, p2)
		return true
	}

	override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
		val completions = mutableListOf<String>()

		val methods = sparklyCommand::class.java.methods

		val currentArgumentIndex = args.size
		val currentArgument = args.last()

		for (method in methods.filter { it.isAnnotationPresent(Subcommand::class.java) }.sortedByDescending { it.parameterCount }) {
			val annotation = method.getAnnotation(Subcommand::class.java)
			for (value in annotation.labels) {
				val split = value.split(" ")
				val arg = split.getOrNull(currentArgumentIndex) ?: continue

				if (arg.startsWith(currentArgument, true))
					completions.add(arg)
			}
		}

		completions.addAll(super.tabComplete(sender, alias, args))

		return completions
	}
}