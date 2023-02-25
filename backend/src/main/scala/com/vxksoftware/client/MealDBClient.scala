package com.vxksoftware.client

import com.vxksoftware.model.dto.{FilterMealDTO, FilterResponseDTO, FindMealResponseDTO, MealDTO}
import com.vxksoftware.model.{IngredientKind, Recipe, Suggestion}
import com.vxksoftware.utils.*
import zio.*
import zio.json.*
import zio.stream.*

import java.net.URL
import java.util.concurrent.TimeUnit
import scala.io.Source
import scala.util.Try

trait MealDBClient {
  def findByIngredient(ingredient: IngredientKind): RIO[Scope, Set[Long]] = ???

  def findByIngredients(availableIngredients: Set[IngredientKind]): RIO[Scope, Set[Recipe]]

  def findByIngredientsMargin(
    availableIngredients: Set[IngredientKind],
    margin: Int
  ): RIO[Scope, (Set[Recipe], Set[Suggestion])]

  def find(id: Long): RIO[Scope, Option[MealDTO]]
}

object MealDBClient {
  lazy val live: ZLayer[Any, Nothing, MealDBClientLive] = ZLayer.fromFunction(MealDBClientLive.apply _)

  case class MealDBClientLive() extends MealDBClient {
    private val apiKey                                = 1
    private val baseUrl: String                       = s"https://www.themealdb.com/api/json/v1/$apiKey"
    private val ingredientFilterUrl: String => String = ingredient => baseUrl + s"/filter.php?i=$ingredient"
    private val mealUrl: Long => String               = id => baseUrl + s"/lookup.php?i=$id"

    private lazy val fetchMealIds = (url: String) => {
      urlSource(new URL(url)).flatMap { src =>
        for {
          lines <- ZIO.attemptBlockingIO(src.getLines())
          response <-
            ZIO.fromEither(lines.mkString.fromJson[FilterResponseDTO]).mapError(new RuntimeException(_)).orDie
          mealIds = response.meals.map(_.map(_.idMeal.toLong).toSet)
        } yield mealIds
      }
    }

    def find(id: Long): RIO[Scope, Option[MealDTO]] = {
      val url = mealUrl(id)

      urlSource(new URL(url)).flatMap { source =>
        val lines = source.getLines()
        val response =
          ZIO.fromEither(lines.mkString.fromJson[FindMealResponseDTO]).mapError(s => new RuntimeException(s))

        response.flatMap {
          case FindMealResponseDTO(Chunk(recipe)) => ZIO.succeed(Some(recipe))
          case FindMealResponseDTO(meals) =>
            Console.printLineError(s"Could not find recipe by id: $id. Got " + meals) *>
              ZIO.succeed(None)
        }
      }
    }

    def findByIngredientsMargin(
      availableIngredients: Set[IngredientKind],
      margin: Int
    ): RIO[Scope, (Set[Recipe], Set[Suggestion])] = {
      val urls = availableIngredients.flatMap(_.mealDbNames) map ingredientFilterUrl

      val sink =
        ZSink.foldLeft[
          MealDTO,
          (Set[Recipe], Set[Suggestion])
        ]((Set.empty[Recipe], Set.empty[Suggestion])) { case ((exactSet, suggestionSet), meal) =>
          val recipe = Recipe.fromMealDbDTO(meal)
          if recipe.canBeMadeWith(availableIngredients, 0) then (exactSet + recipe, suggestionSet)
          else if recipe.canBeMadeWith(availableIngredients, margin) then
            val suggestion = Suggestion(recipe, meal.ingredientKinds.diff(availableIngredients))
            (exactSet, suggestionSet + suggestion)
          else (exactSet, suggestionSet)
        }

      ZStream
        .from(urls)
        .mapZIOPar(20)(fetchMealIds) // meal IDS containing the ingredient
        .collect { case Some(id) => id }
        .flattenIterables
        .mapZIOPar(20)(find) // MEALS containing the ingredient
        .collect { case Some(meal) => meal }
        .run(sink)
    }

    def findByIngredients(availableIngredients: Set[IngredientKind]): RIO[Scope, Set[Recipe]] =
      findByIngredientsMargin(availableIngredients, 0).map(_._1)
  }
}
