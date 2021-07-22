package com.example.freemusic

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.freemusic.activity.HomeActivity
import com.example.freemusic.activity.PlaylistsDownloadActivity
import com.example.freemusic.helper.Constance
import com.example.freemusic.service.PlayerService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                val permissionArr = arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                requestPermissions(
                    permissionArr,
                    Constance.REQUEST_CODE_CHECK_PERMISSION
                )
            } else {
                if (Constance.isConnectedInternet(this)) Intent(
                    this,
                    HomeActivity::class.java
                ).also {
                    startActivity(it)
                    finish()
                } else Intent(this, PlaylistsDownloadActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
                Intent(this, PlayerService::class.java).also {
                    startService(it)
                }
            }
        } else {
            if (Constance.isConnectedInternet(this)) Intent(
                this,
                HomeActivity::class.java
            ).also {
                startActivity(it)
                finish()
            } else Intent(this, PlaylistsDownloadActivity::class.java).also {
                startActivity(it)
                finish()
            }
            Intent(this, PlayerService::class.java).also {
                startService(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constance.REQUEST_CODE_CHECK_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent(this, HomeActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
                if (Constance.isConnectedInternet(this)) Intent(
                    this,
                    HomeActivity::class.java
                ).also {
                    startActivity(it)
                    finish()
                } else Intent(this, PlaylistsDownloadActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            } else {
                requestPermission()
            }
        }
    }
}