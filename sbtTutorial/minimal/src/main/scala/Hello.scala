

// no package necessary

object Hello extends App {

  println("hello")

}


/* syntactic sugar for:
 */

object Hello2 {
  def main(args: Array[String]): Unit = {
    println("hello")
  }
}


// http://www.scala-lang.org/documentation/getting-started.html
