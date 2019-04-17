package com.juniperphoton.myersplash.api

data class APIException(val code: Int,
                        val url: String?,
                        val msg: String? = null
) : Exception()