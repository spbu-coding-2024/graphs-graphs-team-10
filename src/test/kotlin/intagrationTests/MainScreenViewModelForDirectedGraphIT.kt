// author: Arseniy Romanov
package intagrationTests

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import model.DirectedGraph
import org.junit.jupiter.api.Test
import saving.GraphRepository
import viewmodel.EdgeViewModel
import viewmodel.ForceDirectedLayout
import viewmodel.MainScreenViewModelForDirectedGraph
import viewmodel.VertexViewModel
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MainScreenViewModelForDirectedGraphIT {
    private lateinit var graph: DirectedGraph
    private lateinit var viewModel: MainScreenViewModelForDirectedGraph
    private var representationStrategy = ForceDirectedLayout()

    fun assertDefaultVertices(vertices: Collection<VertexViewModel>)  {
        vertices.forEach {
            assertEquals(representationStrategy.defaultVertexRadius.value, it.radius.value)
            assertEquals(Color.Gray, it.color)
        }
    }

    fun assertDefaultEdges(edges: Collection<EdgeViewModel>)  {
        edges.forEach {
            assertEquals(representationStrategy.defaultEdgesWidth, it.width)
            assertEquals(Color.Black, it.color)
        }
    }

    // ====================================
    // init
    // ====================================
    @Test
    fun `init main screen viewModel for directed graph`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(3, 1, 0)
                addEdge(4, 6, 0)
                addEdge(4, 7, 0)
                addEdge(5, 1, 0)
                addEdge(1, 8, 0)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        val vertices = viewModel.graphViewModel.vertices
        val edges = viewModel.graphViewModel.edges

        assertEquals(8, vertices.size)
        assertEquals(6, edges.size)
        assertDefaultVertices(vertices)
        assertDefaultEdges(edges)
    }

    // ====================================
    // resetting the properties of a vertex
    // ====================================
    @Test
    fun `default vertices test`() {
        graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addVertex(5)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        val vertices = viewModel.graphViewModel.vertices
        vertices.forEach {
            it.color = Color.Red
            it.radius = 70.dp
        }
        viewModel.defaultVertices()
        assertDefaultVertices(vertices)
    }

    @Test
    fun `default edges test`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(3, 1, 0)
                addEdge(4, 6, 0)
                addEdge(4, 7, 0)
                addEdge(5, 1, 0)
                addEdge(1, 8, 0)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        val edges = viewModel.graphViewModel.edges
        edges.forEach {
            it.color = Color.Red
            it.width = 5f
        }
        viewModel.defaultEdges()
        assertDefaultEdges(edges)
    }

    @Test
    fun `make new placement`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(3, 1, 0)
                addEdge(4, 6, 0)
                addEdge(4, 7, 0)
                addEdge(5, 1, 0)
                addEdge(1, 8, 0)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        val vertices = viewModel.graphViewModel.vertices
        vertices.forEach {
            it.x = 0.dp
            it.y = 0.dp
        }
        viewModel.makeNewPlacement()
        vertices.forEach {
            assertNotEquals(0f, it.x.value)
            assertNotEquals(0f, it.y.value)
        }
    }

    // ====================================
    // checkForNegativeWeights
    // ====================================
    @Test
    fun `check for negative weights test`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(2, 3, 4)
                addEdge(3, 1, -1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        assertTrue(viewModel.checkForNegativeWeights())
    }

    @Test
    fun `check for negative weights without negative weights`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(2, 3, 4)
                addEdge(3, 1, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        assertFalse(viewModel.checkForNegativeWeights())
    }

    // ====================================
    // Dijkstra
    // ====================================
    @Test
    fun `Dijkstra test`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(1, 3, 3)
                addEdge(3, 4, 4)
                addEdge(2, 4, 2)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathDijkstra(1, 4)
        val newColor = Color(0xFF1E88E5)

        viewModel.graphViewModel.edges.forEach {
            when (it.weight) {
                "1", "2" -> assertEquals(it.color, newColor)
                "3", "4" -> assertEquals(it.color, Color.Black)
            }
        }

        viewModel.graphViewModel.vertices.forEach {
            when (it.value) {
                1L, 4L -> assertEquals(newColor, it.color)
                2L, 3L -> assertEquals(Color.Gray, it.color)
            }
        }
    }

    @Test
    fun `Dijkstra find a non-existent path`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(3, 4, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathDijkstra(1, 4)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    @Test
    fun `Dijkstra find path in graph with negative weights`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(2, 3, 1)
                addEdge(3, 4, -1)
                addEdge(4, 1, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathDijkstra(1, 4)
        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    @Test
    fun `Dijkstra find path between non-existent vertices`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(3, 4, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathDijkstra(5, 6)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    // ====================================
    // strongly connected components
    // ====================================
    @Test
    fun `find strongly connected components`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 0)
                addEdge(2, 3, 0)
                addEdge(3, 4, 0)
                addEdge(4, 1, 0)

                addEdge(5, 6, 0)
                addEdge(6, 7, 0)
                addEdge(7, 8, 0)
                addEdge(8, 5, 0)

                addEdge(3, 5, 0)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findStronglyConnectedComponents()

        val vertices = viewModel.graphViewModel.vertices

        var firstColor: Color? = null
        var secondColor: Color? = null

        for (vertex in vertices) {
            when (vertex.value) {
                1L -> firstColor = vertex.color
                5L -> secondColor = vertex.color
            }
        }

        for (vertex in vertices) {
            if (vertex.value in 1..4) {
                assertEquals(firstColor, vertex.color)
            } else {
                assertEquals(secondColor, vertex.color)
            }
        }
    }

    @Test
    fun `find strongly connected components in empty graph`() {
        graph = DirectedGraph()
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findStronglyConnectedComponents()
    }

    // ====================================
    // leader rank
    // ====================================
    @Test
    fun `highlight key vertices test`() {
        graph =
            DirectedGraph().apply {
                addEdge(2, 1, 0)
                addEdge(3, 1, 0)
                addEdge(4, 1, 0)

                addEdge(6, 5, 0)
                addEdge(7, 5, 0)

                addEdge(9, 8, 0)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.highlightKeyVertices()
        val vertices = viewModel.graphViewModel.vertices.toList().sortedBy { it.value }
        val defaultRadius = representationStrategy.defaultVertexRadius

        for (v in vertices) {
            when (v.value) {
                1L -> assertEquals(v.radius, defaultRadius * 4)
                5L -> assertEquals(v.radius, defaultRadius * 3)
                8L -> assertEquals(v.radius, defaultRadius * 2)
                else -> assertEquals(v.radius, defaultRadius)
            }
        }
    }

    @Test
    fun `highlight key vertices in empty graph`() {
        graph = DirectedGraph()
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.highlightKeyVertices()
    }

    @Test
    fun `highlight key vertices in graph with one vertex`() {
        graph = DirectedGraph().apply { addVertex(1) }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.highlightKeyVertices()

        assertEquals(
            viewModel.graphViewModel.defaultVertexRadius,
            viewModel.graphViewModel.vertices.first().radius,
        )
    }

    @Test
    fun `highlight key vertices in graph with two vertices`() {
        graph = DirectedGraph().apply { addEdge(1, 2, 0) }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.highlightKeyVertices()
        val vertices = viewModel.graphViewModel.vertices.toList().sortedBy { it.value }

        val firstVertexRadius = vertices[0].radius
        val secondVertexRadius = vertices[1].radius

        assertEquals(
            viewModel.graphViewModel.defaultVertexRadius * 4,
            secondVertexRadius,
        )
        assertEquals(
            viewModel.graphViewModel.defaultVertexRadius,
            firstVertexRadius,
        )
    }

    @Test
    fun `highlight key vertices in graph without edges`() {
        graph =
            DirectedGraph().apply {
                addVertex(1)
                addVertex(2)
                addVertex(3)
                addVertex(4)
                addVertex(5)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.highlightKeyVertices()
        val vertices = viewModel.graphViewModel.vertices.toList()
        for (v in vertices) {
            assertEquals(viewModel.graphViewModel.defaultVertexRadius, v.radius)
        }
    }

    // ====================================
    // text display flags
    // ====================================
    @Test
    fun `vertex values and edge weights are hidden`() {
        graph = DirectedGraph().apply { addEdge(2, 1, 0) }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)

        assertFalse(viewModel.showVerticesElements)
        assertFalse(viewModel.showEdgesWeights)
    }

    @Test
    fun `show vertex values and edge weights`() {
        graph = DirectedGraph().apply { addEdge(2, 1, 0) }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.showVerticesElements = true
        viewModel.showEdgesWeights = true

        assertTrue(viewModel.showVerticesElements)
        assertTrue(viewModel.showEdgesWeights)
    }

    // ====================================
    // Ford Bellman
    // ====================================
    @Test
    fun `Ford Bellman test`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(1, 3, 3)
                addEdge(3, 4, 4)
                addEdge(2, 4, 2)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathFordBellman(1, 4)
        val newColor = Color(0xFF1E88E5)

        viewModel.graphViewModel.edges.forEach {
            when (it.weight) {
                "1", "2" -> assertEquals(it.color, newColor)
                "3", "4" -> assertEquals(it.color, Color.Black)
            }
        }
        viewModel.graphViewModel.vertices.forEach {
            when (it.value) {
                1L, 4L -> assertEquals(newColor, it.color)
                2L, 3L -> assertEquals(Color.Gray, it.color)
            }
        }
    }

    @Test
    fun `Ford Bellman with negative cycle`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 3)
                addEdge(2, 1, -4)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathFordBellman(1, 2)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    @Test
    fun `Ford Bellman with unreachable negative cycle`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)

                addEdge(3, 4, -5)
                addEdge(4, 3, 4)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathFordBellman(1, 2)
        val newColor = Color(0xFF1E88E5)

        viewModel.graphViewModel.edges.forEach {
            when (it.firstVertex.value) {
                1L -> assertEquals(newColor, it.color)
                3L, 4L -> assertEquals(Color.Black, it.color)
            }
        }
        viewModel.graphViewModel.vertices.forEach {
            when (it.value) {
                1L, 2L -> assertEquals(newColor, it.color)
                3L, 4L -> assertEquals(Color.Gray, it.color)
            }
        }
    }

    @Test
    fun `Ford Bellman find a non-existent path`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(3, 4, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathFordBellman(1, 4)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    @Test
    fun `Ford Bellman find path between non-existent vertices`() {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(3, 4, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findPathFordBellman(5, 6)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
        viewModel.graphViewModel.vertices.forEach {
            assertEquals(Color.Gray, it.color)
        }
    }

    // ====================================
    // find cycles
    // ====================================
    @Test
    fun `find two cycles`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(2, 3, 2)
                addEdge(3, 1, 3)

                addEdge(2, 5, 6)
                addEdge(5, 2, 5)

                addEdge(2, 4, 4)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findCycles(2)
        val newColor = Color(0xFFFFD700)

        viewModel.graphViewModel.edges.forEach {
            when (it.weight) {
                "1", "2", "3", "5", "6" -> assertEquals(newColor, it.color)
                "4" -> assertEquals(Color.Black, it.color)
            }
        }
    }

    @Test
    fun `find unreachable cycle`()  {
        graph =
            DirectedGraph().apply {
                addEdge(2, 1, 1)
                addEdge(2, 3, 1)
                addEdge(3, 2, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findCycles(1)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
    }

    @Test
    fun `find cycle with non-existent vertex`()  {
        graph =
            DirectedGraph().apply {
                addEdge(2, 1, 1)
                addEdge(2, 3, 1)
                addEdge(3, 2, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findCycles(5)

        viewModel.graphViewModel.edges.forEach {
            assertEquals(Color.Black, it.color)
        }
    }

    @Test
    fun `vertex with loop`()  {
        graph =
            DirectedGraph().apply {
                addEdge(1, 1, 1)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        viewModel.findCycles(1)
        val newColor = Color(0xFFFFD700)

        assertEquals(newColor, viewModel.graphViewModel.edges.first().color)
    }

    // ====================================
    // SQLite
    // ====================================
    @Test
    fun `save and load graph from db`()  {
        val repository =
            GraphRepository(DriverManager.getConnection("jdbc:sqlite::memory:"))
        val graph =
            DirectedGraph().apply {
                addEdge(1, 2, 1)
                addEdge(2, 3, 2)
                addEdge(3, 1, 3)

                addEdge(4, 5, 4)
                addEdge(5, 6, 5)
                addEdge(6, 7, 6)
                addEdge(7, 4, 7)
            }
        viewModel = MainScreenViewModelForDirectedGraph(graph, representationStrategy)
        for (vertex in viewModel.graphViewModel.vertices) {
            when (vertex.value) {
                in 1..3 -> {
                    vertex.color = Color.Red
                    vertex.radius = 5.dp
                    vertex.x = Dp(vertex.value.toFloat() * 10)
                    vertex.y = Dp(vertex.value.toFloat() * 10)
                }
                in 4..7 -> {
                    vertex.color = Color.Blue
                    vertex.radius = 10.dp
                    vertex.x = Dp(vertex.value.toFloat() * 30)
                    vertex.y = Dp(vertex.value.toFloat() * 30)
                }
            }
        }
        for (edge in viewModel.graphViewModel.edges) {
            when (edge.firstVertex.value) {
                in 1..3 -> {
                    edge.color = Color.Red
                    edge.width = 3f
                }
                in 4..7 -> {
                    edge.color = Color.Blue
                    edge.width = 5f
                }
            }
        }

        repository.addGraph(viewModel.graphViewModel, "sample", true)
        val loadedViewModel = repository.loadGraph("sample")
        for (vertex in loadedViewModel.graphViewModel.vertices) {
            when (vertex.value) {
                in 1..3 -> {
                    assertEquals(Color.Red, vertex.color)
                    assertEquals(5.dp, vertex.radius)
                    assertEquals(Dp(vertex.value.toFloat() * 10), vertex.x)
                    assertEquals(Dp(vertex.value.toFloat() * 10), vertex.y)
                }
                in 4..7 -> {
                    assertEquals(Color.Blue, vertex.color)
                    assertEquals(10.dp, vertex.radius)
                    assertEquals(Dp(vertex.value.toFloat() * 30), vertex.x)
                    assertEquals(Dp(vertex.value.toFloat() * 30), vertex.y)
                }
            }
        }
        for (edge in loadedViewModel.graphViewModel.edges) {
            when (edge.firstVertex.value) {
                in 1..3 -> {
                    assertEquals(Color.Red, edge.color)
                    assertEquals(3f, edge.width)
                }
                in 4..7 -> {
                    assertEquals(Color.Blue, edge.color)
                    assertEquals(5f, edge.width)
                }
            }
        }
        repository.close()
    }
}
