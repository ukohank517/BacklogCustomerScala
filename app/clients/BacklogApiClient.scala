package clients

import javax.inject._
import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BacklogApiClient @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {

  private val baseUrl = "https://nu-rec-uk.backlog.com" // ベースURL
  private val param = "?apiKey=**"

  def get(path: String): Future[String] = {
    ws.url(s"$baseUrl$path$param").get().map { response =>
      response.body
    }
  }

  def post(path: String, data: Map[String, String]): Future[String] = {
    ws.url(s"$baseUrl$path").post(data).map { response =>
      response.body
    }
  }
}

