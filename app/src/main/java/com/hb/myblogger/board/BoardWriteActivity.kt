package com.hb.myblogger.board

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hb.imageup.RetrofitPath
import com.hb.imageup.RetrofitSetting
import com.hb.myblogger.R
import com.hb.myblogger.databinding.ActivityBoardWriteBinding
import com.hb.myblogger.utils.FBAuth
import com.hb.myblogger.utils.FBRef
import kotlinx.android.synthetic.main.activity_board_write.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.*
import java.io.ByteArrayOutputStream
import java.io.File

class BoardWriteActivity:AppCompatActivity() {

    private lateinit var binding : ActivityBoardWriteBinding

    private val TAG = BoardWriteActivity::class.java.simpleName

    lateinit var mCallTodoList : retrofit2.Call<JsonObject>

    private var isImageUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }

        binding.getBtn.setOnClickListener {
            binding.getBtn.visibility = View.INVISIBLE
            callTodoList()
        }

        binding.saveBtn.setOnClickListener {

            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            Log.d(TAG, title)
            Log.d(TAG, content)

            // 파이어베이스 store에 이미지를 저장할 때 이미지 이름을 문서의 key값으로 함
            val key = FBRef.boardRef.push().key.toString()

            FBRef.boardRef
                .child(key)
                .setValue(BoardModel(title, content, uid, time))

            Toast.makeText(this, "게시글 입력 완료", Toast.LENGTH_LONG).show()

            if(isImageUpload == true) {
                imageUpload(key)
            }

            finish()
        }
    }

    //서버로부터 이미지 캡션 받아오기
    private fun callTodoList() {
        mCallTodoList = RetrofitSetting.createBaseService(RetrofitPath::class.java).getCaption() // RetrofitAPI에서 Json객체 요청을 반환하는 메서드를 불러옵니다.
        mCallTodoList.enqueue(mRetrofitCallback) // 콜백, 즉 응답들을 큐에 넣어 대기시켜놓습니다. 응답이 생기면 뱉어내는거죠.
    }

    //http요청을 보냈고 이건 응답을 받을 콜벡메서드
    private val mRetrofitCallback  = (object : retrofit2.Callback<JsonObject>{
        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            t.printStackTrace()
            Log.d(TAG, "에러입니다. => ${t.message.toString()}")
            ErrorMessage.text = "에러\n" + t.message.toString()
            getBtn.visibility = View.VISIBLE
        }

        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            val result = response.body()

            val caption = result?.get("result_caption_str")?.getAsString()
            val subject = result?.get("result_caption_sub")?.getAsString()
            val geo0 = result?.get("geo0")?.getAsString()
            val geo1 = result?.get("geo1")?.getAsString()
            val geo2 = result?.get("geo2")?.getAsString()
            val geo3 = result?.get("geo3")?.getAsString()
            val geo4 = result?.get("geo4")?.getAsString()
            val holiday = result?.get("holiday")?.getAsString()
            val picture_date_ko = result?.get("picture_date_ko")?.getAsString()
            val time_slot = result?.get("time_slot")?.getAsString()
            val weather_rain = result?.get("weather_rain")?.getAsString()
            val weather_ta = result?.get("weather_ta")?.getAsString()
            contentArea.setText( "주어: $subject\n캡션: $caption" )
            HashtagArea.setText("#$geo0 #$geo1 #$geo2 #$geo3 #$geo4 \n#$holiday #$picture_date_ko #$time_slot \n$weather_rain $weather_ta")
            getBtn.visibility = View.VISIBLE
        }
    })

    //글 저장하는 곳에 사진 저장하는 함수(건들지마)
    private fun imageUpload(key : String){
        // Get the data from an ImageView as bytes

        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child(key + ".png")

        val imageView = binding.imageArea
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener { taskSnapshot ->
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }

    }
    //사진 저장하기 전에 보여주는 함수(건들지마)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            binding.imageArea.setImageURI(data?.data)

            val imagePath = data?.data!!

            val file = File(absolutelyPath(imagePath, this))
            val requestFile = RequestBody.create(MediaType.parse("image/*"), file)
            val body = MultipartBody.Part.createFormData("proFile", file.name, requestFile)

            Log.d(ContentValues.TAG,file.name)

            sendImage(body)
        }

    }



    fun absolutelyPath(path: Uri?, context : Context): String {
        var proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        var c: Cursor? = context.contentResolver.query(path!!, proj, null, null, null)
        var index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        var result = c?.getString(index!!)

        return result!!
    }

    fun sendImage(image : MultipartBody.Part) {
        val service = RetrofitSetting.createBaseService(RetrofitPath::class.java) //레트로핏 통신 설정
        val call = service.profileSend(image)!! //통신 API 패스 설정

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response?.isSuccessful) {
                    Log.d("로그 ",""+response?.body().toString())
                    Toast.makeText(applicationContext,"통신성공",Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(applicationContext,"통신실패",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("로그 ",t.message.toString())
            }
        })
    }


}

