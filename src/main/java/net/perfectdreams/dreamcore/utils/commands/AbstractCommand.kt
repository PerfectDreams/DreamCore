package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.Bukkit
import org.bukkit.command.*
import org.bukkit.entity.Player
import java.util.*

abstract class AbstractCommand @JvmOverloads constructor(command: String, protected val usage: String? = null, protected val description: String? = null, protected val permMessage: String? = null, protected val alias: List<String>? = null) : CommandExecutor, TabExecutor {
	private val command: String = command.toLowerCase()

	constructor(command: String, aliases: List<String>) : this(command, null, null, null, aliases) {}

	constructor(command: String, usage: String, aliases: List<String>) : this(command, null, null, null, aliases) {}

	constructor(command: String, usage: String, description: String, aliases: List<String>) : this(command, usage, description, null, aliases) {}

	fun register(): AbstractCommand {
		val cmd = ReflectCommand(this.command)
		if (this.alias != null) {
			cmd.aliases = this.alias
		}
		if (this.description != null) {
			cmd.description = this.description
		}
		if (this.usage != null) {
			cmd.usage = this.usage
		}
		if (this.permMessage != null) {
			cmd.permissionMessage = this.permMessage
		}
		this.getCommandMap().register("", cmd as Command)
		cmd.setExecutor(this)
		return this
	}

	fun unregister() {
		val cmd = this.getCommandMap().getCommand(this.command)
		try {
			val clazz = this.getCommandMap().javaClass
			val f = clazz.getDeclaredField("knownCommands")
			f.isAccessible = true
			val knownCommands = f.get(this.getCommandMap()) as MutableMap<String, Command>
			val toRemove = ArrayList<String>()
			for ((key, value) in knownCommands) {
				if (value === cmd) {
					toRemove.add(key)
				}
			}
			for (str in toRemove) {
				knownCommands.remove(str)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}

	}

	fun getCommandMap(): CommandMap {
		if (cmap == null) {
			try {
				val f = Bukkit.getServer().javaClass.getDeclaredField("commandMap")
				f.isAccessible = true
				cmap = f.get(Bukkit.getServer()) as CommandMap
				return this.getCommandMap()
			} catch (e: Exception) {
				e.printStackTrace()
				return this.getCommandMap()
			}

		}
		if (cmap != null) {
			return cmap as CommandMap
		}
		return this.getCommandMap()
	}

	override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
		run(p0, p1, p2, p3)
		if (p0 is Player)
			runIfPlayer(p0, p1, p2, p3)
		else
			runIfConsole(p0, p1, p2, p3)
		return true
	}

	open fun run(p0: CommandSender, p1: Command, p2: String, p3: Array<String>) {

	}

	open fun runIfPlayer(p0: Player, p1: Command, p2: String, p3: Array<String>) {

	}

	open fun runIfConsole(p0: CommandSender, p1: Command, p2: String, p3: Array<String>) {

	}

	override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String>? {
		if (args.size > 0) {
			val argument = args[args.size - 1].toLowerCase()
			val players = ArrayList<String>()
			for (player in Bukkit.getOnlinePlayers()) {
				if (player.name.toLowerCase().startsWith(argument)) {
					players.add(player.name)
				}
			}
			return players
		}
		return null
	}

	private inner class ReflectCommand constructor(command: String) : Command(command) {
		private var exe: AbstractCommand? = null

		init {
			this.exe = null
		}

		fun setExecutor(exe: AbstractCommand) {
			this.exe = exe
		}

		override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
			return this.exe != null && this.exe!!.onCommand(sender, this, commandLabel, args)
		}

		override fun tabComplete(sender: CommandSender, alais: String, args: Array<String>): List<String>? {
			if (this.exe != null) {
				return this.exe!!.onTabComplete(sender, this, alais, args)
			}
			return null
		}
	}

	companion object {
		protected var cmap: CommandMap? = null
	}
}