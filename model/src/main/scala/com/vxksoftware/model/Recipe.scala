package com.vxksoftware.model

import com.vxksoftware.model.dto.MealDTO
import com.vxksoftware.model.IngredientKind.{*, given}
import zio.json.*

type Measure    = String
type Ingredient = String

final case class Recipe(
  id: Long,
  name: String,
  ingredients: List[(Ingredient, Measure)]
) derives JsonEncoder { self =>
  def canBeMadeWith(availableIngredients: Set[IngredientKind], margin: Int): Boolean = {
    val recipeIngredients = self.ingredients.toMap.keys.map(IngredientKind.fromIngredient).toSet
    (recipeIngredients subsetOf availableIngredients) || {
      val diffSize = recipeIngredients.diff(availableIngredients).size
      val result   = diffSize <= margin
      if result then {
        println("GOT 'EEEEEMMMMM")
        println(recipeIngredients.map(_.name))
        println(availableIngredients.map(_.name))
        println(result)
      }
      result
    }
  }

}

object Recipe:

  def fromMealDbDTO(mealDTO: MealDTO): Recipe = {
    //classify ingredients
    val ingredients = mealDTO.ingredientMeasures.map { case (ingredient, measure) =>
      IngredientKind.fromIngredient(ingredient) -> measure
    }.collect { case Some(kind) -> measure => kind -> measure }

    Recipe(
      id = mealDTO.idMeal.toLong,
      name = mealDTO.strMeal,
      ingredients = ingredients.toList
    )
  }
