package net.wa9nnn.rc210.security.authorzation

import be.objectify.deadbolt.scala.filters.{AuthorizedRoute, AuthorizedRoutes, FilterConstraints}
import com.typesafe.scalalogging.LazyLogging

import javax.inject.{Inject, Singleton}

@Singleton
class RcAuthorizedRoutes @Inject()(filterConstraints: FilterConstraints) extends AuthorizedRoutes with LazyLogging{
logger.debug("AmAuthorizedRoutes")
  override val routes: Seq[AuthorizedRoute] = {
    Seq.empty
//    Seq(
//      AuthorizedRoute(Any, "/link", filterConstraints.subjectPresent),
//    )
  }
}