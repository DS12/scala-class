
#Lecture 6a: State Monad

---

`cats.data.State` allows us to pass additonal state around as part of a computa on. We define State in- stances represen ng atomic opera ons on the state, and thread them together using map and flatMap. In this way we can model “mutable” state in a purely func onal way without using muta on.

---

Boiled down to its simplest form, instances of State[S, A] represent func ons of type S => (S, A). S is the type of the state and and A is the type of the result.


---

In other words, an instance of State is a combina on of two things: • atransforma onfromaninputstatetoanoutputstate;
• acomputa onofaresult.
We can “run” our monad by supplying an ini al state. State provides three methods—run, runS, and runA— that return different combina ons of state and result as follows:
// Get the state and the result:
val (state, result) = a.run(10).value // state: Int = 10
// result: String = The state is 10
// Get the state, ignore the result:
val state = a.runS(10).value // state: Int = 10
// Get the result, ignore the state:
val result = a.runA(10).value
// result: String = The state is 10

---

#Composing and Transforming State

As we’ve seen with Reader and Writer, the power of the State monad comes from combining instances.

The map and flatMap methods thread the State from one instance to another. Because each primi ve instance represents a transforma on on the state, the combined instance represents a more complex transforma on.


---


case class State[S,A](run: S => (S,A)) {
  def flatMap[B](f: A => State[S,B]): State[S,B] =
    State { s1 =>
      val (s2, a) = run(s1)
      f(a).run(s2)
    }
  def map[B](f: A => B): State[S,B] =
    State { s1 =>
      val (s2, a) = run(s1)
      (s2, f(a))
    }
}

---

val aheadS:  Int => (Int, Int) =
  x => { println(s"fwd: (s,a) = (${x+1},${x})")
         (x+1,x) }
val behindS: Int => (Int, Int) =   
  x => { println(s"rev: (s,a) = (${x-1},${x})")
         (x-1,x) }
val s = State[Int, Int] {aheadS}
s.run(3)
//res0: (Int, Int) = ???
s.map(_+1).run(3)
//res1: (Int, Int) = ???

---

def reverse(n: Int) = n match {
  case i if i > 1 => State[Int, Int] {aheadS}
  case _ => State[Int, Int] {behindS}
}
s.run(2)
//res0: (Int, Int) = (3,2)
s.flatMap(reverse).run(2)
//res1: (Int, Int) = ???

---

def runN[S, A](its: Int)(st: State[S, A]): State[S, A] =
  State((s0: S) =>
    (1 to its).foldLeft(st.run(s0)){
      case ((si: S, ai: A), _: Int) => st.run(si)
    }
  )
runN(3)(s).run(0)
//res0: (Int, Int) = ???

---

runN(3)(s).flatMap(reverse).run(0)
//res0: (Int, Int) = ???
runN(3)(s).flatMap(reverse).run(-10)
//res0: (Int, Int) = ???

---

#`get`, `set`, and `pure`

def get[S]: State[S, S] =
  State(s => (s, s))
def pure[S, A](a: A): State[S, A] =
  State(s => (s, a))
def set[S](s: S): State[S, Unit] =
  State(_ => (s, ()))

---

!scala
val foo = get[Int]
foo.run(1)
//res0: (Int, Int) = ???
foo.run(2)
//res1: (Int, Int) = ???
val baz: State[Int, Int] = pure(7)
baz.run(4)
//res2: (Int, Int) = ???

---

!scala
val bar = set(1)
bar.run(1)
//res0: (Int, Unit) = (1,())
bar.run(2)
//res1: (Int, Unit) = ???
bar.flatMap(_ => foo).run(2)
//res2: (Int, Int) = ???

---

!scala
val hi = bar.flatMap(_ => pure("hi"))
hi.map(_++" there").run(2)
//res0: (Int, String) = (1,hi there)

---

#Imperative State

implicit class StateExtras[S,A](s: State[S,A]) {
  def andThen[B](x: State[S,B]): State[S,B] =
    s.flatMap { _ => x }
}


val six = set(6)
six.run(4)
//res0: (Unit, Int) = ((),6)

val times7: State[Int,Unit] = State { x => (x*7,()) }
val print: State[Int,Unit] =
  State { x => (x,println(s"x = ${x}")) }

---

val sixBy7: State[Int, Unit] =
  six andThen print andThen times7 andThen print
sixBy7.run(54)
//???

---

We'll do the last examples in Cats. The Cats version of `State` uses an `Eval` monad:

    !scala
    import cats._, cats.std.all._
    import cats.syntax.eq._, cats.data.State
    val s = State[Int, Int] {aheadS}
    //s: cats.data.State[Int,Int] = cats.data.StateT@6664d3e9
    s.run(1)
    //res0: cats.Eval[(Int, Int)] = cats.Eval$$anon$8@397cae2
    s.run(1).value
    //res1: (Int, Int) = (2,1)
<br>
We'll discuss Eval in a moment. For now we can assume that the Cats version behaves like ours, but for the extra step of calling `value` at the end.

---

val a = State[Int, String] { state => (state, s"The state is $state")
}
// a: cats.data.State[Int,String] = cats.data.StateT@4fc60cd5

val step1 = State[Int, String] { num => val ans = num + 1
(ans, s"Result of step1: $ans")
}
// step1: cats.data.State[Int,String] = cats.data.StateT@2100d3cd
val step2 = State[Int, String] { num => val ans = num * 2
(ans, s"Result of step2: $ans")
}
// step2: cats.data.State[Int,String] = cats.data.StateT@25728404
val both = for { a <- step1
b <- step2
} yield (a, b)
// both: StateT[Eval,Int,(String, String)] = cats.data.StateT@3b70d9da
val (state, result) = both.run(20).value

---

#Example: Stacks

    !scala
    type Stack = List[Int]
    def push(x: Int) = State[Stack, Unit] {
      case xs => (x :: xs, ())
    }
    val pop = State[Stack, Int] {
      case x :: xs => (xs, x)
      case Nil     => sys.error("empty")
    }

---

    !scala
    val l = List(2, 1)
    //l: List[Int] = List(2, 1)
    push(3).run(l).value
    //res7: (Stack, Unit) = (List(3, 2, 1),())
    pop.run(l).value
    //res8: (Stack, Int) = (List(1),2)

---

    !scala
    push(3).flatMap( _ => pop.map(a => a+10))
    //res9: StateT[Eval,Stack,Int] = StateT@494f7a8
    res9.run(l).value
    //res10: (Stack, Int) = (List(2, 1),13)

---

    !scala
    def stackManip: State[Stack, Int] =
      for {
        _ <- push(3)
        a <- pop
      } yield(a)

---

    !scala
    stackManip.run(l).value
    //res11: (Stack, Int) = (List(2, 1),3)

---

    !scala
    def push1(x: Int): State[Stack, Unit] =
      for {
        xs <- State.get[Stack]
        s <- State.set(x :: xs)
      } yield s
   <br>
   <br>
    scala> push2(3).run(l).value
    //res12: (Stack, Unit) = (List(3, 2, 1),())

---

#Exercise

  Work through this on paper using the substitution rule.

    !scala
    def push2(x: Int): State[Stack, Unit] =
      State.get[Stack]
           .flatMap( xs => State.set(x :: xs))
    push2(3).run(l).value
    //res13: (Stack, Unit) = (List(3, 2, 1),())

---



---

    !scala
    def set[S](s: S): State[S, Unit] =
      State(_ => (s, ()))
    def get[S]: State[S, S] =
      State(s => (s, s))
    def flatMap[B](f: A => State[S, B]): State[S, B] =
      State(s => {
        val (a, s1) = run(s)
        f(a).run(s1)
      })
    def push2(x: Int): State[Stack, Unit] =
      State.get[Stack]
           .flatMap( xs => State.set(x :: xs))

---

    !scala
    val pop1: State[Stack, Int] =
      for {
        s <- State.get[Stack]
        (x :: xs) = s
      } yield x
   <br>
   <br>
    scala> pop1.runS(l).value
    res14: Stack = List(4, 3, 2, 1)

---

    !scala
    val pop2: State[Stack, Int] =
      State.get[Stack]
           .flatMap(s => {
              val (x::xs) = s
              pure(x) //stack is stale
              })
   <br>
   <br>
    scala> pop2.run(l).value
    res15: (Stack, Int) = (List(4, 3, 2, 1),4)

---

    !scala
    val pop3: State[Stack, Int] =
      State.get[Stack].flatMap(s => {
        val (x::xs) = s
        State.set(xs).map(_ => x)

  })

   <br>
   <br>
    scala> pop3.run(l).value
    res16: (Stack, Int) = (List(3, 2, 1),4)

---


#Eval Monad

`cats.Eval` is a monad that allows us to abstract over different models of evaluation. We typically hear of two such models: eager and lazy. Eval throws in a further dis nc ontiof memoized and unmemoized to create three models of evaluation:
• now—evaluatedonceimmediately(equivalenttoval);
• later—evaluatedoncewhenvalueisneeded(equivalenttolazyval); • always—evaluatedevery mevalueisneeded(equivalenttodef).

---

What do these terms mean?

Eager computations happen immediately, whereas lazy computations happention access.

For example, Scala vals are eager definitions. We can see this using a computation with a visible side-effect. In the following example, the code to compute the value of x happens eagerly at the defini on site. Accessing x simply recalls the stored value without re-running the code.



val x = { println("Computing X") 1+1
}
// Computing X
// x: Int = 2
x // first access // res0: Int = 2
x // second access // res1: Int = 2

---

By contrast, defs are lazy and not memoized. The code to compute y below is not run un l we access it (lazy), and is re-runtion every access (not memoized):
def y = { println("Computing Y") 1+1
}
// y: Int
y // first access // Computing Y
// res2: Int = 2
y // second access // Computing Y
// res3: Int = 2

---

Last but not least, lazy vals are eager and memoized. The code to compute z below is not run un l we access it for the first  me (lazy). The result is then cached and re-used on subsequent accesses (memoized):

lazy val z = { println("Computing Z") 1+1
}
// z: Int = <lazy>
z // first access // Computing Z
// res4: Int = 2
z // second access // res5: Int = 2

---

#Eval’s models of evaluation
Eval has three subtypes: Eval.Now, Eval.Later, and Eval.Always. We construct these with three construc- tor methods, which create instances of the three classes and return them typed as Eval:
import cats.Eval
// import cats.Eval
val now = Eval.now(1 + 2)
// now: cats.Eval[Int] = Now(3)
val later = Eval.later(3 + 4)
// later: cats.Eval[Int] = cats.Later@10ab850e
val always = Eval.always(5 + 6)
// always: cats.Eval[Int] = cats.Always@f76e9a0
We can extract the result of an Eval using its value method:
now.value
// res6: Int = 3
later.value
// res7: Int = 7
always.value
// res8: Int = 11

---

Each type of Eval calculates its result using one of the evaluation models defined above. Eval.now captures a value right now. Its seman cs are similar to a val—eager and memoized:

val x = Eval.now { println("Computing X") 1+1
}
// Computing X
// x: cats.Eval[Int] = Now(2)
x.value // first access // res9: Int = 2
x.value // second access // res10: Int = 2

---

Eval.always captures a lazy computation, similar to a def:
val y = Eval.always { println("Computing Y") 1+1
}
// y: cats.Eval[Int] = cats.Always@fc4a7df
y.value // first access // Computing Y
// res11: Int = 2
y.value // second access // Computing Y
// res12: Int = 2

---

Finally, Eval.later captures a lazy computation and memoizes the result, similar to a lazy val:
val z = Eval.later { println("Computing Z") 1+1
}
// z: cats.Eval[Int] = cats.Later@56d1796c
z.value // first access // Computing Z
// res13: Int = 2
z.value // second access // res14: Int = 2

---

The three behaviours are summarized below:

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;border-color:#ccc;margin:0px auto;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#fff;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#ccc;color:#333;background-color:#f0f0f0;}
.tg .tg-9hbo{font-weight:bold;vertical-align:top}
.tg .tg-yw4l{vertical-align:top}
</style>
<table class="tg">
 <tr>
   <th class="tg-9hbo"></th>
   <th class="tg-9hbo">Eager</th>
   <th class="tg-9hbo">Lazy</th>
 </tr>
 <tr>
   <td class="tg-9hbo">Memoized</td>
   <td class="tg-yw4l">`val, Eval.now`</th>
   <td class="tg-yw4l">`lazy val, Eval.later`</th>
 </tr>
 <tr>
   <td class="tg-9hbo">Not Memoized</td>
   <td class="tg-yw4l">-</th>   
   <td class="tg-yw4l">`def, Eval.always`</th>
 </tr>
</table>

---

#Homework

Read Chapter 7 of _Functional Programming in Scala_.
