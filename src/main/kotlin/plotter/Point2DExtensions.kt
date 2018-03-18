package plotter

import javafx.geometry.Point2D
import model.Point

/**
 * @author leon
 */
operator fun Point2D.plus(other: Point2D): Point2D = add(other)

operator fun Point2D.plus(point: Point): Point2D = add(point.to2D())

operator fun Point2D.plus(point: Pair<Double, Double>): Point2D = add(point.first, point.second)

operator fun Point2D.minus(other: Point2D): Point2D = subtract(other)

operator fun Point2D.minus(point: Point): Point2D = subtract(point.to2D())

operator fun Point2D.minus(point: Pair<Double, Double>): Point2D = subtract(point.first, point.second)

operator fun Point2D.times(factor: Double): Point2D = multiply(factor)

operator fun Point2D.times(factor: Int): Point2D = this * factor.toDouble()

operator fun Point2D.times(point: Pair<Double, Double>): Point2D = Point2D(x * point.first, y * point.second)

operator fun Point2D.unaryMinus() = this * -1