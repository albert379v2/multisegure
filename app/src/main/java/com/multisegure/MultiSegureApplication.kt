package com.multisegure

import android.app.Application
import com.multisegure.data.local.AppDatabase

class MultiSegureApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
