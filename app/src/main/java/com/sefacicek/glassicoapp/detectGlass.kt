package com.sefacicek.glassicoapp

import android.R.attr.src
import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.core.MatOfDouble
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.MatOfPoint3f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.math.tan


const val D_TAG = "MyCamera"
var lineWidth = -1
var lineLength = -1.0
var lineTopLeft: Point? = null
var lineBottomRight: Point? = null


fun detectGlass(bitmap: Bitmap, flag: Boolean): Mat? {

    var circleFounded = false

    val colorType: Bitmap.Config = bitmap.config

    val img = Mat()
    Utils.bitmapToMat(bitmap, img)

    val type: Int = img.type()

    Log.d(D_TAG, "Detect glass started")

    val modelSize = Size(720.0, 480.0)

    if (img.empty()) {
        Log.d(D_TAG, "Image is empty")
        return null
    }

    Imgproc.resize(img, img, modelSize)


    // Convert the image from BGR to HSV
    val hsv = Mat()
    Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV)

    // Define the lower and upper threshold values for red in HSV
    // lower mask (0-10)
    val lowerRed = Scalar(0.0, 50.0, 50.0)
    val upperRed = Scalar(10.0, 255.0, 255.0)
    val mask1 = Mat()
    Core.inRange(hsv, lowerRed, upperRed, mask1)

    // upper mask (170-180)
    val lowerRed2 = Scalar(170.0, 50.0, 50.0)
    val upperRed2 = Scalar(180.0, 255.0, 255.0)
    val mask2 = Mat()
    Core.inRange(hsv, lowerRed2, upperRed2, mask2)

    // Combine the masks
    val mask = Mat()
    Core.bitwise_or(mask1, mask2, mask)

    Log.d(D_TAG, "mask size: ${mask.size()}")

    // Find contours in the binary mask
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(
        mask,
        contours,
        hierarchy,
        Imgproc.RETR_EXTERNAL,
        Imgproc.CHAIN_APPROX_NONE
    )

    var redBlockCount = 0
    val topLeftPoints = ArrayList<Point>()
    val topRightPoints = ArrayList<Point>()
    val bottomLeftPoints = ArrayList<Point>()
    val bottomRightPoints = ArrayList<Point>()
    val redBlocksPoints = ArrayList<Point>()

    // Iterate over the contours and filter out the ones that are not rectangular or have a small area
    for (contour in contours) {

        // Calculate the contour area
        val area = Imgproc.contourArea(contour)

        // Filter out small polygons
        if (area < 1000) {  // size buyudukce degisir
            continue
        }

        val curve = MatOfPoint2f()
        contour.convertTo(curve, CvType.CV_32F)

        // Approximate the contour as a polygon
        val epsilon = 0.015 * Imgproc.arcLength(curve, true)
        val approxCurve = MatOfPoint2f()
        Imgproc.approxPolyDP(curve, approxCurve, epsilon, true)


        // Filter out non-rectangular polygons
        if (approxCurve.rows() != 4) {
            Log.d(D_TAG, "approxCurve < 4")
            continue
        }


        val contourList = contour.toList()

        // Find the top-left (tl), top-right (tr), bottom-right (br), and bottom-left (bl) points
        var tl = contourList[0]
        var tr = contourList[0]
        var br = contourList[0]
        var bl = contourList[0]

        for (point in contourList) {
            val x = point.x
            val y = point.y

            if (x + y < tl.x + tl.y)
                tl = point

            if (x - y > tr.x - tr.y)
                tr = point

            if (x + y > br.x + br.y)
                br = point

            if (x - y < bl.x - bl.y)
                bl = point
        }


        topLeftPoints.add(Point(tl.x, tl.y))
        topRightPoints.add(Point(tr.x, tr.y))
        bottomRightPoints.add(Point(br.x, br.y))
        bottomLeftPoints.add(Point(bl.x, bl.y))

        redBlocksPoints.add(Point(tl.x, tl.y))
        redBlocksPoints.add(Point(tr.x, tr.y))
        redBlocksPoints.add(Point(br.x, br.y))
        redBlocksPoints.add(Point(bl.x, bl.y))

        Imgproc.circle(img, Point(tl.x, tl.y), 5, Scalar(0.0, 0.0, 255.0), -1)
        Imgproc.circle(img, Point(tr.x, tr.y), 5, Scalar(0.0, 255.0, 0.0), -1)
        Imgproc.circle(img, Point(br.x, br.y), 5, Scalar(255.0, 255.0, 255.0), -1)
        Imgproc.circle(img, Point(bl.x, bl.y), 5, Scalar(255.0, 0.0, 0.0), -1)

        redBlockCount++

    }

    if (redBlocksPoints.size < 16) {
        Log.d(D_TAG, "4 kare algılanmadı")
        return null
    } else {
        // Find the top-left point of the array
        val tl = topLeftPoints.minBy { it.x + it.y }
        val tr = topRightPoints.maxBy { it.x - it.y }
        val br = bottomRightPoints.maxBy { it.x + it.y }
        val bl = bottomLeftPoints.minBy { it.x - it.y }

        val srcPoints = MatOfPoint2f(
            Point(tl.x, tl.y),
            Point(tr.x, tr.y),
            Point(br.x, br.y),
            Point(bl.x, bl.y)
        )
        val dstPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(modelSize.width, 0.0),
            Point(modelSize.width, modelSize.height),
            Point(0.0, modelSize.height)
        )


        // Find homography for perspective transformation
        val H = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        val firstBoxPoints = MatOfPoint2f(
            Point(redBlocksPoints[0].x, redBlocksPoints[0].y),
            Point(redBlocksPoints[1].x, redBlocksPoints[1].y),
            Point(redBlocksPoints[2].x, redBlocksPoints[2].y),
            Point(redBlocksPoints[3].x, redBlocksPoints[3].y)
        )

        val secondBoxPoints = MatOfPoint2f(
            Point(redBlocksPoints[4].x, redBlocksPoints[4].y),
            Point(redBlocksPoints[5].x, redBlocksPoints[5].y),
            Point(redBlocksPoints[6].x, redBlocksPoints[6].y),
            Point(redBlocksPoints[7].x, redBlocksPoints[7].y)
        )

        val thirdBoxPoints = MatOfPoint2f(
            Point(redBlocksPoints[8].x, redBlocksPoints[8].y),
            Point(redBlocksPoints[9].x, redBlocksPoints[9].y),
            Point(redBlocksPoints[10].x, redBlocksPoints[10].y),
            Point(redBlocksPoints[11].x, redBlocksPoints[11].y)
        )

        val fourthBoxPoints = MatOfPoint2f(
            Point(redBlocksPoints[12].x, redBlocksPoints[12].y),
            Point(redBlocksPoints[13].x, redBlocksPoints[13].y),
            Point(redBlocksPoints[14].x, redBlocksPoints[14].y),
            Point(redBlocksPoints[15].x, redBlocksPoints[15].y)
        )


        val firstWarpedBoxPoints = MatOfPoint2f()
        Core.perspectiveTransform(firstBoxPoints, firstWarpedBoxPoints, H)

        val secondWarpedBoxPoints = MatOfPoint2f()
        Core.perspectiveTransform(secondBoxPoints, secondWarpedBoxPoints, H)

        val thirdWarpedBoxPoints = MatOfPoint2f()
        Core.perspectiveTransform(thirdBoxPoints, thirdWarpedBoxPoints, H)

        val fourthWarpedBoxPoints = MatOfPoint2f()
        Core.perspectiveTransform(fourthBoxPoints, fourthWarpedBoxPoints, H)

        val firstBoxWidth =
            firstWarpedBoxPoints.toArray()[1].x - firstWarpedBoxPoints.toArray()[0].x
        val secondBoxWidth =
            secondWarpedBoxPoints.toArray()[1].x - secondWarpedBoxPoints.toArray()[0].x
        val thirdBoxWidth =
            thirdWarpedBoxPoints.toArray()[1].x - thirdWarpedBoxPoints.toArray()[0].x
        val fourthBoxWidth =
            fourthWarpedBoxPoints.toArray()[1].x - fourthWarpedBoxPoints.toArray()[0].x


        val redBlocksWidth = listOf(firstBoxWidth, secondBoxWidth, thirdBoxWidth, fourthBoxWidth)
        val sortedRedBlocksWidth =
            redBlocksWidth.sorted()


        val firstBoxHeight =
            firstWarpedBoxPoints.toArray()[2].y - firstWarpedBoxPoints.toArray()[1].y
        val secondBoxHeight =
            secondWarpedBoxPoints.toArray()[2].y - secondWarpedBoxPoints.toArray()[1].y
        val thirdBoxHeight =
            thirdWarpedBoxPoints.toArray()[2].y - thirdWarpedBoxPoints.toArray()[1].y
        val fourthBoxHeight =
            fourthWarpedBoxPoints.toArray()[2].y - fourthWarpedBoxPoints.toArray()[1].y

        val redBlocksHeight =
            listOf(firstBoxHeight, secondBoxHeight, thirdBoxHeight, fourthBoxHeight)

        // Calculate the sum of the widths
        val total_width = redBlocksWidth.sum()
        val total_height = redBlocksHeight.sum()

        // Calculate the average value
        val block_width = total_width / 4
        val block_height = total_height / 4


        val blocksAreas = listOf(
            firstBoxWidth * firstBoxHeight,
            secondBoxWidth * secondBoxHeight,
            thirdBoxWidth * thirdBoxHeight,
            fourthBoxWidth * fourthBoxHeight
        )

        val blockAreasSorted = blocksAreas.sorted()

        val maxDiff = blockAreasSorted[3] - blockAreasSorted[0]

        if (maxDiff > 5000) {
            println("max diff > 5000 blocks are not fully visible..")
            return null
        } else {
            val warpedImg = Mat()
            Imgproc.warpPerspective(img, warpedImg, H, Size(modelSize.width, modelSize.height))

            val tolerance = 5
            val x1 = (ceil(sortedRedBlocksWidth[3]) + tolerance).toInt()
            val y1 = 0
            val x2 = (modelSize.width - ceil(sortedRedBlocksWidth[3]) - tolerance).toInt()
            val y2 = modelSize.height.toInt()

            val roi = Mat(warpedImg, Rect(x1, y1, x2 - x1, y2 - y1))

            val img_copy = roi.clone()

            val img_gray = Mat()
            val img_blur_g = Mat()
            val img_blur_m = Mat()
            Imgproc.cvtColor(roi, img_gray, Imgproc.COLOR_RGB2GRAY)   // TODO
            Imgproc.GaussianBlur(img_gray, img_blur_g, Size(5.0, 5.0), 0.0)
            Imgproc.medianBlur(img_gray, img_blur_m, 7)

            val thresh = Mat()
            Imgproc.adaptiveThreshold(
                img_blur_m, thresh, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY_INV, 11, 2.0
            )

            val contours_2 = mutableListOf<MatOfPoint>()
            val hierarchy_2 = Mat()
            Imgproc.findContours(
                thresh,
                contours_2,
                hierarchy_2,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            val max_cnt = contours_2.maxByOrNull { Imgproc.contourArea(it) }
            val max_cnt2f = MatOfPoint2f(*max_cnt!!.toArray())
            val peri = Imgproc.arcLength(max_cnt2f, true)

            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(max_cnt2f, approx, 0.0015 * peri, true)

//                Imgproc.drawContours(roi, listOf(approx), -1, Scalar(255.0, 255.0, 0.0), 2)

            val rect = Imgproc.boundingRect(approx)
            Imgproc.rectangle(roi, rect.tl(), rect.br(), Scalar(0.0, 0.0, 255.0), 2)

            if (rect.width > 0) {
                val glass = roi.submat(rect)
                val glass_copy = glass.clone()

                Imgproc.cvtColor(glass, glass, Imgproc.COLOR_RGB2GRAY)
                val glass_blur = Mat()
                Imgproc.medianBlur(glass, glass_blur, 3)

                val thresh_glass = Mat()
                Imgproc.adaptiveThreshold(
                    glass_blur, thresh_glass, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                    Imgproc.THRESH_BINARY_INV, 17, 4.0
                )

                val kernel_glass =
                    Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
                val glass_closed = Mat()
                Imgproc.morphologyEx(
                    thresh_glass,
                    glass_closed,
                    Imgproc.MORPH_CLOSE,
                    kernel_glass
                )

                // # # # LINE Detection Another Method # # #
                val contours_3 = mutableListOf<MatOfPoint>()
                Imgproc.findContours(
                    thresh_glass,
                    contours_3,
                    hierarchy,
                    Imgproc.RETR_TREE,
                    Imgproc.CHAIN_APPROX_SIMPLE
                )

                val gb_width = glass_copy.width()
                val gb_height = glass_copy.height()


                for (cnt_3 in contours_3) {
                    val rect_2 = Imgproc.boundingRect(cnt_3)

                    if (rect_2.width > (gb_width / 2.5) && rect_2.height < 30 && rect_2.tl().y > (gb_height / 5) && rect_2.tl().y < (gb_height - 50) && rect_2.br().y < (gb_height - 40)) {
                        lineWidth = rect_2.width
                        lineTopLeft = rect_2.tl()
                        lineBottomRight = rect_2.br()
                        lineLength =
                            sqrt((rect_2.width * rect_2.width + rect_2.height * rect_2.height).toDouble())
                    }
                }

                val height = glass_copy.rows()
                val width = glass_copy.cols()
                val center = Point(width / 2.0, height / 2.0)

                val blockPx = (block_width + block_height) / 2.0
                val blockRealMm = 34
                val mmInPixel = blockPx / blockRealMm

                if (flag) {
                    val resCenterX = center.x / mmInPixel
                    val resCenterY = center.y / mmInPixel
                    val resCenter =
                        "(${String.format("%.2f", resCenterX)} mm ${
                            String.format("%.2f", resCenterY)
                        } mm)"
                    glassInfo.add(resCenter)
                }

                Imgproc.circle(glass_copy, center, 4, Scalar(255.0, 0.0, 0.0), -1)  // geo center

                if (lineLength != -1.0) {

                    if (flag) {
                        val resLineLength = lineLength / mmInPixel
                        val resLineCenterX =
                            ((lineTopLeft!!.x + lineBottomRight!!.x) / 2) / mmInPixel
                        val resLineCenterY =
                            ((lineTopLeft!!.y + lineBottomRight!!.y) / 2) / mmInPixel
                        val resLineTopLeftX = lineTopLeft!!.x / mmInPixel
                        val resLineTopLeftY = lineTopLeft!!.y / mmInPixel
                        val resLineBottomRightX = lineBottomRight!!.x / mmInPixel
                        val resLineBottomRightY = lineBottomRight!!.y / mmInPixel

                        val resLineLengthInMm = "${String.format("%.2f", resLineLength)} mm"
                        val resLineCenter = "(${String.format("%.2f", resLineCenterX)} mm ${
                            String.format(
                                "%.2f",
                                resLineCenterY
                            )
                        } mm)"
                        val resLineTopLeft = "(${String.format("%.2f", resLineTopLeftX)} mm ${
                            String.format("%.2f", resLineTopLeftY)
                        } mm)"
                        val resLineBottomRight =
                            "(${String.format("%.2f", resLineBottomRightX)} mm ${
                                String.format("%.2f", resLineBottomRightY)
                            } mm)"

                        glassInfo.add(resLineLengthInMm)
                        glassInfo.add(resLineCenter)
                        glassInfo.add(resLineTopLeft)
                        glassInfo.add(resLineBottomRight)

                    }

                    Imgproc.rectangle(
                        glass_copy,
                        lineTopLeft,
                        lineBottomRight,
                        Scalar(0.0, 0.0, 255.0),
                        2
                    )
                } else if (flag) {
                    glassInfo.add("-")
                    glassInfo.add("-")
                    glassInfo.add("-")
                    glassInfo.add("-")
                }

                val circles = Mat()
                Imgproc.HoughCircles(
                    glass_blur,
                    circles,
                    Imgproc.CV_HOUGH_GRADIENT,
                    1.0,
                    3.0,
                    30.0,
                    10.0,
                    1,
                    15
                )

                var maxContrast = 0.0
                var bestCircleX = 0
                var bestCircleY = 0
                var bestCircleR = 0

                // Draw the detected circles
                if (!circles.empty()) {
                    for (i in 0 until circles.cols()) {

                        val circle = circles[0, i]
                        val circle_x = circle[0].toInt()
                        val circle_y = circle[1].toInt()
                        val circle_r = circle[2].toInt()

                        // Extract the circle region
                        val circleRegionRect = Rect(
                            circle_x - circle_r,
                            circle_y - circle_r,
                            circle_r * 2,
                            circle_r * 2
                        )
                        if (circleRegionRect.x >= 0 && circleRegionRect.y >= 0 &&
                            circleRegionRect.x + circleRegionRect.width < glass_blur.cols() &&
                            circleRegionRect.y + circleRegionRect.height < glass_blur.rows()
                        ) {
                            // Extract the circle region
                            val circleRoi = glass_blur.submat(circleRegionRect)

                            // Calculate the contrast (e.g., using standard deviation)
                            val stdDev = MatOfDouble()
                            Core.meanStdDev(circleRoi, MatOfDouble(), stdDev)
                            val contrast = stdDev.toArray()[0]

                            // Find the best circle that is not a reflection
                            if (lineTopLeft != null) {   // eger line varsa bundan yukarıda olmalı
                                if (circle_y < lineTopLeft!!.y - 10 && circle_x > lineTopLeft!!.x && contrast > maxContrast && circle_x > 30 && circle_x < gb_width - 40 && circle_y > 5 && circle_y < gb_height - 50) {
                                    circleFounded = true
                                    maxContrast = contrast
                                    bestCircleX = circle_x
                                    bestCircleY = circle_y
                                    bestCircleR = circle_r
                                }
                            } else {   // line yoksa koşul contrast sadece
                                if (contrast > maxContrast) {
                                    circleFounded = true
                                    maxContrast = contrast
                                    bestCircleX = circle_x
                                    bestCircleY = circle_y
                                    bestCircleR = circle_r
                                }
                            }

                        }
                    }
                }

                if (flag) {
                    if (!circleFounded) {
                        glassInfo.add("-")
                        glassInfo.add("-")
                    } else {
                        val resCircleX = bestCircleX / mmInPixel
                        val resCircleY = bestCircleY / mmInPixel
                        val resCircleR = bestCircleR / mmInPixel

                        val resCircleLocation =
                            "(${String.format("%.2f", resCircleX)} mm ${
                                String.format("%.2f", resCircleY)
                            } mm)"

                        val resCircleRadius =
                            "${String.format("%.2f", resCircleR)} mm"

                        glassInfo.add(resCircleLocation)
                        glassInfo.add(resCircleRadius)
                    }
                }

                Imgproc.circle(
                    glass_copy,
                    Point(bestCircleX.toDouble(), bestCircleY.toDouble()),
                    bestCircleR,
                    Scalar(0.0, 0.0, 255.0),
                    1
                )


                if (flag) {

                    val resGlassWidth = glass_copy.cols() / mmInPixel
                    val resGlassHeight = glass_copy.rows() / mmInPixel

                    val resGlassWidthInMm =
                        "${String.format("%.2f", resGlassWidth)} mm"
                    val resGlassHeightInMm =
                        "${String.format("%.2f", resGlassHeight)} mm"

                    glassInfo.add(resGlassWidthInMm)
                    glassInfo.add(resGlassHeightInMm)
                }

                Log.d(D_TAG, "Glass is detected")
                return glass_copy  // adaptive glass
            }

        }
    }
    return null
}





