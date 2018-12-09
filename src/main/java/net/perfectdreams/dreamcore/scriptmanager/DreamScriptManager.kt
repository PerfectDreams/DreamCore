package net.perfectdreams.dreamcore.scriptmanager

import net.perfectdreams.dreamcore.DreamCore
import org.bukkit.Bukkit
import org.graalvm.polyglot.Context
import java.io.File

class DreamScriptManager(val m: DreamCore) {
	val scripts = mutableListOf<DreamScript>()

	fun loadScripts() {
		m.logger.info("Carregando DreamScripts...")
		val scriptsFolder = File(m.dataFolder, "scripts")

		scriptsFolder.mkdirs()
		scriptsFolder.listFiles().forEach {
			if (it.extension == "js") {
				loadScript(it)
			}
		}
		m.logger.info("DreamScripts carregados com sucesso! ${scripts.size} scripts foram carregados!")
	}

	fun loadScript(file: File) {
		m.logger.info("Carregando DreamScript \"${file.name}\"...")
		val content = file.readText()

		val cl = m.javaClass.classLoader
		Thread.currentThread().contextClassLoader = cl

		val graalContext = Context.newBuilder()
				.allowAllAccess(true) // Permite usar coisas da JVM dentro do GraalJS (e várias outras coisas)
				.build()

		try {
			val dreamScript = DreamScript(file.name)
			val bindings = graalContext.getBindings("js")
			bindings.putMember("server", Bukkit.getServer())
			bindings.putMember("script", dreamScript)
			graalContext.eval("js", content)

			dreamScript.enable()

			scripts.add(dreamScript)

			m.logger.info("DreamScript \"${file.name}\" carregado com sucesso!")
		} catch (e: Exception) {
			m.logger.warning("Erro ao carregar o script  \"${file.name}\"!")
			e.printStackTrace()
		}
	}

	fun unloadScripts() {
		scripts.forEach {
			unloadScript(it, false)
		}
		scripts.clear()
	}

	fun unloadScript(script: DreamScript, removeFromList: Boolean = true) {
		script.disable()
		if (removeFromList)
			scripts.remove(script)
	}
}