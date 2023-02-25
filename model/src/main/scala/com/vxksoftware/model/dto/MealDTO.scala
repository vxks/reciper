package com.vxksoftware.model.dto

import com.vxksoftware.model.IngredientKind
import zio.json.*

case class MealDTO(
  idMeal: String,
  strMeal: String,
  strInstructions: String,
  strIngredient1: Option[String],
  strIngredient2: Option[String],
  strIngredient3: Option[String],
  strIngredient4: Option[String],
  strIngredient5: Option[String],
  strIngredient6: Option[String],
  strIngredient7: Option[String],
  strIngredient8: Option[String],
  strIngredient9: Option[String],
  strIngredient10: Option[String],
  strMeasure1: Option[String],
  strMeasure2: Option[String],
  strMeasure3: Option[String],
  strMeasure4: Option[String],
  strMeasure5: Option[String],
  strMeasure6: Option[String],
  strMeasure7: Option[String],
  strMeasure8: Option[String],
  strMeasure9: Option[String],
  strMeasure10: Option[String]
) {
  def ingredients: Set[String] =
    Set(
      strIngredient1,
      strIngredient2,
      strIngredient3,
      strIngredient4,
      strIngredient5,
      strIngredient6,
      strIngredient7,
      strIngredient8,
      strIngredient9,
      strIngredient10
    ).flatten.filterNot(_.isEmpty)

  def ingredientKinds: Set[IngredientKind] =
    ingredients.flatMap(IngredientKind.fromIngredient)
    
  def measures: Set[String] =
    Set(
      strMeasure1,
      strMeasure2,
      strMeasure3,
      strMeasure4,
      strMeasure5,
      strMeasure6,
      strMeasure7,
      strMeasure8,
      strMeasure9,
      strMeasure10,
    ).flatten.filterNot(_.isEmpty)

  def ingredientMeasures: Map[String, String] =
    ingredients.zip(measures).toMap

}

object MealDTO {
  given JsonDecoder[MealDTO] = DeriveJsonDecoder.gen[MealDTO]
}
