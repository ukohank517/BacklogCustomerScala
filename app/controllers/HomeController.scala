package controllers

import clients.BacklogApiClient

import javax.inject._
import play.api._
import play.api.libs.ws._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
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

  def dashboard() = Action.async { implicit request: Request[AnyContent] =>
    // val queryParam = request.queryString
    val code = request.getQueryString("code").getOrElse("")
    if (code.isEmpty()) {
      Future.successful(BadRequest("Missing code parameter"))
    }else{
      backlogApi.getToken(code).map { case (accessToken, refreshToken) =>
        Redirect("/showToken").withCookies(
          Cookie("refreshToken", refreshToken, httpOnly = true),
        ).flashing("accessToken" -> accessToken)
      }
    }
  }

  def showActivity() = Action.async { implicit request: Request[AnyContent] =>
    val accessToken = request.flash.get("accessToken").getOrElse("No access token")
    val refreshToken = request.cookies.get("refreshToken").map(_.value).getOrElse("No refresh token")

    println(s"accessToken: $accessToken, refreshToken: $refreshToken")

    backlogApi.get("/api/v2/space/activities", accessToken).map { response =>
      Ok(response)
    }
  }

  def dashboard() = Action { request =>
    val queryParam = request.queryString
    Ok(queryParam.toString())
  }
}
