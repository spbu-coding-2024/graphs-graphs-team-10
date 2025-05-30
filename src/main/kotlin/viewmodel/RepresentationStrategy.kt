package viewmodel

import androidx.compose.ui.unit.Dp

interface RepresentationStrategy {
    val defaultVertexRadius: Dp
    val defaultEdgesWidth: Float

    fun place(
        width: Double,
        height: Double,
        vertices: Collection<VertexViewModel>,
        edges: Collection<EdgeViewModel>,
    )

    fun resetVertices(vertices: Collection<VertexViewModel>)

    fun resetEdges(edges: Collection<EdgeViewModel>)
}
