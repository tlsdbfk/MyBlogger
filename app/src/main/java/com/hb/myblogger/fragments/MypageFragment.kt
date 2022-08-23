package com.hb.myblogger.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.hb.myblogger.R
import com.hb.myblogger.auth.IntroActivity
import com.hb.myblogger.board.BoardInsideActivity
import com.hb.myblogger.board.BoardListLVAdapter
import com.hb.myblogger.board.BoardModel
import com.hb.myblogger.board.BoardWriteActivity
import com.hb.myblogger.databinding.FragmentHomeBinding
import com.hb.myblogger.databinding.FragmentMypageBinding
import com.hb.myblogger.myboard.MyBoardListLVAdapter
import com.hb.myblogger.utils.FBAuth
import com.hb.myblogger.utils.FBRef
import kotlinx.coroutines.NonDisposableHandle.parent

class MypageFragment : Fragment() {
    private lateinit var binding : FragmentMypageBinding
    private val boardDataList = mutableListOf<BoardModel>()
    private val boardKeyList = mutableListOf<String>()

    private val TAG = MypageFragment::class.java.simpleName
    private lateinit var auth: FirebaseAuth
    private lateinit var myboardRVAdapter : MyBoardListLVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mypage, container, false)

        myboardRVAdapter = MyBoardListLVAdapter(boardDataList)
        binding.boardListView.adapter = myboardRVAdapter


        val user= Firebase.auth.currentUser
        val email = user?.email

        binding.myId.text = email


        binding.boardListView.setOnItemClickListener { parent, view, position, id ->

            // 두번째 방법으로는 Firebase에 있는 board에 대한 데이터의 id를 기반으로 다시 데이터를 받아오는 방법
            val intent = Intent(context, BoardInsideActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            startActivity(intent)

        }
        //binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.homeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_mypageFragment_to_homeFragment)
        }
        binding.writeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_mypageFragment_to_writeFragment)
        }
        getFBBoardData()

        return binding.root
    }


    private fun getFBBoardData(){

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardDataList.clear()

                for (dataModel in dataSnapshot.children) {

                    Log.d(TAG, dataModel.toString())
//                    dataModel.key

                    val item = dataModel.getValue(BoardModel::class.java)
                    val storageReference = Firebase.storage.reference.child(dataModel.key.toString() + ".png")

                    //내가 쓴 글 모아보기
                    val myUid = FBAuth.getUid()
                    val writerUid = item?.uid

                    if(myUid.equals(writerUid)) {
                        boardDataList.add(item!!)
                        boardKeyList.add(dataModel.key.toString())
                    }
                    else{}

                }
                boardKeyList.reverse()
                boardDataList.reverse()

                myboardRVAdapter.notifyDataSetChanged()

                Log.d(TAG, boardDataList.toString())


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        FBRef.boardRef.addValueEventListener(postListener)

    }

}