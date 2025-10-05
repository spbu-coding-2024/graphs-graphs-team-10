import algos.findCommunities
import model.UndirectedGraph
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CommunitiesTest {
    @Test
    fun `empty graph returns empty map`() {
        val graph = UndirectedGraph()
        val communities = findCommunities(graph)
        assertEquals(emptyMap(), communities)
    }

    @Test
    fun `two disconnected edges are two communities`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(3L, 4L, 1)
        }

        val comm = findCommunities(graph)

        assertEquals(comm[1L], comm[2L])
        assertEquals(comm[3L], comm[4L])

        assertNotEquals(comm[1L], comm[3L])
    }

    @Test
    fun `chain of three nodes is a single community`() {
        val graph = UndirectedGraph().apply {
            addEdge(1L, 2L, 1)
            addEdge(2L, 3L, 1)
        }
        val comm = findCommunities(graph)

        assertEquals(comm[1L], comm[2L])
        assertEquals(comm[2L], comm[3L])
        assertEquals(comm[1L], comm[3L])
    }

    @Test
    fun `four distinct vertices are four communities`() {
        val graph = UndirectedGraph().apply {
            addVertex(1L)
            addVertex(2L)
            addVertex(3L)
            addVertex(4L)
        }
        val comm = findCommunities(graph)

        assertNotEquals(comm[1L], comm[2L])
        assertNotEquals(comm[1L], comm[3L])
        assertNotEquals(comm[1L], comm[4L])
        assertNotEquals(comm[2L], comm[3L])
        assertNotEquals(comm[2L], comm[4L])
        assertNotEquals(comm[3L], comm[4L])
    }
}
