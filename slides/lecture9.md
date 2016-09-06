
#Lecture 9: Parsing

---

In this chapter, we develop a top-down, LL parser that can detect context-sensitive grammars.

---
# Parsing

> A parser is a software component that takes input data (frequently text) and builds a data structure – often some kind of parse tree, abstract syntax tree or other hierarchical structure – giving a structural representation of the input, checking for correct syntax in the process.

[Wikipedia](https://en.wikipedia.org/wiki/Parsing#Computer_languages)

---

#`Parser[+A]`

	!scala
	type Parser[+A] =
	  Location => XorErrors[String, (A, Int)]

<br />
<br />

Developed from the same functional design concepts that gave us:

	!scala
	type Rand[A] = RNG => (A, RNG)


---

# Input to `Parser[+A]`

	!scala
	case class Location(
	  input: String, offset: Integer) {
	  ...
	}


<br />
<br />

A `Parser[+A]` will scan its input from a given offset.

`Location("foobar", 3)` -- a `Parser` will look at "bar".

<br />

This contrasts with an alternative approach that makes `offset` unnecessary: cutting down the input string.  Then the `Parser`'s feedback is less informative.

.notes: Alternative - cutting off the consumed characters of the string and passing along the tail. The first part of the string is lost if an error occurs down the line.


---

# Output from `Parser[+A]`

Left: Parser rejects input

Right: Parser accepts input

	!scala
	XorErrors[String, (A, Int)]

	// equivalent to

	Xor[NonEmptyList[String], (A, Int)]

---

# Left: Parser rejects input

Inside `Left`:

`NonEmptyList[String]` accumulates errors.  

These `String`s should explain why `Parser[+A]` rejected its input.


---


# Right: Parser accepts input
Inside `Right`:

`(A, Int)` holds a *token* of type `A` and the number of characters consumed to create this token.

We will develop various types of *token*.



---
# A `Parser` Primitive

Here, the *token* is a `String`.

	!scala
	def string(detect: String): Parser[String] =
	  (loc: Location) => {
      val matches: Boolean =
        detect.regionMatches(0, loc.input,
		  loc.offset, detect.length())
      if(matches)
        Right((detect, detect.length()))
      else
        Left(NonEmptyList(
		  s"$detect not in ${loc.input}
		  at offset ${loc.offset}"
		  ))
    }



---


# `run`

Our `Parser`s require a `Location` as input.  This wraps the input in a `Location`.

	!scala
	def run[A](parserA: Parser[A])(input: String):
	  XorErrors[String, (A, Int)] =
      parserA(Location(input,0))


---

	!scala
	val detectFoo: Parser[String] = string("foo")
	val document = "foobar"
	val resultFoo:
	  XorErrors[String, (String, Int)] =
	  run(detectFoo)(document)

	// `parserResultToString`
	// formats the output `Xor` nicely
	println(parserResultToString(resultFoo))


---

	[info] Running slideCode.lecture9.SimpleParser...
	document
	foobar
	detect 'foo'
	Right: Accept.  token tree = foo  |
	                chars consumed = 3

<br />

We need an "end of field" Parser; equivalent to the `$` or `\z` regular expression.

---
# Compatibility with Scala's [`Regex`](http://www.scala-lang.org/api/2.11.8/#scala.util.matching.Regex) type

	scala> "\\z".r
	res1: scala.util.matching.Regex = \z

	scala> "\\z".r.regex
	res2: String = \z

---
# Compatibility with Scala's `Regex` type

	!scala
	// input example: "\\z".r
	def regex(r: Regex): Parser[String] =
      string(r.regex)

`regex` constructs a `Parser[String]` that will accept a given Scala `Regex`

	!scala
	val eof: Parser[String] =
      regex("\\z".r)

Now we have an "end of field" Parser, equivalent to "$"

---
# How to combine `detectFoo` and `eof`?

	!scala
	val detectFoo: Parser[String] = string("foo")
	val eof: Parser[String] = regex("\\z".r)

---
# `product`

`product` puts two parsers in sequence.  

	!scala
	def product[A,B](parserA: Parser[A],
	                 parserB: => Parser[B]):
	  Parser[(A,B)] = {
      def f(a: A, b: => B): (A,B) = (a,b)
      map2(parserA, parserB)(f)
    }


If both parsers accept their input, they will produce tokens `A` and `B`.

`Parser[A]` consumes as much as it needs to, then sets the `offset` for `Parser[B]`.

<br />
<br />

`map2` and its dependencies will be shown later.

---
	!scala
	val detectFooEOF = product(detectFoo, eof)

	val resultFooEOF = run(detectFooEOF)(document)

	println(parserResultToString(resultFooEOF))



---
# Rejection

	document
	foobar
	detect 'foo' with end-of-field
	Left: Reject.
	      Parse errors: \z not in foobar at offset 3

---
# Combinators

	!scala
	def flatMap[A,B](parserA: Parser[A])
	  (aParserB: A => Parser[B]): Parser[B] = ...

	def map[A,B](parserA: Parser[A])
	            (f: A => B): Parser[B] = ...

	def map2[A,B,C](parserA: Parser[A],
	                parserB: => Parser[B])
					(f: (A,=>B)=>C): Parser[C] = ...

---
# Analogies to `Rand`

Recall that `unit` for `Rand` always generated the same "random" value.

	!scala
	def unit[A](a: A): Rand[A] =
      rng => (a, rng)

Analogously, `unit` for `Parser` always *accepts*.

	!scala
	def succeed[A](a: A): Parser[A] =
      (loc: Location) =>  Right((a, 0))

	def unit[A](a: A): Parser[A] = succeed(a)

---
# Analogies to `Rand`

Recall that with the use of the `Rand` combinators,

`RNG` was passed *implicitly*.  One fewer place to make an error -- mishandling `RNG`.

	!scala
	type Rand[A] = RNG => (A, RNG)

---

In `Parser`,

`Location(input, offset)` is passed implicitly.

	!scala
	type Parser[+A] =
	  Location => XorErrors[String, (A, Int)]

The right side of the `Xor` is used to increment the `Location` -- consuming characters of the input string.


	!scala
	def advanceParserResult[A](
	  xor: XorErrors[String,(A,Int)], consumed: Int):
      XorErrors[String, (A, Int)] = ...

---

We mentioned earlier:

"`Parser[A]` consumes as much as it needs to, then sets the `offset` for `Parser[B]`."

	!scala
	def product[A,B](parserA: Parser[A],
	                 parserB: => Parser[B]):
	  Parser[(A,B)] = {
      def f(a: A, b: => B): (A,B) = (a,b)
      map2(parserA, parserB)(f)
    }

Do you see `Location` or `offset` anywhere in here?  It is passed *implicitly*.

---
# or

	!scala
	def or[A](p1: Parser[A], p2: => Parser[A]):
      Parser[A] = ...

`or` is our first clue that we are building an *LL Parser*, for the *leftmost* accepted derivation of the syntax tree.

We give priority to the left input of `or`.


---
# Tokens

	!scala
	sealed trait Alphabet

	case class X(nested: Alphabet) extends Alphabet
    case class Y(nested: Alphabet) extends Alphabet
    case object Z extends Alphabet



---
# Grammar

"Start" -> X

X -> xY

Y -> yX | yZ

Z -> z

##Accepted:

"xyz"         into         `X(Y(Z))`

"xyxyxyz"  into `X(Y(X(Y(X(Y(Z))))))`

"xyxyxyxyxyz"  into `X(Y(X(Y(X(Y(X(Y(X(Y(Z))))))))))`

##Rejected:

"xxxyyyz"

"xyyyz"

(even though the `X`, `Y` and `Z` tokens can contain these strings)

---



	document: xyxyxyxyz
	Right: Accept.  
	  token tree = X(Y(X(Y(X(Y(X(Y(Z))))))))  
	  chars consumed = 9

	------------

	document: xyyyxyz
	Left: Reject.
	  Parse errors:
	    x not in xyyyxyz at offset 2
		z not in xyyyxyz at offset 2

---



# Another grammar, another set of Tokens

	!scala
	sealed trait Alphabet

	case class AC(nested: Alphabet) extends Alphabet {
      override def toString = "A"+nested.toString+"C"
	}
	case object B extends Alphabet {
      override def toString = "B"
	}



---
# Grammar

"Start" ->  aBc

B -> aBc | b

##Accepted:

"abc" into `AC(B)`

"aabcc" into `AC(AC(B))`

##Rejected:

"aaabcc"

"aabbcc"

---
# More prerequisite combinators

	!scala
	// Sequences two parsers,
	// ignoring the result of the first.
	def skipL[B](p: Parser[Any], p2: => Parser[B]):
	  Parser[B] =
      map2(p, p2)((_,b) => b)

	// Sequences two parsers,
	// ignoring the result of the second.
	def skipR[A](p: Parser[A], p2: => Parser[Any]):
	  Parser[A] =
      map2(p, p2)((a,_) => a)

---
# More prerequisite combinators

	!scala
	def surround[A](left: Parser[Any],
	                right: Parser[Any])
	               (middle: => Parser[A]): Parser[A] =
      skipL(left, skipR(middle, right))

	// necessary for "aBc"

---

Parsing without EOF at end

	[info] Running slideCode.lecture9.ABC
	document: abc

	Right: Accept.  token tree = ABC  |
	       chars consumed = 3

	---------------------------
    document: abcccc
    Right: Accept.  token tree = ABC  |
	       chars consumed = 3


---

Parsing with EOF at end

    document: abcccc
    Left: Reject. Parse errors: \z not in abcccc
	              at offset 3

	---------------------------
    document: aaabccc
    Right: Accept.  token tree = AAABCCC  |
	                chars consumed = 7

---
# Conclusion

* We've attempted to emphasize how more complex grammars require more complex combinators

* Just as complex grammars build on simple grammars, complex combinators build on simple combinators

---
# Chapter comments

* The chapter is primarily about functional design

* It is a difficult first introduction to parsing and grammars

* `Commiting` is an important feature we have omitted.  It gives the user of the parser control over backtracking.

    * Our implementation always backtracks, when possible.
	* Sometimes its advantageous to block backtracking, and reject.


---

Our simplified implementation hard-wires a `Parser` as:

	!scala
	type Parser[+A] =
	  Location => XorErrors[String, (A, Int)]

The book's implementation is considerable more complicated.

In the book, the type of `Parser` changes frequently in the chapter to demonstrate top-down reasoning about possible implementations, and algebraic design.  Different `Parser[+A]` inputs and outputs are explored.

---

#Homework

Read Chapter 10 of _Functional Programming in Scala_.
