package com.vxksoftware.model

import zio.json.*

case class IngredientKind(
  mealDbIds: Set[Long],
  mealDbNames: Set[String],
  name: Kind
) derives JsonCodec

object IngredientKind {

  def fromIngredient(ingredient: String): Option[IngredientKind] = {
    val source = io.Source.fromResource("ingredients_map.json")
    val string = source.getLines().mkString
    source.close()

    val json = string.fromJson[IngredientsMap]
    json.flatMap { ingrMap =>
      val p = (ime: IngredientsMapEntry) =>
        ime.name.toLowerCase == ingredient.toLowerCase ||
          ime.ingredients.map(_.name.toLowerCase).exists(_.contains(ingredient.toLowerCase))

      ingrMap.map.find(p) match
        case Some(mapEntry) =>
          Right(
            IngredientKind(
              name = ingredient,
              mealDbIds = mapEntry.ingredients.map(_.id.toLong).toSet,
              mealDbNames = mapEntry.ingredients.map(_.name).toSet
            )
          )
        case None => //todo:
          println(s"Could not find kind for [$ingredient] in the ingredients map")
          Right()
          ???
    } match
      case Left(error) =>
        println(s"[ERROR] when making IngredientKind from string [$ingredient]: " + error)
        None
      case Right(value) =>
        Some(value)
  }
}
