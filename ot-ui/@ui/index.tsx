import * as React from "preact/compat"
import * as ReactDOM from "preact/compat"
import { useReducer } from "preact/hooks"
import Router from 'preact-router'

import { uiApiKeys, uiConfigsNsId, uiConfigsNsIdCid, uiGroups, uiGroupsId, uiInit, uiLogin, uiNs, uiNsNsId, uiUnseal, uiNsNsIdValues, uiValuesVid } from "@ui/routes"

import { initialState, UiContext, UiReducer } from "@ui/store"
import OtInit from "@ui/routes/OtInit"
import OtUnseal from "@ui/routes/OtUnseal"
import OtLogin from "@ui/routes/OtLogin"
import UiLock from "@ui/components/UiLock"
import UiMenu from "@ui/components/UiMenu"
import OtApiKeys from "@ui/routes/OtApiKeys"
import OtGroups from "@ui/routes/OtGroups"
import OtGroup from "@ui/routes/OtGroup"
import OtNamespaces from "@ui/routes/OtNamespaces"
import OtNamespace from "@ui/routes/OtNamespace"
import OtValues from "@ui/routes/OtValues"
import OtConfigs from "@ui/routes/OtConfigs"
import OtConfig from "@ui/routes/OtConfig"
import OtApiKey from "@ui/routes/OtApiKey"
import OtValue from "@ui/routes/OtValue"

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
                    <OtApiKeys    path={uiApiKeys} />
                    <OtGroups     path={uiGroups} />
                    <OtGroup      path={uiGroupsId} />
                    <OtNamespaces path={uiNs} />
                    <OtNamespace  path={uiNsNsId} />
                    <OtValues     path={uiNsNsIdValues} />
                    <OtValue      path={uiValuesVid} />
                    <OtConfigs    path={uiConfigsNsId} />
                    <OtConfig     path={uiConfigsNsIdCid} />
                    <OtApiKey     default />
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
