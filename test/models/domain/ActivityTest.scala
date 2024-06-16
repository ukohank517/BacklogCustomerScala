package models.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import models.domain.Activity
import models.dto._

import java.time.OffsetDateTime
import play.api.libs.json.Json

class ActivitySpec extends AnyFlatSpec with Matchers {
  val project = Project(
    id = 1,
    projectKey = "PRJ",
    name = "Project Name",
    chartEnabled = true,
    useResolvedForChart = false,
    subtaskingEnabled = true,
    projectLeaderCanEditProjectLeader = true,
    useWiki = false,
    useFileSharing = true,
    useWikiTreeView = false,
    useOriginalImageSizeAtWiki = true,
    textFormattingRule = "markdown",
    archived = false,
    displayOrder = 1,
    useDevAttributes = true
  )
  val nulabAccount = NulabAccount(
    nulabId = "nulab-id",
    name = "Nulab User",
    uniqueId = "unique-id",
    iconUrl = "http://example.com/icon.png"
  )
  val user = User(
    id = 1,
    userId = "user-id",
    name = "User Name",
    roleType = 1,
    lang = "ja",
    mailAddress = "user@example.com",
    nulabAccount = nulabAccount,
    keyword = "keyword",
    lastLoginTime = OffsetDateTime.now().toString
  )

  "Activity.fromActivityDto" should "convert normal" in {
    val content = Content(
      summary = Some("Activity Summary")
    )

    val activityDto = ActivityDto(
      id = 1,
      project = project,
      `type` = 1,
      content = content,
      createdUser = user,
      created = "2024-06-01T12:31:17Z"
    )

    val expectedActivity = Activity(
      id = 1,
      projectName = "Project Name",
      typeStr = "課題の追加",
      contentSummary = "Activity Summary",
      createdUserName = "User Name",
      created = "2024/06/01 12:31:17"
    )

    val activity = Activity.fromActivityDto(activityDto)
    activity shouldEqual expectedActivity
  }

  it should "missing content summary" in {
    val content = Content(
      summary = None
    )

    val activityDto = ActivityDto(
      id = 1,
      project = project,
      `type` = 1,
      content = content,
      createdUser = user,
      created = "2024-06-01T12:31:17Z"
    )

    val expectedActivity = Activity(
      id = 1,
      projectName = "Project Name",
      typeStr = "課題の追加",
      contentSummary = "", // missing
      createdUserName = "User Name",
      created = "2024/06/01 12:31:17"
    )

    val activity = Activity.fromActivityDto(activityDto)
    activity shouldEqual expectedActivity
  }

  it should "unknown activity" in {
    val content = Content(
      summary = Some("Activity Summary")
    )

    val activityDto = ActivityDto(
      id = 1,
      project = project,
      `type` = 999,
      content = content,
      createdUser = user,
      created = "2024-06-01T12:31:17Z"
    )

    val expectedActivity = Activity(
      id = 1,
      projectName = "Project Name",
      typeStr = "不明な活動", // unknown
      contentSummary = "Activity Summary",
      createdUserName = "User Name",
      created = "2024/06/01 12:31:17"
    )

    val activity = Activity.fromActivityDto(activityDto)
    activity shouldEqual expectedActivity
  }
}
