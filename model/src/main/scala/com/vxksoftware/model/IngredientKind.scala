package com.vxksoftware.model

import zio.json.*

enum IngredientKind1:
  case Exact(mealDbIds: Set[Long], mealDbNames: Set[String], name: Kind)
  case Wildcard

case class IngredientKind(
  mealDbIds: Set[Long],
  mealDbNames: Set[String],
  name: Kind
)

object IngredientKind {
  def fromString(kind: Kind): Option[IngredientKind] = {
    val source = io.Source.fromResource("ingredients_map.json")
    val string = source.getLines().mkString
    source.close()

    val json = string.fromJson[IngredientsMap]
    json.flatMap { ingrMap =>
      val p = (ime: IngredientsMapEntry) =>
        ime.name.toLowerCase == kind.toLowerCase ||
          ime.ingredients.map(_.name.toLowerCase).exists(_.contains(kind.toLowerCase))

      ingrMap.map.find(p) match
        case Some(mapEntry) =>
          Right(
            IngredientKind(
              name = kind,
              mealDbIds = mapEntry.ingredients.map(_.id.toLong).toSet,
              mealDbNames = mapEntry.ingredients.map(_.name).toSet
            )
          )
        case None => Left(s"Could not find kind [$kind] in the ingredients map")
    } match
      case Left(error) =>
        println(s"[ERROR] when making IngredientKind from string [$kind]: " + error)
        None
      case Right(value) =>
        Some(value)
  }
}
