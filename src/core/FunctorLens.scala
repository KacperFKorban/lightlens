package lightlens

trait QuicklensFunctor[F[_]] {
  def map[A, B](fa: F[A], f: A => B): F[B]
}

object QuicklensFunctor {
  given QuicklensFunctor[List] with {
    def map[A, B](fa: List[A], f: A => B): List[B] = fa.map(f)
  }

  given QuicklensFunctor[Seq] with {
    def map[A, B](fa: Seq[A], f: A => B): Seq[B] = fa.map(f)
  }

  given QuicklensFunctor[Option] with {
    def map[A, B](fa: Option[A], f: A => B): Option[B] = fa.map(f)
  }
}