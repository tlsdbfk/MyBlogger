package com.hb.myblogger.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.hb.myblogger.R
import com.hb.myblogger.board.BoardWriteActivity
import com.hb.myblogger.databinding.FragmentHomeBinding
import com.hb.myblogger.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {

    private lateinit var binding : FragmentMypageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_mypage, container, false)

        binding.writeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_mypageFragment_to_writeFragment)
        }
        binding.homeTap.setOnClickListener{
            it.findNavController().navigate(R.id.action_mypageFragment_to_homeFragment)
        }
        return binding.root
    }


}