package models.dto

import play.api.libs.json._

case class Project(
  id: Long,
  projectKey: String,
  name: String,
  chartEnabled: Boolean,
  useResolvedForChart: Boolean,
  subtaskingEnabled: Boolean,
  projectLeaderCanEditProjectLeader: Boolean,
  useWiki: Boolean,
  useFileSharing: Boolean,
  useWikiTreeView: Boolean,
  useOriginalImageSizeAtWiki: Boolean,
  textFormattingRule: String,
  archived: Boolean,
  displayOrder: Int,
  useDevAttributes: Boolean
)

case class NulabAccount(
  nulabId: String,
  name: String,
  uniqueId: String,
  iconUrl: String
)

case class User(
  id: Long,
  userId: String,
  name: String,
  roleType: Int,
  lang: String,
  mailAddress: String,
  nulabAccount: NulabAccount,
  keyword: String,
  lastLoginTime: String
)

// TODO: この中の要素の規則性が不明。今回処理に必要な部分のみ定義
case class Content(
  summary: Option[String],
)
case class ActivityDto(
  id: Long,
  project: Project,
  `type`: Int,
  content: Content,
  //notifications 削除された
  createdUser: User,
  created: String
)

object JsonFormats {
  implicit val projectFormat: OFormat[Project] = Json.format[Project]
  implicit val nulabAccountFormat: OFormat[NulabAccount] = Json.format[NulabAccount]
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val contentFormat: OFormat[Content] = Json.format[Content]
  implicit val activityFormat: OFormat[ActivityDto] = Json.format[ActivityDto]
}