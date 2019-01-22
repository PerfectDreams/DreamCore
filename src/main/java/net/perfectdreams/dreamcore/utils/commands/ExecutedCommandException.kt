package net.perfectdreams.dreamcore.utils.commands

import org.bukkit.ChatColor

class ExecutedCommandException(val minecraftMessage: String? = null, message: String? = null) : RuntimeException(message ?: ChatColor.stripColor(minecraftMessage))