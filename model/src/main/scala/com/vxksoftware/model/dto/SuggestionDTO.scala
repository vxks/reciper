package com.vxksoftware.model.dto

import com.vxksoftware.model.{Suggestion, Recipe}
import zio.json.JsonEncoder

case class SuggestionDTO(
  missingIngredients: Set[String],
  recipe: RecipeResponseDTO
) derives JsonEncoder

object SuggestionDTO:
  def fromSuggestion(suggestion: Suggestion): SuggestionDTO =
    SuggestionDTO(
      missingIngredients = suggestion.missingIngredientKinds.map(_.name),
      recipe = RecipeResponseDTO.fromRecipe(suggestion.recipe)
    )
