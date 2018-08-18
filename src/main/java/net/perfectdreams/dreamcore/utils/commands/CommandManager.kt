package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.command.CommandSender

object CommandManager {
	val argumentContexts = mutableMapOf<Class<*>, ((CommandSender, String) -> (Any?))>()
	val contexts = mutableMapOf<Class<*>, ((CommandSender) -> (Any?))>()

	fun <T> registerArgumentContext(clazz: Class<T>, callback: (CommandSender, String) -> (Any?)) {
		argumentContexts[clazz] = callback
	}

	fun <T> registerSenderContext(clazz: Class<T>, callback: (CommandSender) -> (Any?)) {
		contexts[clazz] = callback
	}
}