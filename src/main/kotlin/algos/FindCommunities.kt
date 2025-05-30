package algos

import model.Graph
import model.UndirectedGraph


//Simplified Louvain's Method
fun findCommunities(graph: Graph, resolution: Double = 1.0): Map<Long, Int> {
    if (graph !is UndirectedGraph || graph.isEmpty()) return emptyMap()

    val communities = graph.vertices.associateWith { it.toInt() }.toMutableMap()
    var currentModularity = Double.NEGATIVE_INFINITY
    var improved = true

    val totalWeight = graph.edges.sumOf { it.weight }.toDouble() / 2.0

    fun calculateModularity(): Double {
        var q = 0.0
        val communityDegrees = mutableMapOf<Int, Double>()
        val communityLinks = mutableMapOf<Int, Double>()

        graph.edges.forEach { edge ->
            val (i, j) = edge.vertices
            val ci = communities[i]!!
            val cj = communities[j]!!
            val weight = edge.weight.toDouble()

            if (ci == cj) {
                communityLinks[ci] = (communityLinks[ci] ?: 0.0) + weight
            }
            communityDegrees[ci] = (communityDegrees[ci] ?: 0.0) + weight
            communityDegrees[cj] = (communityDegrees[cj] ?: 0.0) + weight
        }

        communityDegrees.forEach { (c, deg) ->
            q += (communityLinks[c] ?: 0.0) - resolution * (deg * deg) / (4.0 * totalWeight * totalWeight)
        }

        return q / (2.0 * totalWeight)
    }

    fun getNeighborCommunities(node: Long): Map<Int, Double> {
        val neighborComms = mutableMapOf<Int, Double>()
        graph.edges.forEach { edge ->
            when (node) {
                edge.vertices.first -> {
                    val neighbor = edge.vertices.second
                    val comm = communities[neighbor]!!
                    neighborComms[comm] = (neighborComms[comm] ?: 0.0) + edge.weight.toDouble()
                }
                edge.vertices.second -> {
                    val neighbor = edge.vertices.first
                    val comm = communities[neighbor]!!
                    neighborComms[comm] = (neighborComms[comm] ?: 0.0) + edge.weight.toDouble()
                }
            }
        }
        return neighborComms
    }

    while (improved) {
        improved = false
        val newModularity = calculateModularity()

        if (newModularity > currentModularity + 0.0001) {
            currentModularity = newModularity
            improved = true
        }

        val nodes = graph.vertices.shuffled()
        nodes.forEach { node ->
            val currentComm = communities[node]!!
            val neighborComms = getNeighborCommunities(node)
            var bestComm = currentComm
            var bestGain = 0.0

            neighborComms.forEach { (comm, weight) ->
                if (comm != currentComm) {
                    val gain = weight - resolution * (neighborComms.values.sum() * (neighborComms[comm] ?: 0.0)) / (2.0 * totalWeight)
                    if (gain > bestGain) {
                        bestGain = gain
                        bestComm = comm
                    }
                }
            }
            if (bestComm != currentComm) {
                communities[node] = bestComm
            }
        }
    }
    return communities
}