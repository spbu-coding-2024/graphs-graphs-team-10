package viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import saving.toPx
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class ForceDirectedLayout : RepresentationStrategy {
    override val defaultVertexRadius: Dp = 25.dp
    override val defaultEdgesWidth: Float = 1f

    private var repulsionForce = 300.0
    private var attractionForce = 0.1
    private var damping = 0.8
    private var maxIterations = 500
    private var overlapPreventionForce = 1500.0 // Increased force
    private val minDistanceMultiplier = 3.0

    override fun place(
        width: Double,
        height: Double,
        vertices: Collection<VertexViewModel>,
        edges: Collection<EdgeViewModel>
    ) {
        val area = width * height
        val optimalDistance = sqrt(area / vertices.size)

        if (vertices.all { it.x == 0.dp && it.y == 0.dp }) {
            vertices.forEach {
                it.x = randomDouble(width).dp
                it.y = randomDouble(height).dp
            }
        }

        val vertexRadii = vertices.associateWith { it.radius.toPx() }
        val defaultRadiusPx = defaultVertexRadius.toPx()

        repeat(maxIterations) {
            vertices.forEach { currentVertex ->
                var forceX = 0.0
                var forceY = 0.0

                val currentRadius = vertexRadii[currentVertex] ?: defaultRadiusPx
                val minAllowedDistance = defaultRadiusPx * minDistanceMultiplier

                vertices.forEach { otherVertex ->
                    if (currentVertex != otherVertex) {
                        val otherRadius = vertexRadii[otherVertex] ?: defaultRadiusPx
                        val minDistance = max(minAllowedDistance, (currentRadius + otherRadius).toDouble())

                        val dx = (currentVertex.x - otherVertex.x).toPx()
                        val dy = (currentVertex.y - otherVertex.y).toPx()
                        val distance = max(sqrt((dx*dx + dy*dy).toDouble()), 0.1)

                        val repulsion = repulsionForce / (distance * distance)

                        val antiOverlap = if (distance < minDistance) {
                            val overlapFactor = (minDistance - distance) / minDistance
                            overlapPreventionForce * overlapFactor / (distance * distance)
                        } else 0.0

                        forceX += (dx / distance) * (repulsion + antiOverlap)
                        forceY += (dy / distance) * (repulsion + antiOverlap)
                    }
                }

                edges.filter {
                    it.firstVertex == currentVertex || it.secondVertex == currentVertex
                }.forEach { edge ->
                    val neighbor = if (edge.firstVertex == currentVertex)
                        edge.secondVertex else edge.firstVertex

                    val neighborRadius = vertexRadii[neighbor] ?: defaultRadiusPx
                    val minDistance = max(minAllowedDistance, (currentRadius + neighborRadius).toDouble())

                    val dx = (currentVertex.x - neighbor.x).toPx()
                    val dy = (currentVertex.y - neighbor.y).toPx()
                    val distance = max(sqrt((dx*dx + dy*dy).toDouble()), 0.1)

                    val attraction = if (distance < minDistance) {
                        -attractionForce * (minDistance - distance) / optimalDistance
                    } else {
                        attractionForce * (distance - minDistance) / optimalDistance
                    }

                    forceX -= (dx / distance) * attraction
                    forceY -= (dy / distance) * attraction
                }

                currentVertex.x += (forceX * damping).dp
                currentVertex.y += (forceY * damping).dp

                currentVertex.x = currentVertex.x.coerceIn(
                    currentRadius.dp,
                    (width - currentRadius).dp
                )
                currentVertex.y = currentVertex.y.coerceIn(
                    currentRadius.dp,
                    (height - currentRadius).dp
                )
            }
        }
    }

    private fun randomDouble(max: Double): Double {
        return 0.0 + Random.nextDouble() * (max)
    }

    override fun resetVertices(vertices: Collection<VertexViewModel>) {
        vertices.forEach {
            it.color = Color.Gray
            it.radius = defaultVertexRadius
        }
    }

    override fun resetEdges(edges: Collection<EdgeViewModel>) {
        edges.forEach {
            it.color = Color.Black
            it.width = defaultEdgesWidth
        }
    }
}