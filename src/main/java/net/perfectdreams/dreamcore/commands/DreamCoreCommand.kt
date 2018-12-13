package net.perfectdreams.dreamcore.commands

import com.okkero.skedule.schedule
import net.perfectdreams.dreamcore.DreamCore
import net.perfectdreams.dreamcore.scriptmanager.DreamScriptManager
import net.perfectdreams.dreamcore.scriptmanager.Imports
import net.perfectdreams.dreamcore.utils.commands.AbstractCommand
import net.perfectdreams.dreamcore.utils.commands.ExecutedCommandException
import net.perfectdreams.dreamcore.utils.commands.annotation.Subcommand
import net.perfectdreams.dreamcore.utils.scheduler
import net.perfectdreams.dreamcore.utils.stripColorCode
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import java.io.BufferedReader
import java.io.File
import kotlin.concurrent.thread

class DreamCoreCommand(val m: DreamCore) : AbstractCommand("dreamcore", permission = "dreamcore.setup") {
	@Subcommand(["set_spawn"])
	fun setSpawn(player: Player) {
		DreamCore.dreamConfig.spawn = player.location
		m.config.set("spawn-location", player.location)
		m.saveConfig()

		player.sendMessage("§aSpawn atualizado!")
	}

	@Subcommand(["plreload"])
	fun reloadAndRecompilePlugin(executor: CommandSender, pluginName: String) {
		// A maior gambiarra já vista na face da terra:tm:
		// Iremos compilar e recarregar o plugin automaticamente, magicamente!
		thread {
			val outputFolder = File("/home/dreamcore_compile/$pluginName")
			val targetFolder = File("/home/dreamcore_compile/$pluginName/target")

			if (targetFolder.exists()) {
				targetFolder.listFiles().forEach {
					if (it.extension == "jar")
						it.delete()
				}
			}

			if (!outputFolder.exists()) {
				executor.sendMessage("§aClonando projeto $pluginName...")
				val processBuilder = ProcessBuilder("git", "clone", "https://github.com/PerfectDreams/$pluginName.git")
						.redirectErrorStream(true)
						.directory(File("/home/dreamcore_compile/"))

				val process = processBuilder.start()
				readProcessInput(process, executor)
				process.waitFor()
			}

			run {
				executor.sendMessage("§aFetcheando a origin de $pluginName...")
				val processBuilder = ProcessBuilder("git", "fetch", "origin")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, executor)
				process.waitFor()
			}

			run {
				executor.sendMessage("§aResetando para a origin/development de $pluginName...")
				val processBuilder = ProcessBuilder("git", "reset", "--hard", "origin/development")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, executor)
				process.waitFor()
			}

			run {
				executor.sendMessage("§aCompilando $pluginName...")
				val processBuilder = ProcessBuilder("mvn", "install")
						.redirectErrorStream(true)
						.directory(outputFolder)

				val process = processBuilder.start()
				readProcessInput(process, executor)
				process.waitFor()
			}

			run {
				executor.sendMessage("§aCopiando target do projeto para a pasta de plugins do servidor...")
				if (targetFolder.exists()) {
					val jar = targetFolder.listFiles().firstOrNull {it.extension == "jar" }

					if (jar == null) {
						executor.sendMessage("§cJAR não existe!")
						return@thread
					} else {
						val output = File("./plugins/$pluginName.jar")
						output.delete()
						jar.copyTo(output, true)
					}
				} else {
					executor.sendMessage("§cTarget folder não existe!")
					return@thread
				}
			}

			run {
				executor.sendMessage("§aRecarregando $pluginName...")
				scheduler().schedule(m) {
					Bukkit.dispatchCommand(executor, "plugman reload $pluginName")
				}
			}
		}
	}

	fun readProcessInput(process: Process, executor: CommandSender) {
		val reader = process.inputStream.bufferedReader()

		var line : String? = ""
		while (line != null) {
			line = reader.readLine()
			executor.sendMessage(line)
		}
	}

	@Subcommand(["reload"])
	fun reloadScripts(executor: CommandSender, fileName: String) {
		if (fileName == "all") {
			executor.sendMessage("§aRecarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			m.dreamScriptManager.loadScripts()
			executor.sendMessage("§aProntinho! ${m.dreamScriptManager.scripts.size} scripts foram carregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			executor.sendMessage("§aRecarregando script $fileName!")
			if (script != null)
				m.dreamScriptManager.unloadScript(script)
			try {
				m.dreamScriptManager.loadScript(File(m.dataFolder, "scripts/$fileName"), true)
				executor.sendMessage("§aProntinho! $fileName foi carregado com sucesso!")
			} catch (e: Exception) {
				executor.sendMessage("§cAlgo deu errado ao carregar $fileName! ${e.message}")
			}
		}
		// m.dreamScriptManager
	}

	@Subcommand(["unload"])
	fun unloadScripts(executor: CommandSender, fileName: String) {
		if (fileName == "all") {
			executor.sendMessage("§Descarregando TODOS os scripts!")
			m.dreamScriptManager.unloadScripts()
			executor.sendMessage("§aProntinho! Todos os scripts foram descarregados ^-^")
		} else {
			val script = m.dreamScriptManager.scripts.firstOrNull { it.fileName == fileName }
			if (script == null) {
				executor.sendMessage("§cO script ${fileName} existe! Use reload seu tosco!")
				return
			}

			executor.sendMessage("§aDescarregando script $fileName!")
			m.dreamScriptManager.unloadScript(script)
			executor.sendMessage("§aProntinho! $fileName foi descarregado com sucesso!")
		}
		// m.dreamScriptManager
	}

	@Subcommand(["eval"])
	fun evaluate(player: Player) {
		val heldItem = player.inventory.itemInMainHand

		if (heldItem.type != Material.WRITABLE_BOOK && heldItem.type != Material.WRITTEN_BOOK) {
			throw ExecutedCommandException("§cVocê precisa estar segurando um livro!")
		}

		val bookMeta = heldItem.itemMeta as BookMeta
		val lines = bookMeta.pages.map { it.stripColorCode() }.joinToString("\n")

		player.sendMessage("§dExecutando...")
		player.sendMessage(lines)

		val content = """
			${Imports.IMPORTS}

			class EvaluatedCode {
				fun doStuff(player: Player) {
					${lines}
				}
			}

			EvaluatedCode()
		""".trimIndent()

		try {
			val result = DreamScriptManager.evaluate<Any>(m, content)
			result::class.java.getMethod("doStuff", Player::class.java).invoke(result, player)
		} catch (e: Exception) {
			e.printStackTrace()
			player.sendMessage("§dDeu ruim! ${e.message}")
		}
	}
}