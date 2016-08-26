* In *Scalaz*, `scalaz.Validation`
    * `scalaz/core/src/main/scala/scalaz/Validation.scala`
	* [ScalaDoc](https://oss.sonatype.org/service/local/repositories/releases/archive/org/scalaz/scalaz_2.11/7.2.1/scalaz_2.11-7.2.1-javadoc.jar/!/index.html#scalaz.Validation)



*Trick* Challenge Question

Convert an `Option[Integer]` into `Option[Char]` *only if* the `Integer` is an ASCII code for a capital letter.

'`A`' = 65; '`Z`' = 90

Using only `unit` and `map2`

	!scala
	def unit(a: A): Option
 	def map2[A,B,C](a: Option[A], b: Option[B])
	  (f: (A, B) => C): Option[C]

Convert the `Integer` into its ASCII character

	val sixtyFiveOption: Option[Integer] = Some(65)
	def getCapitalLetterOption(asciiCode: Integer): Option[Char] = 
	  if(asciiCode >= 65 && asciiCode <= 90) Some(asciiCode.toChar)
	  else None
	  
	val capitalAOption: Option[Char] = ???
	  
You must use the `Option[Integer]` as input -- using just `Integer` does not demonstrate the point.	
		
---

	!scala
	val capitalA: Option[Option[Char]] = ???
	
is not a correct answer, but this is the best you can do.

	!scala
	val sixtyFiveOption: Option[Integer] = Some(65)
	def getCapitalLetterOption(asciiCode: Integer): Option[Char] = 
	  if(asciiCode >= 65 && asciiCode <= 90) Some(asciiCode.toChar)
	  else None
	
	val optionGetCapitalLetterOption: Option[Integer => Option[Char]] =
	  unit(getCapitalLetter)
	  
	def merge(sixtyFive: Integer, 
       	    getCapitalLetterOption: Integer => Option[Char]): 
	  Option[Char] = 
	  getCapitalLetterOption(sixtyFive)
	  
	val capitalAOptionOption: Option[Option[Char]] = 
	  map2(sixtyFiveOption, optionGetCapitalLetterOption)(merge)
	  
"Ringed container" lacks `flatMap`.  
This is impossible: `Option[Option[Char]] => Option[Char]`

