package com.datascience.education.tutorial.lecture5


object SineExample extends App {
  import Sine._
  import breeze.linalg._
  import breeze.plot._

  sine.print(32)

  val f = Figure()
  val p = f.subplot(0)
  val x = linspace(0.0,2.5*math.Pi, 16)

  val out: List[Double] = sine.toListFinite(16)

  p+= plot(x, out)
  p+= plot(x, out, '.')

  p.xlabel = "theta radians"
  p.ylabel = "sin(theta)"

  f.saveas("sine_wave.png")

  println("done plotting")

  

}


object StepperExample extends App {
  import Stepper._

  import breeze.linalg._
  import breeze.plot._


  val stepperOut = stepperSine.toListFinite(32)

  val f = Figure()
  val p2 = f.subplot(0)
  val x2 = linspace(0.0,2.5*math.Pi, 32)

  p2 += plot(x2, stepperOut)
  p2 += plot(x2, stepperOut, '.')

  p2.xlabel = "theta radians"
  p2.ylabel = "sin(theta)"

  f.saveas("sine_wave_stepper.png")

  println("done plotting")

}

object InterpolationExample extends App {

  import Interpolation._

  import breeze.linalg._
  import breeze.plot._

  val linearInterpolatedOut = linearInterpolated.toListFinite(32)

  println(linearInterpolatedOut)

  val f = Figure()
  val p3 = f.subplot(0)
  val x3 = linspace(0.0,2.5*math.Pi, 32)

  p3 += plot(x3, linearInterpolatedOut)
  p3 += plot(x3, linearInterpolatedOut, '.')

  p3.xlabel = "theta radians"
  p3.ylabel = "sin(theta)"

  f.saveas("sine_wave_linear_interpolation.png")

  println("done plotting")


}
