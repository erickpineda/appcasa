package com.appcasa.core.data.local.base

interface Auditable {
  val createdAt: Long
  val createdBy: String?
  val updatedAt: Long
  val updatedBy: String?
  val deletedAt: Long?
  val deletedBy: String?
}
