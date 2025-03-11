package com.xavim.testsimpleact.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
//import com.xavim.testsimpleact.presentation.features.dataEntry.DataEntryScreen
//import com.xavim.testsimpleact.presentation.features.datasetInstances.DatasetInstanceDetailScreen
//import com.xavim.testsimpleact.presentation.features.datasetInstances.DatasetInstanceListScreen
import com.xavim.testsimpleact.presentation.features.datasets.DatasetScreen
import com.xavim.testsimpleact.presentation.features.login.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object DatasetList : Screen("dataset_list")

    object DatasetInstanceList : Screen("dataset_instances/{datasetId}") {
        fun createRoute(datasetId: String) = "dataset_instances/$datasetId"
    }

    object DatasetInstanceDetail : Screen(
        "dataset_instance/{datasetId}/{periodId}/{orgUnitId}/{attributeOptionComboId}"
    ) {
        fun createRoute(
            datasetId: String,
            periodId: String,
            orgUnitId: String,
            attributeOptionComboId: String
        ) = "dataset_instance/$datasetId/$periodId/$orgUnitId/$attributeOptionComboId"
    }

    object DataEntry : Screen("data_entry") {
        val routeForNewEntry = "data_entry/{datasetId}"
        val routeForExistingEntry = "data_entry/{datasetId}/{periodId}/{orgUnitId}/{attributeOptionComboId}"

        fun createRouteForNewEntry(datasetId: String) = "data_entry/$datasetId"

        fun createRouteForExistingEntry(
            datasetId: String,
            periodId: String,
            orgUnitId: String,
            attributeOptionComboId: String
        ) = "data_entry/$datasetId/$periodId/$orgUnitId/$attributeOptionComboId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {

                    navController.navigate(Screen.DatasetList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Dataset List Screen
        composable(Screen.DatasetList.route) {
            DatasetScreen(
                onDatasetClick = { datasetId ->
                    navController.navigate(Screen.DatasetInstanceList.createRoute(datasetId.toString()))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.DatasetList.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
        // Dataset Instance List Screen
//        composable(
//            route = Screen.DatasetInstanceList.route,
//            arguments = listOf(
//                navArgument("datasetId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val datasetId = backStackEntry.arguments?.getString("datasetId") ?: ""
//
//            DatasetInstanceListScreen(
//                datasetId = datasetId,
//                onNavigateBack = { navController.popBackStack() },
//                onItemClick = { datasetId, periodId, orgUnitId, attributeOptionComboId ->
//                    navController.navigate(
//                        Screen.DatasetInstanceDetail.createRoute(
//                            datasetId, periodId, orgUnitId, attributeOptionComboId
//                        )
//                    )
//                },
//                onNewEntryClick = { datasetId ->
//                    navController.navigate(Screen.DataEntry.createRouteForNewEntry(datasetId))
//                }
//            )
//        }
//
//        // Dataset Instance Detail Screen
//        composable(
//            route = Screen.DatasetInstanceDetail.route,
//            arguments = listOf(
//                navArgument("datasetId") { type = NavType.StringType },
//                navArgument("periodId") { type = NavType.StringType },
//                navArgument("orgUnitId") { type = NavType.StringType },
//                navArgument("attributeOptionComboId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val datasetId = backStackEntry.arguments?.getString("datasetId") ?: ""
//            val periodId = backStackEntry.arguments?.getString("periodId") ?: ""
//            val orgUnitId = backStackEntry.arguments?.getString("orgUnitId") ?: ""
//            val attributeOptionComboId = backStackEntry.arguments?.getString("attributeOptionComboId") ?: ""
//
//            DatasetInstanceDetailScreen(
//                datasetId = datasetId,
//                periodId = periodId,
//                orgUnitId = orgUnitId,
//                attributeOptionComboId = attributeOptionComboId,
//                onNavigateBack = { navController.popBackStack() },
//                onNavigateToDataEntry = { datasetId, periodId, orgUnitId, attributeOptionComboId ->
//                    navController.navigate(
//                        Screen.DataEntry.createRouteForExistingEntry(
//                            datasetId, periodId, orgUnitId, attributeOptionComboId
//                        )
//                    )
//                }
//            )
//        }
//
//        // Data Entry Screen - New Entry
//        composable(
//            route = Screen.DataEntry.routeForNewEntry,
//            arguments = listOf(
//                navArgument("datasetId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            val datasetId = backStackEntry.arguments?.getString("datasetId") ?: ""
//
//            DataEntryScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//
//        // Data Entry Screen - Existing Entry
//        composable(
//            route = Screen.DataEntry.routeForExistingEntry,
//            arguments = listOf(
//                navArgument("datasetId") { type = NavType.StringType },
//                navArgument("periodId") { type = NavType.StringType },
//                navArgument("orgUnitId") { type = NavType.StringType },
//                navArgument("attributeOptionComboId") { type = NavType.StringType }
//            )
//        ) { backStackEntry ->
//            DataEntryScreen(
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//    }
//}