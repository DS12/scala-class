
#IO Monad

---

#Links

case class IO[A](unsafePerformIO: () => A) {
  def map[B](ab: A => B): IO[B] = IO(() => ab(unsafePerformIO()))
  def flatMap[B](afb: A => IO[B]): IO[B] =IO(() => afb(unsafePerformIO()).unsafePerformIO())
  def tryIO(ta: Throwable => A): IO[A] =
    IO(() => IO.tryIO(unsafePerformIO()).unsafePerformIO() match {
      case Left(t) => ta(t)
      case Right(a) => a
    })
}
object IO {
  def point[A](a: => A): IO[A] = IO(() => a)
  def tryIO[A](a: => A): IO[Either[Throwable, A]] =
    IO(() => try { Right(a) } catch { case t : Throwable => Left(t) })
}


liftM f m1 = do { x1 <- m1; return (f x1) }
           = m1 >>= \x1 -> return (f x1)

do { x1 <- m1; x2 <- m2; return (f x1 x2) }
m1 >>= (\x1 -> m2 >>= (\x2 -> return (f x1 x2) ))
