package com.ubusmobilidade.ubus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform