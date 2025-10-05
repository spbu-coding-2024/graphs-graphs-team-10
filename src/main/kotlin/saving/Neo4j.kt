package saving

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import model.DirectedGraph
import model.UndirectedGraph
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.SessionConfig
import org.neo4j.driver.Values
import viewmodel.GraphViewModel
import viewmodel.MainScreenViewModel
import viewmodel.MainScreenViewModelForDirectedGraph
import viewmodel.MainScreenViewModelForUndirectedGraph
import viewmodel.ForceDirectedLayout
import java.io.Closeable

class Neo4jGraphRepository(
    private val uri: String,
    private val user: String,
    private val password: String,
    private val database: String? = null
) : Closeable {
    private val driver: Driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))

    private fun createSession() = if (database != null) {
        driver.session(SessionConfig.builder().withDatabase(database).build())
    } else {
        driver.session()
    }

    override fun close() {
        driver.close()
    }

    fun saveGraph(graphViewModel: GraphViewModel, graphName: String, isDirected: Boolean) {
        createSession().use { session ->
            
            // Delete existing graph
            session.executeWrite { tx ->
                tx.run("""
                MATCH (g:Graph {name: $${"graphName"}}) - [:CONTAINS] - (n)
                DETACH DELETE g, n
            """, Values.parameters("graphName", graphName)).consume()
            }

            session.executeWrite { tx ->
                tx.run("""
                CREATE (g:Graph {
                    name: $${"graphName"},
                    isDirected: $${"isDirected"},
                    createdAt: datetime()
                })
            """, Values.parameters(
                    "graphName", graphName,
                    "isDirected", isDirected
                )).consume()
            }

            graphViewModel.vertices.forEach { vertex ->
                session.executeWrite { tx ->
                    tx.run("""
                    MATCH (g:Graph {name: $${"graphName"}})
                    CREATE (v:Vertex {
                        value: $${"value"},
                        x: $${"x"},
                        y: $${"y"},
                        color: $${"color"},
                        radius: $${"radius"}
                    })-[:CONTAINS]->(g)
                """, Values.parameters(
                        "graphName", graphName,
                        "value", vertex.value,
                        "x", vertex.x.value,
                        "y", vertex.y.value,
                        "color", vertex.color.toArgb(),
                        "radius", vertex.radius.value
                    )).consume()
                }
            }

            graphViewModel.edges.forEach { edge ->
                session.executeWrite { tx ->
                    tx.run("""
                    MATCH (a:Vertex {value: $${"fromValue"}}), (b:Vertex {value: $${"toValue"}})
                    CREATE (a)-[r:EDGE {
                        weight: $${"weight"},
                        color: $${"color"},
                        width: $${"width"}
                    }]->(b)
                """, Values.parameters(
                        "fromValue", edge.firstVertex.value,
                        "toValue", edge.secondVertex.value,
                        "weight", edge.weight.toLong(),
                        "color", edge.color.toArgb(),
                        "width", edge.width
                    )).consume()
                }
            }
        }
    }


    fun loadGraph(graphName: String): MainScreenViewModel {
        val viewModel: MainScreenViewModel
        createSession().use { session ->
            val isDirected = session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (g:Graph {name: $${"graphName"}}) RETURN g.isDirected AS isDirected",
                    Values.parameters("graphName", graphName)
                )
                result.single()["isDirected"].asBoolean()
            }

            val graph = if (isDirected) DirectedGraph() else UndirectedGraph()

            // Load vertices
            session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (g:Graph {name: $${"graphName"}})-[:CONTAINS]-(v:Vertex) RETURN v",
                    Values.parameters("graphName", graphName)
                )
                result.forEach { rec ->
                    val node = rec["v"].asNode()
                    val value = node["value"].asLong()
                    graph.addVertex(value)
                }
            }

            // Load edges
            session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (a:Vertex)-[r:EDGE]->(b:Vertex) RETURN a.value AS from, b.value AS to, r",
                    Values.parameters()
                )
                result.forEach { rec ->
                    val from = rec["from"].asLong()
                    val to = rec["to"].asLong()
                    val rel = rec["r"].asRelationship()
                    graph.addEdge(from, to, rel["weight"].asLong())
                }
            }

            viewModel = if (isDirected) {
                MainScreenViewModelForDirectedGraph(graph as DirectedGraph, ForceDirectedLayout())
            } else {
                MainScreenViewModelForUndirectedGraph(graph as UndirectedGraph, ForceDirectedLayout())
            }

            // Apply vertex properties to viewModel
            session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (g:Graph {name: \$graphName})-[:CONTAINS]-(v:Vertex) RETURN v",
                    Values.parameters("graphName", graphName)
                )
                result.forEach { rec ->
                    val node = rec["v"].asNode()
                    val value = node["value"].asLong()
                    viewModel.graphViewModel.vertices.find { it.value == value }?.apply {
                        x = node["x"].asDouble().dp
                        y = node["y"].asDouble().dp
                        color = Color(node["color"].asInt())
                        radius = node["radius"].asDouble().dp
                    }
                }
            }

            // Apply edge properties to viewModel
            session.executeRead { tx ->
                val result = tx.run(
                    "MATCH (g:Graph {name: \$graphName})-[:CONTAINS]-(a:Vertex)-[r:EDGE]->(b:Vertex) " +
                            "RETURN a.value AS from, b.value AS to, r",
                    Values.parameters("graphName", graphName)
                )
                result.forEach { rec ->
                    val from = rec["from"].asLong()
                    val to = rec["to"].asLong()
                    val rel = rec["r"].asRelationship()
                    viewModel.graphViewModel.setEdgeColor(from, to, Color(rel["color"].asInt()))
                    viewModel.graphViewModel.setEdgeWidth(from, to, rel["width"].asFloat())
                }
            }
        }
        return viewModel
    }

    fun listSavedGraphs(): List<String> = createSession().use { session ->
        session.executeRead { tx ->
            tx.run("MATCH (g:Graph) RETURN g.name AS name ORDER BY name")
                .list { it["name"].asString() }
        }
    }
}
