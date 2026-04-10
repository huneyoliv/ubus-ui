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
        initialConfiguration = Config.Splash,
        handleBackButton = true,
        childFactory = ::createChild,
    )

    private fun createChild(config: Config, context: ComponentContext): Child = when (config) {
        // Auth
        Config.Splash -> Child.Splash
        Config.Login -> Child.Login
        Config.Cadastro -> Child.Cadastro
        Config.RedefinirSenha -> Child.RedefinirSenha

        // Student
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

        // Driver
        Config.MotoristaSplash -> Child.MotoristaSplash
        Config.SelecionarVeiculo -> Child.SelecionarVeiculo
        Config.CadastroVeiculo -> Child.CadastroVeiculo
        Config.Mapa -> Child.Mapa
        Config.Avisos -> Child.Avisos
        Config.DriverConfig -> Child.DriverConfig

        // Manager
        Config.ManagerDashboard -> Child.ManagerDashboard
        Config.ManagerRoutes -> Child.ManagerRoutes
        Config.ManagerValidations -> Child.ManagerValidations
        Config.ManagerFrota -> Child.ManagerFrota
        Config.ManagerMotoristas -> Child.ManagerMotoristas
        Config.ManagerRelatorios -> Child.ManagerRelatorios
        Config.ManagerConfiguracoes -> Child.ManagerConfiguracoes
        Config.SuperAdminManagement -> Child.SuperAdminManagement
    }

    // ── Navigation actions ──

    fun navigateTo(config: Config) {
        navigation.pushNew(config)
    }

    fun goBack() {
        navigation.pop()
    }

    fun replaceWith(config: Config) {
        navigation.replaceAll(config)
    }

    fun onLoginSuccess() {
        val role = authStorage.userRole
        when (role) {
            RoleUsuario.DRIVER -> replaceWith(Config.MotoristaSplash)
            RoleUsuario.MANAGER, RoleUsuario.SUPER_ADMIN -> replaceWith(Config.ManagerDashboard)
            else -> replaceWith(Config.StudentHome)
        }
    }

    fun logout() {
        authStorage.clear()
        replaceWith(Config.Login)
    }

    // ── Sealed configs ──

    @Serializable
    sealed class Config {
        // Auth
        @Serializable data object Splash : Config()
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
        @Serializable data object Regras : Config()

        // Driver
        @Serializable data object MotoristaSplash : Config()
        @Serializable data object SelecionarVeiculo : Config()
        @Serializable data object CadastroVeiculo : Config()
        @Serializable data object Mapa : Config()
        @Serializable data object Avisos : Config()
        @Serializable data object DriverConfig : Config()

        // Manager
        @Serializable data object ManagerDashboard : Config()
        @Serializable data object ManagerRoutes : Config()
        @Serializable data object ManagerValidations : Config()
        @Serializable data object ManagerFrota : Config()
        @Serializable data object ManagerMotoristas : Config()
        @Serializable data object ManagerRelatorios : Config()
        @Serializable data object ManagerConfiguracoes : Config()
        @Serializable data object SuperAdminManagement : Config()
    }

    // ── Sealed children ──

    sealed class Child {
        // Auth
        data object Splash : Child()
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
        data object Regras : Child()

        // Driver
        data object MotoristaSplash : Child()
        data object SelecionarVeiculo : Child()
        data object CadastroVeiculo : Child()
        data object Mapa : Child()
        data object Avisos : Child()
        data object DriverConfig : Child()

        // Manager
        data object ManagerDashboard : Child()
        data object ManagerRoutes : Child()
        data object ManagerValidations : Child()
        data object ManagerFrota : Child()
        data object ManagerMotoristas : Child()
        data object ManagerRelatorios : Child()
        data object ManagerConfiguracoes : Child()
        data object SuperAdminManagement : Child()
    }
}
