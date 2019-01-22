package net.perfectdreams.dreamcore.utils.commands.annotation

annotation class SubcommandPermission(val permission: String, val message: String = "{UseDefaultMessage}", val callbackName: String = "")