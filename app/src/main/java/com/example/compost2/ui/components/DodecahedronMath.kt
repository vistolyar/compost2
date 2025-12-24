package com.example.compost2.ui.components

import kotlin.math.*

data class Vector3(val x: Float, val y: Float, val z: Float) {
    fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z
}

fun Vector3.rotX(a: Float): Vector3 {
    val c = cos(a); val s = sin(a)
    return Vector3(x, y * c - z * s, y * s + z * c)
}

fun Vector3.rotY(a: Float): Vector3 {
    val c = cos(a); val s = sin(a)
    return Vector3(x * c + z * s, y, -x * s + z * c)
}

fun Vector3.rotZ(a: Float): Vector3 {
    val c = cos(a); val s = sin(a)
    return Vector3(x * c - y * s, x * s + y * c, z)
}

operator fun Vector3.minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
operator fun Vector3.plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
operator fun Vector3.unaryMinus() = Vector3(-x, -y, -z)

fun Vector3.cross(v: Vector3) = Vector3(
    y * v.z - z * v.y,
    z * v.x - x * v.z,
    x * v.y - y * v.x
)

fun Vector3.norm(): Vector3 {
    val m = sqrt(x * x + y * y + z * z).coerceAtLeast(0.0001f)
    return Vector3(x / m, y / m, z / m)
}

object DodecahedronGeometry {
    val phi = (1f + sqrt(5f)) / 2f
    val inv = 1f / phi
    val vertices = listOf(
        Vector3(1f, 1f, 1f), Vector3(1f, 1f, -1f), Vector3(1f, -1f, 1f), Vector3(1f, -1f, -1f),
        Vector3(-1f, 1f, 1f), Vector3(-1f, 1f, -1f), Vector3(-1f, -1f, 1f), Vector3(-1f, -1f, -1f),
        Vector3(0f, inv, phi), Vector3(0f, inv, -phi), Vector3(0f, -inv, phi), Vector3(0f, -inv, -phi),
        Vector3(inv, phi, 0f), Vector3(inv, -phi, 0f), Vector3(-inv, phi, 0f), Vector3(-inv, -phi, 0f),
        Vector3(phi, 0f, inv), Vector3(phi, 0f, -inv), Vector3(-phi, 0f, inv), Vector3(-phi, 0f, -inv)
    )
    val faces = listOf(
        listOf(11, 9, 5, 19, 7), listOf(12, 1, 9, 5, 14), listOf(5, 14, 4, 18, 19), listOf(16, 17, 1, 12, 0),
        listOf(2, 13, 15, 6, 10), listOf(6, 15, 7, 19, 18), listOf(17, 16, 2, 13, 3), listOf(1, 17, 3, 11, 9),
        listOf(13, 3, 11, 7, 15), listOf(8, 10, 6, 18, 4), listOf(2, 16, 0, 8, 10), listOf(8, 0, 12, 14, 4)
    )

    val faceBasis = faces.map { idxs ->
        var center = Vector3(0f, 0f, 0f)
        idxs.forEach { center += vertices[it] }
        center = Vector3(center.x / 5f, center.y / 5f, center.z / 5f)
        val v0 = vertices[idxs[0]]
        val v1 = vertices[idxs[1]]
        val v2 = vertices[idxs[2]]
        var normal = ((v1 - v0).cross(v2 - v0)).norm()
        if (normal.dot(center) < 0) normal = -normal
        val up = (v0 - center).norm()
        val right = up.cross(normal).norm()
        Triple(center, right, up)
    }
}

enum class ControlState { RECORDING, SELECTION, FOCUSED }