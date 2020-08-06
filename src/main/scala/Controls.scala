package main

import main._
import scalafx.Includes._
import scalafx.stage.Stage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox,HBox}
import scalafx.scene.control.{Slider, TextField, TextFormatter, Button}
import scalafx.scene.text.{Text, Font}
import scalafx.beans.property.{StringProperty, DoubleProperty}

import javafx.util.converter.FormatStringConverter
import java.text.NumberFormat
import java.util.ResourceBundle.Control
import scalafx.geometry.Pos

object ControlPanel {
	val stage = new Stage {
		scene = new Scene {
			content = new VBox (
				sliderConstructor("R", changeColorFunction(cr), 0.001, 0.5, 0.05, 100),
				sliderConstructor("G", changeColorFunction(cg), 0.001, 0.5, 0.05, 100),
				sliderConstructor("B", changeColorFunction(cb), 0.001, 0.5, 0.05, 100),
				new HBox(
					new Button("Generate") {
						onAction = e => GUI.generate()
					}
				)
			)            
		}
	}
		
	def changeColorFunction(c: ColorChanger): (Double) => Unit = {
		v => c.value = v
	}

	def sliderConstructor(label: String, function: (Double) => Unit, min: Double, max: Double, default: Double, sliderWidth: Int): HBox = {
		def doFunction(v: Double) = {
			function(v)
		}

		val slider = new Slider(min, max, default){
			prefHeight = 30
			maxWidth = sliderWidth
			onMouseClicked = e => doFunction(value.toDouble)
			onMouseDragged = e => doFunction(value.toDouble)
		}

		val textField = new TextField {
			maxWidth = 60
			//binds value of slider to its label
			val numberFormat = NumberFormat.getInstance()
			numberFormat.setMaximumFractionDigits(3)
			numberFormat.setMinimumFractionDigits(3)
			textFormatter = new TextFormatter(new FormatStringConverter[Number](numberFormat)) {
					value <==> slider.value
			}
		onAction = e => doFunction(slider.value.toDouble)
		}

		val text = new VBox(
				new Text {
				text = label
				font = Font.font(20)
			}
		) { 
			prefWidth = label.length*20 
			alignment = Pos.CENTER_LEFT
		}

		new HBox(text, slider,textField)
		}  

	def zoomSliderConstructor = {
		def changeZoomLevel(v: Double) = {
			GUI.zoomLevel = v
			GUI.generate()
		}
		
		val slider = new Slider(0.005, 30, 1){
			maxWidth = 200
			onMouseDragged = e => changeZoomLevel(value.toDouble)
			onMouseClicked = e => changeZoomLevel(value.toDouble)
		}
	
		val textField = new TextField {
			maxWidth = 60
			//binds value of slider to its label
			val numberFormat = NumberFormat.getInstance()
			numberFormat.setMaximumFractionDigits(3)
			numberFormat.setMinimumFractionDigits(3)
			textFormatter = new TextFormatter(new FormatStringConverter[Number](numberFormat)) {
				value <==> slider.value
			}

			onAction = e => changeZoomLevel(slider.value.toDouble)
		}
		new HBox(slider,textField)
	}
}

//Not used atm, used if multithreading is enabled
/*
class SceneUpdater extends Runnable {
	def run(): Unit = {
		try {
			GUI.updateScene()
		} catch {
				// We've been interrupted: no more messages.        
				case e: InterruptedException => println("Interrupted")
		}
	}
}
*/