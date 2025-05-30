//author: Roman Epishkin
package intagrationTests

import algos.findCyclesForDirected
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.DirectedGraph
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import saving.loadMainScreenViewModelFromJson
import saving.saveToJson
import viewmodel.ForceDirectedLayout
import viewmodel.MainScreenViewModelForDirectedGraph
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/*
* Test Documentation
* Purpose:
* to verify the correct interaction between various components of project,
* such as running algorithms, interacting with json format, and handling logic with model - view model.
* Case description:
* 1 step: create model and viewmodel for directed graph
* 2 step: running find cycles and Ford-Bellman algos and check that the result is correct
* 3 step: save result to json file and check that the file is created and not empty
* 4 step: load viewmodel from saved file and check that result is not null
* 5 step: checking the loaded viewmodel for compliance with original
* */

class JsonSaveAndLoadTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `test full cycle save and load directed graph`() {
        //1 step
        val originalGraph = DirectedGraph().apply {
            addVertex(1)
            addVertex(2)
            addVertex(3)
            addVertex(4)
            addVertex(5)
            addEdge(1, 2, 10)
            addEdge(2, 3, 20)
            addEdge(3, 4, 30)
            addEdge(3, 5, 40)
            addEdge(4, 5, 9)
            addEdge(5, 3, 40)
        }

        val originalViewModel = MainScreenViewModelForDirectedGraph(
            graph = originalGraph,
            representationStrategy = ForceDirectedLayout()
        ).apply {
            graphViewModel.setVertexColor(1, Color.Red)
            graphViewModel.setVertexSize(1, 20.dp)
            graphViewModel.setEdgeColor(1, 2, Color.Blue)
            graphViewModel.setEdgeWidth(1, 2, 3f)
        }

        //2 step
        originalViewModel.findCycles(2)
        originalViewModel.findPathFordBellman(2, 5)
        originalViewModel.graphViewModel.vertices.forEach { vertex ->
            if (vertex.value == 2L || vertex.value == 5L) {
                assertEquals(Color(0xFF1E88E5), vertex.color)
            }
        }
        originalViewModel.graphViewModel.edges.forEach { edge ->
            if (edge.weight == "20" || edge.weight == "30" || edge.weight == "9") {
                assertEquals(Color(0xFF1E88E5), edge.color)
            } else if (edge.weight == "40" && edge.firstVertex.value == 5L) {
                assertEquals(Color(0xFF800020), edge.color)
            }
        }

        // 3 step
        val testFile = File(tempDir, "test_graph.json")
        saveToJson(originalViewModel, testFile.absolutePath)

        assertTrue(testFile.exists())
        assertTrue(testFile.readText().isNotBlank())

        // 4 step
        val loadedViewModel = loadMainScreenViewModelFromJson(
            testFile.absolutePath,
            ForceDirectedLayout()
        ) as? MainScreenViewModelForDirectedGraph

        assertNotNull(loadedViewModel)

        // 5 step
        val loadedGraph = loadedViewModel.graphViewModel
        assertEquals(5, loadedGraph.vertices.size )
        assertEquals(6, loadedGraph.edges.size )
        loadedGraph.vertices.forEach { loadVertex ->
            when(loadVertex.value){
                1L -> {
                    assertEquals(Color.Red, loadVertex.color)
                    assertEquals(20.dp, loadVertex.radius)
                }
                2L -> assertEquals(Color(0xFF1E88E5), loadVertex.color)
                3L -> assertEquals(Color.Gray, loadVertex.color)
                4L -> assertEquals(Color.Gray, loadVertex.color)
                5L -> assertEquals(Color(0xFF1E88E5), loadVertex.color)
                else -> fail("Vertex ${loadVertex.value} shouldn't be in loaded graph")
            }
        }
        loadedGraph.edges.forEach { loadEdge ->
            when(loadEdge.weight) {
                "10" -> {
                    assertEquals(Color.Blue, loadEdge.color)
                    assertEquals(3f, loadEdge.width)
                }
                "20" -> assertEquals(Color(0xFF1E88E5), loadEdge.color)
                "30" -> assertEquals(Color(0xFF1E88E5), loadEdge.color)
                "9" -> assertEquals(Color(0xFF1E88E5), loadEdge.color)
                "40" -> {
                    if (loadEdge.firstVertex.value == 5L) {
                        assertEquals(Color(0xFF800020), loadEdge.color)
                    }
                }
                else -> fail("Edge with weight ${loadEdge.weight} shouldn't be in loaded graph")
            }
        }
    }
}