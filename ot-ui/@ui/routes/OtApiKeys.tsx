import * as React from "preact/compat"

import { appendPage, boxResult, headers, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { IcnAdd } from "@ui/components/UiIcons"
import { apiV1KeyGet, apiV1KeyPost, OtApiKey, OtApiKeyOp, OtList } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { RenderableProps } from "preact"
import { rpcUiHld } from "@ui/routes"

type OtApiKeysProps = RenderableProps<{ s?: UiStore }>
type OtApiKeysState = {
  keys: OtList<OtApiKey, string>
  keyOp?: OtApiKeyOp
}

const BlankOp = (): OtApiKeyOp => ({ key: { leaf: true } })

class OtApiKeys extends React.Component<OtApiKeysProps, OtApiKeysState> {

  componentDidMount(): void {
    this.loadKeys()
  }

  loadKeys() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
      .then(() => apiV1KeyGet(2, this.state.keys?.page?.nx1))
      .then(keys => {
        if (keys.error) {
          throw keys
        } else {
          keys = this.state.keys ? appendPage(this.state.keys, keys) : keys
          this.setState({...this.state, keys})
        }
      }), d
    )
  }

  saveKey() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1KeyPost(this.state.keyOp))
        .then(keyOp => {
          if (keyOp.key.kid) {
            keyOp.key.name = ""
            this.setState({...this.state, keyOp})
            return this.loadKeys()
          } else {
            this.setState({...this.state, keyOp})
          }
        }), d
    )
  }

  withEdit(name: string, management: boolean) {
    const key = {...this.state.keyOp?.key}
    if (key) {
      if (name !== undefined) {
        key.name = name
      }
      if (management !== undefined) {
        key.leaf = !management
      }
      this.setState({...this.state, keyOp: {...this.state.keyOp, key}})
    }
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>API keys</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, keyOp: BlankOp()})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.keyOp && boxResult(
          this.state.keyOp,
          <div>Key created: <code>{this.state.keyOp.raw}</code></div>
        )}
        {this.state.keyOp && (
          <div class="grid">
            <input placeholder="Key Name" value={this.state.keyOp?.key.name || ""}
              onChange={e => this.withEdit((e.target as any).value, undefined)} />
            <fieldset>
              <input type="checkbox" id="manage"
                checked={!this.state.keyOp?.key?.leaf}
                onChange={e => this.withEdit(undefined, (e.target as any).checked)} />
              <label htmlFor="enc">Management key</label>
            </fieldset>
            <input type="submit" value="Save"
              disabled={!this.state.keyOp?.key?.name}
              onClick={() => this.saveKey()} />
          </div>
        )}
        {this.state.keys && (
          <div>
            <table class="striped">
              {headers(["Name", "Created", "Management"])}
              <tbody>
                {this.state.keys.page.items.map(k => row([
                  k.name,
                  utcYyyyMmDdHhMm(k.createUtcMs),
                  k.leaf ? "no" : "yes"
                ]))}
              </tbody>
            </table>
            {this.state.keys.page.nx1 && (
              <div class="grid">
                <button onClick={() => this.loadKeys()}>Load more</button>
              </div>
            )}
          </div>
        )}
      </div>
    )
  }
}

export default (props: OtApiKeysProps) => <OtApiKeys {...props} s={React.useContext(UiContext)} />
