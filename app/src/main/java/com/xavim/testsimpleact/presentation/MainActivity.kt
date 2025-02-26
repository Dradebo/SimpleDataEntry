package com.xavim.testsimpleact.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.xavim.testsimpleact.navigation.Screen.DatasetScreen.AppNavigation
import com.xavim.testsimpleact.presentation.core.AppScaffold
import dagger.hilt.android.AndroidEntryPoint
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarType
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DHIS2Theme {
                val navController = rememberNavController()

                AppScaffold(
                    topBarTitle = "Simple Data Entry",
                    onMenuClick = {
                        // If there's a side menu or navigation drawer, handle it here
                    },
                    bottomBarItems = listOf(
                        NavigationBarItem(
                            id = 0,
                            icon = Icons.Outlined.Edit,
                            selectedIcon = Icons.Filled.Edit,
                            label = "Edit"
                        ),
                        NavigationBarItem(
                            id = 1,
                            icon = Icons.Outlined.Mood,
                            selectedIcon = Icons.Filled.Mood,
                            label = "Refresh"
                        )
                    ),
                    selectedBottomBarIndex = 1,
                    onBottomBarItemClick = { itemId ->
                        when (itemId) {
                            0 -> {
                                // Example: open some editing flow
                            }
                            1 -> {
                                // Example: refresh data
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Column(Modifier.padding(horizontal = Spacing.Spacing16)) {
                            AppNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    }
}