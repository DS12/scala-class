package com.datascience.education.misc.sudoku

import scala.language.postfixOps

object Sudoku {

  /* 
   "Unflatten" an element and return the row index it belongs to.
   0-indexed

   val initial = List((0,5),(1,3),(4,7),(9,6),(12,1),(13,9),(14,5),...

   For example, element (1,3) belongs to row 0.
   */
  def row(element: (Int, Int)): Int = element._1 / 9

  /*
   "Unflatten" an element and return the column index it belongs to.
   0-indexed

   For example, element(1,3) belongs to column 1.
   */
  def column(element: (Int, Int)): Int = element._1 % 9

  /*
   Tiles

   v index 0   
 > 0 0 0 1 1 1 2 2 2
   0 0 0 1 1 1 2 2 2
   0 0 0 1 1 1 2 2 2
   3 3 3 4 4 4 5 5 5
   3 3 3 4 4 4 5 5 5
   3 3 3 4 4 4 5 5 5
   6 6 6 7 7 7 8 8 8  <- index 62
   6 6 6 7 7 7 8 8 8
   6 6 6 7 7 7 8 8 8  <- index 80
                   ^ 
   "Unflatten" an element and return the tile index it belongs to.

   For example, element (1,3) belongs to tile 0.

   */

  def tile(element: (Int, Int)): Int = {
    val j = column(element)
    val tileX = j/3

    val i = row(element)
    val tileY = i/3

    val tile = tileX+3*tileY

    tile
  }


  /* 
   1a
   check rows for uniqueness

   In a valid Sudoku board,
   each row contains only one copy of each digit 1-9.
   In other words, the 9 elements of each row correspond one-to-one with the 9 digits in [1,9].

   Check that each row satisfies this requirement.
   */
  def checkX(board: List[(Int,Int)], next: (Int,Int)): Boolean = {
    def check(b: Boolean, tuple: (Int,Int)): Boolean = {
      b && ((row(tuple) == row(next) && tuple._2 != next._2) || row(tuple) != row(next))

    }

    val c = board.foldLeft(0)((count: Int, _: (Int, Int)) => count+1)


    board.foldLeft(true)(check)
  }

  /*
   TASK 
   check columns for uniqueness

   In a valid Sudoku board,
   each column contains only one copy of each digit 1-9.
   This is the same rule as in `checkX`, but for columns.

   Check that each column satisfies this requirement.
   */
  def checkY(board: List[(Int,Int)], next: (Int,Int)): Boolean = ???

  /*
   TASK 1b
   check tiles for uniqueness
   
   A Sudoku board contains 9 3x3 tiles.
   
   As with each row and each column of the board,
   each tile must contain only one copy of each digit 1-9.

   */
  def checkT(board: List[(Int,Int)], next: (Int,Int)): Boolean = ???
  /*
   TASK 1c
   check that a given position has not been filled
   */
  def notPlayed(board: List[(Int,Int)], index: Int): Boolean = ???

  /*
   TASK 1d
   check that a given position is legal with respect to checkX, checkY, checkT
   */
  def isLegal(board: List[(Int,Int)], next: (Int,Int)): Boolean = ???

  //recursively provide all solutions to puzzle w/ given initial conds
  def sudokuSolve(initial: List[(Int,Int)]): Set[List[(Int,Int)]] = {

    // indices of empty board elements, given initial conditions (pre-filled board)
    val indices: List[Int] =
      (0 until 81) filter { index => notPlayed(initial, index)} toList

    /*
     TASK 1e
     */
    def sudokuIter(indices: List[Int]): Set[List[(Int,Int)]] = ???

    sudokuIter(indices)
  }


  def sudokuAll(index: Int): Set[List[(Int,Int)]] = {
    if (index == -1) Set(List())
    else
      for {
	board <- sudokuAll(index-1)
	k <- 0 until 9
	if isLegal(board, (index,k))
      } yield (index,k)::board
  }

  //plotting util
  def sudokuPlot(board: List[(Int,Int)]): String = {
    val out = Array.ofDim[Int](9,9)
    for {move <- board} out(move._1 / 9)(move._1 % 9) = move._2
    out.map({_.mkString(" ")}).mkString("\n")
  }

  /*
   
   A sample Sudoku board as a List of (position, value) tuples. Positions are in row major format.

   Row-major order
   https://en.wikipedia.org/wiki/Row-major_order
   
   Each element of the list below corresponds to an element in the Sudoku board.
   This board has been "flattened" into a row-major list.  The first element of each tuple is the "flattened" coordinate in the Sudoku board (explained momentarily), and the second element of each tuple is the value itself.
   
   First, let's establish syntax for elements of the board

   Given element M_ij,
   i is the row and j is the column
   
   Our row and column indices are 0-indexed, in contrast to the 1-indexed convention of a mathematical matrix.

   A Sudoku board is 9 by 9 elements.

   (0, 5) is placed at M_00
   (1, 3) is placed at M_01
   (4, 7) is placed at M_04
   (12, 1) is placed at M_13

   Here is partial plot of these four elements, with absent elements of the board filled by 0

   5 3 0 0 7 0 0 0 0
   0 0 0 1 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0
   0 0 0 0 0 0 0 0 0

   You can print all of `initial` by running `SudokuPreview`
   */
  val initial = List((0,5),(1,3),(4,7),(9,6),(12,1),(13,9),(14,5),
    (19,9),(20,8),(25,6),(27,8),(31,6),(35,3),(36,4),
    (39,8),(41,3),(44,1),(45,7),(49,2),(53,6),(55,6),
    (60,2),(61,8),(66,4),(67,1),(68,9),(71,5),(76,8),(79,7),(80,9))

  val initial2 = List((0,5),(1,3),(4,7),      (12,1),(13,9),(14,5),
    (19,9),(20,8),       (27,8),(31,6),(35,3),
    (39,8),       (44,1),(45,7),(49,2),       (55,6),
    (60,2),(61,8),(66,4),       (68,9),(71,5),(76,8),(79,7),(80,9))



}

object SudokuPreview extends App {
  import Sudoku._


  println("find solutions for this Sudoku board:")

  println(sudokuPlot(initial))

  val test: List[(Int, Int)] = (0 until 81).toList.map(d => (d, d))

  val rows = test.map(tuple => (tuple._1, row(tuple)))

  println("rows")
  println(sudokuPlot(rows))

  val columns = test.map(tuple => (tuple._1, column(tuple)))

  println("columns")
  println(sudokuPlot(columns))

  val tiles = test.map(tuple => (tuple._1, tile(tuple)))

  println("tiles")
  println(sudokuPlot(tiles))

}

// TASK 1f
object SudokuSolver extends App {
  import Sudoku._

  println("find solutions for this Sudoku board:")
  println(sudokuPlot(initial))

  val solutionStrings: Set[String] = sudokuSolve(initial).map(board => sudokuPlot(board))

  solutionStrings.foreach { (solution: String) => println(s"solution \n $solution") }



  println("-----------------")



  println("find solutions for this Sudoku board (initial2):")
  println(sudokuPlot(initial2))

  val solutionStrings2: Set[String] = sudokuSolve(initial2).map(board => sudokuPlot(board))

  solutionStrings2.foreach { (solution: String) => println(s"solution \n $solution") }





}


