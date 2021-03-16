package org.kacperfkorban

import scala.quoted.*

package object notsoquicklens {

  private val shapeInfo = "focus must have shape: _.field1.field2.field3"

  inline def modify[S, A](inline obj: S)(inline focus: S => A)(mod: A => A): Any = ${modifyImpl('obj, 'focus, 'mod)}

  def modifyImpl[S, A](obj: Expr[S], focus: Expr[S => A], mod: Expr[A => A])(using qctx: Quotes, tpeS: Type[S], tpeA: Type[A]): Expr[Any] = {
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

    // person
    // person.copy(parent = f(person.parent))
    // person.copy(parent = person.parent.copy(name = person.parent.name.copy(value = f(person.parent.name.value))))
    def mapToCopy[X](objTerm: Term, path: Seq[String]): Term = path match
      case Nil =>
        val apply = termMethodByNameUnsafe(mod.asTerm, "apply")
        Apply(Select(mod.asTerm, apply), List(objTerm))
      case field :: tail =>
        val copy = termMethodByNameUnsafe(objTerm, "copy")
        val (fieldMethod, idx) = termAccessorMethodByNameUnsafe(objTerm, field)
        val defaultFieldsIdxs = 1.to(objTerm.tpe.typeSymbol.caseFields.length).filter(_ != idx)
        val defaults = defaultFieldsIdxs.map { i =>
          Select(objTerm, termMethodByNameUnsafe(objTerm, "copy$default$" + i.toString))
        }
        Apply(
          Select(objTerm, copy),
          List(
            NamedArg(field, mapToCopy(Select(objTerm, fieldMethod), tail))
          ) ++ defaults
        )

    val objTree: Tree = obj.asTerm
    val objTerm: Term = objTree match {
      case Inlined(_, _, term) => term
    }

    val focusTree: Tree = focus.asTerm
    val (path, pathSelects) = focusTree match {
      case Inlined(_, _, Block(List(DefDef(_, _, _, Some(p))), _)) =>
        (fromTree(p), p)
      case _ =>
        report.error(shapeInfo)
        (Seq.empty, null) // TODO Oopsie
    }

    val modTree: Tree = mod.asTerm
    
    val res = mapToCopy(objTerm, path)
    res.asExpr.asInstanceOf[Expr[Any]]
  }
}
