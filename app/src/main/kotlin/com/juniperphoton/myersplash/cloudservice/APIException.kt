package com.juniperphoton.myersplash.cloudservice

data class APIException(val code: Int,
                        val url: String?,
                        val msg: String? = null
) : Exception()