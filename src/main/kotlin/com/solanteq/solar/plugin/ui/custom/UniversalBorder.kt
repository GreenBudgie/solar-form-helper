package com.solanteq.solar.plugin.ui.custom

import com.intellij.ui.JBColor
import com.solanteq.solar.plugin.ui.custom.UniversalBorder.Companion.builder
import java.awt.*
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import javax.swing.border.AbstractBorder
import kotlin.math.sqrt

/**
 * A universal border that may have independent borders with different radii and background fill.
 * Use [builder] to construct a border.
 */
class UniversalBorder private constructor() : AbstractBorder() {

    private var allRadius: Int? = null
    private var topLeftRadius: Int = 0
    private var bottomLeftRadius: Int = 0
    private var topRightRadius: Int = 0
    private var bottomRightRadius: Int = 0
    private var drawTop: Boolean = true
    private var drawBottom: Boolean = true
    private var drawLeft: Boolean = true
    private var drawRight: Boolean = true
    private var drawOutline: Boolean = true
    private var borderColor: Color = JBColor.border()
    private var backgroundColor: Color? = null

    override fun paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
        val graphics = g as Graphics2D
        val oldAntialiasing = graphics.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val oldColor = graphics.color
        graphics.color = borderColor

        if (allRadius != null && drawAllSides()) {
            drawWithRectangle(x, y, width, height, graphics)
        } else {
            drawWithPath(x, y, width, height, graphics)
        }

        graphics.color = oldColor
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing)
    }

    private fun drawAllSides() = drawLeft && drawBottom && drawRight && drawTop

    private fun drawWithRectangle(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        graphics: Graphics2D
    ) {
        val allRadius = allRadius ?: error("All radius is null")
        val noRadius = allRadius == 0
        if (backgroundColor != null) {
            graphics.color = backgroundColor
            if (noRadius) {
                graphics.fillRect(
                    x,
                    y,
                    width,
                    height
                )
            } else {
                graphics.fillRoundRect(
                    x,
                    y,
                    width,
                    height,
                    allRadius * 2,
                    allRadius * 2
                )
            }
        }
        if (drawOutline) {
            graphics.color = borderColor
            if (noRadius) {
                graphics.drawRect(
                    x,
                    y,
                    width - 1,
                    height - 1
                )
            } else {
                graphics.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    allRadius * 2,
                    allRadius * 2
                )
            }
        }
    }

    private fun drawWithPath(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        graphics: Graphics2D
    ) {
        val pathToFill = constructPath(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), true)
        if (backgroundColor != null) {
            graphics.color = backgroundColor
            graphics.fill(pathToFill)
        }
        val canReusePath = drawAllSides()
        val pathToDraw = if (canReusePath) {
            pathToFill
        } else {
            constructPath(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble(), false)
        }
        if (drawOutline) {
            graphics.color = borderColor
            graphics.draw(pathToDraw)
        }
    }

    private fun constructPath(left: Double, top: Double, width: Double, height: Double, fill: Boolean): Path2D {
        val adjustment = if (fill) 0 else 1
        val right = left + width - adjustment
        val bottom = top + height - adjustment
        val path = Path2D.Double()
        path.moveTo(left, top + topLeftRadius)
        // Left border
        path.lineOrMoveTo(left, bottom - bottomLeftRadius, drawLeft || fill)

        // Bottom left curve
        if (bottomLeftRadius > 0) {
            val curve = curve(bottomLeftRadius)
            val endX = left + bottomLeftRadius
            path.curveTo(
                left,
                bottom - bottomLeftRadius + curve,
                endX - curve,
                bottom,
                endX,
                bottom
            )
        }

        // Bottom border
        path.lineOrMoveTo(right - bottomRightRadius, bottom, drawBottom || fill)

        // Bottom right curve
        if (bottomRightRadius > 0) {
            val curve = curve(bottomRightRadius)
            val endY = bottom - bottomRightRadius
            path.curveTo(
                right - bottomRightRadius + curve,
                bottom,
                right,
                endY + curve,
                right,
                endY
            )
        }

        // Right border
        path.lineOrMoveTo(right, top + topRightRadius, drawRight || fill)

        // Top right curve
        if (topRightRadius > 0) {
            val curve = curve(topRightRadius)
            val endX = right - topRightRadius
            path.curveTo(
                right,
                top + topRightRadius - curve,
                endX + curve,
                top,
                endX,
                top
            )
        }

        // Top border
        path.lineOrMoveTo(left + topLeftRadius, top, drawTop || fill)

        // Top left curve
        if (topLeftRadius > 0) {
            val curve = curve(topLeftRadius)
            val endY = top + topLeftRadius
            path.curveTo(
                left + topLeftRadius - curve,
                top,
                left,
                endY - curve,
                left,
                endY
            )
        }
        return path
    }

    private fun Path2D.Double.lineOrMoveTo(x: Double, y: Double, drawCondition: Boolean): Point2D.Double {
        if (drawCondition) {
            lineTo(x, y)
        } else {
            moveTo(x, y)
        }
        return Point2D.Double(x, y)
    }

    private fun curve(radius: Int) = radius * CURVE_APPROXIMATION

    companion object {

        fun builder() = UniversalBorderBuilder()

        val CURVE_APPROXIMATION = 4 * (sqrt(2.0) - 1) / 3

    }

    class UniversalBorderBuilder {

        private val border = UniversalBorder()

        /**
         * Sets the radius for all corners
         */
        fun radius(all: Int): UniversalBorderBuilder {
            border.allRadius = all
            return this
        }

        /**
         * Sets independent radius for each corners
         */
        fun radius(topLeft: Int, bottomLeft: Int, bottomRight: Int, topRight: Int): UniversalBorderBuilder {
            border.topLeftRadius = topLeft
            border.bottomLeftRadius = bottomLeft
            border.topRightRadius = bottomRight
            border.bottomRightRadius = topRight
            return this
        }

        /**
         * Sets radius for top-left and top-right corners
         */
        fun topRadius(top: Int): UniversalBorderBuilder {
            border.topLeftRadius = top
            border.topRightRadius = top
            return this
        }

        /**
         * Sets radius for bottom-left and bottom-right corners
         */
        fun bottomRadius(bottom: Int): UniversalBorderBuilder {
            border.bottomLeftRadius = bottom
            border.bottomRightRadius = bottom
            return this
        }

        /**
         * Sets radius for top-left corner
         */
        fun topLeftRadius(topLeft: Int): UniversalBorderBuilder {
            border.topLeftRadius = topLeft
            return this
        }

        /**
         * Sets radius for bottom-left corner
         */
        fun bottomLeftRadius(bottomLeft: Int): UniversalBorderBuilder {
            border.bottomLeftRadius = bottomLeft
            return this
        }

        /**
         * Sets radius for top-right corner
         */
        fun topRightRadius(topRight: Int): UniversalBorderBuilder {
            border.topRightRadius = topRight
            return this
        }

        /**
         * Sets radius for bottom-right corner
         */
        fun bottomRightRadius(bottomRight: Int): UniversalBorderBuilder {
            border.bottomRightRadius = bottomRight
            return this
        }

        /**
         * Sets whether to draw top outline of the border
         */
        fun drawTop(draw: Boolean): UniversalBorderBuilder {
            border.drawTop = draw
            return this
        }

        /**
         * Sets whether to draw left outline of the border
         */
        fun drawLeft(draw: Boolean): UniversalBorderBuilder {
            border.drawLeft = draw
            return this
        }

        /**
         * Sets whether to draw bottom outline of the border
         */
        fun drawBottom(draw: Boolean): UniversalBorderBuilder {
            border.drawBottom = draw
            return this
        }

        /**
         * Sets whether to draw right outline of the border
         */
        fun drawRight(draw: Boolean): UniversalBorderBuilder {
            border.drawRight = draw
            return this
        }

        /**
         * Sets whether to draw each outline of the border
         */
        fun draw(top: Boolean, left: Boolean, bottom: Boolean, right: Boolean): UniversalBorderBuilder {
            drawTop(top)
            drawLeft(left)
            drawBottom(bottom)
            drawRight(right)
            return this
        }

        /**
         * The border outline will not be drawn.
         * Use with [background].
         */
        fun noOutline(): UniversalBorderBuilder {
            border.drawOutline = false
            return this
        }

        /**
         * Sets the background color for the entire element.
         * Do not set background color for the element itself as it will overwrite this background.
         */
        fun background(color: Color): UniversalBorderBuilder {
            border.backgroundColor = color
            return this
        }

        /**
         * Sets the color of the border
         */
        fun color(color: Color): UniversalBorderBuilder {
            border.borderColor = color
            return this
        }

        fun build(): UniversalBorder {
            if (border.allRadius != null) {
                return border
            }
            val radiiEqual = setOf(
                border.topLeftRadius,
                border.bottomLeftRadius,
                border.bottomRightRadius,
                border.topRightRadius
            ).size == 1
            if (radiiEqual) {
                border.allRadius = border.topLeftRadius
            }
            return border
        }

    }

}