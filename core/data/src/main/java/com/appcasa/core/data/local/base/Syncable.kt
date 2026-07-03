package com.appcasa.core.data.local.base

interface Syncable {
  val id: String
  var lastSyncedAt: Long?
}
