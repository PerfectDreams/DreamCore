package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.BaseCommand

open class SparklyCommand(override val labels: Array<out String>, val permission: String? = null) : BaseCommand {
	override val subcommands: MutableList<BaseCommand> = mutableListOf()

	init {
		registerSubcommands()
	}
}