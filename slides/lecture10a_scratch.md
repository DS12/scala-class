[https://github.com/twitter/algebird/issues/128](https://github.com/twitter/algebird/issues/128)


#[HyperLogLogMonoid](http://twitter.github.io/algebird/#com.twitter.algebird.HyperLogLogMonoid)



in `slideCode.lecture10a.HyperLogLogUsage`

[tutorial](https://github.com/twitter/algebird/wiki/HyperLogLog)



---
# Challenge question

Given the type inside `CMSMonoid` and the generic letter `K`, what is the significance of the mapping from `tweet` to `tweet.userId`?

	!scala
	val cmsMonoid: CMSMonoid[Int] = ...

	val userIds: Seq[Int] = 
	  tweets.map { tweet => tweet.userId }

	// cmsMonoid.create(data: Seq[K]): CMS[K]`
	val sketch: CMS[Int] = cmsMonoid.create(userIds)

---



---
# Answer

User ID is the key of this key-value aggregation.  The value is not used for anything and disappears.

`CMSMonoid[Int]` can't combine two `Tweet`s.



----
Producer {
	def flatMap[U](fn: (T) ⇒ TraversableOnce[U]): Producer[P, U]
	}
Only use this function if you may return more than 1 item sometimes.


----

---
# Summingbird

Our Summingbird "pipeline" will begin with a [`Source`](https://github.com/twitter/summingbird/wiki/Core-concepts#source),

aggregate to a  [`Store`](https://github.com/twitter/summingbird/wiki/Core-concepts#store), 

and terminate in a [`Sink`](https://github.com/twitter/summingbird/wiki/Core-concepts#sink).

<br />
<br />
<br />

Our "pipeline" is stored in a [`Plan`](https://github.com/twitter/summingbird/wiki/Core-concepts#plan) -- description rather than evaluation.


---

The implementations and type signatures of `Source`, `Store`, and `Sink` depend on which [Platform](https://github.com/twitter/summingbird/wiki/Core-concepts#platform) is used.
<br />
<br />

[Storm Platform](http://twitter.github.io/summingbird/#com.twitter.summingbird.storm.Storm) and [Scalding Platform](http://twitter.github.io/summingbird/#com.twitter.summingbird.scalding.Scalding) are the two primary choices.
<br />
<br />

For design and learning we use the In-Memory Platform: 
[com.twitter.summingbird.memory.Memory](http://twitter.github.io/summingbird/#com.twitter.summingbird.memory.Memory)

---
[In-Memory Platform](http://twitter.github.io/summingbird/#com.twitter.summingbird.memory.Memory) and Source

	!scala
	val tweets: Seq[Tweet] = generateTweets(1024)

	val platform = new Memory

	val tweetProducer: Producer[Memory, Tweet] = 
	  Memory.toSource(tweets)
	
<br />
<br />

in `slideCode.lecture10a.CountTweets` and `slideCode.lecture10a.CountTweetsExample`

---
Converting from [`Producer`](http://twitter.github.io/summingbird/#com.twitter.summingbird.Producer) to [`KeyedProducer`](http://twitter.github.io/summingbird/#com.twitter.summingbird.KeyedProducer)

	!scala
	val tweetProducer: Producer[Memory, Tweet] = ...

	val separatedKeys: Producer[Memory, (Int, Tweet)] =
      tweetProducer.map { (tweet: Tweet) => 
	    (tweet.userId, tweet) }
	
	val kp: KeyedProducer[Memory, Int, Tweet] =
      Producer.toKeyed(separatedKeys)

	val kp2: KeyedProducer[Memory, Int, Int] =
      kp.mapValues { (tweet: Tweet) => 1 }

---
Reduction / Storage with an implicit Monoid / Semigroup -- integer addition

Tutorial session exercise of the `mapMergeMonoid` from listing 10.1 is relevant.

	!scala
	val store: MutableMap[Int, Int] = 
	  MutableMap[Int,Int]()

	// def sumByKey(store: P.Store[K, V])
	//             (implicit semigroup: Semigroup[V]): 
	//			   Summer[P, K, V]
	val summed: Summer[Memory, Int, Int] = 
	  kp2.sumByKey(store)



<br />
<br />
<br />
<br />

[Mutable Map](http://www.scala-lang.org/api/2.11.8/#scala.collection.mutable.Map)

---

Omitting the rest of the pipeline, the `Store` has aggregated counts of Tweets by user ID.

	store = Map(17 -> 8, 11 -> 5, 2 -> 4, 5 -> 4, 
	            14 -> 5, 4 -> 1, 13 -> 3, 16 -> 2, 
				7 -> 3, 10 -> 2, 1 -> 4, 19 -> 4, 
				18 -> 2, 9 -> 3, 3 -> 3, 12 -> 4, 
				6 -> 2, 15 -> 2, 0 -> 3)

---

# Count-Min Sketch in Summingbird

Our prior usage example of the Count-Min Sketch Monoid was not suitable for production.

	!scala
	// cmsMonoid.create(data: Seq[K]): CMS[K]`
	// Creates a sketch out of multiple items.
	val sketch: CMS[Int] = cmsMonoid.create(userIds)

<br />
<br />
<br />

`cmsMonoid.create` simplifies much into a one-liner.  

Instead of using this advanced Monoid in this simple way, we will use Summingbird.

With Summingbird, our Monoid will be closer to production-ready.

---

	!scala
	val cmsMonoid: CMSMonoid[Int] = ...
	
	...
	
	val keyedTweetProducer: 
	  KeyedProducer[Memory, Int, Tweet] =
      Producer.toKeyed(separatedKeys)

---

	!scala
	val keyedCount: 
	  KeyedProducer[Memory, Int, CMS[Int]] =
      keyedTweetProducer.mapValues 
	    { (tweet: Tweet) => cmsMonoid.create(1) }

Equivalent to

	!scala
	// def create(item: K): CMS[K] 
	// Creates a sketch out of a single item.
	val sketch1: CMS[Int] = cmsMonoid.create(4)
	val sketch2: CMS[Int] = cmsMonoid.create(4)
	val sketch3: CMS[Int] = cmsMonoid.create(6)

---

	!scala
	val summed: Summer[Memory, Int, CMS[Int]] =
      keyedCount.sumByKey(store)(cmsMonoid)

---


	exact and Count Min Sketch counts
	user 0 exact count Some(216) 
	       approx count Some(216) equal true
	user 1 exact count Some(182) 
	       approx count Some(182) equal true
	user 2 exact count Some(200) 
	       approx count Some(200) equal true
	user 3 exact count Some(195) 
	       approx count Some(195) equal true
	user 4 exact count Some(196) 
	       approx count Some(196) equal true
	...
# [Summingbird](https://github.com/twitter/summingbird)

"Summingbird is a library that lets you write MapReduce programs that look like native Scala or Java collection transformations and execute them on a number of well-known distributed MapReduce platforms, including Storm and Scalding."

<br />
<br />

* Uses simple and complex monoids from Algebird
* I believe it to be the simplest way to make Algebird examples that resemble production usage
* Summingbird uses functional patterns to a greater extent than Storm - [source](https://youtu.be/U_0wmpasfXA?t=204)


---

Why cover Summingbird?

* The previous lecture on Monoids was fairly distant from production use.

* Without demonstration using Scalding, Storm, or Summingbird, this lecture on Algebird would be similarly distant from production use.


[Summingbird: streaming portable map-reduce](https://www.youtube.com/watch?v=t1FZ4q0cYfY)

[Summingbird core concepts](https://github.com/twitter/summingbird/wiki/Core-concepts)

[Summingbird: A Streaming Map-Reduce API for Storm, Hadoop, & More](https://www.youtube.com/watch?v=U_0wmpasfXA)
