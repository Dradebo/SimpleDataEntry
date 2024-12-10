package com.xavim.testsimpleact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.xavim.testsimpleact.screens.FormScreen
import com.xavim.testsimpleact.ui.theme.TestsimpleactTheme
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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DHIS2Theme {
                Scaffold (
                    topBar = {
                        TopBar(
                            title = { Title(text = "Simple Form activity", textColor = TextColor.OnPrimary) },
                            type = TopBarType.CENTERED,
                            navigationIcon = {
                                IconButton(IconButtonStyle.STANDARD, icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Menu,
                                        contentDescription = "Menu",
                                        tint = TextColor.OnPrimary,
                                        )
                                }) { }
                            },
                            actions = {  },
                            colors = TopAppBarColors(containerColor = SurfaceColor.Primary, titleContentColor = TextColor.OnSurface, navigationIconContentColor =  TextColor.OnSurface,
                                actionIconContentColor = TextColor.OnSurface,
                                scrolledContainerColor = SurfaceColor.Container,),

                        )
                    },
                    bottomBar = {
                        NavigationBar(items = listOf(NavigationBarItem(id = 0, icon = Icons.Outlined.Edit, label = "Edit"), NavigationBarItem(id = 1, icon = Icons.Outlined.Mood, label = "diff")),
                            selectedItemIndex = 0,
                            onItemClick = { itemIndex->
                                when(itemIndex) {
                                }
                            })

                    }
                ){ innerPadding ->

                    Column (modifier = Modifier.padding(innerPadding)) {
                        Column (Modifier.padding(horizontal = Spacing.Spacing16)) {
                            FormScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestsimpleactTheme {
        Greeting("Android")
    }
}