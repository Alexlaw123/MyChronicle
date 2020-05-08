package com.openlattice.chronicle.utils

import android.content.Context
import android.content.pm.PackageManager
import com.crashlytics.android.Crashlytics
import java.lang.Exception

import java.util.UUID

object Utils {

    fun isValidUUID(possibleUUID: String): Boolean = try {
        val uuid = UUID.fromString(possibleUUID)
        val uuidAsString = uuid.toString()
        uuidAsString == possibleUUID
    }
    catch (e: IllegalArgumentException) {
        Crashlytics.logException(e)
        false
    }

    fun getAppFullName(context :Context, packageName: String) :String  {
        return try {
            val packageManager = context.applicationContext.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName;
        }

    }
}
