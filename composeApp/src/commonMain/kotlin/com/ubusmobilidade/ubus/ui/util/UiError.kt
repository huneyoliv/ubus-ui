package com.ubusmobilidade.ubus.ui.util

import com.ubusmobilidade.ubus.data.api.ApiError

fun Throwable.toUserMessage(defaultMessage: String): String {
    return when (this) {
        is ApiError -> this.toUserMessage()
        else -> defaultMessage
    }
}

private fun ApiError.toUserMessage(): String {
    return when (status) {
        400 -> "Dados inválidos. Revise e tente novamente."
        401 -> "Sua sessão expirou. Faça login novamente."
        403 -> "Você não tem permissão para esta ação."
        404 -> "Recurso não encontrado."
        409 -> "Conflito de dados. Atualize e tente novamente."
        429 -> "Muitas tentativas. Aguarde alguns instantes."
        in 500..599 -> "Serviço indisponível no momento. Tente novamente."
        else -> "Não foi possível concluir a operação. Tente novamente."
    }
}
