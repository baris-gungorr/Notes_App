package com.barisgungorr.data

data class NoteModel(
    var id :String,
    var title:String,
    var content:String,
    var date:Long,
    var color:Int
)
{
    fun toHashMap() {

    }

    constructor():this(
     "",
        "",
          "",
             0,
               -1
    )
}
