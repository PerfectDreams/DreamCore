package net.perfectdreams.dreamcore.utils.extensions

import br.com.devsrsouza.kotlinbukkitapi.dsl.command.Executor
import br.com.devsrsouza.kotlinbukkitapi.dsl.command.KCommand
import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

inline fun <reified T : CommandSender> KCommand.execute(noinline block: Executor<T>.() -> Unit) {
	genericExecutor(T::class, block)
}