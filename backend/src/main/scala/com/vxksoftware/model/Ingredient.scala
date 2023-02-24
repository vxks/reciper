package com.vxksoftware.model

enum IngredientType {
  case Meat
}

case class Ingredient(
  id: Long,
  name: String,
  description: Option[String],
  `type`: Option[IngredientType]
)
