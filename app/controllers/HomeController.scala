package controllers

import play.api.mvc._

import javax.inject._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def index() = Action { _ =>
    throw new RuntimeException("このページは何ありません。認証してアクティビティページを確認してください。")
  }

  def dashboard() = Action { request =>
    val queryParam = request.queryString
    Ok(queryParam.toString())
  }
}
