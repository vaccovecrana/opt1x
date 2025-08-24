import * as React from "preact/compat"
import * as ReactDOM from "preact/compat"
import { useReducer } from "preact/hooks"
import Router from 'preact-router'

import { initialState, UiContext, UiReducer } from "@ui/store"
import { uiApiKeys, uiInit, uiLogin, uiNamespaces, uiNamespacesId, uiNamespacesIdConfig, uiNamespacesIdConfigId, uiNamespacesIdValues, uiUnseal } from "@ui/util"
import OtInit from "@ui/routes/OtInit"
import OtUnseal from "@ui/routes/OtUnseal"
import OtLogin from "@ui/routes/OtLogin"
import UiLock from "@ui/components/UiLock"
import UiMenu from "@ui/components/UiMenu"
import OtApiKeys from "@ui/routes/OtApiKeys"
import OtNamespaces from "@ui/routes/OtNamespaces"
import OtNamespace from "@ui/routes/OtNamespace"
import OtValues from "@ui/routes/OtValues"
import OtConfig from "@ui/routes/OtConfig"
import OtConfigs from "@ui/routes/OtConfigs"

class UiShell extends React.Component {
  public render() {
    const [state, dispatch] = useReducer(UiReducer, initialState)
    return (
      <UiContext.Provider value={{state, dispatch}}>
        <UiLock>
          <div class="container">
            <Router>
              <OtInit path={uiInit} />
              <OtUnseal path={uiUnseal} />
              <OtLogin path={uiLogin} />
              <div class="row" default>
                <UiMenu />
                <div class="col auto">
                  <Router>
                    <OtApiKeys path={uiApiKeys} />
                    <OtNamespaces path={uiNamespaces} />
                    <OtNamespace path={uiNamespacesId} />
                    <OtValues path={uiNamespacesIdValues} />
                    <OtConfigs path={uiNamespacesIdConfig} />
                    <OtConfig path={uiNamespacesIdConfigId} />
                    <div default>
                      <h1>Dashboard?</h1>
                    </div>
                  </Router>
                </div>
              </div>
            </Router>
          </div>
        </UiLock>
      </UiContext.Provider>
    )
  }
}

var app = document.getElementById("app")

if (app) {
  ReactDOM.render(<UiShell />, app)
}
