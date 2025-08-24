import * as React from "preact/compat"

import { lockUi, UiContext, UiStore } from '@ui/store'
import { apiV1NamespaceIdGet, apiV1NamespaceIdValueGet, apiV1NamespaceIdValuePost, OtList, OtNamespace, OtValue, OtValueOp, OtValueType } from '@ui/rpc'
import { rpcUiHld, utcYyyyMmDdHhMm } from '@ui/util'
import { RenderableProps } from "preact"
import { IcnAdd } from "@ui/components/UiIcons"
import { boxError } from "@ui/components/Ui"

type OtValuesVProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtValuesVState = {
  ns?: OtNamespace
  values?: OtList<OtValue, string>
  valOp?: OtValueOp
}

const NoType = ""
const BlankOp: Partial<OtValueOp> = { val: { vid: 0, encrypted: false } }

class OtValuesV extends React.Component<OtValuesVProps, OtValuesVState> {

  componentDidMount() {
    this.loadData()
  }

  loadData() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => Promise.all([
          apiV1NamespaceIdGet(this.props.nsId),
          apiV1NamespaceIdValueGet(this.props.nsId, 100, undefined)
        ]))
        .then(([ns, values]) => this.setState({ ns: ns.page.items[0], values })),
        d
    )
  }

  saveValue() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceIdValuePost(this.props.nsId, this.state.valOp))
        .then(valOp => {
          if (valOp.error) {
            throw valOp
          } else if (valOp.val.vid) {
            this.setState({...this.state, valOp}, () => this.withEdit("", "", false, undefined))
            return this.loadData()
          } else {
            this.setState({...this.state, valOp})
          }
        }), d
    )
  }

  withEdit(name: string, value: string, encrypted: boolean, type: OtValueType) {
    if (this.state.valOp?.val) {
      this.state.valOp.val = {...this.state.valOp.val}
      if (name !== undefined) {
        this.state.valOp.val.name = name
      }
      if (value !== undefined) {
        this.state.valOp.val.value = value
      }
      if (encrypted !== undefined) {
        this.state.valOp.val.encrypted = encrypted
      }
      if (type !== undefined) {
        this.state.valOp.val.type = type
      }
    }
    this.setState({...this.state, valOp: {...this.state.valOp}})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.ns?.name}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, valOp: {...BlankOp}})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.valOp?.error && boxError(this.state.valOp.error)}
        {this.state.valOp?.validations?.length > 0 && boxError(
          this.state.valOp.validations.map(v => <div>{v.message}</div>)
        )}
        {this.state.valOp?.val && this.state.valOp?.val?.vid !== 0 && (
          <article>Value created</article>
        )}
        {this.state.valOp && (
          <div class="grid">
            <input placeholder="Name" value={this.state.valOp?.val?.name || ""}
              onChange={e => this.withEdit((e.target as any).value, undefined, undefined, undefined)}
            />
            <input placeholder="Value" value={this.state.valOp?.val?.value || ""}
              onChange={e => this.withEdit(undefined, (e.target as any).value, undefined, undefined)}
            />
            <select required value={this.state.valOp?.val?.type || NoType}
              onChange={e => this.withEdit(undefined, undefined, undefined, (e.target as any).value)}>
              <option value={NoType} disabled>Type</option>
              <option value={OtValueType.Boolean}>{OtValueType.Boolean}</option>
              <option value={OtValueType.Number}>{OtValueType.Number}</option>
              <option value={OtValueType.String}>{OtValueType.String}</option>
            </select>
            {(this.state.valOp?.val?.type !== undefined && this.state.valOp?.val?.type !== OtValueType.Boolean) && (
              <fieldset>
                <input type="checkbox" id="enc"
                  checked={this.state.valOp?.val?.encrypted}
                  onChange={e => this.withEdit(undefined, undefined, (e.target as any).checked, undefined)}
                />
                <label htmlFor="enc">Encrypted</label>
              </fieldset>
            )}
            <input type="submit" value="Save"
              disabled={!this.state.valOp?.val?.name || !this.state.valOp?.val?.value}
              onClick={() => this.saveValue()}
            />
          </div>
        )}
        {this.state.values && (
          <table class="striped">
            <thead>
              <tr>
                <th>Name</th>
                <th>Value</th>
                <th>Encrypted</th>
                <th>Created At</th>
              </tr>
            </thead>
            <tbody>
              {this.state.values.page.items.map((v) => (
                <tr key={v.vid}>
                  <td>{v.name}</td>
                  <td>{v.encrypted ? "*****" : v.value}</td>
                  <td>{v.encrypted ? "Yes" : "No"}</td>
                  <td>{utcYyyyMmDdHhMm(v.createdAtUtcMs)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    )
  }

}

export default (props: OtValuesVProps) => <OtValuesV {...props} s={React.useContext(UiContext)} />
