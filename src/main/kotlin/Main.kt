import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.Navigator
import view.MainScreenForDirected
import view.MainScreenForUndirected
import view.homeScreen
import viewmodel.MainScreenViewModel
import viewmodel.MainScreenViewModelForDirectedGraph
import viewmodel.MainScreenViewModelForUndirectedGraph
import javax.swing.UIManager

class FirstScreen() : Screen {
    override val key: ScreenKey = "FirstScreen"

    @Composable
    override fun Content() {
        homeScreen()
    }
}

data class GraphScreen(val mainScreenViewModel: MainScreenViewModel) : Screen {
    @Composable
    override fun Content() {
        if (mainScreenViewModel is MainScreenViewModelForDirectedGraph) {
            MainScreenForDirected(mainScreenViewModel)
        }
        if (mainScreenViewModel is MainScreenViewModelForUndirectedGraph) {
            MainScreenForUndirected(mainScreenViewModel)
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(FirstScreen())
    }
}

fun main() {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    application {
        Window(onCloseRequest = ::exitApplication) {
            App()
        }
    }
}
