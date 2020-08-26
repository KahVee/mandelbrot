package main

import main._

class Zoom(val zoomLevel: Double) {

  val maxIterations = math.floor(zoomLevel*zoomLevel*zoomLevel+100).asInstanceOf[Int]

  val constant = 1.25/(math.exp(GUI.zoomLevel))

  val localRMin = zoomPoint._1 - constant
  val localRMax = zoomPoint._1 + constant

  //screen space y is "reversed", it increases downwards
  val localIMin = zoomPoint._2 + constant
  val localIMax = zoomPoint._2 - constant

  def scaleToMandelbrotSpaceR(pixelR: Int) = {
    (localRMax - localRMin) * (pixelR.toDouble / windowWidth.toDouble) + localRMin
  }

  def scaleToMandelbrotSpaceI(pixelI: Int) = {
    (localIMax - localIMin) * (pixelI.toDouble / windowHeight.toDouble) + localIMin
  }
}