package net.perfectdreams.dreamcore.dao

import net.perfectdreams.dreamcore.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID
import java.util.*

class User(id: EntityID<UUID>) : Entity<UUID>(id) {
	companion object : EntityClass<UUID, User>(Users)

	var username by Users.username
}