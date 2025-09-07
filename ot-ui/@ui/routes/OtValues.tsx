import * as React from "preact/compat"

import { lockUi, UiContext, UiStore } from '@ui/store'
import { apiV1ValueNsIdGet, apiV1ValueNsIdPost, OtValueOp, OtValueType } from '@ui/rpc'
import { RenderableProps } from "preact"
import { IcnAdd } from "@ui/components/UiIcons"
import { boxResult, headers, options, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { rpcUiHld } from "."

type OtValuesVProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtValuesVState = {
  valOp?: OtValueOp
  valEdit?: OtValueOp
}

const NoType = ""
const BlankOp = (): OtValueOp => ({ val: { vid: 0, encrypted: false } })

class OtValuesV extends React.Component<OtValuesVProps, OtValuesVState> {

  componentDidMount() {
    this.loadValues()
  }

  loadValues() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1ValueNsIdGet(this.props.nsId, 25, undefined))
        .then((valOp) => this.setState({ valOp })),
        d
    )
  }

  saveValue() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1ValueNsIdPost(this.props.nsId, this.state.valEdit))
        .then(valEdit => {
          if (valEdit.val?.vid !== 0) {
            this.setState({...this.state, valEdit}, () => this.withEdit("", "", false, undefined))
            return this.loadValues()
          } else {
            this.setState({...this.state, valEdit})
          }
        }), d
    )
  }

  withEdit(name: string, value: string, encrypted: boolean, type: OtValueType) {
    if (this.state.valEdit?.val) {
      this.state.valEdit.val = {...this.state.valEdit.val}
      if (name !== undefined) {
        this.state.valEdit.val.name = name
      }
      if (value !== undefined) {
        this.state.valEdit.val.value = value
      }
      if (encrypted !== undefined) {
        this.state.valEdit.val.encrypted = encrypted
      }
      if (type !== undefined) {
        this.state.valEdit.val.type = type
      }
    }
    this.setState({...this.state, valEdit: {...this.state.valEdit}})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.valOp?.namespace.path}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, valEdit: BlankOp()})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.valEdit && boxResult(
          this.state.valEdit,
          this.state.valEdit?.val?.vid !== 0 && (
            <div>Value created</div>
          )
        )}
        {this.state.valEdit && (
          <div class="grid">
            <input placeholder="Name" value={this.state.valEdit?.val?.name || ""}
              onChange={e => this.withEdit((e.target as any).value, undefined, undefined, undefined)}
            />
            <input placeholder="Value" value={this.state.valEdit?.val?.value || ""}
              onChange={e => this.withEdit(undefined, (e.target as any).value, undefined, undefined)}
            />
            <select required value={this.state.valEdit?.val?.type || NoType}
              onChange={e => this.withEdit(undefined, undefined, undefined, (e.target as any).value)}>
              <option value={NoType} disabled>Type</option>
              {options([OtValueType.Boolean, OtValueType.Number, OtValueType.String])}
            </select>
            {(this.state.valEdit?.val?.type !== undefined && this.state.valEdit?.val?.type !== OtValueType.Boolean) && (
              <fieldset>
                <input type="checkbox" id="enc"
                  checked={this.state.valEdit?.val?.encrypted}
                  onChange={e => this.withEdit(undefined, undefined, (e.target as any).checked, undefined)}
                />
                <label htmlFor="enc">Encrypted</label>
              </fieldset>
            )}
            <input type="submit" value="Save"
              disabled={!this.state.valEdit?.val?.name || !this.state.valEdit?.val?.value || !this.state.valEdit?.val?.type}
              onClick={() => this.saveValue()}
            />
          </div>
        )}
        {this.state.valOp?.valPage?.items?.length > 0 && (
          <table class="striped">
            {headers(["Name", "Value", "Encrypted", "Created"])}
            <tbody>
              {this.state.valOp.valPage.items.map((v) => {
                return row([
                  v.name,
                  v.encrypted ? "*****" : v.value,
                  v.encrypted ? "Yes" : "No",
                  utcYyyyMmDdHhMm(v.createUtcMs)
                ])
              })}
            </tbody>
          </table>
        )}
      </div>
    )
  }

}

export default (props: OtValuesVProps) => <OtValuesV {...props} s={React.useContext(UiContext)} />
