package views.html
package forum

import lila.api.Context
import lila.app.templating.Environment._
import lila.app.ui.ScalatagsTemplate._
import lila.common.paginator.Paginator

import controllers.routes

object categ {

  def index(categs: List[lila.forum.CategView])(implicit ctx: Context) = bits.layout(
    title = trans.forum.txt(),
    openGraph = lila.app.ui.OpenGraph(
      title = "Lichess community forum",
      url = s"$netBaseUrl${routes.ForumCateg.index.url}",
      description = "Chess discussions and feedback about lichess development"
    ).some
  ) {
      main(cls := "forum index box")(
        h1(dataIcon := "d", cls := "text")("Lichess Forum"),
        showCategs(categs.filterNot(_.categ.isTeam)),
        if (categs.exists(_.categ.isTeam)) frag(
          h1("Your teams boards"),
          showCategs(categs.filter(_.categ.isTeam))
        )
      )
    }

  def show(
    categ: lila.forum.Categ,
    topics: Paginator[lila.forum.TopicView],
    canWrite: Boolean,
    stickyPosts: List[lila.forum.TopicView]
  )(implicit ctx: Context) = {

    val newTopicButton = canWrite option
      a(href := routes.ForumTopic.form(categ.slug), cls := "button button-empty button-green text", dataIcon := "m")(
        trans.createANewTopic.frag()
      )
    def showTopic(sticky: Boolean)(topic: lila.forum.TopicView) = tr(cls := List("sticky" -> sticky))(
      td(cls := "subject")(
        a(href := routes.ForumTopic.show(categ.slug, topic.slug))(topic.name)
      ),
      td(cls := "right")(topic.views.localize),
      td(cls := "right")(topic.nbReplies.localize),
      td(
        topic.lastPost.map { post =>
          frag(
            a(href := s"${routes.ForumTopic.show(categ.slug, topic.slug, topic.lastPage)}#${post.number}")(
              momentFromNow(post.createdAt)
            ),
            br,
            trans.by.frag(authorLink(post))
          )
        }
      )
    )
    val bar = div(cls := "bar")(
      bits.pagination(routes.ForumCateg.show(categ.slug, 1), topics, showPost = false),
      newTopicButton
    )

    bits.layout(
      title = categ.name,
      openGraph = lila.app.ui.OpenGraph(
        title = s"Forum: ${categ.name}",
        url = s"$netBaseUrl${routes.ForumCateg.show(categ.slug).url}",
        description = categ.desc
      ).some
    ) {
        main(cls := "forum forum-categ box")(
          h1(
            a(
              href := categ.team.fold(routes.ForumCateg.index)(routes.Team.show(_)),
              dataIcon := "I",
              cls := "text"
            ),
            categ.team.fold(frag(categ.name))(teamIdToName)
          ),
          bar,
          table(cls := "topics slist slist-pad")(
            thead(
              tr(
                th,
                th(cls := "right")(trans.views.frag()),
                th(cls := "right")(trans.replies.frag()),
                th(trans.lastPost.frag())
              )
            ),
            tbody(
              stickyPosts map showTopic(true),
              topics.currentPageResults map showTopic(false)
            )
          ),
          bar
        )
      }
  }

  private def showCategs(categs: List[lila.forum.CategView])(implicit ctx: Context) =
    table(cls := "categs slist slist-pad")(
      thead(
        tr(
          th,
          th(cls := "right")(trans.topics.frag()),
          th(cls := "right")(trans.posts.frag()),
          th(trans.lastPost.frag())
        )
      ),
      tbody(
        categs.map { categ =>
          tr(
            td(cls := "subject")(
              h2(a(href := routes.ForumCateg.show(categ.slug))(categ.name)),
              p(categ.desc)
            ),
            td(cls := "right")(categ.nbTopics.localize),
            td(cls := "right")(categ.nbPosts.localize),
            td(
              categ.lastPost.map {
                case (topic, post, page) => frag(
                  a(href := s"${routes.ForumTopic.show(categ.slug, topic.slug, page)}#${post.number}")(
                    momentFromNow(post.createdAt)
                  ),
                  br,
                  trans.by.frag(authorName(post))
                )
              }
            )
          )
        }
      )
    )
}
