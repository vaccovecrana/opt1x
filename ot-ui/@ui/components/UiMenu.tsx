import * as React from "preact/compat"

import { uiApiKeys, uiNamespaces, uiRoot } from "@ui/util"
import { IcnCode, IcnDatabase, IcnLogo } from "@ui/components/UiIcons"
import { UiContext } from "@ui/store"
import { avatar } from "./Ui"

const UiMenu = () => {
  const s = React.useContext(UiContext)
  return (
    <div class="col xs-12 col md-3 col lg-2">
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
                <li>
                  <a href={uiApiKeys} class="secondary mt16">
                    <IcnDatabase height={32} /><small class="ml8">API Keys</small>
                  </a>
                </li>
                <li>
                  <a href={uiNamespaces} class="secondary">
                    <IcnCode height={32} /><small class="ml8">Namespaces</small>
                  </a>
                </li>
                {s.state.apiKey && avatar(s.state.apiKey, true)}
              </ul>
            </nav>
          </aside>
        </div>
      </div>
      <div class="row md-up-hide">
        <div class="col auto">
          <nav>
            <ul>
              <li><a href={uiRoot}><IcnLogo height={48} /></a></li>
            </ul>
            <ul>
              <li><a href={uiApiKeys}><IcnDatabase height={32} /></a></li>
              <li><a href={uiNamespaces}><IcnCode height={32} /></a></li>
              {s.state.apiKey && avatar(s.state.apiKey, false)}
            </ul>
          </nav>
        </div>
      </div>
    </div>
  )
}

export default UiMenu
