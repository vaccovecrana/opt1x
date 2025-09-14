import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { lockUi, UiContext, UiStore } from '@ui/store'
import { apiV1NsNsIdValGet, apiV1NsNsIdValPost, apiV1ValVidDelete, OtValue, OtValueOp, OtValueType } from '@ui/rpc'
import { IcnAdd, IcnDelete, IcnEdit } from "@ui/components/UiIcons"
import { boxHero, boxResult, headers, options, row, utcYyyyMmDdHhMm, valTruncate } from "@ui/components/Ui"
import { rpcUiHld, uiValuesVidFmt } from "@ui/routes"

type OtValuesVProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtValuesVState = {
  listOp?: OtValueOp
  editOp?: OtValueOp
  delOp?: OtValueOp
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
        .then(() => apiV1NsNsIdValGet(this.props.nsId, 25, undefined))
        .then((listOp) => this.setState({ listOp })),
        d
    )
  }

  saveValue() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsNsIdValPost(this.props.nsId, this.state.editOp))
        .then(editOp => {
          if (editOp.val?.vid !== 0) {
            this.setState({...this.state, editOp}, () => this.withEdit("", "", false, undefined))
            return this.loadValues()
          } else {
            this.setState({...this.state, editOp})
          }
        }), d
    )
  }

  deleteValue(v: OtValue) {
    if (confirm(`Delete value [${v.name}]?`)) {
      const { dispatch: d } = this.props.s
      rpcUiHld(
        lockUi(true, d)
          .then(() => apiV1ValVidDelete(v.vid))
          .then(delOp => {
            if (delOp.val?.vid !== 0) {
              this.setState({...this.state, delOp})
              return this.loadValues()
            } else {
              this.setState({...this.state, delOp})
            }
          }), d
      )
    }
  }

  withEdit(name: string, value: string, encrypted: boolean, type: OtValueType) {
    if (this.state.editOp?.val) {
      this.state.editOp.val = {...this.state.editOp.val}
      if (name !== undefined) {
        this.state.editOp.val.name = name
      }
      if (value !== undefined) {
        this.state.editOp.val.val = value
      }
      if (encrypted !== undefined) {
        this.state.editOp.val.encrypted = encrypted
      }
      if (type !== undefined) {
        this.state.editOp.val.type = type
      }
    }
    this.setState({...this.state, editOp: {...this.state.editOp}})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.listOp?.namespace.path}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, editOp: BlankOp()})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.editOp && boxResult(
          this.state.editOp,
          this.state.editOp?.val?.vid !== 0 && (
            <div>Value set</div>
          )
        )}
        {this.state.delOp && boxResult(
          this.state.delOp,
          this.state.delOp?.val?.vid !== 0 && (
            <div>Value deleted</div>
          )
        )}
        {this.state.editOp && (
          <div class="grid">
            <input placeholder="Name" value={this.state.editOp?.val?.name || ""}
              onChange={e => this.withEdit((e.target as any).value, undefined, undefined, undefined)}
            />
            <input placeholder="Value" value={this.state.editOp?.val?.val || ""}
              onChange={e => this.withEdit(undefined, (e.target as any).value, undefined, undefined)}
            />
            <select required value={this.state.editOp?.val?.type || NoType}
              onChange={e => this.withEdit(undefined, undefined, undefined, (e.target as any).value)}>
              <option value={NoType} disabled>Type</option>
              {options([OtValueType.Boolean, OtValueType.Number, OtValueType.String])}
            </select>
            {(this.state.editOp?.val?.type !== undefined && this.state.editOp?.val?.type !== OtValueType.Boolean) && (
              <fieldset>
                <input type="checkbox" id="enc"
                  checked={this.state.editOp?.val?.encrypted}
                  onChange={e => this.withEdit(undefined, undefined, (e.target as any).checked, undefined)}
                />
                <label htmlFor="enc">Encrypted</label>
              </fieldset>
            )}
            <input type="submit" value="Save"
              disabled={!this.state.editOp?.val?.name || !this.state.editOp?.val?.val || !this.state.editOp?.val?.type}
              onClick={() => this.saveValue()}
            />
          </div>
        )}
        {this.state.listOp?.valPage?.items?.length > 0 ? (
          <table class="striped">
            {headers(["Name", "Value", "Encrypted", "Created", "Actions"])}
            <tbody>
              {this.state.listOp.valPage.items.map((v) => {
                return row([
                  v.name,
                  <code>
                    <small>{v.encrypted ? "*****" : valTruncate(v.val)}</small>
                  </code>,
                  v.encrypted ? "Yes" : "No",
                  <small>{utcYyyyMmDdHhMm(v.createUtcMs)}</small>,
                  <div class="row">
                    <div class="col auto">
                      <a href={uiValuesVidFmt(v.vid)}>
                        <IcnEdit height={28} />
                      </a>
                    </div>
                    <div class="col auto">
                      <a class="ptr" onClick={() => this.deleteValue(v)}>
                        <IcnDelete height={28} />
                      </a>
                    </div>
                  </div>
                ])
              })}
            </tbody>
          </table>
        ) : boxHero([
          "Here you can define values that configuration trees can reference.",
          "Child namespaces can also define value lists that override values in parent namespaces."
        ])}
      </div>
    )
  }

}

export default (props: OtValuesVProps) => <OtValuesV {...props} s={React.useContext(UiContext)} />
