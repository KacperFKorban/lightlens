package lightlens

import scala.quoted.*

extension [S, A](inline obj: S)
  inline def focus(inline f: S => A): ModificationByPath[S, A] = {
    ${modifyImpl('obj, 'f)}
  }

case class ModificationByPath[S, A](f: (A => A) => S) {
  def modify(mod: A => A): S = f.apply(mod)
  def set(v: A): S = f.apply(Function.const(v))
}

private val shapeInfo = "focus must have shape: _.field1.mapped.field3"

private val specialAccessors = List("mapped")

def toModificationByPath[S: Type, A: Type](f: Expr[(A => A) => S])(using Quotes): Expr[ModificationByPath[S, A]] = '{ ModificationByPath( ${f} ) }

def to[T: Type, R: Type](f: Expr[T] => Expr[R])(using Quotes): Expr[T => R] = '{ (x: T) => ${ f('x) } }

def modifyImpl[S, A](obj: Expr[S], focus: Expr[S => A])(using qctx: Quotes, tpeS: Type[S], tpeA: Type[A]): Expr[ModificationByPath[S, A]] = {
  import qctx.reflect.*

  enum PathSymbol:
    case Field(name: String)
    case Mapped(givn: Term, typeTree: TypeTree)

  object PathSymbol {
    def specialSymbolByName(term: Term, name: String, typeTree: TypeTree): PathSymbol = {
      if(name.contains("FunctorLens")) Mapped(term, typeTree)
      else
        report.error(shapeInfo)
        ???
    }
  }

  import PathSymbol.*
  def toPath(tree: Tree): Seq[PathSymbol] = {
    tree match {
      case s@Select(deep, ident) =>
        toPath(deep) :+ Field(ident)
      case Apply(Apply(TypeApply(Ident(s), typeTrees), idents), List(ident: Ident)) if specialAccessors.contains(s) =>
        idents.flatMap(toPath) :+ PathSymbol.specialSymbolByName(ident, ident.asInstanceOf[Ident].name, typeTrees.last)
      case a@Apply(deep, idents) =>
        toPath(deep) ++ idents.flatMap(toPath)
      case i: Ident if i.name.startsWith("_") =>
        Seq.empty
      case _ =>
        report.error(shapeInfo)
        ???
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

  def mapToCopy[X](mod: Expr[A => A], objTerm: Term, path: Seq[PathSymbol]): Term = path match
    case Nil =>
      val apply = termMethodByNameUnsafe(mod.asTerm, "apply")
      Apply(Select(mod.asTerm, apply), List(objTerm))
    case (field: Field) :: tail =>
      val copy = termMethodByNameUnsafe(objTerm, "copy")
      val (fieldMethod, idx) = termAccessorMethodByNameUnsafe(objTerm, field.name)
      val namedArg = NamedArg(field.name, mapToCopy(mod, Select(objTerm, fieldMethod), tail))
      val fieldsIdxs = 1.to(objTerm.tpe.typeSymbol.caseFields.length)
      val args = fieldsIdxs.map { i =>
        if(i == idx) namedArg
        else Select(objTerm, termMethodByNameUnsafe(objTerm, "copy$default$" + i.toString))
      }.toList
      Apply(
        Select(objTerm, copy),
        args
      )
    case (m: Mapped) :: tail =>
      val defdefSymbol = Symbol.newMethod(
        Symbol.spliceOwner,
        "$anonfun",
        MethodType(List("x"))(_ => List(m.typeTree.tpe), _ => m.typeTree.tpe)
      )
      val mapMethod = termMethodByNameUnsafe(m.givn, "map")
      val map = TypeApply(
        Select(m.givn, mapMethod),
        List(m.typeTree, m.typeTree)
      )
      val defdefStatements = DefDef(
        defdefSymbol, {
          case List(List(x)) => Some(mapToCopy(mod, x.asExpr.asTerm, tail))
        }
      )
      val closure = Closure(Ref(defdefSymbol), None)
      val block = Block(List(defdefStatements), closure)
      Apply(map, List(objTerm, block))
  
  val focusTree: Tree = focus.asTerm
  val path = focusTree match {
    case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
      toPath(p)
    case _ =>
      report.error(shapeInfo)
      ???
  }

  val objTree: Tree = obj.asTerm
  val objTerm: Term = objTree match {
    case Inlined(_, _, term) => term
  }
  
  val res: (Expr[A => A] => Expr[S]) = (mod: Expr[A => A]) => mapToCopy(mod, objTerm, path).asExpr.asInstanceOf[Expr[S]]
  toModificationByPath(to(res))
}
