package clients

import play.api.http.HttpVerbs
import play.api.libs.ws._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ApiClient @Inject()(ws: WSClient, baseUrl: String)(implicit ec: ExecutionContext) {

  def getApi(path: String, headers: Seq[(String, String)] = Seq()): Future[String] = {
    callApi(HttpVerbs.GET, path, headers)
  }
  def postApi(path: String, headers: Seq[(String, String)] = Seq(), body: String): Future[String] = {
    callApi(HttpVerbs.POST, path, headers, Some(body))
  }

  // TODO: callApi[T]ジェネリクスを使用する
  def callApi(method: String, path: String, headers: Seq[(String, String)] = Seq(), body: Option[String] = None): Future[String] = {
    val url = s"$baseUrl$path"

    val request = ws.url(url).withHttpHeaders(headers.toSeq: _*)

    val responseFuture = method.toUpperCase match {
      case HttpVerbs.GET => request.get()
      case HttpVerbs.POST => request.post(body.getOrElse(""))
      case _ => Future.failed(new IllegalArgumentException("Invalid HTTP method"))
    }

    responseFuture.flatMap { response =>
      response.status match {
        case 200 =>
          Future.successful(response.body)
        case _ =>
          Future.failed(new RuntimeException(s"API call failed with status ${response.status}: ${response.body}"))
      }
    }
  }
}
