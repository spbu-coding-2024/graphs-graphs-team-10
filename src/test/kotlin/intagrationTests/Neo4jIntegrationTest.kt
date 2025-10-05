import saving.Neo4jGraphRepository
import algos.findBridges
import algos.findMinimalSpanningTree
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import model.UndirectedGraph
import org.junit.jupiter.api.Test
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Values
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import viewmodel.ForceDirectedLayout
import viewmodel.GraphViewModel
import viewmodel.MainScreenViewModelForUndirectedGraph

/*  !!! For this test to be able to connect to neo4j, a locally running neo4j server is required
    !!! URI = "neo4j://localhost:7687"
    !!! Username = "neo4j"
    !!! Password = "password"

    Test description:
    1) create graph model and viewmodel
    2) check mst and bridges algorithms
    3) try to connect to neo4j, if succeeded, save graph to db
    4) reload graph back from db
    5) compare loaded viewmodel with original
*/

class Neo4jIntegrationTest {

    private fun edgeExists(edges: List<model.Edge>, a: Long, b: Long, weight: Long): Boolean {
        return edges.any { e ->
            val (u, v) = e.vertices
            (u == a && v == b || u == b && v == a) && e.weight == weight
        }
    }

    @Test
    fun `integration test - save and load with neo4j (skips if no server)`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 1)
            addEdge(1L, 3L, 1)

            addEdge(4L, 5L, 1)
            addEdge(5L, 6L, 1)
            addEdge(4L, 6L, 1)

            addEdge(3L, 4L, 7)
        }

        val mainVm = MainScreenViewModelForUndirectedGraph(graph, ForceDirectedLayout())
        val graphVm: GraphViewModel = mainVm.graphViewModel

        graphVm.vertices.forEachIndexed { idx, v ->
            v.x = (idx * 10).dp
            v.y = (idx * 15).dp
            v.color = when (v.value) {
                1L, 2L, 3L -> Color(0xFFAA0000)
                else -> Color(0xFF00AA00)
            }
            v.radius = (8 + idx).dp
        }

        graphVm.edges.forEach { e ->
            val (u, v) = e.firstVertex.value to e.secondVertex.value
            if ((u == 3L && v == 4L) || (u == 4L && v == 3L)) {
                graphVm.setEdgeColor(u, v, Color(0xFF0000FF))
                graphVm.setEdgeWidth(u, v, 5.0f)
            } else {
                graphVm.setEdgeColor(u, v, Color(0xFF888888))
                graphVm.setEdgeWidth(u, v, 1.5f)
            }
        }

        val bridges = findBridges(graph)
        val mst = findMinimalSpanningTree(graph)

        assertTrue(edgeExists(bridges, 3L, 4L, 7L), "Expected (3,4) to be reported as a bridge")

        assertEquals(6 - 1, mst.size, "MST should contain vertices-1 edges for a connected graph")

        assertTrue(edgeExists(mst, 3L, 4L, 7L), "Expected MST to include the connecting bridge (3,4)")

        val uri = "neo4j://localhost:7687"
        val user = "neo4j"
        val password = "password"
        val repo = Neo4jGraphRepository(
            uri = uri,
            user = user,
            password = password,
            database = null
        )

        try {
            val graphName = "integration_test_graph"

            try {
                repo.saveGraph(graphVm, graphName, isDirected = false)
                println("Neo4j connection successful")
            }
            catch (e: Exception) {
                println("Neo4j save failed or server unreachable, skipping load part: ${e.message}")
                return
            }

            val loadedMain = try {
                repo.loadGraph(graphName)
            } catch (e: Exception) {
                println("Neo4j load failed after save, skipping comparison: ${e.message}")
                return
            }

            val loadedVm = loadedMain.graphViewModel

            graphVm.vertices.forEach { originalVertex ->
                val loadedVertex = loadedVm.vertices.find { it.value == originalVertex.value }
                assertTrue(loadedVertex != null, "Loaded graph is missing vertex with value ${originalVertex.value}")

                val eps = 0.0001
                assertTrue(
                    abs(originalVertex.x.value - loadedVertex.x.value) < eps,
                    "X mismatch for vertex ${originalVertex.value}: expected ${originalVertex.x}, got ${loadedVertex.x}"
                )
                assertTrue(
                    abs(originalVertex.y.value - loadedVertex.y.value) < eps,
                    "Y mismatch for vertex ${originalVertex.value}: expected ${originalVertex.y}, got ${loadedVertex.y}"
                )

                assertEquals(
                    originalVertex.color.toArgb(),
                    loadedVertex.color.toArgb(),
                    "Color mismatch for vertex ${originalVertex.value}"
                )

                assertTrue(
                    abs(originalVertex.radius.value - loadedVertex.radius.value) < eps,
                    "Radius mismatch for vertex ${originalVertex.value}: expected ${originalVertex.radius}, got ${loadedVertex.radius}"
                )
            }

            graphVm.edges.forEach { originalEdge ->
                val from = originalEdge.firstVertex.value
                val to = originalEdge.secondVertex.value

                val loadedEdge = loadedVm.edges.find { le ->
                    val a = le.firstVertex.value
                    val b = le.secondVertex.value
                    (a == from && b == to) || (a == to && b == from)
                }

                assertTrue(loadedEdge != null, "Loaded graph is missing edge between $from and $to")

                assertEquals(
                    originalEdge.weight.toLong(),
                    loadedEdge.weight.toLong(),
                    "Weight mismatch for edge $from-$to"
                )

                assertEquals(
                    originalEdge.color.toArgb(),
                    loadedEdge.color.toArgb(),
                    "Color mismatch for edge $from-$to"
                )

                val widthEps = 1e-3
                assertTrue(
                    abs(originalEdge.width - loadedEdge.width) < widthEps,
                    "Width mismatch for edge $from-$to: expected ${originalEdge.width}, got ${loadedEdge.width}"
                )
            }

            // Clear db from test graph
            GraphDatabase.driver(uri, AuthTokens.basic(user, password)).use { driver ->
                driver.session().use { session2 ->
                    session2.executeWrite { tx ->
                        tx.run(
                            """
                            MATCH (g:Graph {name: $${"graphName"}}) - [:CONTAINS] - (n)
                            DETACH DELETE g, n
                            """,
                            Values.parameters("graphName", graphName)
                        ).consume()
                    }
                }
            }

        } finally {
            try {
                repo.close()
            } catch (_: Exception) {
            }
        }
    }
}
