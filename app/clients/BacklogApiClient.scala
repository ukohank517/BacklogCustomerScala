package clients

import models.domain.Activity
import models.dto.ActivityDto
import models.dto.JsonFormats._
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BacklogApiClient @Inject()(ws: WSClient, config: Configuration)(implicit ec: ExecutionContext) {

  private val baseUrl = "https://nu-rec-uk.backlog.com" // ベースURL
  private val apiClient = new ApiClient(ws, baseUrl)

  private val clientId = config.get[String]("backlogApi.clientId")
  private val clientSecret = config.get[String]("backlogApi.clientSecret")
  private val redirectUri = config.get[String]("backlogApi.redirectUri")

  /**
    * OAuth2認証用のURLを取得
    */
  def getOAuthPath: String = {
    s"${baseUrl}/OAuth2AccessRequest.action?response_type=code&client_id=$clientId&redirect_uri=$redirectUri"
  }

  /**
    * 認証コードを使ってアクセストークンを取得
    *
    * @param code 認証コード
    * @return (アクセストークン, リフレッシュトークン, 有効期限)
    */
  def codeToToken(code: String): Future[(String, String, Int)] = {
    val path = "/api/v2/oauth2/token"
    val headers = Seq("Content-Type" -> "application/x-www-form-urlencoded")
    val data = Map(
      "grant_type" -> "authorization_code",
      "code" -> code,
      "redirect_uri" -> redirectUri,
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    )
    val body = data.map { case (key, value) => s"$key=$value"}.mkString("&")

    apiClient.postApi(path, headers, body).map { response =>
      val json = Json.parse(response)
      val accessToken = (json.as[JsObject] \ "access_token").as[String]
      val refreshToken = (json.as[JsObject] \ "refresh_token").as[String]
      val expiresIn = (json.as[JsObject] \ "expires_in").as[Int]

      // トークンの失効時間を30秒短くして返す
      (accessToken, refreshToken, expiresIn - 30)
    }
  }

  /**
    * リフレッシュトークンを使ってアクセストークンを取得
    *
    * @param refreshToken リフレッシュトークン
    * @return (アクセストークン, 有効期限)
    */
  def getRefreshToken(refreshToken: String): Future[(String, Int)] = {
    val path = "/api/v2/oauth2/token"
    val headers = Seq("Content-Type" -> "application/x-www-form-urlencoded")
    val data = Map(
      "grant_type" -> "refresh_token",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "refresh_token" -> refreshToken,
    )
    val body = data.map { case (key, value) => s"$key=$value"}.mkString("&")

    apiClient.postApi(path, headers, body).map { response =>
      val json = Json.parse(response)
      val newAccessToken = (json.as[JsObject] \ "access_token").as[String]
      val expiresIn = (json.as[JsObject] \ "expires_in").as[Int]
      // トークンの失効時間を30秒短くして返す
      (newAccessToken, expiresIn - 30)
    }
  }

  /**
    * 最新のアクティビティを取得
    *
    * @param accessToken アクセストークン
    * @return アクティビティのJSON文字列
    */
  def getActivities(accessToken: String): Future[List[Activity]] = {
    val path = "/api/v2/space/activities"
    val headers = Seq("Authorization" -> s"Bearer $accessToken")
    apiClient.getApi(path, headers).map { response =>
      Json.parse(response).as[List[ActivityDto]].map { data =>
        Activity.fromActivityDto(data)
      }
    }
  }
}

