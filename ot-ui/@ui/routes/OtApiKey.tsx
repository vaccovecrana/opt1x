import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { boxResult } from "@ui/components/Ui"
import { IcnRefresh } from "@ui/components/UiIcons"
import { apiV1KeyRotatePost, apiV1NsGet, OtApiKeyOp, OtKeyAccess } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { rpcUiHld } from "@ui/routes"

type OtApiKeyProps = RenderableProps<{ s?: UiStore }>
type OtApiKeyState = {
  access?: OtKeyAccess
  rotateOp?: OtApiKeyOp
}

class OtApiKey extends React.Component<OtApiKeyProps, OtApiKeyState> {

  componentDidMount(): void {
    this.loadNamespaces()
  }

  loadNamespaces() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsGet())
        .then(access => this.setState({...this.state, access}))
      , d
    )
  }

  rotateKey() {
    if (confirm("Rotate API key?")) {
      const { dispatch: d } = this.props.s
      rpcUiHld(
        lockUi(true, d)
          .then(() => apiV1KeyRotatePost())
          .then(rotateOp => {
            this.setState({...this.state, rotateOp})
          }), d
      )
    }
  }

  renderCard(title: string, content: any, footer: string) {
    return (
      <article className="card">
        <header>
          <h4>{title}</h4>
        </header>
        <p className="contrast">
          {content}
        </p>
        <footer>
          <small>{footer}</small>
        </footer>
      </article>
    )
  }

  render() {
    const {apiKey} = this.props.s.state
    return (
      <div className="container">
        <nav>
          <ul><li><h1>{apiKey?.path}</h1></li></ul>
          <ul>
            <li>
              <a className="ptr" onClick={() => this.rotateKey()}>
                <IcnRefresh height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.rotateOp && boxResult(
          this.state.rotateOp,
          <div>Key rotated: <code>{this.state.rotateOp?.raw}</code></div>
        )}
        {this.state.access && (
          <div className="grid">
            {this.renderCard("Groups", this.state.access.groupTree?.length || 0, "Accessible groups")}
            {this.renderCard("Namespaces", this.state.access.namespaceTree?.length || 0, "Accessible namespaces")}
          </div>
        )}
      </div>
    )
  }
}

export default (props: OtApiKeyProps) => <OtApiKey {...props} s={React.useContext(UiContext)} />