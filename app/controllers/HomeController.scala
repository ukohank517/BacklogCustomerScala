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
    // TODO: token情報なし|api呼び出し失敗時エラー画面飛ばす。
    Redirect(routes.HomeController.errorPage("todo"))
  }

  // auth2認証
  def auth() = Action { implicit request: Request[AnyContent] =>
    Redirect(backlogApi.getOAuthPath)
  }

  // oauthのcallback
  def authCallback() = Action.async { implicit request: Request[AnyContent] =>
    val code = request.getQueryString("code").getOrElse("")
    if (code.isEmpty()) {
      // Redirect(routes.HomeController.errorPage("Missing code parameter"))
      Future.successful(BadRequest("Missing code parameter"))
    }else{
      backlogApi.getToken(code).map { case (accessToken, refreshToken) =>
        Redirect("/backlog/activity").withCookies(
          Cookie("refreshToken", refreshToken, httpOnly = true),
          Cookie("accessToken", accessToken, httpOnly = true)
        )
      }
    }
  }

  // error page, re-certification
  def errorPage(errorMessage: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.error(errorMessage))
  }


  def showActivity() = Action.async { implicit request: Request[AnyContent] =>
    val accessToken = request.cookies.get("accessToken").map(_.value).getOrElse("No refresh token")
    val refreshToken = request.cookies.get("refreshToken").map(_.value).getOrElse("No refresh token")

    // TODO: delete
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
