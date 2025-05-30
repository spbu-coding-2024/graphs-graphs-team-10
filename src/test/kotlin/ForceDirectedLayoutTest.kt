//author: Roman Epishkin

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import model.DirectedGraph
import saving.toPx
import viewmodel.EdgeViewModel
import viewmodel.ForceDirectedLayout
import viewmodel.MainScreenViewModelForDirectedGraph
import viewmodel.VertexViewModel
import kotlin.math.sqrt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForceDirectedLayoutTest {

    private lateinit var layout: ForceDirectedLayout
    private lateinit var vertices: Collection<VertexViewModel>
    private lateinit var edges: Collection<EdgeViewModel>

    @BeforeTest
    fun setup() {
        layout = ForceDirectedLayout()

        val graph = DirectedGraph().apply {
            addVertex(1)
            addVertex(2)
            addVertex(3)
            addEdge(1, 2, 0)
            addEdge(2, 3, 0)
        }

        val viewModel = MainScreenViewModelForDirectedGraph(graph, layout)
        vertices = viewModel.graphViewModel.vertices.toSet()
        edges = viewModel.graphViewModel.edges.toList()
    }

    @Test
    fun `vertices should respect bounds`() {
        val width = 800.0
        val height = 600.0
        val verticesList = vertices.toList()
        val edgesSet = edges.toSet()

        repeat(5) {
            layout.place(width, height, verticesList, edgesSet)

            verticesList.forEach { vertex ->
                assertTrue(vertex.x.value >= vertex.radius.value,
                    "Vertex x (${vertex.x}) should be >= radius (${vertex.radius})")
                assertTrue(vertex.x.value <= width - vertex.radius.value,
                    "Vertex x (${vertex.x}) should be <= ${width - vertex.radius.value}")
                assertTrue(vertex.y.value >= vertex.radius.value,
                    "Vertex y (${vertex.y}) should be >= radius (${vertex.radius})")
                assertTrue(vertex.y.value <= height - vertex.radius.value,
                    "Vertex y (${vertex.y}) should be <= ${height - vertex.radius.value}")
            }
        }
    }

    @Test
    fun `reset should work with different collection types`() {
        vertices.forEach {
            it.color = Color.Red
            it.radius = 30.dp
        }
        edges.forEach {
            it.color = Color.Blue
            it.width = 2f
        }

        layout.resetVertices(vertices.toSet())
        layout.resetEdges(edges.toList())

        vertices.forEach {
            assertEquals(Color.Gray, it.color,
                "Vertex color should be reset to Gray")
            assertEquals(layout.defaultVertexRadius, it.radius,
                "Vertex radius should be reset to default")
        }

        edges.forEach {
            assertEquals(Color.Black, it.color,
                "Edge color should be reset to Black")
            assertEquals(layout.defaultEdgesWidth, it.width,
                "Edge width should be reset to default")
        }
    }

    @Test
    fun `should handle empty collections`() {
        layout.place(800.0, 600.0, emptyList(), emptySet())
        layout.resetVertices(emptySet())
        layout.resetEdges(emptyList())
    }
}
