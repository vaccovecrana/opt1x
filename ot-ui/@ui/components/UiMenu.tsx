import * as React from "preact/compat"

import { IcnCode, IcnKey, IcnGroup, IcnLogo, IcnLogout } from "@ui/components/UiIcons"
import { UiContext } from "@ui/store"
import { avatar } from "@ui/components/Ui"
import { logout, uiApiKeys, uiGroups, uiNs, uiRoot } from "@ui/routes"
import UiVersion from "@ui/components/UiVersion"

const UiMenu = () => {
  const s = React.useContext(UiContext)
  const {apiKey} = s.state
  return (
    <div class="col xs-12 sm-12 md-3 lg-2">
      <div class="row sm-down-hide">
        <div class="col auto">
          <div class="sm-down-hide pt16" />
          <aside>
            <nav>
              <ul>
                <li>
                  <a href={uiRoot} class="secondary">
                    <IcnLogo height={64} /><code class="ml8">Opt1x</code>
                  </a>
                </li>
                {apiKey && !apiKey.leaf && (
                  <li>
                    <a href={uiApiKeys} class="secondary mt16">
                      <IcnKey height={32} /><small class="ml8">API Keys</small>
                    </a>
                  </li>
                )}
                <li>
                  <a href={uiGroups} class="secondary">
                    <IcnGroup height={32} /><small class="ml8">Key Groups</small>
                  </a>
                </li>
                <li>
                  <a href={uiNs} class="secondary">
                    <IcnCode height={32} /><small class="ml8">Namespaces</small>
                  </a>
                </li>
                <li>
                  <div class="col auto">
                    <a class="ptr secondary" onClick={() => logout()}>
                      <IcnLogout height={32} /><small class="ml8">Logout</small>
                    </a>
                  </div>
                </li>
                {s.state.apiKey && avatar(s.state.apiKey, true)}
              </ul>
            </nav>
            <div class="mt16">
              <UiVersion />
            </div>
          </aside>
        </div>
      </div>
      <div class="row md-up-hide">
        <div class="col auto">
          <nav>
            <ul>
              <li>
                <div class="row">
                  <div class="col auto">
                    <a href={uiRoot}><IcnLogo height={48} /></a>
                  </div>
                  <div class="col auto">
                    <UiVersion />                    
                  </div>
                </div>
              </li>
            </ul>
            <ul>
              {apiKey && !apiKey.leaf && (<li><a href={uiApiKeys}><IcnKey height={32} /></a></li>)}
              <li><a href={uiGroups}><IcnGroup height={32} /></a></li>
              <li><a href={uiNs}><IcnCode height={32} /></a></li>
              <li><a class="ptr" onClick={() => logout()}><IcnLogout height={32} /></a></li>
              {s.state.apiKey && avatar(s.state.apiKey, false)}
            </ul>
          </nav>
        </div>
      </div>
    </div>
  )
}

export default UiMenu
