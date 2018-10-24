package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Users : IdTable<UUID>() {
	override val id: Column<EntityID<UUID>>
		get() = uuid("id").primaryKey().entityId()

	val username = varchar("username", 16)
}