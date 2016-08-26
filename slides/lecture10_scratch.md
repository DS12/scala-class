---

"In Haskell, the Monoid typeclass is a class for types which have

(1) a single most natural operation for combining values

together with 

(2) a value which doesn't do anything when you combine it with others (this is called the identity element)."

<br />
<br />

[https://wiki.haskell.org/Monoid](https://wiki.haskell.org/Monoid)


---

	!scala
	val op1: FPOption[Int] = Some(5)
	val op2: FPOption[Int] = None

	val oneOrTwo = monoidOption[Int].op(op1, op2)
	println(oneOrTwo)

in `slideCode.lecture10.BasicExamples`


---

# `foldLeft` needs

## the dual monoid

	!scala
	def dual[A](m: Monoid[A]): Monoid[A] = ...

and 

## the endo monoid 

	!scala
	def endoMonoid[A]: Monoid[A=>A] = ...




    val monoidIntMultiplication: Monoid[Int] = 
	  new Monoid[Int] {
        def op(i1: Int, i2: Int): Int = i1 * i2
        val zero: Int = 1
      }


---
# A Monoid for Counting

We want to count the elements of `List[A]`.  No knowledge of `A` should be necessary.

How can we change the list of characters to make reducible with the integer addition monoid?

	!scala
	val chars: List[Char] = 
	  (65 to 75).toList.map(_.toChar)

	val monoidIntAddition: Monoid[Int] = 
	  new Monoid[Int] {
        def op(i1: Int, i2: Int): Int = i1 + i2
        val zero: Int = 0
      }
	  
---

	!scala
	val charMapped: List[Int] = chars.map(_ => 1)

	println("count "+chars)
	println(reduce(charMapped, monoidIntAddition))
	
	// count List(A, B, C, D, E, F, G, H, I, J, K)
    // 11
	
---
# Justifies existence of `foldMap` combinator


---

Monoidal parsing

http://stackoverflow.com/questions/11808539/monoidal-parsing-what-is-it

---

<br />
---
# `foldLeft`

	!scala
	def foldLeft[A, B](as: List[A])
	                  (z: B)
					  (f: (B, A) => B): B = {
      type C = B => B
      val g: A => C = (a: A) => {(b: B) => f(b,a)}

      val endoMonoidC: Monoid[C] = endoMonoid[B]

      val b: B = foldMap(as, dual(endoMonoidC))(g)(z)

      b
    }
  def foldRight[A, B](as: List[A])(z: B)(f: (A, B) => B): B =
    foldMap(as, endoMonoid[B])(f.curried)(z)  // f.curried: Function1[A, Function1[B, B]]

  // endoMonoid[B] : Monoid[B => B]
  // 

  // Folding to the left is the same except we flip the arguments to
  // the function `f` to put the `B` on the correct side.
  // Then we have to also "flip" the monoid so that it operates from left to right.
  def foldLeft[A, B](as: List[A])(z: B)(f: (B, A) => B): B =
    foldMap(as, dual(endoMonoid[B]))(a => b => f(b, a))(z)



---
# Monoid isomorphism

In the opposite direction, you can implement `or` with `and`.

<br />

Because there exist 

* a morphism from the `or` monoid to the `and` monoid

and 

* a morphism from the `and` monoid to the `or` monoid,

## the `and` and `or` monoids are isomorphic.


---

# Another [Monoid homomorphism](https://en.wikipedia.org/wiki/Monoid#Monoid_homomorphisms)

	!scala
	val stopWords = List("the", "be", "to", "at", 
	                     "which", "is", "on", "this", 
						 "it")
	val sentence = "It is critically important 
	                this message remains intact"

* Use the monoid of integers under addition
* Count the strings in this list that are not [stop words](https://en.wikipedia.org/wiki/Stop_words)

	
---

"morphism" from the monoid of strings under concatenation to the monoid of integers under addition

	!scala
	def filterStopWords(
	    word: String, stopWords: List[String]): Int =
      if(stopWords.contains(word)) 0
      else 1

	def filteredWordCount(
	    words: List[String], 
		stopWords: List[String]): Int =
      foldMap(words, monoidIntAddition)
	         (filterStopWords(_, stopWords))
<br />
<br />
in `slideCode.lecture10.Homomorphism`

---

	!scala
	val stopWords: List[String] = ...
	val sentence = "It is critically important 
	                this message remains intact"

	val words: List[String] = 
	  sentence.split(" ").toList

	val importantWordsCount: Int = 
	  filteredWordCount(words, stopWords)

	println(importantWordsCount)
	// prints 6

<br />
<br />
in `slideCode.lecture10.HomomorphismExample`


---

	!scala
	object FoldList {
  	  def reduce[A](listA: List[A], 
	                monoidA: Monoid[A]): A = ...

      def foldMap[A,B](listA: List[A], 
	                   monoidB: Monoid[B])
					   (f: Function1[A,B]): B = {
        val listB: List[B] = listA.map(a => f(a))
        reduce(listB, monoidB)

    }

---

# Answer
## Which Option from the List will `reduce` return?

	!scala
	val listOptions = List(Some(6), None, Some(8), 
	                       Some(9), None, Some(11))

	val foldedByDual: FPOption[Int] = 
	  listOptions.foldLeft(monoidOptionDual[Int].zero)
	                      (monoidOptionDual[Int].op)

	println(foldedByDual)
	// prints "Some(11)"

<br />
<br />
in `slideCode.lecture10.BasicExamples`
