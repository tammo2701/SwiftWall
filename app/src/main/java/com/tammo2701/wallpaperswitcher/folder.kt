package com.tammo2701.wallpaperswitcher

data class Folder(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val images: MutableList<String> = mutableListOf()
)