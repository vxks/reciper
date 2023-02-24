import com.vxksoftware.client.MealDBClient
import com.vxksoftware.model.CommonIngredients.*
import com.vxksoftware.service.RecipeFinder
import zio.*
import zio.json.*
import zio.http.*
import zio.http.Middleware.*
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.*

object BackendApp extends ZIOAppDefault:

  val corsMW = cors(
    CorsConfig(
      anyOrigin = false,
      allowedOrigins = _.startsWith("http://localhost:3000")
    )
  )
  val httpApp = Http.collectZIO[Request] {
    case request @ Method.GET -> !! / "find" => {
      for {
        recipeFinder <- ZIO.service[RecipeFinder]
        ingredients <- ZIO
                         .fromOption(request.url.queryParams.get("i"))
                         .mapError(_ => new RuntimeException("Could not extract `i` query param from find request"))
        results <- recipeFinder.findRecipes(ingredients.toSet)
      } yield Response.json(results.toJsonPretty)
    }
  } @@ corsMW

  val program = for {
    serverFiber <-
      Server
        .serve(
          httpApp
            .tapErrorZIO(t => Console.printLineError(t.getMessage))
            .mapError(t => Response(Status.InternalServerError, body = Body.fromString(t.getMessage)))
        )
        .fork
    _ <- Console.readLine
    _ <- serverFiber.interrupt
  } yield ExitCode.success

  val run = program.provide(
    Server.default,
    MealDBClient.live,
    RecipeFinder.mealDBRecipeFinder
  )
