package com.appcasa.core.domain.repository

interface NetworkRepository {
    fun isNetworkAvailable(): Boolean
}
