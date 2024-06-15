package controllers

import clients.BacklogApiClient

import javax.inject._
import play.api._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import java.lang.ProcessBuilder.Redirect
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(backlogApi: BacklogApiClient, config: Configuration, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def index() = Action { implicit request: Request[AnyContent] =>
    // TODO: redirect to activity page
    throw new RuntimeException("This is a test exception")
  }

  // auth2認証
  def auth() = Action { implicit request: Request[AnyContent] =>
    Redirect(backlogApi.getOAuthPath)
  }

  // oauthのcallback
  def authCallback() = Action.async { implicit request: Request[AnyContent] =>
    val code = request.getQueryString("code").getOrElse("")
    if (code.isEmpty()) {
      throw new RuntimeException("Missing code parameter")
    }else{
      backlogApi.codeToToken(code).map { case (accessToken, refreshToken, expiresIn) =>
        var appDomain = config.get[String]("app.domain")

        Redirect("/backlog/activity").withCookies(
          Cookie("refreshToken", refreshToken, httpOnly = true, secure = true, domain = Some(appDomain)),
          Cookie("accessToken", accessToken, httpOnly = true, secure = true, maxAge = Some(expiresIn), domain = Some(appDomain)),
        )
      }
    }
  }

  def showActivity() = Action.async { implicit request: Request[AnyContent] =>
    val accessToken = request.cookies.get("accessToken").map(_.value).getOrElse("")
    val refreshToken = request.cookies.get("refreshToken").map(_.value).getOrElse("")

    if(accessToken.isEmpty() && refreshToken.isEmpty()){
      throw new RuntimeException("Missing token")
    }

    var futureAccessToken: Future[(String, Seq[Cookie])] = accessToken match {
      case "" => refreshToken match {
        case "" => Future.failed(new RuntimeException("Missing token"))
        case _ => backlogApi.getRefreshToken(refreshToken).map { case (newAccessToken, expiresIn) =>
          println(s"newAccessToken: $newAccessToken, expiresIn: $expiresIn")
          var appDomain = config.get[String]("app.domain")
          (newAccessToken, Seq(
            Cookie("accessToken", newAccessToken, httpOnly = true, secure = true, maxAge = Some(expiresIn), domain = Some(appDomain))
          ))
        }
      }
      case _ => Future.successful(accessToken, Seq.empty)
    }

    futureAccessToken.flatMap { case (newAccessToken, cookies) =>
      backlogApi.getActivities(newAccessToken).map { activities =>
        Ok(activities).withCookies(cookies: _*)
      }
    }
  }

  def dashboard() = Action { request =>
    val queryParam = request.queryString
    Ok(queryParam.toString())
  }
}
