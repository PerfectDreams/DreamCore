package net.perfectdreams.dreamcore.scriptmanager

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.*
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.RegisteredListener

open class DreamScript {
	lateinit var fileName: String

	val commands = mutableListOf<AbstractCommand>()
	val listeners = mutableListOf<RegisteredListener>()
	var onLoad: DreamScriptGenericCallback? = null
	var onUnload: DreamScriptGenericCallback? = null

	fun enable() {
		onLoad?.execute()
	}

	fun disable() {
		onUnload?.execute()
		commands.forEach {
			it.unregister()
		}
		commands.clear()
		listeners.forEach {
			for (handler in HandlerList.getHandlerLists()) {
				handler.unregister(it)
			}
		}
		listeners.clear()
	}

	fun onCommand(label: String, callback: DreamScriptCommandCallback, aliases: Array<String>?) {
		val command = object: AbstractCommand(label, (aliases ?: arrayOf()).toMutableList()) {
			@Subcommand
			fun stuff(sender: CommandSender) {
				callback.execute(sender)
			}
		}
		command.register()
		commands.add(command)
	}

	fun onEvent(event: String, priority: String, ignoreCancelled: Boolean, callback: DreamScriptGenericCallback) {
		onEvent(event, EventPriority.valueOf(priority), ignoreCancelled, callback)
	}

	fun onEvent(event: String, priority: EventPriority, ignoreCancelled: Boolean, callback: DreamScriptGenericCallback) {
		val globalListener = object: RegisteredListener(object: Listener {}, EventExecutor { p0, p1 ->
			if (p1.eventName != event)
				return@EventExecutor

			callback.execute(p1)
		}, priority, DreamCore.INSTANCE, ignoreCancelled) {}

		for (handler in HandlerList.getHandlerLists()) {
			handler.register(globalListener)
		}

		listeners.add(globalListener)
	}

	@FunctionalInterface
	interface DreamScriptCommandCallback {
		fun execute(sender: CommandSender)
	}

	@FunctionalInterface
	interface DreamScriptGenericCallback {
		fun execute(vararg any: Any)
	}
}