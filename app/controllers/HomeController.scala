package controllers

import clients.BacklogApiClient

import javax.inject._
import play.api._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.ExecutionContext
import java.lang.ProcessBuilder.Redirect


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(backlogApi: BacklogApiClient, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def index() = Action { implicit request: Request[AnyContent] =>
    // Ok(views.html.index())
    var oauth2Url = "https://nu-rec-uk.backlog.com/OAuth2AccessRequest.action?response_type=code&client_id=cUOxzn2NgqneQTYeOlzMbmu3QT4jUmmt&redirect_uri=http://localhost:9000/dashboard/activity"
    Redirect(oauth2Url)
  }

  def fetchApiData() = Action.async {
    backlogApi.get("/api/v2/space/activities").map { response =>
      Ok(response)
    }
  }

  def dashboard() = Action { request =>
    val queryParam = request.queryString
    Ok(queryParam.toString())
  }
}
