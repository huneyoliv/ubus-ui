package com.ubusmobilidade.ubus.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.ubusmobilidade.ubus.data.model.RoleUsuario
import com.ubusmobilidade.ubus.data.storage.AuthStorage
import kotlinx.serialization.Serializable

class RootComponent(
    componentContext: ComponentContext,
    val authStorage: AuthStorage = AuthStorage(),
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val childStack: Value<ChildStack<Config, Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = initialConfig(),
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, context: ComponentContext): Child = when (config) {
        // Auth
        Config.Login -> Child.Login
        Config.Cadastro -> Child.Cadastro
        Config.RedefinirSenha -> Child.RedefinirSenha

        // Student
        is Config.SelecionarAssento -> Child.SelecionarAssento(config.tripId, config.pendingInboundTripId)
        is Config.SelecionarPontoEmbarque -> Child.SelecionarPontoEmbarque(config.tripId, config.seatNumber, config.pendingInboundTripId)
        is Config.SelecionarDirecao -> Child.SelecionarDirecao(config.routeId, config.tripDate, config.shift, config.outboundTripId, config.inboundTripId)
        is Config.AvaliarViagem -> Child.AvaliarViagem(config.reservationId, config.tripId)
        Config.StudentHome -> Child.StudentHome
        Config.Reservar -> Child.Reservar
        Config.Bilhete -> Child.Bilhete
        Config.Perfil -> Child.Perfil
        Config.Historico -> Child.Historico
        Config.Carteirinha -> Child.Carteirinha
        Config.Pagamentos -> Child.Pagamentos
        Config.Lider -> Child.Lider
        Config.PontoEmbarque -> Child.PontoEmbarque
        Config.MeusDados -> Child.MeusDados
        Config.AlterarSenha -> Child.AlterarSenha
        Config.RenovarSemestre -> Child.RenovarSemestre
        Config.BaixaMobilidade -> Child.BaixaMobilidade
        Config.Regras -> Child.Regras
        is Config.ReservaConcluida -> Child.ReservaConcluida(config.isRideShare)

        // Driver
        Config.DriverHome -> Child.DriverHome
        Config.SelecionarVeiculo -> Child.SelecionarVeiculo
        is Config.CadastroVeiculoMultiStep -> Child.CadastroVeiculoMultiStep(config.municipalityId, config.prefillNumber)
        Config.Mapa -> Child.Mapa
        Config.Avisos -> Child.Avisos
        Config.DriverConfig -> Child.DriverConfig
        is Config.TrocarOnibus -> Child.TrocarOnibus(config.currentBusId)

        // Manager
        Config.ManagerDashboard -> Child.ManagerDashboard
        Config.ManagerRoutes -> Child.ManagerRoutes
        Config.ManagerValidations -> Child.ManagerValidations
        Config.ManagerFrota -> Child.ManagerFrota
        Config.ManagerMotoristas -> Child.ManagerMotoristas
        Config.ManagerRelatorios -> Child.ManagerRelatorios
        Config.ManagerConfiguracoes -> Child.ManagerConfiguracoes
        Config.SuperAdminManagement -> Child.SuperAdminManagement
        Config.ManagerCadastroMotorista -> Child.ManagerCadastroMotorista
        Config.ManagerNotificacoes -> Child.ManagerNotificacoes
        is Config.ManagerMotoristaDetail -> Child.ManagerMotoristaDetail(config.userId)
        is Config.ManagerRouteDetail -> Child.ManagerRouteDetail(config.routeId)
        is Config.ManagerBusDetail -> Child.ManagerBusDetail(config.busId)
        is Config.ManagerStudentDetail -> Child.ManagerStudentDetail(config.userId)
        is Config.ManagerPickupPointForm -> Child.ManagerPickupPointForm(config.routeId, config.pointId)
        Config.ManagerRouteForm -> Child.ManagerRouteForm
    }

    // ── Navigation actions ──

    fun navigateTo(config: Config) {
        navigation.pushNew(guardRoute(config))
    }

    fun goBack() {
        navigation.pop()
    }

    fun replaceWith(config: Config) {
        navigation.replaceAll(guardRoute(config))
    }

    fun onLoginSuccess() {
        val role = authStorage.userRole
        when (role) {
            RoleUsuario.DRIVER -> replaceWith(Config.DriverHome)
            RoleUsuario.MANAGER, RoleUsuario.SUPER_ADMIN -> replaceWith(Config.ManagerDashboard)
            else -> replaceWith(Config.StudentHome)
        }
    }

    fun logout() {
        authStorage.clear()
        replaceWith(Config.Login)
    }

    private fun initialConfig(): Config {
        if (!authStorage.isAuthenticated) return Config.Login
        return homeForRole(authStorage.userRole)
    }

    private fun guardRoute(config: Config): Config {
        if (!authStorage.isAuthenticated) {
            return if (config in publicRoutes) config else Config.Login
        }

        if (config == Config.Lider && authStorage.userRole != RoleUsuario.LEADER) {
            return homeForRole(authStorage.userRole)
        }

        if (config in authRoutes) {
            return homeForRole(authStorage.userRole)
        }

        val allowed = isRoleAllowed(config, authStorage.userRole)
        return if (allowed) config else homeForRole(authStorage.userRole)
    }

    private fun homeForRole(role: RoleUsuario?): Config = when (role) {
        RoleUsuario.DRIVER -> Config.DriverHome
        RoleUsuario.MANAGER, RoleUsuario.SUPER_ADMIN -> Config.ManagerDashboard
        else -> Config.StudentHome
    }

    private fun isRoleAllowed(config: Config, role: RoleUsuario?): Boolean = when (role) {
        RoleUsuario.DRIVER -> when (config) {
            is Config.CadastroVeiculoMultiStep -> true
            is Config.TrocarOnibus -> true
            else -> config in driverRoutes
        }
        RoleUsuario.MANAGER -> when (config) {
            is Config.ManagerMotoristaDetail -> true
            is Config.ManagerRouteDetail -> true
            is Config.ManagerBusDetail -> true
            is Config.ManagerStudentDetail -> true
            is Config.ManagerPickupPointForm -> true
            Config.ManagerRouteForm -> true
            is Config.CadastroVeiculoMultiStep -> true
            else -> config in managerRoutes
        }
        RoleUsuario.SUPER_ADMIN -> when (config) {
            is Config.ManagerMotoristaDetail -> true
            is Config.ManagerRouteDetail -> true
            is Config.ManagerBusDetail -> true
            is Config.ManagerStudentDetail -> true
            is Config.ManagerPickupPointForm -> true
            Config.ManagerRouteForm -> true
            Config.SuperAdminManagement -> true
            is Config.CadastroVeiculoMultiStep -> true
            else -> config in managerRoutes
        }
        else -> isStudentRoute(config)
    }

    fun isStudentRoute(config: Config): Boolean = when (config) {
        is Config.SelecionarAssento -> true
        is Config.SelecionarPontoEmbarque -> true
        is Config.AvaliarViagem -> true
        is Config.SelecionarDirecao -> true
        is Config.ReservaConcluida -> true
        else -> config in studentRoutes
    }

    // ── Sealed configs ──

    @Serializable
    sealed class Config {
        // Auth
        @Serializable data object Login : Config()
        @Serializable data object Cadastro : Config()
        @Serializable data object RedefinirSenha : Config()

        // Student
        @Serializable data object StudentHome : Config()
        @Serializable data object Reservar : Config()
        @Serializable data object Bilhete : Config()
        @Serializable data object Perfil : Config()
        @Serializable data object Historico : Config()
        @Serializable data object Carteirinha : Config()
        @Serializable data object Pagamentos : Config()
        @Serializable data object Lider : Config()
        @Serializable data object PontoEmbarque : Config()
        @Serializable data object MeusDados : Config()
        @Serializable data object AlterarSenha : Config()
        @Serializable data object RenovarSemestre : Config()
        @Serializable data object BaixaMobilidade : Config()
        @Serializable data class SelecionarAssento(val tripId: String, val pendingInboundTripId: String? = null) : Config()
        @Serializable data class SelecionarPontoEmbarque(val tripId: String, val seatNumber: Int?, val pendingInboundTripId: String? = null) : Config()
        @Serializable data class SelecionarDirecao(val routeId: String, val tripDate: String, val shift: String, val outboundTripId: String?, val inboundTripId: String?) : Config()
        @Serializable data class AvaliarViagem(val reservationId: String, val tripId: String) : Config()
        @Serializable data object Regras : Config()
        @Serializable data class ReservaConcluida(val isRideShare: Boolean) : Config()

        // Driver
        @Serializable data object DriverHome : Config()
        @Serializable data object SelecionarVeiculo : Config()
        @Serializable data class CadastroVeiculoMultiStep(val municipalityId: String? = null, val prefillNumber: String? = null) : Config()
        @Serializable data object Mapa : Config()
        @Serializable data object Avisos : Config()
        @Serializable data object DriverConfig : Config()
        @Serializable data class TrocarOnibus(val currentBusId: String? = null) : Config()

        // Manager
        @Serializable data object ManagerDashboard : Config()
        @Serializable data object ManagerRoutes : Config()
        @Serializable data object ManagerValidations : Config()
        @Serializable data object ManagerFrota : Config()
        @Serializable data object ManagerMotoristas : Config()
        @Serializable data object ManagerRelatorios : Config()
        @Serializable data object ManagerConfiguracoes : Config()
        @Serializable data object SuperAdminManagement : Config()
        @Serializable data object ManagerCadastroMotorista : Config()
        @Serializable data object ManagerNotificacoes : Config()
        @Serializable data class ManagerMotoristaDetail(val userId: String) : Config()
        @Serializable data class ManagerRouteDetail(val routeId: String) : Config()
        @Serializable data class ManagerBusDetail(val busId: String) : Config()
        @Serializable data class ManagerStudentDetail(val userId: String) : Config()
        @Serializable data class ManagerPickupPointForm(val routeId: String, val pointId: String? = null) : Config()
        @Serializable data object ManagerRouteForm : Config()
    }

    // ── Sealed children ──

    sealed class Child {
        // Auth
        data object Login : Child()
        data object Cadastro : Child()
        data object RedefinirSenha : Child()

        // Student
        data object StudentHome : Child()
        data object Reservar : Child()
        data object Bilhete : Child()
        data object Perfil : Child()
        data object Historico : Child()
        data object Carteirinha : Child()
        data object Pagamentos : Child()
        data object Lider : Child()
        data object PontoEmbarque : Child()
        data object MeusDados : Child()
        data object AlterarSenha : Child()
        data object RenovarSemestre : Child()
        data object BaixaMobilidade : Child()
        data class SelecionarAssento(val tripId: String, val pendingInboundTripId: String? = null) : Child()
        data class SelecionarPontoEmbarque(val tripId: String, val seatNumber: Int?, val pendingInboundTripId: String? = null) : Child()
        data class SelecionarDirecao(val routeId: String, val tripDate: String, val shift: String, val outboundTripId: String?, val inboundTripId: String?) : Child()
        data class AvaliarViagem(val reservationId: String, val tripId: String) : Child()
        data object Regras : Child()
        data class ReservaConcluida(val isRideShare: Boolean) : Child()

        // Driver
        data object DriverHome : Child()
        data object SelecionarVeiculo : Child()
        data class CadastroVeiculoMultiStep(val municipalityId: String? = null, val prefillNumber: String? = null) : Child()
        data object Mapa : Child()
        data object Avisos : Child()
        data object DriverConfig : Child()
        data class TrocarOnibus(val currentBusId: String? = null) : Child()

        // Manager
        data object ManagerDashboard : Child()
        data object ManagerRoutes : Child()
        data object ManagerValidations : Child()
        data object ManagerFrota : Child()
        data object ManagerMotoristas : Child()
        data object ManagerRelatorios : Child()
        data object ManagerConfiguracoes : Child()
        data object SuperAdminManagement : Child()
        data object ManagerCadastroMotorista : Child()
        data object ManagerNotificacoes : Child()
        data class ManagerMotoristaDetail(val userId: String) : Child()
        data class ManagerRouteDetail(val routeId: String) : Child()
        data class ManagerBusDetail(val busId: String) : Child()
        data class ManagerStudentDetail(val userId: String) : Child()
        data class ManagerPickupPointForm(val routeId: String, val pointId: String? = null) : Child()
        data object ManagerRouteForm : Child()
    }

    companion object {
        private val authRoutes = setOf(
            Config.Login,
            Config.Cadastro,
            Config.RedefinirSenha,
        )

        private val publicRoutes = authRoutes

        private val studentRoutes = setOf(
            Config.StudentHome,
            Config.Reservar,
            Config.Bilhete,
            Config.Perfil,
            Config.Historico,
            Config.Carteirinha,
            Config.Pagamentos,
            Config.Lider,
            Config.PontoEmbarque,
            Config.MeusDados,
            Config.AlterarSenha,
            Config.RenovarSemestre,
            Config.BaixaMobilidade,
            Config.Regras,
        )

        private val driverRoutes = setOf(
            Config.DriverHome,
            Config.SelecionarVeiculo,
            Config.Mapa,
            Config.Avisos,
            Config.DriverConfig,
            Config.MeusDados,
            Config.AlterarSenha,
        )

        private val managerRoutes = setOf(
            Config.ManagerDashboard,
            Config.ManagerRoutes,
            Config.ManagerValidations,
            Config.ManagerFrota,
            Config.ManagerMotoristas,
            Config.ManagerRelatorios,
            Config.ManagerConfiguracoes,
            Config.ManagerCadastroMotorista,
            Config.ManagerNotificacoes,
            Config.MeusDados,
            Config.AlterarSenha,
        )

        private val superAdminRoutes = managerRoutes + Config.SuperAdminManagement
    }
}
