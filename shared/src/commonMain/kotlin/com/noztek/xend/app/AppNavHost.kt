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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.noztek.xend.core.session.SessionEventType
import com.noztek.xend.core.session.SessionEventBus
import com.noztek.xend.core.ui.components.AppBottomBar
import com.noztek.xend.core.ui.components.BackTopBar
import com.noztek.xend.core.ui.components.RootBottomBarTab
import com.noztek.xend.core.ui.components.RootTopBar
import com.noztek.xend.core.ui.components.rememberRootBottomBarItems
import com.noztek.xend.feature.auth.presentation.screen.LoginScreen
import com.noztek.xend.feature.auth.presentation.screen.RegisterScreen
import com.noztek.xend.feature.auth.presentation.screen.VerifyEmailScreen
import com.noztek.xend.feature.challenges.presentation.screen.ChallengesScreen
import com.noztek.xend.feature.dailycheckin.presentation.screen.DailyCheckInScreen
import com.noztek.xend.feature.dailyritual.presentation.screen.DailyRitualScreen
import com.noztek.xend.feature.games.presentation.screen.GamesScreen
import com.noztek.xend.feature.incominginvite.presentation.screen.IncomingInviteScreen
import com.noztek.xend.feature.invites.presentation.screen.InvitePartnerScreen
import com.noztek.xend.feature.invites.presentation.screen.InvitesScreen
import com.noztek.xend.feature.message.presentation.screen.MessageScreen
import com.noztek.xend.feature.outgoinginvite.presentation.screen.OutgoingInviteScreen
import com.noztek.xend.feature.settings.presentation.screen.SettingsScreen
import com.noztek.xend.feature.auth.presentation.viewmodel.AuthViewModel
import com.noztek.xend.feature.space.domain.usecase.GetDefaultRelationshipSpaceUseCase
import com.noztek.xend.feature.space.presentation.screen.SpaceScreen
import com.noztek.xend.feature.space.presentation.screen.HiddenSpacesScreen
import com.noztek.xend.feature.spacesetup.presentation.screen.SpaceSetupScreen
import com.noztek.xend.feature.welcome.presentation.screen.OfflineScreen
import com.noztek.xend.feature.welcome.presentation.screen.WelcomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private object AppRoutes {
    const val Startup = "startup"
    const val Offline = "offline"
    const val Welcome = "welcome"
    const val Login = "login"
    const val AuthGraph = "auth"
    const val IncomingInvite = "incoming-invite"
    const val OutgoingInvite = "outgoing-invite"
    const val SpaceSetup = "space-setup"
    const val Main = "main"
    const val DailyCheckIn = "daily-checkin"
    const val DailyRituals = "daily-rituals"
    const val Challenges = "challenges"
    const val Games = "games"
    const val InvitePartner = "invite-partner"
    const val Invites = "invites"
    const val HiddenSpaces = "hidden-spaces"
    const val Settings = "settings"
    const val Message = "message"
}

private object AuthRoutes {
    const val Register = "register"
    const val VerifyEmail = "verify-email"
}

@Composable
fun AppNavHost(
    startupViewModel: StartupViewModel = koinInject(),
) {
    val navController = rememberNavController()
    val authViewModel = koinInject<AuthViewModel>()
    val getDefaultRelationshipSpace = koinInject<GetDefaultRelationshipSpaceUseCase>()
    val sessionEventBus = koinInject<SessionEventBus>()
    val scope = rememberCoroutineScope()
    var activeConversationId by rememberSaveable { mutableStateOf("") }

    fun openChat() {
        scope.launch {
            val resolvedConversationId = activeConversationId.ifBlank {
                getDefaultRelationshipSpace()?.conversationId.orEmpty()
            }
            if (resolvedConversationId.isBlank()) return@launch

            activeConversationId = resolvedConversationId
            navController.navigate(AppRoutes.Message) {
                launchSingleTop = true
            }
        }
    }

    fun openMain() {
        navController.navigate(AppRoutes.Main) {
            popUpTo(AppRoutes.Main) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun openDailyRituals() {
        navController.navigate(AppRoutes.DailyRituals) {
            launchSingleTop = true
        }
    }

    fun openDailyCheckIn() {
        navController.navigate(AppRoutes.DailyCheckIn) {
            launchSingleTop = true
        }
    }

    fun openGames() {
        navController.navigate(AppRoutes.Games) {
            launchSingleTop = true
        }
    }

    fun openChallenges() {
        navController.navigate(AppRoutes.Challenges) {
            launchSingleTop = true
        }
    }

    LaunchedEffect(sessionEventBus, navController) {
        sessionEventBus.events.collect { event ->
            if (event.type != SessionEventType.Expired) return@collect

            activeConversationId = ""
            authViewModel.handleSessionExpired()
            navController.navigate(AppRoutes.Login) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Startup,
    ) {
        composable(AppRoutes.Startup) {
            val state by startupViewModel.state.collectAsState()

            LaunchedEffect(
                state.destination,
                state.pendingVerificationEmail,
                state.pendingVerificationResendAvailableAtEpochSeconds,
            ) {
                when (state.destination) {
                    StartupDestination.MAIN -> {
                        navController.navigate(
                            AppRoutes.Main,
                        ) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.WELCOME -> {
                        navController.navigate(AppRoutes.Welcome) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.LOGIN -> {
                        navController.navigate(AppRoutes.Login) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.VERIFY_EMAIL -> {
                        val email = state.pendingVerificationEmail?.trim().orEmpty()
                        if (email.isBlank()) {
                            navController.navigate(AppRoutes.Welcome) {
                                popUpTo(AppRoutes.Startup) { inclusive = true }
                            }
                        } else {
                            authViewModel.prepareVerification(
                                email = email,
                                resendAvailableAtEpochSeconds =
                                    state.pendingVerificationResendAvailableAtEpochSeconds,
                                startResendCooldown = false,
                            )
                            navController.navigate(AuthRoutes.VerifyEmail) {
                                popUpTo(AppRoutes.Startup) { inclusive = true }
                            }
                        }
                    }

                    StartupDestination.SPACE_SETUP -> {
                        navController.navigate(AppRoutes.SpaceSetup) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.INCOMING_INVITE -> {
                        navController.navigate(AppRoutes.IncomingInvite) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.OUTGOING_INVITE -> {
                        navController.navigate(AppRoutes.OutgoingInvite) {
                            popUpTo(AppRoutes.Startup) { inclusive = true }
                        }
                    }

                    StartupDestination.OFFLINE -> {
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
                    startupViewModel.completeOnboarding()
                    navController.navigate(AppRoutes.AuthGraph) {
                        popUpTo(AppRoutes.Welcome) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.Login) {
            val state by authViewModel.state.collectAsState()

            LaunchedEffect(state.session) {
                if (state.session != null) {
                    startupViewModel.checkApiHealth()
                    navController.navigate(AppRoutes.Startup) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                onCreateSpaceClick = {
                    val popped = navController.popBackStack(AuthRoutes.Register, inclusive = false)
                    if (!popped) {
                        navController.navigate(AuthRoutes.Register) {
                            launchSingleTop = true
                        }
                    }
                },
                viewModel = authViewModel,
            )
        }

        composable(AppRoutes.SpaceSetup) {
            val setupViewModel = koinInject<com.noztek.xend.feature.spacesetup.presentation.viewmodel.SpaceSetupViewModel>()
            val setupState by setupViewModel.state.collectAsState()

            LaunchedEffect(setupState.shouldEnterMain) {
                if (!setupState.shouldEnterMain) return@LaunchedEffect
                setupViewModel.consumeEnterMain()
                navController.navigate(AppRoutes.Main) {
                    popUpTo(AppRoutes.SpaceSetup) { inclusive = true }
                }
            }

            LaunchedEffect(setupState.shouldOpenIncomingInvite) {
                if (!setupState.shouldOpenIncomingInvite) return@LaunchedEffect
                setupViewModel.consumeOpenIncomingInvite()
                navController.navigate(AppRoutes.IncomingInvite) {
                    launchSingleTop = true
                }
            }

            LaunchedEffect(setupState.shouldOpenOutgoingInvite) {
                if (!setupState.shouldOpenOutgoingInvite) return@LaunchedEffect
                setupViewModel.consumeOpenOutgoingInvite()
                navController.navigate(AppRoutes.OutgoingInvite) {
                    launchSingleTop = true
                }
            }

            SpaceSetupScreen(
                onOpenInvites = { navController.navigate(AppRoutes.Invites) },
                viewModel = setupViewModel,
            )
        }

        composable(AppRoutes.IncomingInvite) {
            IncomingInviteScreen(
                onAccepted = {
                    navController.navigate(AppRoutes.Main) {
                        popUpTo(AppRoutes.IncomingInvite) { inclusive = true }
                    }
                },
                onDeclined = {
                    navController.navigate(AppRoutes.SpaceSetup) {
                        popUpTo(AppRoutes.IncomingInvite) { inclusive = true }
                    }
                },
            )
        }

        composable(AppRoutes.OutgoingInvite) {
            OutgoingInviteScreen(
                onAccepted = {
                    navController.navigate(AppRoutes.Main) {
                        popUpTo(AppRoutes.OutgoingInvite) { inclusive = true }
                    }
                },
                onReturnedToSetup = {
                    navController.navigate(AppRoutes.SpaceSetup) {
                        popUpTo(AppRoutes.OutgoingInvite) { inclusive = true }
                    }
                },
            )
        }

        authNavGraph(
            navController = navController,
            authViewModel = authViewModel,
            onAuthenticated = {
                startupViewModel.checkApiHealth()
                navController.navigate(AppRoutes.Startup) {
                    popUpTo(AppRoutes.AuthGraph) { inclusive = true }
                }
            },
        )

        composable(AppRoutes.Main) {
            val bottomItems = rememberRootBottomBarItems(
                selectedTab = RootBottomBarTab.Space,
                onSpaceClick = {},
                onRitualsClick = ::openDailyRituals,
                onGamesClick = ::openGames,
                onChallengesClick = ::openChallenges,
                onChatClick = ::openChat,
            )

            AppRouteScaffold(
                topBar = {
                    RootTopBar(
                        title = "Xend",
                        onNotificationClick = { navController.navigate(AppRoutes.Invites) },
                        onSettingsClick = { navController.navigate(AppRoutes.Settings) },
                        showLogo = true,
                        showNotification = true,
                        showSearch = false,
                        showMenu = true,
                    )
                },
                bottomBar = {
                    AppBottomBar(items = bottomItems)
                },
            ) {
                SpaceScreen(
                    onMessageClick = { conversationId ->
                        if (conversationId.isBlank()) return@SpaceScreen
                        activeConversationId = conversationId
                        navController.navigate(AppRoutes.Message)
                    },
                    onDailyCheckInClick = ::openDailyCheckIn,
                    onDailyRitualClick = { navController.navigate(AppRoutes.DailyRituals) },
                    onGamesClick = { navController.navigate(AppRoutes.Games) },
                    onChallengesClick = { navController.navigate(AppRoutes.Challenges) },
                )
            }
        }

        composable(AppRoutes.DailyCheckIn) {
            AppRouteScaffold(
                topBar = {},
            ) {
                DailyCheckInScreen()
            }
        }

        composable(AppRoutes.DailyRituals) {
            val bottomItems = rememberRootBottomBarItems(
                selectedTab = RootBottomBarTab.Rituals,
                onSpaceClick = ::openMain,
                onRitualsClick = {},
                onGamesClick = ::openGames,
                onChallengesClick = ::openChallenges,
                onChatClick = ::openChat,
            )
            AppRouteScaffold(
                topBar = {},
                bottomBar = {
                    AppBottomBar(items = bottomItems)
                },
            ) {
                DailyRitualScreen()
            }
        }

        composable(AppRoutes.Challenges) {
            val bottomItems = rememberRootBottomBarItems(
                selectedTab = RootBottomBarTab.Challenges,
                onSpaceClick = ::openMain,
                onRitualsClick = ::openDailyRituals,
                onGamesClick = ::openGames,
                onChallengesClick = {},
                onChatClick = ::openChat,
            )
            AppRouteScaffold(
                topBar = {},
                bottomBar = {
                    AppBottomBar(items = bottomItems)
                },
            ) {
                ChallengesScreen()
            }
        }

        composable(AppRoutes.Games) {
            val bottomItems = rememberRootBottomBarItems(
                selectedTab = RootBottomBarTab.Games,
                onSpaceClick = ::openMain,
                onRitualsClick = ::openDailyRituals,
                onGamesClick = {},
                onChallengesClick = ::openChallenges,
                onChatClick = ::openChat,
            )
            AppRouteScaffold(
                topBar = {},
                bottomBar = {
                    AppBottomBar(items = bottomItems)
                },
            ) {
                GamesScreen(
                    onBackClick = navController::popBackStack,
                )
            }
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
                        onSettingsClick = { navController.navigate(AppRoutes.Settings) },
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

        composable(AppRoutes.Settings) {
            AppRouteScaffold(
                topBar = {
                    BackTopBar(
                        title = "Settings",
                        onBackClick = navController::popBackStack,
                    )
                },
            ) {
                SettingsScreen(
                    onLoggedOut = {
                        startupViewModel.checkApiHealth()
                        navController.navigate(AppRoutes.Startup) {
                            popUpTo(AppRoutes.Main) { inclusive = true }
                        }
                    },
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

            LaunchedEffect(state.existingAccountEmail) {
                val email = state.existingAccountEmail ?: return@LaunchedEffect
                authViewModel.consumeExistingAccountEmail()
                authViewModel.prepareLogin(email)
                navController.navigate(AppRoutes.Login) {
                    launchSingleTop = true
                }
            }

            RegisterScreen(
                onLoginClick = { email ->
                    authViewModel.prepareLogin(email)
                    navController.navigate(AppRoutes.Login) {
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
                navController.navigate(AppRoutes.Login) {
                    popUpTo(AuthRoutes.VerifyEmail) { inclusive = true }
                }
            }

            VerifyEmailScreen(
                onChangeEmailClick = {
                    navController.navigate(AuthRoutes.Register) {
                        popUpTo(AuthRoutes.VerifyEmail) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = authViewModel,
            )
        }
    }
}

@Composable
private fun AppRouteScaffold(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        bottomBar = bottomBar,
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
