package com.dong.baselibrary.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.requestMultiplePermissions(multiRequest:Array<String>,permissionsState: (Boolean)->Unit){
    var requestLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allPermissionsGranted = true
        permissions.entries.forEach { entry ->
            if (!entry.value) {
                allPermissionsGranted = false
            }
        }
        permissionsState.invoke(allPermissionsGranted)
    }
    requestLauncher.launch(multiRequest)
}

fun AppCompatActivity.requestSinglePermissions(multiRequest:String, permissionsState: (Boolean)->Unit){
    val requestLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissions ->
       permissionsState.invoke(permissions)
    }
    requestLauncher.launch(multiRequest)
}
