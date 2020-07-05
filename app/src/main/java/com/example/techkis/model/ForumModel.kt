package com.example.techkis.model

data class ForumModel (
    val forumID: String = "",
    val judulForum: String = "",
    val isiForum: String = "",
    val authorForumID: String = "",
    val namaAuthor: String = "",
    val commentCount: Int = 0,
    val timestampForum: Long = 0
)