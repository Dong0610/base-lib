package com.dong.baselib.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.dong.baselib.api.isApi33orHigher

class Permission {

    private lateinit var context: Context
    fun initialize(context: Context): Permission {
        this.context = context
        return this
    }



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val notificationRequest = Manifest.permission.POST_NOTIFICATIONS
    val storageRequest = if (isApi33orHigher) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    val contactRequest =
        arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    val cameraRequest = Manifest.permission.CAMERA
    val locationRequest = Manifest.permission.ACCESS_FINE_LOCATION

    val checkGrantedFile: Boolean
        get() {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                if (isApi33orHigher) {
                    return ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                }
                return false
            }
        }

    val checkGrantedContact: Boolean
        get() {
            return (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED)

        }

    val checkGrantedLocation: Boolean
        get() {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                return false
            }
        }
    val checkGrantedWriteSetting: Boolean
        get() {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_SETTINGS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else {
                return false
            }
        }

    val checkGrantedCamera: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

    val checkGrantNotification: Boolean
        get() {

            return if (isApi33orHigher) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        }
}