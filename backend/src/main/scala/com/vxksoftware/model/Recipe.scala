package com.vxksoftware.model

import com.vxksoftware.model.dto.MealDTO
import zio.json.*

type Measure = String

final case class Recipe(
  id: Long,
  name: String,
  ingredients: Map[String, Measure]
)

object Recipe:
  given JsonCodec[Recipe] = DeriveJsonCodec.gen[Recipe]

  def fromMealDbDTO(mealDTO: MealDTO): Recipe =
    Recipe(
      id = mealDTO.idMeal.toLong,
      name = mealDTO.strMeal,
      //TODO
      ingredients = Map(
        mealDTO.strIngredient1.getOrElse("") -> mealDTO.strMeasure1.getOrElse(""),
        mealDTO.strIngredient2.getOrElse("") -> mealDTO.strMeasure2.getOrElse(""),
        mealDTO.strIngredient3.getOrElse("") -> mealDTO.strMeasure3.getOrElse(""),
        mealDTO.strIngredient4.getOrElse("") -> mealDTO.strMeasure4.getOrElse(""),
        mealDTO.strIngredient5.getOrElse("") -> mealDTO.strMeasure5.getOrElse("")
      )
    )
