package org.virtuslab

import scala.quoted._

package object notsoquicklens {

  private val shapeInfo = "focus must have shape: _.field1.field2.field3"

  inline def modify[S, A](inline obj: S)(inline focus: S => A)(mod: A => A): S = ${modifyImpl('obj, 'focus)}

  def modifyImpl[S, A](obj: Expr[S], focus: Expr[S => A])(using Quotes): Expr[S] = {
    import quotes.reflect._

    def fromTree(tree: Tree, acc: Seq[String] = Seq.empty): Seq[String] = {
      tree match {
        case Select(deep, ident) => fromTree(deep, ident +: acc)
        case _: Ident => acc
        case _ =>
          report.error(shapeInfo)
          Seq.empty
      }
    }
    
    // person
    // person.copy(parent = f(person.parent))
    // person.copy(parent = person.parent.copy(name = person.parent.name.copy(value = f(person.parent.name.value))))
    def mapToCopy[X](obj: Term, path: Seq[String]): Term = path match {
      case Nil => obj
      case field :: tail =>
        Apply(Select(obj, "copy"), List(NamedArg(field, mapToCopy(Select(obj, field.asInstanceOf[Symbol]), tail))))
    }

    val objTree: Tree = Tree.of(obj)
    val objTerm: Term = objTree match {
      case Inlined(_, _, term) => term
    }

    val focusTree: Tree = Tree.of(focus)
    val path = focusTree match {
      case Inlined(_, _, Block(List(DefDef(_, _, _, _, Some(path))), _)) =>
        fromTree(path)
      case _ =>
        report.error(shapeInfo)
        Seq.empty
    }
    
    println(objTree)
    mapToCopy(objTerm, path).asInstanceOf[Expr[S]]
  }
}
