package com.example.freemusic.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constance {
    const val ACTION_NEXT: String  = "action_next"
    const val ACTION_PLAY: String  = "action_play"
    const val ACTION_PREVIOUS: String  = "action_previous"
    const val ACTION_STOP_SERVICE: String  = "action_stop_service"
    const val ITEM_ADD_PLAYLIST = "item_add_play_list"
    const val ITEM_FAVORITE_PLAYLIST = "item_favorite_play_list"
    const val ITEM_DOWNLOAD_PLAYLIST = "item_download_play_list"
    const val KEY_BUNDLE_PLAYLISTS = "key_bundle_playlists"
    const val REQUEST_CODE_CHECK_PERMISSION = 101
    const val PLAY_MODE_REPEAT_ALL = "repeat_all"
    const val PLAY_MODE_REPEAT_ONE = "repeat_ONE"
    const val PLAY_MODE_NORMAL = "normal"
    const val PLAY_MODE_SHUFFLE = "shuffle"

    fun isConnectedInternet(context: Context):Boolean{
        var isInternet = false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            connectivityManager?.run {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                    isInternet = when{
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                        else -> false
                    }
                }
            }
        }else{
            connectivityManager.activeNetworkInfo?.run {
                isInternet = when(type){
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_VPN -> true
                    else -> false
                }
            }
        }
        return isInternet
    }
}