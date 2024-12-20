package views.html

import net.wa9nnn.rc210.KeyMetadata
import net.wa9nnn.rc210.KeyMetadata.Port.tabKind
import net.wa9nnn.rc210.ui.{Tab, Tabs}
import net.wa9nnn.rc210.ui.nav.TabKind
import play.twirl.api.Html
import views.html.navMainPage

import javax.inject.{Inject, Singleton}

/**
 * A shim that allows multiple get methods
 *
 * @param navMainPage the actual Html contins of the NavMain.
 */
@Singleton
class NavMain @Inject()(navMainPage: navMainPage):
  def apply(activeTab: Tab, content: Html) =
    navMainPage(activeTab.tabKind, activeTab,  content)

  def apply(tabKind: TabKind) =
    navMainPage(tabKind, Tabs.noTab, views.html.landing())

