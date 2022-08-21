package com.hb.myblogger.board

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import android.provider.MediaStore.Images
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.DocumentsContract
import com.hb.myblogger.MainActivity
import java.net.URL
import androidx.core.content.ContextCompat
import java.io.File


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
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

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
                }
                //다른 앱으로 글 보내기
                R.id.menu_blog -> {
                    writeBlog()
                    return@setOnMenuItemClickListener true
            }
                R.id.menu_insta -> {
                    //  이미지
                    val shareIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        val bitmap = (binding.getImageArea.drawable as BitmapDrawable).bitmap
                        //putExtra(Intent.EXTRA_TEXT, "\n${binding.contentArea.text}\n\n${binding.hashArea.text}")
                        putExtra(Intent.EXTRA_STREAM, getImageUri(getApplicationContext(), bitmap))
                        type = "image/*"
                        setPackage("com.instagram.android")
                    }
                    startActivity(Intent.createChooser(shareIntent, null))
                    return@setOnMenuItemClickListener true
                }
                else ->{
                return@setOnMenuItemClickListener false
            }
            }
        }
    }

    private fun writeBlog() {
        val bitmap = (binding.getImageArea.drawable as BitmapDrawable).bitmap
        val picUri = getImageUri(getApplicationContext(), bitmap)
        val version = 1
        val title = "${binding.titleArea.text}"
        val content = "${binding.contentArea.text}\n${binding.hashArea.text}"
        val imageUrls: MutableList<String> = ArrayList()
        val storageReference = Firebase.storage.reference.child(key + ".png")
        print("here제발 ${storageReference.downloadUrl}")
        //imageUrls.add("${storageReference.downloadUrl}")
        val videoUrls: MutableList<String> = ArrayList()
        //videoUrls.add("http://tvcast.naver.com/v/791662")
        val ogTagUrls: MutableList<String> = ArrayList()
        //ogTagUrls.add("http://m.naver.com")
        val tags: MutableList<String> = ArrayList()
        tags.add("MyBlogger")
        val ImgUri = picUri
        NaverBlog(this@BoardInsideActivity).write(
            version,
            title,
            content,
            imageUrls,
            videoUrls,
            ogTagUrls,
            tags,
            ImgUri
        )
    }

    //비트맵에서 이미지URi 가져오는 코드
    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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