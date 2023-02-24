package com.vxksoftware.service

import com.vxksoftware.client.MealDBClient
import com.vxksoftware.model.{IngredientKind, Recipe}
import zio.*

trait RecipeFinder {
  def findMatchingRecipes(ingredients: Set[IngredientKind]): Task[Set[Recipe]]
  
  // find recipes that are off by 1-2 ingredients
  def findSuggestions(ingredients: Set[IngredientKind], margin: Short): Task[Set[Recipe]] = ???
}

object RecipeFinder {
  lazy val mealDBRecipeFinder: ZLayer[MealDBClient, Nothing, MealDBRecipeFinder] =
    ZLayer.fromFunction(MealDBRecipeFinder.apply _)

  final case class MealDBRecipeFinder(mealDBClient: MealDBClient) extends RecipeFinder {
    def findMatchingRecipes(ingredients: Set[IngredientKind]): Task[Set[Recipe]] =
      for {
        recipes <- ZIO.scoped(mealDBClient.findByIngredients(ingredients))
      } yield recipes.map(Recipe.fromMealDbDTO)
  }
}
