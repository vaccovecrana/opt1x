import * as React from "preact/compat"

import { box, boxError, headers, options, row } from "@ui/components/Ui"
import { IcnAdd } from "@ui/components/UiIcons"
import { apiV1KeyGet, apiV1KeyPost, OtApiKey, OtApiKeyOp, OtList, OtRole } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { utcYyyyMmDdHhMm, rpcUiHld } from "@ui/util"
import { RenderableProps } from "preact"

type OtApiKeysProps = RenderableProps<{ s?: UiStore }>
type OtApiKeysState = {
  keys: OtList<OtApiKey, string>
  keyEdit?: OtApiKeyOp
}

class OtApiKeys extends React.Component<OtApiKeysProps, OtApiKeysState> {

  componentDidMount(): void {
    this.loadKeys()
  }

  loadKeys() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
      .then(() => apiV1KeyGet(25, this.state.keys?.page?.nx1))
      .then(keys => this.setState({ keys })), d
    )
  }

  saveKey() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1KeyPost(this.state.keyEdit))
        .then(keyEdit => {
          if (keyEdit.error) {
            throw keyEdit
          } else {
            this.setState({ keyEdit: {...keyEdit, name: "", role: undefined} })
            return this.loadKeys()
          }
        }), d
    )
  }

  withEdit(name: string, role: string) {
    const keyEdit = {...this.state.keyEdit}
    if (name !== undefined) {
      keyEdit.name = name
    }
    if (role !== undefined) {
      keyEdit.role = role as OtRole
    }
    this.setState({...this.state, keyEdit})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>API keys</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, keyEdit: {}})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.keyEdit?.error && boxError(this.state.keyEdit.error)}
        {this.state.keyEdit?.validations?.length > 0 && boxError(
          this.state.keyEdit.validations.map(v => <div>{v.message}</div>)
        )}
        {this.state.keyEdit?.key?.kid && box(
          <div>Key created: <code>{this.state.keyEdit.raw}</code></div>
        )}
        {this.state.keyEdit && (
          <div class="grid">
            <input placeholder="Key Name" value={this.state.keyEdit?.name || ""}
              onChange={e => this.withEdit((e.target as any).value, undefined)}
            />
            <select
              required value={this.state.keyEdit?.role || ""}
              onChange={e => this.withEdit(undefined, (e.target as any).value)}>
              <option selected disabled value="">Select</option>
              {options([OtRole.Admin, OtRole.Application, OtRole.Auditor])}
            </select>
            <input type="submit" value="Save"
              disabled={!this.state.keyEdit?.name || !this.state.keyEdit?.role}
              onClick={() => this.saveKey()}
            />
          </div>
        )}
        {this.state.keys && (
          <table class="striped">
            {headers(["Name", "Role", "Created"])}
            <tbody>
              {this.state.keys.page.items.map(k => row([
                k.name, k.role, utcYyyyMmDdHhMm(k.createdAtUtcMs)
              ]))}
            </tbody>
          </table>
        )}
      </div>
    )
  }
}

export default (props: OtApiKeysProps) => <OtApiKeys {...props} s={React.useContext(UiContext)} />
