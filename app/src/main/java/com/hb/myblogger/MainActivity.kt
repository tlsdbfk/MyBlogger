package com.hb.myblogger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hb.myblogger.auth.IntroActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<ImageView>(R.id.settingIcon).setOnClickListener{
            showMenu()
        }
    }

    private fun showMenu() {
        var popupMenu = PopupMenu(applicationContext, findViewById<ImageView>(R.id.settingIcon))

        menuInflater?.inflate(R.menu.menu_logout, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu_logout -> {
                    auth.signOut()
                    Toast.makeText(this, "로그아웃 완료", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, IntroActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    return@setOnMenuItemClickListener true
                }else ->{
                return@setOnMenuItemClickListener false
            }
            }
        }
    }

}