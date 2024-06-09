package clients

import javax.inject._
import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.Configuration

@Singleton
class BacklogApiClient @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  private val baseUrl = "https://nu-rec-uk.backlog.com" // ベースURL
  private val param = "?apiKey=**"

  def get(path: String, accessToken: String): Future[String] = {
    ws.url(s"$baseUrl$path$param")
    .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
    .get().map { response =>
      response.body
    }
  }

  def post(path: String, data: Map[String, String]): Future[String] = {
    ws.url(s"$baseUrl$path").post(data).map { response =>
      response.body
    }
  }

  def getToken(code: String): Future[(String, String)] = {
    val tokenUrl = s"$baseUrl/api/v2/oauth2/token"
    val data = Map(
      "grant_type" -> "authorization_code",
      "code" -> code,
      "redirect_uri" -> config.get[String]("backlogApi.redirectUri"),
      "client_id" -> config.get[String]("backlogApi.clientId"),
      "client_secret" -> config.get[String]("backlogApi.clientSecret"),
    )

    ws.url(tokenUrl)
    .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    .post(data.map { case (key, value) => s"$key=$value"}.mkString("&"))
    .map { response =>
      val json = Json.parse(response.body)
      val accessToken = (json \ "access_token").as[String]
      val refreshToken = (json \ "refresh_token").as[String]
      (accessToken, refreshToken)
    }
  }

}

