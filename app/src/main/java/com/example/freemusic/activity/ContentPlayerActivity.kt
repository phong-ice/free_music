package com.example.freemusic.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.freemusic.R
import com.example.freemusic.databinding.ActivityContentPlayerBinding
import com.example.freemusic.fragments.PlayerFragment

class ContentPlayerActivity : AppCompatActivity() {

    private val binding: ActivityContentPlayerBinding by lazy {
        ActivityContentPlayerBinding.inflate(
            layoutInflater
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val playerFragment = PlayerFragment(communication)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_content_player,playerFragment,playerFragment.tagName)
        transaction.addToBackStack(playerFragment.tagName)
        transaction.commit()
    }

    override fun onBackPressed() {
        when(supportFragmentManager.backStackEntryCount){
            1 -> finish()
            else -> supportFragmentManager.popBackStack()
        }
    }

    private val communication:(Fragment,String) -> Unit = { fragment, s ->
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container_content_player,fragment,s)
        transaction.addToBackStack(s)
        transaction.commit()
    }
}