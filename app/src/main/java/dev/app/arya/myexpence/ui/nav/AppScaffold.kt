package dev.app.arya.myexpence.ui.nav

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.app.arya.myexpence.ui.screens.BudgetsScreen
import dev.app.arya.myexpence.ui.screens.DashboardScreen
import dev.app.arya.myexpence.ui.screens.SettingsScreen
import dev.app.arya.myexpence.ui.screens.TransactionsScreen
import dev.app.arya.myexpence.ui.state.AppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(factory: AppViewModelFactory) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val topLevel = listOf(Route.Dashboard, Route.Transactions, Route.Budgets, Route.Settings)
    val showBottomBar = currentRoute in topLevel.map { it.route } || currentRoute == null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar {
                    NavButton("Dashboard", selected = currentRoute == Route.Dashboard.route) {
                        navController.navigate(Route.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    NavButton("Transactions", selected = currentRoute == Route.Transactions.route) {
                        navController.navigate(Route.Transactions.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    NavButton("Budgets", selected = currentRoute == Route.Budgets.route) {
                        navController.navigate(Route.Budgets.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    NavButton("Settings", selected = currentRoute == Route.Settings.route) {
                        navController.navigate(Route.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Route.Transactions.route) {
                FloatingActionButton(onClick = { navController.navigate(Route.AddTransaction.route) }) {
                    Text("+")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Dashboard.route,
            modifier = Modifier,
        ) {
            composable(Route.Dashboard.route) { DashboardScreen(factory = factory, contentPadding = innerPadding) }
            composable(Route.Transactions.route) { TransactionsScreen(factory = factory, contentPadding = innerPadding) }
            composable(Route.Budgets.route) { BudgetsScreen(factory = factory, contentPadding = innerPadding) }
            composable(Route.Settings.route) { SettingsScreen(factory = factory, contentPadding = innerPadding) }
            composable(Route.AddTransaction.route) {
                dev.app.arya.myexpence.ui.screens.AddTransactionScreen(
                    factory = factory,
                    contentPadding = innerPadding,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun NavButton(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(if (selected) "• $label" else label)
    }
}

