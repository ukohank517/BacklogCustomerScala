import javax.inject._
import play.api._
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._

@Singleton
class ErrorHandler @Inject()(implicit env: Environment, config: Configuration) extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  // re-certification
  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      InternalServerError(views.html.errorPage(exception))
    )
  }
}
