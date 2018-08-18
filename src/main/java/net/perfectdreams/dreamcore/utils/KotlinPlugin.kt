package net.perfectdreams.dreamcore.utils

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

/**
 * Classe que permite "reload" sem explodir o servidor
 *
 * Todos os plugins do PerfectDreams devem extender a classe KotlinPlugin!
 */
open class KotlinPlugin : JavaPlugin() {
	// Lista de comandos registrados por este plugin
	val commandList = mutableListOf<AbstractCommand>()
	val commandManager by lazy { PaperCommandManager(this) }

	override fun onEnable() {
		softEnable()
	}

	override fun onDisable() {
		softDisable()
	}

	open fun softEnable() {

	}

	open fun softDisable() {
		// Primeiro nós iremos desregistrar todos os comandos deste plugin
		commandList.forEach{
			// it.unregister()
		}

		// E depois nós iremos desregistrar todos os eventos ao desligar
		HandlerList.getHandlerLists().forEach{
			it.unregister(this)
		}

		// Problema resolvido!
	}

	/**
	 * Registra um comando
	 */
	fun registerCommand(command: AbstractCommand) {
		command.register()
		commandList.add(command)
	}

	/**
	 * Registra um comando
	 *
	 * @param command comando
	 */
	fun registerCommand(command: BaseCommand) {
		commandManager.registerCommand(command)
	}
}