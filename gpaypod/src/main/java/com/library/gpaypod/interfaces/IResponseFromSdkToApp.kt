package com.library.gpaypod.interfaces

interface IResponseFromSdkToApp {
    fun onResponse(responseMessage : String, errorMessage : String)
}