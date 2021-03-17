package lightlens

import scala.quoted.*

extension [S, A](obj: S)
  inline def focus(inline f: S => A): ObjectModifyPath[S, A] = {
    ${modifyImpl('obj, 'f)}
  }

case class ObjectModifyPath[S, A](f: (A => A) => S) {
  def modify(mod: A => A): S = f.apply(mod)
  def set(v: A): S = f.apply(Function.const(v))
}

private val shapeInfo = "focus must have shape: _.field1.field2.field3"

def toObjectModifyPath[S: Type, A: Type](f: Expr[(A => A) => S])(using Quotes): Expr[ObjectModifyPath[S, A]] = '{ ObjectModifyPath( ${f} ) }

def to[T: Type, R: Type](f: Expr[T] => Expr[R])(using Quotes): Expr[T => R] = '{ (x: T) => ${ f('x) } }

def modifyImpl[S, A](obj: Expr[S], focus: Expr[S => A])(using qctx: Quotes, tpeS: Type[S], tpeA: Type[A]): Expr[ObjectModifyPath[S, A]] = {
  import qctx.reflect.*
  
  def fromTree(tree: Tree, acc: Seq[String] = Seq.empty): Seq[String] = {
    tree match {
      case s@Select(deep, ident) =>
        fromTree(deep, ident +: acc)
      case _: Ident => acc
      case _ =>
        report.error(shapeInfo)
        Seq.empty
    }
  }

  def termMethodByNameUnsafe(term: Term, name: String): Symbol = {
    term.tpe.typeSymbol.declaredMethod(name).head
  }

  def termAccessorMethodByNameUnsafe(term: Term, name: String): (Symbol, Int) = {
    val caseFields = term.tpe.typeSymbol.caseFields
    val idx = caseFields.map(_.name).indexOf(name)
    (caseFields.find(_.name == name).get, idx+1)
  }

  def mapToCopy[X](mod: Expr[A => A], objTerm: Term, path: Seq[String]): Term = path match
    case Nil =>
      val apply = termMethodByNameUnsafe(mod.asTerm, "apply")
      Apply(Select(mod.asTerm, apply), List(objTerm))
    case field :: tail =>
      val copy = termMethodByNameUnsafe(objTerm, "copy")
      val (fieldMethod, idx) = termAccessorMethodByNameUnsafe(objTerm, field)
      val namedArg = NamedArg(field, mapToCopy(mod, Select(objTerm, fieldMethod), tail))
      val fieldsIdxs = 1.to(objTerm.tpe.typeSymbol.caseFields.length)
      val args = fieldsIdxs.map { i =>
        if(i == idx) namedArg
        else Select(objTerm, termMethodByNameUnsafe(objTerm, "copy$default$" + i.toString))
      }.toList
      Apply(
        Select(objTerm, copy),
        args
      )
  
  val focusTree: Tree = focus.asTerm
  val path = focusTree match {
    case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
      fromTree(p)
    case _ =>
      report.error(shapeInfo)
      Seq.empty
  }

  val objTree: Tree = obj.asTerm
  val objTerm: Term = objTree match {
    case Inlined(_, _, term) => term
  }
  
  val res: (Expr[A => A] => Expr[S]) = (mod: Expr[A => A]) => mapToCopy(mod, objTerm, path).asExpr.asInstanceOf[Expr[S]]
  toObjectModifyPath(to(res))
}
