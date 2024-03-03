package com.barisgungorr.data

data class NoteModel(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var date: Long = 0,
    var color: Int = -1
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "content" to content,
            "date" to date,
            "color" to color
        )
    }
}
