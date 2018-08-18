package net.perfectdreams.dreamcore.utils.commands

import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.utils.commands.annotation.ArgumentType
import net.perfectdreams.dreamcore.utils.commands.annotation.InjectArgument
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.commands.annotation.SubcommandPermission
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.kotlinFunction

open class AbstractCommand(
		val label: String,
		val aliases: List<String> = listOf(),
		val permission: String? = null,
		val withoutPermission: String? = null,
		val withoutPermissionCallback: ((CommandSender, String, Array<String>) -> (Unit))? = null
) {
	lateinit var reflectCommand: ReflectCommand
	val withoutPermissionCallbacks = mutableMapOf<String, ((CommandSender, String, Array<String>) -> (Unit))>()

	fun register(): AbstractCommand {
		val cmd = ReflectCommand(this.label, this)
		reflectCommand = cmd
		cmd.aliases = this.aliases
		this.getCommandMap().register(label, cmd as Command)
		return this
	}

	fun getCommandMap(): CommandMap {
		return Bukkit.getCommandMap()
	}

	class ReflectCommand constructor(command: String, val abstractCommand: AbstractCommand) : Command(command) {
		override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
			val baseClass = abstractCommand::class.java

			if (abstractCommand.permission != null && !sender!!.hasPermission(abstractCommand.permission)) {
				if (abstractCommand.withoutPermissionCallback != null) {
					abstractCommand.withoutPermissionCallback.invoke(sender, commandLabel, args)
					return true
				}
				sender.sendMessage(abstractCommand.withoutPermission ?: DreamCore.dreamConfig.withoutPermission)
				return true
			}

			// Ao executar, nós iremos pegar várias anotações para ver o que devemos fazer agora
			val methods = this.abstractCommand::class.java.methods

			for (method in methods.filter { it.isAnnotationPresent(Subcommand::class.java) }.sortedByDescending { it.parameterCount }) {
				val subcommandAnnotation = method.getAnnotation(Subcommand::class.java)
				val values = subcommandAnnotation.values
				for (value in values.map { it.split(" ") }) {
					var matchedCount = 0
					for ((index, text) in value.withIndex()) {
						val arg = args.getOrNull(index)
						if (text == arg)
							matchedCount++
					}
					val matched = matchedCount == value.size
					if (matched) {
						if (executeMethod(baseClass, method, sender, commandLabel, args,matchedCount))
							return true
					}
				}
			}

			// Nenhum comando foi executado... #chateado
			for (method in methods.filter { it.isAnnotationPresent(Subcommand::class.java) }.sortedByDescending { it.parameterCount }) {
				val subcommandAnnotation = method.getAnnotation(Subcommand::class.java)
				if (subcommandAnnotation.values.isEmpty()) {
					if (executeMethod(baseClass, method, sender, commandLabel, args, 0))
						return true
				}
			}
			return true
		}

		override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String>? {
			return listOf()
		}

		fun executeMethod(baseClass: Class<out AbstractCommand>, method: Method, sender: CommandSender, commandLabel: String, args: Array<String>, skipArgs: Int): Boolean {
			if (!checkPermission(baseClass, method, sender, commandLabel, args))
				return false

			// check method arguments
			val arguments = args.toMutableList()
			for (i in 0 until skipArgs)
				arguments.removeAt(0)

			val params = getContextualArgumentList(method, sender, arguments)

			// Agora iremos "validar" o argument list antes de executar
			for ((index, parameter) in method.kotlinFunction!!.valueParameters.withIndex()) {
				if (!parameter.type.isMarkedNullable && params[index] == null)
					return false
			}

			if (params.size != method.parameterCount)
				return false

			method.invoke(abstractCommand, *params.toTypedArray())
			return true
		}

		fun checkPermission(baseClass: Class<out AbstractCommand>, annotatedElement: AnnotatedElement, sender: CommandSender?, commandLabel: String, args: Array<String>): Boolean {
			val permissionAnnotation = annotatedElement.getAnnotation(SubcommandPermission::class.java)
			// println("Has permission annotation? $permissionAnnotation")

			if (permissionAnnotation != null && sender?.hasPermission(permissionAnnotation.permission) ?: false) {
				// Se o usuário não tem permissão...
				if (permissionAnnotation.callbackName.isNotEmpty()) {
					val callback = abstractCommand.withoutPermissionCallbacks[permissionAnnotation.callbackName] ?: throw RuntimeException("Callback ${permissionAnnotation.callbackName} não encontrado!")
					callback.invoke(sender!!, commandLabel, args)
					return false
				}
				val message = permissionAnnotation.permission.replace("{UseDefaultMessage}", DreamCore.dreamConfig.withoutPermission)
				sender?.sendMessage(message)
				return false
			}
			return true
		}

		fun getContextualArgumentList(method: Method, sender: CommandSender, arguments: MutableList<String>): List<Any?> {
			var dynamicArgIdx = 0
			val params = mutableListOf<Any?>()

			for ((index, param) in method.parameters.withIndex()) {
				val typeName = param.type.simpleName.toLowerCase()
				val injectArgumentAnnotation = param.getAnnotation(InjectArgument::class.java)
				when {
					injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.PLAYER_EXACT -> {
						val argument = arguments.getOrNull(dynamicArgIdx)
						dynamicArgIdx++
						if (argument == null) {
							params.add(null)
						} else {
							val player = Bukkit.getPlayerExact(argument)
							params.add(player)
						}
					}
					injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.PLAYER -> {
						val argument = arguments.getOrNull(dynamicArgIdx)
						dynamicArgIdx++
						if (argument == null) {
							params.add(null)
						} else {
							val player = Bukkit.getPlayer(argument)
							params.add(player)
						}
					}
					injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.WORLD -> {
						val argument = arguments.getOrNull(dynamicArgIdx)
						dynamicArgIdx++
						if (argument == null) {
							params.add(null)
						} else {
							val player = Bukkit.getWorld(argument)
							params.add(player)
						}
					}
					injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.CUSTOM_ARGUMENT -> {
						// Suporte a injected arguments personalizados
						val argument = arguments.getOrNull(dynamicArgIdx)
						val customInjector = CommandManager.argumentContexts.entries.firstOrNull { it.key == param.type }
						dynamicArgIdx++
						if (customInjector == null || argument == null) {
							params.add(null)
						} else {
							val value = customInjector.value.invoke(sender, argument)
							params.add(value)
						}
					}
					injectArgumentAnnotation != null && injectArgumentAnnotation.type == ArgumentType.ARGUMENT_LIST -> {
						params.add(arguments.joinToString(" "))
					}
					CommandManager.contexts.entries.any { it.key == param.type } -> {
						val customInjector = CommandManager.contexts.entries.firstOrNull { it.key == param.type }
						if (customInjector == null) {
							params.add(null)
						} else {
							val value = customInjector.value.invoke(sender)
							params.add(value)
						}
					}
					param.type.isAssignableFrom(Player::class.java) && sender is Player -> { params.add(sender) }
					param.type.isAssignableFrom(CommandSender::class.java) && sender is CommandSender -> { params.add(sender) }
					param.type.isAssignableFrom(String::class.java) -> {
						params.add(arguments.getOrNull(dynamicArgIdx))
						dynamicArgIdx++
					}
				// Sim, é necessário usar os nomes assim, já que podem ser tipos primitivos ou objetos
					typeName == "int" || typeName == "integer" -> {
						params.add(arguments.getOrNull(dynamicArgIdx)?.toIntOrNull())
						dynamicArgIdx++
					}
					typeName == "double" -> {
						params.add(arguments.getOrNull(dynamicArgIdx)?.toDoubleOrNull())
						dynamicArgIdx++
					}
					typeName == "float" -> {
						params.add(arguments.getOrNull(dynamicArgIdx)?.toFloatOrNull())
						dynamicArgIdx++
					}
					typeName == "long" -> {
						params.add(arguments.getOrNull(dynamicArgIdx)?.toLongOrNull())
						dynamicArgIdx++
					}
					param.type.isAssignableFrom(Array<String>::class.java) -> {
						params.add(arguments.subList(dynamicArgIdx, arguments.size).toTypedArray())
					}
					param.type.isAssignableFrom(Array<Int?>::class.java) -> {
						params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toIntOrNull() }.toTypedArray())
					}
					param.type.isAssignableFrom(Array<Double?>::class.java) -> {
						params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toDoubleOrNull() }.toTypedArray())
					}
					param.type.isAssignableFrom(Array<Float?>::class.java) -> {
						params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toFloatOrNull() }.toTypedArray())
					}
					param.type.isAssignableFrom(Array<Long?>::class.java) -> {
						params.add(arguments.subList(dynamicArgIdx, arguments.size).map { it.toLongOrNull() }.toTypedArray())
					}
					else -> params.add(null)
				}
			}
			return params
		}
	}
}