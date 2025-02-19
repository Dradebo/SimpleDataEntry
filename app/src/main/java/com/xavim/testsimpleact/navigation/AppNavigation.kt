package com.xavim.testsimpleact.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.xavim.testsimpleact.presentation.features.datasetInstances.DatasetInstanceListScreen
import com.xavim.testsimpleact.presentation.features.datasets.DatasetScreen
import com.xavim.testsimpleact.presentation.features.login.LoginScreen

//sealed class Screen(val route: String) {
//    data object LoginScreen : Screen("login")  // Added login screen
//    data object FormScreen : Screen("forms")
//    data object DatasetScreen : Screen("datasets")
//    data class DatasetInstanceListScreen(val datasetId: String) : Screen("datasetInstanceList/{datasetId}") {
//        companion object {
//            const val DATASET_ID_KEY = "datasetId"
//            fun createRoute(datasetId: String) = "datasetInstanceList/$datasetId"
//        }
//    }
//
//    data class DataEntryScreen(
//        val datasetId: String,
//        val periodId: String? = null,
//        val orgUnitId: String? = null,
//        val attributeOptionComboId: String? = null
//    ) : Screen("dataEntry/{datasetId}?periodId={periodId}&orgUnitId={orgUnitId}&attributeOptionComboId={attributeOptionComboId}") {
//        companion object {
//            const val DATASET_ID_KEY = "datasetId"
//            const val PERIOD_ID_KEY = "periodId"
//            const val ORG_UNIT_ID_KEY = "orgUnitId"
//            const val ATTRIBUTE_OPTION_COMBO_ID_KEY = "attributeOptionComboId"
//
//            fun createRoute(
//                datasetId: String,
//                periodId: String? = null,
//                orgUnitId: String? = null,
//                attributeOptionComboId: String? = null
//            ): String {
//                val params = listOfNotNull(
//                    periodId?.let { "periodId=$it" },
//                    orgUnitId?.let { "orgUnitId=$it" },
//                    attributeOptionComboId?.let { "attributeOptionComboId=$it" }
//                ).joinToString("&")
//                return "dataEntry/$datasetId" + if (params.isNotEmpty()) "?$params" else ""
//            }
//        }
//    }
//
//
//@Composable
//fun AppNavigation(
//    navController: NavHostController = rememberNavController(),
//    startDestination: String = LoginScreen.route // Changed default start destination
//) {
//    NavHost(
//        navController = navController,
//        startDestination = startDestination
//    ) {
//        // Added login screen composable
//        composable(LoginScreen.route) {
//            LoginScreen(
//                onLoginSuccess = {
//                    // Clear back stack and navigate to datasets
//                    navController.navigate(DatasetScreen.route) {
//                        popUpTo(LoginScreen.route) {
//                            inclusive = true
//                        }
//                    }
//                }
//            )
//        }
//
//        composable(DatasetScreen.route) {
//            DatasetScreen(
//                onDatasetClick = { datasetId ->
//                    navController.navigate(DatasetInstanceListScreen.createRoute(datasetId.toString()))
//                }
//            )
//        }
//
//        composable(
//            route = DatasetInstanceListScreen("").route,
//            arguments = listOf(navArgument(DatasetInstanceListScreen.DATASET_ID_KEY) {
//                type = NavType.StringType
//            })
//        ) { backStackEntry ->
//            val datasetId = backStackEntry.arguments?.getString(DatasetInstanceListScreen.DATASET_ID_KEY)
//            datasetId?.let { nonNullDatasetId ->
//
//                DatasetInstanceListScreen(
//                    onNewEntryClick = { navController.navigate(FormScreen.route) },
//                    onEntryClick = {
//                        Log.i("AppNavigation", "Navigating to DataEntryScreen with datasetId: $nonNullDatasetId")
//                        navController.navigate(DataEntryScreen.createRoute(nonNullDatasetId, periodId = "", orgUnitId = "", attributeOptionComboId = "", ))
//                    },
//                    onNavigateBack = { navController.popBackStack() },
//                    datasetId = nonNullDatasetId
//                )
//            }
//        }
//
//        composable(
//            route = DataEntryScreen(datasetId = "").route,
//            arguments = listOf(
//                navArgument(DataEntryScreen.DATASET_ID_KEY) { type = NavType.StringType },
//                navArgument(DataEntryScreen.PERIOD_ID_KEY) {
//                    type = NavType.StringType
//                    nullable = true
//                },
//                navArgument(DataEntryScreen.ORG_UNIT_ID_KEY) {
//                    type = NavType.StringType
//                    nullable = true
//                },
//                navArgument(DataEntryScreen.ATTRIBUTE_OPTION_COMBO_ID_KEY) {
//                    type = NavType.StringType
//                    nullable = true
//                }
//            )
//        ) { backStackEntry ->
//            val datasetId = backStackEntry.arguments?.getString(DataEntryScreen.DATASET_ID_KEY)
//            val periodId = backStackEntry.arguments?.getString(DataEntryScreen.PERIOD_ID_KEY)
//            val orgUnitId = backStackEntry.arguments?.getString(DataEntryScreen.ORG_UNIT_ID_KEY)
//            val attributeOptionComboId = backStackEntry.arguments?.getString(DataEntryScreen.ATTRIBUTE_OPTION_COMBO_ID_KEY)
//
//            if (datasetId != null) {
//                DataEntryScreen(
//                    datasetId = datasetId,
//                    periodId = periodId,
//                    orgUnitId = orgUnitId,
//                    attributeOptionComboId = attributeOptionComboId
//                )
//            }
//        }
//    }
//    }
//}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = LoginScreen.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ... existing composables ...

        composable(
            route = DatasetInstanceListScreen("").route,
            arguments = listOf(navArgument(DatasetInstanceListScreen.DATASET_ID_KEY) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val datasetId = backStackEntry.arguments?.getString(DatasetInstanceListScreen.DATASET_ID_KEY)
            datasetId?.let { nonNullDatasetId ->
                DatasetInstanceListScreen(
                    onNewEntryClick = {
                        navController.navigate(
                            DataEntryScreen.createRoute(
                                datasetId = nonNullDatasetId
                            )
                        )
                    },
                    onEntryClick = { periodId, orgUnitId, attributeOptionComboId ->
                        navController.navigate(
                            DataEntryScreen.createRoute(
                                datasetId = nonNullDatasetId,
                                periodId = periodId,
                                orgUnitId = orgUnitId,
                                attributeOptionComboId = attributeOptionComboId
                            )
                        )
                    },
                    onNavigateBack = { navController.popBackStack() },
                    datasetId = nonNullDatasetId
                )
            }
        }
    }
}