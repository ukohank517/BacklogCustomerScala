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

@Singleton
class BacklogController @Inject()(backlogApi: BacklogApiClient, config: Configuration, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  /**
    * /backlog/auth
    * 二段階認証ページへ遷移
    */
  def auth() = Action { implicit request: Request[AnyContent] =>
    Redirect(backlogApi.getOAuthPath)
  }

  /**
    * /backlog/auth/callback
    * 二段階認証後のコールバックの受け皿
    */
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

  /**
    * /backlog/activity
    * アクティビティ一覧を表示
    */
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
        Ok(views.html.activity(activities)).withCookies(cookies: _*)
      }
    }
  }
}

