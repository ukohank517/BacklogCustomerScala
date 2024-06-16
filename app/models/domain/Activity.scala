package models.domain

import models.dto.ActivityDto
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

case class Activity(
  id: Long,
  projectName: String,
  typeStr: String,
  contentSummary: String,
  createdUserName: String,
  created: String
)

object Activity {
  def fromActivityDto(activityDto: ActivityDto): Activity = {
    val typeStr = typeMapping.get(activityDto.`type`).getOrElse("不明な活動")
    val createdDate = OffsetDateTime.parse(activityDto.created).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))

    Activity(
      id = activityDto.id,
      projectName = activityDto.project.name,
      typeStr = typeStr,
      contentSummary = activityDto.content.summary.getOrElse(""),
      createdUserName = activityDto.createdUser.name,
      created = createdDate
    )
  }
  private val typeMapping = Map(
    1 -> "課題の追加",
    2 -> "課題の更新",
    3 -> "課題にコメント",
    4 -> "課題の削除",
    5 -> "Wikiを追加",
    6 -> "Wikiを更新",
    7 -> "Wikiを削除",
    8 -> "共有ファイルを追加",
    9 -> "共有ファイルを更新",
    10 -> "共有ファイルを削除",
    11 -> "Subversionコミット",
    12 -> "GITプッシュ",
    13 -> "GITリポジトリ作成",
    14 -> "課題をまとめて更新",
    15 -> "ユーザーがプロジェクトに参加",
    16 -> "ユーザーがプロジェクトから脱退",
    17 -> "コメントにお知らせを追加",
    18 -> "プルリクエストの追加",
    19 -> "プルリクエストの更新",
    20 -> "プルリクエストにコメント",
    21 -> "プルリクエストの削除",
    22 -> "マイルストーンの追加",
    23 -> "マイルストーンの更新",
    24 -> "マイルストーンの削除",
    25 -> "グループがプロジェクトに参加",
    26 -> "グループがプロジェクトから脱退"
  )
}