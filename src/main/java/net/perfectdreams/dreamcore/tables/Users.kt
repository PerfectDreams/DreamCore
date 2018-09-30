package net.perfectdreams.dreamcore.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {
	val id = uuid("id").primaryKey()
	val username = varchar("username", 16)
}