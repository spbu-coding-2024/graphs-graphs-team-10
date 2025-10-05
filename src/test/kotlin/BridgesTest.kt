import algos.findBridges
import model.Edge
import model.UndirectedGraph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BridgesTest {

    private fun toTriples(edges: List<Edge>): Set<Triple<Long, Long, Long>> {
        return edges.map { e ->
            val (u, v) = e.vertices
            Triple(u, v, e.weight)
        }.toSet()
    }

    @Test
    fun `empty graph returns empty list`() {
        val graph = UndirectedGraph()
        val bridges = findBridges(graph)
        assertEquals(emptyList(), bridges)
    }

    @Test
    fun `single edge is a bridge`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 5)
        }
        val bridges = findBridges(graph)
        val expected = setOf(Triple(1L, 2L, 5L))
        assertEquals(expected, toTriples(bridges))
    }

    @Test
    fun `triangle has no bridges`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 1)
            addEdge(1L, 3L, 1)
        }
        val bridges = findBridges(graph)
        assertEquals(emptySet(), toTriples(bridges))
    }

    @Test
    fun `chain graph - every edge is a bridge`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 1)
            addEdge(3L, 4L, 1)
        }
        val bridges = findBridges(graph)
        val expected = setOf(
            Triple(1L, 2L, 1L),
            Triple(2L, 3L, 1L),
            Triple(3L, 4L, 1L)
        )
        assertEquals(expected, toTriples(bridges))
    }

    @Test
    fun `two triangles connected by single bridge`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 1)
            addEdge(1L, 3L, 1)

            addEdge(4L, 5L, 1)
            addEdge(5L, 6L, 1)
            addEdge(4L, 6L, 1)

            addEdge(3L, 4L, 7)
        }
        val bridges = findBridges(graph)
        val expected = setOf(Triple(3L, 4L, 7L))
        assertEquals(expected, toTriples(bridges))
    }

    @Test
    fun `disconnected graph returns bridges in each component`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(3L, 4L, 2)
        }
        val bridges = findBridges(graph)
        val expected = setOf(
            Triple(1L, 2L, 1L),
            Triple(3L, 4L, 2L)
        )
        assertEquals(expected, toTriples(bridges))
    }
}
