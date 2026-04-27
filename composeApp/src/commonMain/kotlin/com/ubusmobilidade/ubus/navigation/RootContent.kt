package com.ubusmobilidade.ubus.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.ubusmobilidade.ubus.ui.screens.auth.CadastroScreen
import com.ubusmobilidade.ubus.ui.screens.auth.LoginScreen
import com.ubusmobilidade.ubus.ui.screens.auth.RedefinirSenhaScreen
import com.ubusmobilidade.ubus.ui.screens.driver.AvisosScreen
import com.ubusmobilidade.ubus.ui.screens.driver.CadastroVeiculoMultiStepScreen
import com.ubusmobilidade.ubus.ui.screens.driver.DriverConfigScreen
import com.ubusmobilidade.ubus.ui.screens.driver.MapaScreen
import com.ubusmobilidade.ubus.ui.screens.driver.MotoristaSplashScreen
import com.ubusmobilidade.ubus.ui.screens.driver.SelecionarVeiculoScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerConfiguracoesScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerDashboardScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerFrotaScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerMotoristasScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerCadastroMotoristaScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerNotificacoesScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerRelatoriosScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerRoutesScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerValidationsScreen
import com.ubusmobilidade.ubus.ui.screens.manager.SuperAdminManagementScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerMotoristaDetailScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerRouteDetailScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerBusDetailScreen
import com.ubusmobilidade.ubus.ui.screens.manager.ManagerStudentDetailScreen
import com.ubusmobilidade.ubus.ui.screens.student.AlterarSenhaScreen
import com.ubusmobilidade.ubus.ui.screens.student.BaixaMobilidadeScreen
import com.ubusmobilidade.ubus.ui.screens.student.BilheteScreen
import com.ubusmobilidade.ubus.ui.screens.student.CarteirinhaScreen
import com.ubusmobilidade.ubus.ui.screens.student.HistoricoScreen
import com.ubusmobilidade.ubus.ui.screens.student.HomeScreen
import com.ubusmobilidade.ubus.ui.screens.student.LiderScreen
import com.ubusmobilidade.ubus.ui.screens.student.MeusDadosScreen
import com.ubusmobilidade.ubus.ui.screens.student.PagamentosScreen
import com.ubusmobilidade.ubus.ui.screens.student.PerfilScreen
import com.ubusmobilidade.ubus.ui.screens.student.PontoEmbarqueScreen
import com.ubusmobilidade.ubus.ui.screens.student.RegrasScreen
import com.ubusmobilidade.ubus.ui.screens.student.RenovarSemestreScreen
import com.ubusmobilidade.ubus.ui.screens.student.ReservarScreen

@Composable
fun RootContent(component: RootComponent) {
    Children(
        stack = component.childStack,
        animation = stackAnimation(fade()),
    ) { child ->
        when (val instance = child.instance) {
            // Auth
            is RootComponent.Child.Login -> LoginScreen(component)
            is RootComponent.Child.Cadastro -> CadastroScreen(component)
            is RootComponent.Child.RedefinirSenha -> RedefinirSenhaScreen(component)

            // Student
            is RootComponent.Child.StudentHome -> HomeScreen(component)
            is RootComponent.Child.Reservar -> ReservarScreen(component)
            is RootComponent.Child.Bilhete -> BilheteScreen(component)
            is RootComponent.Child.Perfil -> PerfilScreen(component)
            is RootComponent.Child.Historico -> HistoricoScreen(component)
            is RootComponent.Child.Carteirinha -> CarteirinhaScreen(component)
            is RootComponent.Child.Pagamentos -> PagamentosScreen(component)
            is RootComponent.Child.Lider -> LiderScreen(component)
            is RootComponent.Child.PontoEmbarque -> PontoEmbarqueScreen(component)
            is RootComponent.Child.MeusDados -> MeusDadosScreen(component)
            is RootComponent.Child.AlterarSenha -> AlterarSenhaScreen(component)
            is RootComponent.Child.RenovarSemestre -> RenovarSemestreScreen(component)
            is RootComponent.Child.BaixaMobilidade -> BaixaMobilidadeScreen(component)
            is RootComponent.Child.Regras -> RegrasScreen(component)

            // Driver
            is RootComponent.Child.MotoristaSplash -> MotoristaSplashScreen(component)
            is RootComponent.Child.SelecionarVeiculo -> SelecionarVeiculoScreen(component)
            is RootComponent.Child.CadastroVeiculoMultiStep -> CadastroVeiculoMultiStepScreen(component)
            is RootComponent.Child.Mapa -> MapaScreen(component)
            is RootComponent.Child.Avisos -> AvisosScreen(component)
            is RootComponent.Child.DriverConfig -> DriverConfigScreen(component)

            // Manager
            is RootComponent.Child.ManagerDashboard -> ManagerDashboardScreen(component)
            is RootComponent.Child.ManagerRoutes -> ManagerRoutesScreen(component)
            is RootComponent.Child.ManagerValidations -> ManagerValidationsScreen(component)
            is RootComponent.Child.ManagerFrota -> ManagerFrotaScreen(component)
            is RootComponent.Child.ManagerMotoristas -> ManagerMotoristasScreen(component)
            is RootComponent.Child.ManagerRelatorios -> ManagerRelatoriosScreen(component)
            is RootComponent.Child.ManagerConfiguracoes -> ManagerConfiguracoesScreen(component)
            is RootComponent.Child.SuperAdminManagement -> SuperAdminManagementScreen(component)
            is RootComponent.Child.ManagerCadastroMotorista -> ManagerCadastroMotoristaScreen(component)
            is RootComponent.Child.ManagerNotificacoes -> ManagerNotificacoesScreen(component)
            is RootComponent.Child.ManagerMotoristaDetail -> ManagerMotoristaDetailScreen(component, instance.userId)
            is RootComponent.Child.ManagerRouteDetail -> ManagerRouteDetailScreen(component, instance.routeId)
            is RootComponent.Child.ManagerBusDetail -> ManagerBusDetailScreen(component, instance.busId)
            is RootComponent.Child.ManagerStudentDetail -> ManagerStudentDetailScreen(component, instance.userId)
        }
    }
}
