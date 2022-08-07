package com.hb.myblogger.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.hb.myblogger.R
import com.hb.myblogger.databinding.ActivityBoardInsideBinding
import com.hb.myblogger.utils.FBRef
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hb.myblogger.utils.FBAuth
import java.lang.Exception

class BoardInsideActivity : AppCompatActivity() {

    private val TAG = BoardInsideActivity::class.java.simpleName

    private lateinit var binding : ActivityBoardInsideBinding

    private lateinit var key:String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_inside)

        key = intent.getStringExtra("key").toString()

        getBoardData(key)
        getImageData(key)

        binding.blogSettingIcon.setOnClickListener {
            showMenu()
        }

    }

    private fun showMenu() {
        var popupMenu = PopupMenu(applicationContext, binding.blogSettingIcon)

        menuInflater?.inflate(R.menu.menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu_del -> {
                    FBRef.boardRef.child(key).removeValue()
                    finish()
                    Toast.makeText(this, "삭제완료", Toast.LENGTH_LONG).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_mod -> {
                    val intent = Intent(this, BoardEditActivity::class.java)
                    intent.putExtra("key",key)
                    startActivity(intent)
                    return@setOnMenuItemClickListener true
                }else ->{
                return@setOnMenuItemClickListener false
            }
            }
        }
    }


    private fun getImageData(key : String){

        // Reference to an image file in Cloud Storage
        val storageReference = Firebase.storage.reference.child(key + ".png")

        // ImageView in your Activity
        val imageViewFromFB = binding.getImageArea

        storageReference.downloadUrl.addOnCompleteListener(OnCompleteListener { task ->
            if(task.isSuccessful) {
                Glide.with(this)
                    .load(task.result)
                    .into(imageViewFromFB)

            } else {

            }
        })


    }


    private fun getBoardData(key : String){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                try{
                    val dataModel = dataSnapshot.getValue(BoardModel::class.java)

                    binding.titleArea.text = dataModel!!.title
                    binding.contentArea.text = dataModel!!.content
                    binding.hashArea.text = dataModel!!.hashtag
                    binding.timeArea.text = dataModel!!.time

                    val myUid = FBAuth.getUid()
                    val writerUid = dataModel.uid

                    if(myUid.equals(writerUid)){
                        binding.blogSettingIcon.isVisible = true
                    }else{

                    }
                }catch (e: Exception){
                    Log.d(TAG, "삭제 완료")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.child(key).addValueEventListener(postListener)

    }

}