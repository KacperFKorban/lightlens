package lightlens

trait FunctorLens[F[_]] {
  def map[A, B](fa: F[A], f: A => B): F[B]
}

extension [F[_]: FunctorLens, A](fa: F[A])
  def mapped: A = ???

object FunctorLens {
  given FunctorLens[List] with {
    def map[A, B](fa: List[A], f: A => B): List[B] = fa.map(f)
  }

  given FunctorLens[Seq] with {
    def map[A, B](fa: Seq[A], f: A => B): Seq[B] = fa.map(f)
  }

  given FunctorLens[Option] with {
    def map[A, B](fa: Option[A], f: A => B): Option[B] = fa.map(f)
  }
}