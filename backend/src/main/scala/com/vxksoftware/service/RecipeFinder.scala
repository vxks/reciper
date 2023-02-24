package com.vxksoftware.service

import com.vxksoftware.client.MealDBClient
import com.vxksoftware.model.{Ingredient, Recipe}
//import com.vxksoftware.persistence.RecipesRepo
import zio.*

trait RecipeFinder {
  def findRecipes(ingredients: Set[String]): Task[Set[Recipe]]
}

object RecipeFinder {
  lazy val mealDBRecipeFinder: ZLayer[MealDBClient, Nothing, MealDBRecipeFinder] =
    ZLayer.fromFunction(MealDBRecipeFinder.apply _)

  final case class MealDBRecipeFinder(mealDBClient: MealDBClient) extends RecipeFinder {
    // find all recipes by ingredient
    // get intersection
    def findRecipes(ingredients: Set[String]): Task[Set[Recipe]] =
      for {
        recipes <- ZIO.scoped(mealDBClient.findByIngredients(ingredients).debug)
      } yield recipes.map(Recipe.fromMealDbDTO)
  }
}
