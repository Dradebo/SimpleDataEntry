package com.xavim.testsimpleact.presentation.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.component.IconButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.Title
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarType
import org.hisp.dhis.mobile.ui.designsystem.component.menu.MenuLeadingElement
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import java.lang.reflect.Modifier
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    topBarTitle: String = "Simple Data Entry",
    onMenuClick: () -> Unit = {},
    bottomBarItems: List<NavigationBarItem<Int>>,
    selectedBottomBarIndex: Int,
    onBottomBarItemClick: (Int) -> Unit,
    content: @Composable (padding: PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(
                title = { Title(text = topBarTitle, textColor = TextColor.OnPrimary) },
                type = TopBarType.CENTERED,
                navigationIcon = {
                    IconButton(IconButtonStyle.STANDARD, icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "Menu",
                            tint = TextColor.OnPrimary
                        )
                    }) {
                        onMenuClick()
                    }
                },
                actions = { /* If you have extra actions, place them here */ },
                colors = TopAppBarColors(
                    containerColor = SurfaceColor.Primary,
                    titleContentColor = TextColor.OnSurface,
                    navigationIconContentColor = TextColor.OnSurface,
                    actionIconContentColor = TextColor.OnSurface,
                    scrolledContainerColor = SurfaceColor.Primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                items = bottomBarItems,
                selectedItemIndex = selectedBottomBarIndex,
                onItemClick = onBottomBarItemClick
            )
        },
        content = content
    )
}