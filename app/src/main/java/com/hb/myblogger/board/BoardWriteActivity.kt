package com.hb.myblogger.board

import RetrofitAPI
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hb.myblogger.R
import com.hb.myblogger.RetrofitAPI
import com.hb.myblogger.databinding.ActivityBoardWriteBinding
import com.hb.myblogger.flask.DataModel
import com.hb.myblogger.flask.RetrofitAPI
import com.hb.myblogger.utils.FBAuth
import com.hb.myblogger.utils.FBRef
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class BoardWriteActivity:AppCompatActivity() {

    private lateinit var binding : ActivityBoardWriteBinding

    private val TAG = BoardWriteActivity::class.java.simpleName

    private var isImageUpload = false

    //flask 서버 연결 테스트
    lateinit var mRetrofit : Retrofit // 사용할 레트로핏 객체입니다.
    lateinit var mRetrofitAPI: RetrofitAPI // 레트로핏 api객체입니다.
    lateinit var mCallTodoList : retrofit2.Call<JsonObject> // Json형식의 데이터를 요청하는 객체입니다.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //flask
        setRetrofit()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_board_write)

        binding.saveBtn.setOnClickListener {

            val title = binding.titleArea.text.toString()
            val content = binding.contentArea.text.toString()
            val uid = FBAuth.getUid()
            val time = FBAuth.getTime()

            Log.d(TAG, title)
            Log.d(TAG, content)


            // 파이어베이스 store에 이미지를 저장하고 싶습니다
            // 만약에 내가 게시글을 클릭했을 때, 게시글에 대한 정보를 받아와야 하는데
            // 이미지 이름에 대한 정보를 모르기 때문에
            // 이미지 이름을 문서의 key값으로 해줘서 이미지에 대한 정보를 찾기 쉽게 해놓음.

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

        binding.getBtn.setOnClickListener {
            binding.getBtn.visibility = View.INVISIBLE
//            progressBar.visibility = View.VISIBLE
            callTodoList()
        }

        binding.imageArea.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, 100)
            isImageUpload = true
        }

    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK && requestCode == 100) {
            binding.imageArea.setImageURI(data?.data)
        }

    }

    private fun setRetrofit(){
        //레트로핏으로 가져올 url설정하고 세팅
        mRetrofit = Retrofit
            .Builder()
            .baseUrl(getString(R.string.baseUrl))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //인터페이스로 만든 레트로핏 api요청 받는 것 변수로 등록
        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
    }

    // 리스트를 불러온다.
    private fun callTodoList() {
        mCallTodoList = mRetrofitAPI.getTodoList()
        mCallTodoList.enqueue(mRetrofitCallback)//응답을 큐 대기열에 넣는다.
    }

    //http요청을 보냈고 이건 응답을 받을 콜벡메서드
    private val mRetrofitCallback  = (object : retrofit2.Callback<JsonObject>{//Json객체를 응답받는 콜백 객체

        //응답을 가져오는데 실패
        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            t.printStackTrace()
            Log.d(TAG, "에러입니다. => ${t.message.toString()}")
            binding.pic1text.text = "에러\n" + t.message.toString()

//            progressBar.visibility = View.GONE
            binding.getBtn.visibility = View.VISIBLE
        }
        //응답을 가져오는데 성공 -> 성공한 반응 처리
        override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
            val result = response.body()
            Log.d(TAG, "결과는 => $result")

            var mGson = Gson()
            val dataParsed1 = mGson.fromJson(result, DataModel.TodoInfo1::class.java)
            val dataParsed2 = mGson.fromJson(result, DataModel.TodoInfo2::class.java)
            val dataParsed3 = mGson.fromJson(result, DataModel.TodoInfo3::class.java)

            binding.pic1text.text = "해야할 일\n" + dataParsed1.todo1.task+"\n"+dataParsed2.todo2.task +"\n"+dataParsed3.todo3.task

//            progressBar.visibility = View.GONE
            binding.getBtn.visibility = View.VISIBLE
        }
    })
}
