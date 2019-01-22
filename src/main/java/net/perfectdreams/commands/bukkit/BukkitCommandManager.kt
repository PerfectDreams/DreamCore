package net.perfectdreams.commands.bukkit

import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.manager.CommandContinuationType
import net.perfectdreams.commands.manager.DispatchableCommandManager
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class BukkitCommandManager(val plugin: Plugin) : DispatchableCommandManager<CommandSender, SparklyCommand, SparklyDSLCommand>() {
	init {
		contextManager.registerContext<CommandSender>(
				{ clazz: KClass<*> -> clazz.isSubclassOf(Player::class) || clazz == Player::class },
				{ sender, clazz, stack ->
					val pop = stack.pop()
					Bukkit.getPlayer(pop)
				}
		)

		commandListeners.addCommandListener { commandSender, sparklyCommand ->
			// Permissões
			if (sparklyCommand.permission != null && !commandSender.hasPermission(sparklyCommand.permission)) {
				commandSender.sendMessage(DreamCore.dreamConfig.withoutPermission)
				CommandContinuationType.CANCEL
			} else {
				CommandContinuationType.CONTINUE
			}
		}
	}

	private fun getCommandMap(): CommandMap {
		return Bukkit.getCommandMap()
	}

	override fun getRegisteredCommands(): List<SparklyCommand> {
		return getCommandMap().knownCommands.values
				.filter { it is BukkitCommandWrapper }
				.map { it as BukkitCommandWrapper }
				.filter { it.commandManager.plugin == plugin }
				.map { it.sparklyCommand }
	}

	override fun registerCommand(command: SparklyCommand) {
		val cmd = BukkitCommandWrapper(this, command)
		this.getCommandMap().register(cmd.label, cmd as Command)
	}

	override fun unregisterCommand(command: SparklyCommand) {
		val cmd = command

		try {
			val knownCommands = this.getCommandMap().knownCommands
			val toRemove = ArrayList<String>()
			for ((key, value) in knownCommands) {
				if (value == cmd) {
					toRemove.add(key)
				}
			}
			for (str in toRemove) {
				println("Desregistrando ${str}...")
				knownCommands.remove(str)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override suspend fun dispatch(sender: CommandSender, command: SparklyCommand, label: String, arguments: Array<String>): Boolean {
		// O CraftBukkit já irá processar os comandos marotamente, não precisamos verificar a label pois já sabemos que o comando está certo

		for (subCommand in command.subcommands) {
			if (dispatch(sender, subCommand as SparklyCommand, arguments.drop(0).firstOrNull() ?: "", arguments.drop(1).toTypedArray()))
				return true
		}

		return execute(sender, command, arguments)
	}
}