package com.vxksoftware.service

import com.vxksoftware.client.MealDBClient
import com.vxksoftware.model.dto.SuggestionDTO
import com.vxksoftware.model.{IngredientKind, Recipe}
import zio.*

trait RecipeFinder {
  def findMatchingRecipes(ingredients: Set[IngredientKind]): Task[Set[Recipe]]

  def findSuggestions(ingredients: Set[IngredientKind], margin: Int): Task[(Set[Recipe], Set[SuggestionDTO])]
}

object RecipeFinder {
  lazy val mealDBRecipeFinder: ZLayer[MealDBClient, Nothing, MealDBRecipeFinder] =
    ZLayer.fromFunction(MealDBRecipeFinder.apply _)

  final case class MealDBRecipeFinder(mealDBClient: MealDBClient) extends RecipeFinder {
    def findMatchingRecipes(ingredients: Set[IngredientKind]): Task[Set[Recipe]] =
      for {
        recipes <- ZIO.scoped(mealDBClient.findByIngredients(ingredients))
      } yield recipes

    def findSuggestions(ingredients: Set[IngredientKind], margin: Int): Task[(Set[Recipe], Set[SuggestionDTO])] =
      ZIO.scoped {
        mealDBClient.findByIngredientsMargin(ingredients, margin)
          .map { case (exacts, suggestions) =>
            exacts -> suggestions.map(SuggestionDTO.fromSuggestion)
          }
      }
  }
}
