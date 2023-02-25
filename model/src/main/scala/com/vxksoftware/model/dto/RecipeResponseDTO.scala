package com.vxksoftware.model.dto

import zio.json.*
import com.vxksoftware.model.{Recipe, Measure, Kind}

case class RecipeResponseDTO(
  mealDbId: Long,
  name: String,
  ingredients: Set[IngredientDTO]
) derives JsonEncoder

case class IngredientDTO(
  ingredient: Kind,
  measure: String
) derives JsonEncoder

object RecipeResponseDTO:
  def fromRecipe(recipe: Recipe): RecipeResponseDTO =
    RecipeResponseDTO(
      mealDbId = recipe.id,
      name = recipe.name,
      ingredients = recipe.ingredients.map { case (kind, measure) =>
        IngredientDTO(
          kind.name,
          measure
        )
      }.toSet
    )
