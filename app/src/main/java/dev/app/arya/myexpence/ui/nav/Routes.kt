package dev.app.arya.myexpence.ui.nav

sealed class Route(val route: String) {
    data object Dashboard : Route("dashboard")
    data object Transactions : Route("transactions")
    data object Budgets : Route("budgets")
    data object Settings : Route("settings")

    data object AddTransaction : Route("add_transaction")
}

