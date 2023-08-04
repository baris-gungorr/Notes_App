package com.barisgungorr.Model

data class InformationModel(
    var username : String,
    var userEmail : String,
    var userPassword : String,
    var userProfilePhoto: String,
    var userId: String

)
{
    constructor():this(
        "",
           "",
              "",
                "",
                   "",
    )
}