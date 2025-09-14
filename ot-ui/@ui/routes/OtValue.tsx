import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { lockUi, UiContext, UiStore } from '@ui/store'
import { apiV1NsNsIdValPost, apiV1ValVerVvIdDelete, apiV1ValVerVvIdPatch, apiV1ValVidVerGet, OtValueOp, OtValueType, OtValueVer } from '@ui/rpc'
import { boxHero, headers, options, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { rpcUiHld } from "@ui/routes"
import { IcnDelete, IcnRefresh } from "@ui/components/UiIcons"

type OtValueProps = RenderableProps<{ s?: UiStore, vid?: number }>
type OtValueState = { valOp?: OtValueOp }

const NoType = ""

class OtValue extends React.Component<OtValueProps, OtValueState> {

  componentDidMount() {
    this.loadValue()
  }

  loadValue() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1ValVidVerGet(this.props.vid))
        .then((valOp) => this.setState({ valOp })),
        d
    )
  }

  saveValue() {
    const { dispatch: d } = this.props.s
    if (!this.state.valOp.error) {
      this.state.valOp.valVersions = undefined
    }
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsNsIdValPost(this.state.valOp.val.nsId, this.state.valOp))
        .then(valOp => this.setState({...this.state, valOp})), d
    )
  }

  deleteVersion(vv: OtValueVer) {
    if (confirm(`Delete value version [${vv.val}] ?`)) {
      const { dispatch: d } = this.props.s
      if (!this.state.valOp.error) {
        this.state.valOp.valVersions = undefined
      }
      rpcUiHld(
        lockUi(true, d)
          .then(() => apiV1ValVerVvIdDelete(vv.vvId))
          .then(valOp => this.setState({...this.state, valOp})), d
      )
    }
  }

  restoreVersion(vv: OtValueVer) {
    if (confirm(`Restore value version [${vv.val}] ?`)) {
      const { dispatch: d } = this.props.s
      if (!this.state.valOp.error) {
        this.state.valOp.valVersions = undefined
      }
      rpcUiHld(
        lockUi(true, d)
          .then(() => apiV1ValVerVvIdPatch(vv.vvId))
          .then(valOp => this.setState({...this.state, valOp})), d
      )
    }
  }

  withEdit(value: string, type: OtValueType, notes: string) {
    this.state.valOp.val = {...this.state.valOp.val}
    if (value !== undefined) {
      this.state.valOp.val.val = value
    }
    if (type !== undefined) {
      this.state.valOp.val.type = type
    }
    if (notes !== undefined) {
      this.state.valOp.val.notes = notes
    }
    this.setState({...this.state, valOp: {...this.state.valOp}})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.valOp?.namespace.path}</h1></li></ul>
        </nav>
        {this.state.valOp && (
          <h3>{this.state.valOp.val.name}</h3>
        )}
        {this.state.valOp && (
          <div class="mt16">
            <div class="grid">
              <select
                required value={this.state.valOp?.val?.type || NoType}
                onChange={e => this.withEdit(undefined, (e.target as any).value, undefined)}>
                <option value={NoType} disabled>Type</option>
                {options([OtValueType.Boolean, OtValueType.Number, OtValueType.String])}
              </select>
              <input type="submit" value="Save" onClick={() => this.saveValue()} />
            </div>
            <div class="grid">
              <textarea
                placeholder="Value" value={this.state.valOp?.val?.val || ""}
                onChange={e => this.withEdit((e.target as any).value, undefined, undefined)} />
            </div>
            <div class="grid">
              <input
                placeholder="Notes" value={this.state.valOp?.val?.notes || ""}
                onChange={e => this.withEdit(undefined, undefined, (e.target as any).value)} />
            </div>
          </div>
        )}
        {this.state.valOp?.valVersions?.length > 0 ? (
          <table class="striped">
            {headers(["Value", "Type", "Changed", "Notes", "Actions"])}
            <tbody>
              {this.state.valOp.valVersions.map((vv) => {
                return row([
                  <code>
                    <small>{this.state.valOp.val.encrypted ? "*****" : vv.val}</small>
                  </code>,
                  vv.type,
                  <small>{utcYyyyMmDdHhMm(vv.changeUtcMs)}</small>,
                  vv.notes,
                  <div class="row">
                    <div class="col auto">
                      <a class="ptr" onClick={() => this.restoreVersion(vv)}>
                        <IcnRefresh height={28} />
                      </a>
                    </div>
                    <div class="col auto">
                      <a class="ptr" onClick={() => this.deleteVersion(vv)}>
                        <IcnDelete height={28} />
                      </a>
                    </div>
                  </div>
                ])
              })}
            </tbody>
          </table>
        ) : boxHero([
          "Value versions will appear here whenever this value changes."
        ])}
      </div>
    )
  }

}

export default (props: OtValueProps) => <OtValue {...props} s={React.useContext(UiContext)} />
