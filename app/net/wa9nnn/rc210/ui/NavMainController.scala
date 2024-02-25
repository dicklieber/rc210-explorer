package net.wa9nnn.rc210.ui

import com.typesafe.scalalogging.LazyLogging
import play.api.i18n.I18nSupport
import play.api.mvc.*
import views.html.NavMain

import javax.inject.Inject

abstract class NavMainController(components: ControllerComponents)
  extends AbstractController(components)
    with LazyLogging with I18nSupport:
  @Inject
  @volatile protected var navMain: NavMain = _
 


