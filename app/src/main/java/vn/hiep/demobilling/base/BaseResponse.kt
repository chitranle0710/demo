package vn.hiep.demobilling.base

import com.google.gson.annotations.SerializedName

class BaseResponse {
    @SerializedName("data")
    lateinit var data: String

    @SerializedName("code")
    lateinit var code: String

    @SerializedName("error")
    lateinit var error: String

}