package com.hb.myblogger.board

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

data class BoardModel(
    val title: String = "",
    val content: String = "",
    val hashtag: String = "",
    val uid: String = "",
    val time: String = "",
    val key :String=""
)