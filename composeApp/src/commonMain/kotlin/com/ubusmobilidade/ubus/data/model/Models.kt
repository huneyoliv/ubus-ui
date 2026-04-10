package com.ubusmobilidade.ubus.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val cpf: String,
    val role: RoleUsuario,
    val municipalityId: String,
    val phone: String? = null,
    val defaultRouteId: String? = null,
)

@Serializable
data class Prefeitura(
    val id: String,
    val name: String,
    val active: Boolean,
    val managerId: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class Linha(
    val id: String,
    val nome: String,
    val descricao: String? = null,
)

@Serializable
data class Onibus(
    val id: String,
    val numero: String,
    val placa: String? = null,
    val capacidade: Int,
    val idMotorista: String? = null,
)

@Serializable
data class Trip(
    val idViagem: String,
    val dataViagem: String,
    val turno: String,
    val direcao: DirecaoViagem,
    val status: StatusViagem,
    val idLinha: String,
    val idOnibus: String,
    val idMotorista: String? = null,
    val capacidadeReal: Int,
    val aberturaVotacao: String,
    val fechamentoVotacao: String,
    val lideresIds: List<String>? = null,
    val linha: Linha? = null,
    val onibus: Onibus? = null,
)

@Serializable
data class Reservation(
    val id: String,
    val idViagem: String,
    val idUsuario: String,
    val numeroAssento: Int? = null,
    val status: StatusReserva,
    val isCarona: Boolean,
    val createdAt: String? = null,
    val usuario: ReservationUser? = null,
    val viagem: Trip? = null,
)

@Serializable
data class ReservationUser(
    val id: String,
    val nome: String,
    val cpf: String,
)

@Serializable
data class Seat(
    val number: Int,
    val status: SeatStatus,
)

@Serializable
enum class SeatStatus {
    AVAILABLE, OCCUPIED, SELECTED
}
