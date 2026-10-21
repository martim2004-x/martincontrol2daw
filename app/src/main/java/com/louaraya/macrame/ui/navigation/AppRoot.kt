package com.louaraya.macrame.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.louaraya.macrame.LocalAppContainer
import com.louaraya.macrame.data.NotificationEntity
import com.louaraya.macrame.domain.NotificationType
import com.louaraya.macrame.domain.Role
import com.louaraya.macrame.ui.components.FloatingNavBar
import com.louaraya.macrame.ui.components.NavItem
import com.louaraya.macrame.ui.screens.AdminCampaignsScreen
import com.louaraya.macrame.ui.screens.AdminProductsScreen
import com.louaraya.macrame.ui.screens.CampaignFormScreen
import com.louaraya.macrame.ui.screens.CatalogScreen
import com.louaraya.macrame.ui.screens.FavoritesScreen
import com.louaraya.macrame.ui.screens.HomeScreen
import com.louaraya.macrame.ui.screens.LoginScreen
import com.louaraya.macrame.ui.screens.MapScreen
import com.louaraya.macrame.ui.screens.NotificationsScreen
import com.louaraya.macrame.ui.screens.ProductDetailScreen
import com.louaraya.macrame.ui.screens.ProductFormScreen
import com.louaraya.macrame.ui.screens.ProfileScreen
import com.louaraya.macrame.ui.screens.RegisterScreen
import com.louaraya.macrame.ui.screens.RequestFormScreen
import com.louaraya.macrame.ui.screens.RequestsScreen
import com.louaraya.macrame.ui.screens.ScannerScreen
import com.louaraya.macrame.ui.screens.WelcomeScreen
import com.louaraya.macrame.ui.theme.MacrameColors

private val ClientTabs = listOf(
    NavItem(Routes.HOME, "Inicio", Icons.Rounded.Home),
    NavItem(Routes.CATALOG, "Catálogo", Icons.Rounded.Category),
    NavItem(Routes.MAP, "Mapa", Icons.Rounded.Map),
    NavItem(Routes.REQUESTS, "Solicitudes", Icons.Rounded.ReceiptLong),
    NavItem(Routes.PROFILE, "Perfil", Icons.Rounded.Person)
)

private val AdminTabs = listOf(
    NavItem(Routes.HOME, "Panel", Icons.Rounded.Dashboard),
    NavItem(Routes.ADMIN_PRODUCTS, "Productos", Icons.Rounded.Inventory2),
    NavItem(Routes.ADMIN_CAMPAIGNS, "Campañas", Icons.Rounded.Campaign),
    NavItem(Routes.REQUESTS, "Solicitudes", Icons.Rounded.ReceiptLong),
    NavItem(Routes.PROFILE, "Perfil", Icons.Rounded.Person)
)

/** barra de navegacion */
@Composable
fun AppRoot() {
    val session = LocalAppContainer.current.session
    val navController = rememberNavController()
    val user by session.currentUser.collectAsStateWithLifecycle()
    val loggedIn by session.loggedIn.collectAsStateWithLifecycle()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    val role = user?.role ?: if (loggedIn) session.savedRole() else null
    val isAdmin = role == Role.ADMIN
    val tabs = if (isAdmin) AdminTabs else ClientTabs
    val tabRoutes = tabs.map { it.route }
    val showBar = loggedIn && route != null && route in tabRoutes

    val startDestination = remember { if (session.savedUserId() != -1L) Routes.HOME else Routes.WELCOME }

    // Cuando la sesion se cierra (desde el perfil), se vuelve a la bienvenida limpiando el historial.
    LaunchedEffect(loggedIn) {
        if (!loggedIn) {
            val current = navController.currentDestination?.route
            if (current != null && current !in Routes.publicRoutes) {
                navController.navigate(Routes.WELCOME) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    fun navigateTab(target: String) {
        navController.navigate(target) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /** Si el destino es una pestaña de la barra se navega como pestaña si no se apila. */
    fun go(target: String) {
        if (target in tabRoutes) navigateTab(target) else navController.navigate(target) { launchSingleTop = true }
    }

    fun openNotification(n: NotificationEntity) {
        when (n.type) {
            NotificationType.REQUEST -> go(Routes.REQUESTS)
            NotificationType.CAMPAIGN -> go(if (isAdmin) Routes.ADMIN_CAMPAIGNS else Routes.CATALOG)
            NotificationType.CATALOG -> {
                val id = n.refId
                if (id != null) navController.navigate(Routes.product(id)) else go(Routes.CATALOG)
            }
            NotificationType.FAIR -> go(Routes.MAP)
        }
    }

    Scaffold(
        containerColor = MacrameColors.Ash,
        bottomBar = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(200)),
                exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(150))
            ) {
                FloatingNavBar(items = tabs, currentRoute = route, onSelect = { navigateTab(it) })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(320)) { it / 14 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(260)) { it / 14 } }
        ) {
            // ───── autenticacionb ─────
            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onStart = { navController.navigate(Routes.LOGIN) },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { enterApp(navController) },
                    onRegister = { navController.navigate(Routes.REGISTER) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = { enterApp(navController) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ───── pestañas ─────
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigate = { go(it) },
                    onProduct = { navController.navigate(Routes.product(it)) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onRequestDetail = { go(Routes.REQUESTS) }
                )
            }
            composable(Routes.CATALOG) {
                CatalogScreen(
                    showBack = Routes.CATALOG !in tabRoutes,
                    onBack = { navController.popBackStack() },
                    onProduct = { navController.navigate(Routes.product(it)) },
                    onFavorites = { navController.navigate(Routes.FAVORITES) },
                    onScan = { navController.navigate(Routes.SCANNER) }
                )
            }
            composable(Routes.MAP) {
                MapScreen(
                    showBack = Routes.MAP !in tabRoutes,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REQUESTS) {
                RequestsScreen(
                    onNewRequest = { navController.navigate(Routes.requestForm()) },
                    onCatalog = { go(Routes.CATALOG) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSwitchUser = {
                        session.signOut()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) }
                )
            }
            composable(Routes.ADMIN_PRODUCTS) {
                AdminProductsScreen(
                    onNew = { navController.navigate(Routes.adminProductForm(0)) },
                    onEdit = { navController.navigate(Routes.adminProductForm(it)) }
                )
            }
            composable(Routes.ADMIN_CAMPAIGNS) {
                AdminCampaignsScreen(
                    onNew = { navController.navigate(Routes.adminCampaignForm(0)) },
                    onEdit = { navController.navigate(Routes.adminCampaignForm(it)) }
                )
            }

            // ───── pantallas secundarias ─────
            composable(
                Routes.PRODUCT,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                ProductDetailScreen(
                    productId = entry.longArg("id") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onRequest = { navController.navigate(Routes.requestForm(it)) },
                    onEdit = { navController.navigate(Routes.adminProductForm(it)) }
                )
            }
            composable(Routes.SCANNER) {
                ScannerScreen(
                    onBack = { navController.popBackStack() },
                    onProduct = { navController.navigate(Routes.product(it)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onProduct = { navController.navigate(Routes.product(it)) },
                    onRequest = { navController.navigate(Routes.requestForm(it)) }
                )
            }
            composable(
                Routes.REQUEST_FORM,
                arguments = listOf(navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                RequestFormScreen(
                    productId = entry.longArg("productId")?.takeIf { it > 0 },
                    onBack = { navController.popBackStack() },
                    onViewRequests = {
                        navController.popBackStack()
                        go(Routes.REQUESTS)
                    }
                )
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { openNotification(it) }
                )
            }
            composable(
                Routes.ADMIN_PRODUCT_FORM,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                ProductFormScreen(
                    productId = entry.longArg("id") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Routes.ADMIN_CAMPAIGN_FORM,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                CampaignFormScreen(
                    campaignId = entry.longArg("id") ?: 0L,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavBackStackEntry.longArg(key: String): Long? =
    arguments?.getLong(key)

/** Entra a la zona autenticada vaciando el historial de bienvenida/login/registro. */
private fun enterApp(navController: NavHostController) {
    navController.navigate(Routes.HOME) {
        popUpTo(navController.graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
