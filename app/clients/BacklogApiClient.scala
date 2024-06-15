package clients

import javax.inject._
import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import play.api.Configuration

@Singleton
class BacklogApiClient @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  private val baseUrl = "https://nu-rec-uk.backlog.com" // ベースURL

  private val clientId = config.get[String]("backlogApi.clientId")
  private val clientSecret = config.get[String]("backlogApi.clientSecret")
  private val redirectUri = config.get[String]("backlogApi.redirectUri")

  def getOAuthPath: String = {
    s"${baseUrl}/OAuth2AccessRequest.action?response_type=code&client_id=$clientId&redirect_uri=$redirectUri"
  }

  def codeToToken(code: String): Future[(String, String, Int)] = {
    val tokenUrl = s"$baseUrl/api/v2/oauth2/token"
    val data = Map(
      "grant_type" -> "authorization_code",
      "code" -> code,
      "redirect_uri" -> redirectUri,
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    )

    ws.url(tokenUrl)
    .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    .post(data.map { case (key, value) => s"$key=$value"}.mkString("&"))
    .map { response =>
      val json = Json.parse(response.body)
      val accessToken = (json \ "access_token").as[String]
      val refreshToken = (json \ "refresh_token").as[String]
      val expiresIn = (json \ "expires_in").as[Int]

      // Return the token's expiration time 30 seconds shorter
      (accessToken, refreshToken, expiresIn - 30)
    }
  }

  def getRefreshToken(refreshToken: String): Future[(String, Int)] = {
    val refreshTokenUrl = s"$baseUrl/api/v2/oauth2/token"
    val data = Map(
      "grant_type" -> "refresh_token",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "refresh_token" -> refreshToken,
    )

    ws.url(refreshTokenUrl)
    .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    .post(data.map { case (key, value) => s"$key=$value"}.mkString("&"))
    .map { response =>
      val newAccessToken = (Json.parse(response.body) \ "access_token").as[String]
      val expiresIn = (Json.parse(response.body) \ "expires_in").as[Int]

      // Return the token's expiration time 30 seconds shorter
      (newAccessToken, expiresIn - 30)
    }
  }

  def getActivities(accessToken: String): Future[String] = {
    val activitiesUrl = s"$baseUrl/api/v2/space/activities"

    ws.url(activitiesUrl)
    .withHttpHeaders("Authorization" -> s"Bearer $accessToken")
    .get().map { response =>
      if (response.status != 200) {
        throw new RuntimeException("token is invalid." + response.status + "/n" + response.body)
      }
      response.body
    }
  }

}

