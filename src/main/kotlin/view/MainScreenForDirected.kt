package view

import ZoomableBox
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import saving.GraphRepository
import saving.saveToJson
import saving.showFileSaveDialog
import viewmodel.MainScreenViewModelForDirectedGraph
import java.awt.Dimension
import java.sql.DriverManager
import java.sql.SQLException
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun MainScreenForDirected(viewModel: MainScreenViewModelForDirectedGraph) {
    var showAnalyzeMenu by remember { mutableStateOf(false) }
    var showSaveMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var findPathAlgorithm by remember { mutableStateOf("") }
    val navigator = LocalNavigator.currentOrThrow

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.graphViewModel.verticesToFindPath }
            .collect { pathList ->
                if (pathList.size == 2) {
                    if (findPathAlgorithm == "dijkstra") {
                        viewModel.findPathDijkstra(pathList[0], pathList[1])
                    } else if (findPathAlgorithm == "fordBellman") {
                        viewModel.findPathFordBellman(pathList[0], pathList[1])
                    }
                    viewModel.graphViewModel.clearVerticesToFindPath()
                    viewModel.graphViewModel.findPathState = false
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.graphViewModel.vertexToFindCycles }
            .collect { vertex ->
                if (vertex.size == 1) {
                    viewModel.findCycles(vertex[0])
                }
                viewModel.graphViewModel.clearVertexToFindCycles()
                viewModel.graphViewModel.findCyclesState = false
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ZoomableBox {
                directedGraphView(viewModel.graphViewModel)
            }
        }

        Box(
            modifier =
                Modifier
                    .padding(16.dp)
                    .zIndex(2f)
                    .width(350.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = Color.White,
                        ),
                    elevation =
                        ButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp,
                        ),
                ) {
                    Text("Back to start menu", fontSize = 16.sp)
                }

                if (showExitDialog) {
                    Dialog(
                        onDismissRequest = { showExitDialog = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                    ) {
                        Card(
                            modifier =
                                Modifier
                                    .width(600.dp)
                                    .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White,
                            elevation = 8.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                Text(
                                    "Exit confirmation",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )
                                Text(
                                    "Are you sure you want to leave? All unsaved changes will be lost.",
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(
                                        onClick = { showExitDialog = false },
                                        modifier = Modifier.padding(end = 8.dp),
                                    ) {
                                        Text("Cancel")
                                    }
                                    TextButton(
                                        onClick = { navigator.pop() },
                                        colors =
                                            ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colors.primary,
                                            ),
                                    ) {
                                        Text("Ok")
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = {
                            showAnalyzeMenu = !showAnalyzeMenu
                            showSaveMenu = false
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = Color.White,
                            ),
                        elevation =
                            ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                            ),
                    ) {
                        Text("Analyze", fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            showSaveMenu = !showSaveMenu
                            showAnalyzeMenu = false
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = Color.White,
                            ),
                        elevation =
                            ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp,
                            ),
                    ) {
                        Text("Save", fontSize = 16.sp)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showAnalyzeMenu,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier =
                Modifier
                    .zIndex(1f)
                    .width(370.dp)
                    .fillMaxHeight(),
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight(),
                color = Color(0xFFE0E0E0),
                elevation = 8.dp,
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(128.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.showVerticesElements,
                            onCheckedChange = { viewModel.showVerticesElements = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1976D2)),
                        )
                        Text(
                            "Show vertices elements",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = viewModel.showEdgesWeights,
                            onCheckedChange = { viewModel.showEdgesWeights = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1976D2)),
                        )
                        Text(
                            "Show edges weights",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            showSaveMenu = false
                            viewModel.makeNewPlacement()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF1976D2),
                                contentColor = Color.White,
                            ),
                    ) {
                        Text("New placement", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.defaultVertices()
                            showSaveMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF1976D2),
                                contentColor = Color.White,
                            ),
                    ) {
                        Text("Reset vertices", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.defaultEdges()
                            showSaveMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF1976D2),
                                contentColor = Color.White,
                            ),
                    ) {
                        Text("Reset edges", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = viewModel::findStronglyConnectedComponents,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF1976D2),
                                contentColor = Color.White,
                            ),
                    ) {
                        Text("Find strongly connected components", fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.graphViewModel.clearVertexToFindCycles()
                            viewModel.graphViewModel.findCyclesState = !viewModel.graphViewModel.findCyclesState
                            viewModel.graphViewModel.findPathState = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor =
                                    if (viewModel.graphViewModel.findCyclesState) {
                                        Color(0xFF1565C0)
                                    } else {
                                        Color(0xFF1976D2)
                                    },
                                contentColor = Color.White,
                            ),
                    ) {
                        Text(
                            text =
                                if (viewModel.graphViewModel.findCyclesState) {
                                    "Cancel Find Cycles"
                                } else {
                                    "Find Cycles"
                                },
                            fontSize = 18.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!viewModel.checkForNegativeWeights()) {
                        Button(
                            onClick = {
                                viewModel.graphViewModel.clearVerticesToFindPath()
                                findPathAlgorithm = "dijkstra"
                                viewModel.graphViewModel.findPathState = !viewModel.graphViewModel.findPathState
                                viewModel.graphViewModel.findCyclesState = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    backgroundColor =
                                        if (viewModel.graphViewModel.findPathState && findPathAlgorithm == "dijkstra") {
                                            Color(0xFF1565C0)
                                        } else {
                                            Color(0xFF1976D2)
                                        },
                                    contentColor = Color.White,
                                ),
                        ) {
                            Text(
                                text =
                                    if (viewModel.graphViewModel.findPathState && findPathAlgorithm == "dijkstra") {
                                        "Cancel Dijkstra"
                                    } else {
                                        "Find Path (Dijkstra)"
                                    },
                                fontSize = 18.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.graphViewModel.clearVerticesToFindPath()
                            findPathAlgorithm = "fordBellman"
                            viewModel.graphViewModel.findPathState = !viewModel.graphViewModel.findPathState
                            viewModel.graphViewModel.findCyclesState = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor =
                                    if (viewModel.graphViewModel.findPathState && findPathAlgorithm == "fordBellman") {
                                        Color(0xFF1565C0)
                                    } else {
                                        Color(0xFF1976D2)
                                    },
                                contentColor = Color.White,
                            ),
                    ) {
                        Text(
                            text =
                                if (viewModel.graphViewModel.findPathState && findPathAlgorithm == "fordBellman") {
                                    "Cancel Ford-Bellman"
                                } else {
                                    "Find Path (Ford-Bellman)"
                                },
                            fontSize = 18.sp,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.highlightKeyVertices()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF1976D2),
                                contentColor = Color.White,
                            ),
                    ) {
                        Text("Get leaders", fontSize = 18.sp)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showSaveMenu,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier =
                Modifier
                    .width(370.dp)
                    .fillMaxHeight(),
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight(),
                color = Color(0xFFEEEEEE),
                elevation = 8.dp,
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(120.dp))

                    Text(
                        text = "Save to:",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 24.dp),
                        color = Color.Black,
                    )

                    Button(
                        onClick = {
                            val chooser =
                                JFileChooser().apply {
                                    preferredSize = Dimension(800, 600)
                                    dialogTitle = "Select SQLite file"
                                    fileSelectionMode = JFileChooser.FILES_ONLY
                                    isMultiSelectionEnabled = false
                                }
                            if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                                return@Button
                            }

                            val file = chooser.selectedFile
                            val url = "jdbc:sqlite:${file.absolutePath}"

                            try {
                                DriverManager.getConnection(url).use { conn ->
                                    conn.createStatement().use { stmt ->
                                        stmt.executeQuery(
                                            "SELECT name FROM sqlite_master WHERE type='table' LIMIT 1",
                                        )
                                    }
                                }
                            } catch (e: SQLException) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "The selected file is not a valid SQLite database.",
                                    "Invalid Database",
                                    JOptionPane.WARNING_MESSAGE,
                                )
                                return@Button
                            }

                            val graphName =
                                JOptionPane.showInputDialog(
                                    null,
                                    "Enter a name for the graph:",
                                    "Graph Name",
                                    JOptionPane.QUESTION_MESSAGE,
                                )?.trim().takeIf { !it.isNullOrEmpty() }

                            if (graphName == null) {
                                JOptionPane.showMessageDialog(
                                    null,
                                    "Graph name must not be empty.",
                                    "Invalid Name",
                                    JOptionPane.WARNING_MESSAGE,
                                )
                                return@Button
                            }

                            val connection = DriverManager.getConnection(url)
                            val repository = GraphRepository(connection)

                            if (repository.graphExists(graphName)) {
                                val choice =
                                    JOptionPane.showConfirmDialog(
                                        null,
                                        "A graph named \"$graphName\" already exists.\nDo you want to overwrite it?",
                                        "Confirm Overwrite",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                    )
                                if (choice == JOptionPane.YES_OPTION) {
                                    repository.upsertGraph(viewModel.graphViewModel, graphName, true)
                                } else {
                                    repository.close()
                                    return@Button
                                }
                            } else {
                                repository.addGraph(
                                    viewModel.graphViewModel,
                                    graphName,
                                    true,
                                )
                                repository.close()
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF0181FF),
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(10.dp),
                        elevation =
                            ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp,
                                hoveredElevation = 6.dp,
                            ),
                        border = BorderStroke(1.5.dp, Color(0xFF0066CC)),
                    ) {
                        Text(
                            "SQLite",
                            style =
                                MaterialTheme.typography.button.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }

                    Button(
                        onClick = {},
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF0181FF),
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(10.dp),
                        elevation =
                            ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp,
                                hoveredElevation = 6.dp,
                            ),
                        border = BorderStroke(1.5.dp, Color(0xFF0066CC)),
                    ) {
                        Text(
                            "Neo4j",
                            style =
                                MaterialTheme.typography.button.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }

                    Button(
                        onClick = {
                            val filePath: String =
                                showFileSaveDialog(
                                    title = "Save file",
                                    initialDirectory = System.getProperty("user.home"),
                                    defaultFileName = "graph.json",
                                    fileFilter = FileNameExtensionFilter("JSON files", "json"),
                                ) ?: "Canceled"
                            saveToJson(
                                viewModel,
                                filePath,
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF0181FF),
                                contentColor = Color.White,
                            ),
                        shape = RoundedCornerShape(10.dp),
                        elevation =
                            ButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp,
                                hoveredElevation = 6.dp,
                            ),
                        border = BorderStroke(1.5.dp, Color(0xFF0066CC)),
                    ) {
                        Text(
                            "Json",
                            style =
                                MaterialTheme.typography.button.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                ),
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
