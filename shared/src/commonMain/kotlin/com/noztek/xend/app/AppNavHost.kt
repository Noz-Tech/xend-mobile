package com.noztek.xend.app
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.noztek.xend.core.ui.components.BackTopBar
import com.noztek.xend.core.ui.components.RootTopBar
import com.noztek.xend.feature.auth.presentation.screen.LoginScreen
import com.noztek.xend.feature.auth.presentation.screen.RegisterScreen
import com.noztek.xend.feature.auth.presentation.screen.VerifyEmailScreen
import com.noztek.xend.feature.invites.presentation.screen.InvitePartnerScreen
import com.noztek.xend.feature.invites.presentation.screen.InvitesScreen
import com.noztek.xend.feature.message.presentation.screen.MessageScreen
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import com.noztek.xend.feature.space.presentation.screen.SpaceScreen
import com.noztek.xend.feature.space.presentation.screen.HiddenSpacesScreen
import com.noztek.xend.feature.welcome.presentation.screen.OfflineScreen
import com.noztek.xend.feature.welcome.presentation.screen.WelcomeScreen
import org.koin.compose.koinInject

private object AppRoutes {
    const val Startup = "startup"
    const val Offline = "offline"
    const val Welcome = "welcome"
    const val AuthGraph = "auth"
    const val Main = "main"
    const val InvitePartner = "invite-partner"
    const val Invites = "invites"
    const val HiddenSpaces = "hidden-spaces"
    const val Message = "message"
}

private object AuthRoutes {
    const val Register = "register"
    const val VerifyEmail = "verify-email"
    const val Login = "login"
}

@Composable
fun AppNavHost(
    startupViewModel: StartupViewModel = koinInject(),
) {
    val navController = rememberNavController()
    val authViewModel = koinInject<AuthViewModel>()
    var activeConversationId by rememberSaveable { mutableStateOf("") }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Startup,
    ) {
        composable(AppRoutes.Startup) {
            val state by startupViewModel.state.collectAsState()

            LaunchedEffect(state.isApiOnline, state.hasSession) {
                when (state.isApiOnline) {
                    true -> {
                        navController.navigate(
                            if (state.hasSession) AppRoutes.Main else AppRoutes.Welcome,
                        ) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    false -> {
                        navController.navigate(AppRoutes.Offline) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    null -> Unit
                }
            }

            StartupScreen()
        }

        composable(AppRoutes.Offline) {
            OfflineScreen(
                onRetry = {
                    startupViewModel.checkApiHealth()
                    navController.navigate(AppRoutes.Startup) {
                        popUpTo(AppRoutes.Offline) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.Welcome) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(AuthRoutes.Register) {
                        launchSingleTop = true
                    }
                },
            )
        }

        authNavGraph(
            navController = navController,
            authViewModel = authViewModel,
            onAuthenticated = {
                startupViewModel.checkApiHealth()
                navController.navigate(AppRoutes.Main) {
                    popUpTo(AppRoutes.Welcome) { inclusive = true }
                }
            },
        )

        composable(AppRoutes.Main) {
            SpaceScreen(
                onOpenInvites = { navController.navigate(AppRoutes.Invites) },
                onOpenHiddenSpaces = { navController.navigate(AppRoutes.HiddenSpaces) },
                onMessageClick = { conversationId ->
                    if (conversationId.isBlank()) return@SpaceScreen
                    activeConversationId = conversationId
                    navController.navigate(AppRoutes.Message)
                },
                onInviteClick = { navController.navigate(AppRoutes.InvitePartner) },
            )
        }

        composable(AppRoutes.InvitePartner) {
            AppRouteScaffold(
                topBar = {
                    BackTopBar(
                        title = "Invite Partner",
                        onBackClick = navController::popBackStack,
                    )
                },
            ) {
                InvitePartnerScreen()
            }
        }

        composable(AppRoutes.Invites) {
            AppRouteScaffold(
                topBar = {
                    RootTopBar(
                        onInvitesClick = {},
                        onHiddenSpacesClick = { navController.navigate(AppRoutes.HiddenSpaces) },
                    )
                },
            ) {
                InvitesScreen()
            }
        }

        composable(AppRoutes.HiddenSpaces) {
            AppRouteScaffold(
                topBar = {
                    BackTopBar(
                        title = "Hidden Spaces",
                        onBackClick = navController::popBackStack,
                    )
                },
            ) {
                HiddenSpacesScreen(
                    onUnlocked = { navController.popBackStack() },
                )
            }
        }

        composable(AppRoutes.Message) {
            if (activeConversationId.isBlank()) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                StartupScreen()
            } else {
                MessageScreen(
                    conversationId = activeConversationId,
                    onBackClick = navController::popBackStack,
                )
            }
        }
    }
}

private fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
) {
    navigation(
        startDestination = AuthRoutes.Register,
        route = AppRoutes.AuthGraph,
    ) {
        composable(AuthRoutes.Register) {
            val state by authViewModel.state.collectAsState()

            LaunchedEffect(state.registeredEmail) {
                val email = state.registeredEmail ?: return@LaunchedEffect
                authViewModel.consumeRegisterSuccess()
                authViewModel.prepareVerification(email)
                navController.navigate(AuthRoutes.VerifyEmail)
            }

            RegisterScreen(
                onLoginClick = { email ->
                    authViewModel.prepareLogin(email)
                    navController.navigate(AuthRoutes.Login) {
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }

        composable(AuthRoutes.VerifyEmail) {
            val state by authViewModel.state.collectAsState()

            LaunchedEffect(state.emailVerified) {
                if (!state.emailVerified) return@LaunchedEffect
                authViewModel.consumeEmailVerified()
                authViewModel.prepareLogin(state.verificationEmail)
                navController.navigate(AuthRoutes.Login) {
                    popUpTo(AuthRoutes.VerifyEmail) { inclusive = true }
                }
            }

            VerifyEmailScreen(viewModel = authViewModel)
        }

        composable(AuthRoutes.Login) {
            val state by authViewModel.state.collectAsState()

            LaunchedEffect(state.session) {
                if (state.session != null) {
                    onAuthenticated()
                }
            }

            LoginScreen(
                onCreateSpaceClick = {
                    navController.popBackStack(AuthRoutes.Register, inclusive = false)
                },
                viewModel = authViewModel,
            )
        }
    }
}

@Composable
private fun AppRouteScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            content()
        }
    }
}

@Composable
private fun StartupScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Starting Xend...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
