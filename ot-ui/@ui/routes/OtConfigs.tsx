import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { boxResult, headers, options, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { IcnAdd, IcnCopy, IcnTree } from "@ui/components/UiIcons"
import { apiV1CfgGet, apiV1CfgPost, apiV1NsNsIdGet, OtConfig, OtConfigOp, OtKeyAccess, OtList } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { rpcUiHld, uiConfigsNsIdCidFmt, uiConfigsNsIdFmt } from "@ui/routes"

type OtConfigsProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtConfigsState = {
  access?: OtKeyAccess
  cfgOp?: OtConfigOp
  cpyOp?: OtConfigOp
  cfgList?: OtList<OtConfig, string>
}

const NoNsId = -1

class OtConfigs extends React.Component<OtConfigsProps, OtConfigsState> {

  componentDidMount(): void {
    this.loadConfigs()
    this.loadAccess()
  }

  loadAccess() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsNsIdGet(this.props.nsId))
        .then(access => this.setState({...this.state, access})),
      d
    )
  }

  loadConfigs() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1CfgGet(this.props.nsId, 25, this.state.cfgList?.page?.nx1))
        .then(cfgList => this.setState({...this.state, cfgList})),
      d
    )
  }

  configEdit(name: string) {
    const cfgEdit = {...this.state.cfgOp, cfg: {...this.state.cfgOp?.cfg}}
    if (name !== undefined) {
      cfgEdit.cfg.name = name
    }
    this.setState({...this.state, cfgOp: cfgEdit})
  }

  copyEdit(name: string, nsId: number) {
    const cfg = {...this.state.cpyOp.cfg}
    if (name !== undefined) {
      cfg.name = name
    }
    if (nsId !== undefined) {
      cfg.nsId = nsId
    }
    this.setState({...this.state, cpyOp: {...this.state.cpyOp, cfg}})
  }

  cloneConfig() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1CfgPost(this.state.cpyOp.cfg.nsId, this.state.cpyOp))
        .then(cpyOp => {
          if (!cpyOp.error && cpyOp.cfg.cid) {
            window.location.href = uiConfigsNsIdFmt(cpyOp.cfg.nsId)
          } else {
            this.setState({...this.state, cpyOp})
          }
        }), d
    )
  }

  saveConfig() {
    const { dispatch: d } = this.props.s
    this.state.cfgOp.cfg.nsId = this.props.nsId
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1CfgPost(this.props.nsId, this.state.cfgOp))
        .then(cfgOp => {
          if (cfgOp.cfg.cid) {
            this.setState({...this.state, cfgOp}, () => this.configEdit(""))
            return this.loadConfigs()
          } else {
            this.setState({...this.state, cfgOp})
          }
        }), d
    )
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.access?.namespace?.path}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, cfgOp: {...{ cfg: { name: "" } }}})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.cfgOp && boxResult(
          this.state.cfgOp,
          this.state.cfgOp?.cfg?.cid && "Configuration created"
        )}
        {this.state.cpyOp && boxResult(
          this.state.cpyOp,
          this.state.cpyOp?.cfg?.cid && "Configuration cloned"
        )}
        {this.state.cfgOp && (
          <div class="grid">
            <input placeholder="Name" value={this.state.cfgOp?.cfg?.name}
              onChange={e => this.configEdit((e.target as any).value)} />
            <input type="submit" value="Save"
              disabled={!this.state.cfgOp?.cfg?.name}
              onClick={() => this.saveConfig()}
            />
          </div>
        )}
        {this.state.cpyOp && this.state.access && (
          <div class="grid">
            <input placeholder="Copy Name" value={this.state.cpyOp?.cfg.name || ""}
              onChange={e => this.copyEdit((e.target as any).value, undefined)}
            />
            <select
              required value={this.state.cpyOp.cfg?.nsId}
              onChange={e => this.copyEdit(undefined, parseInt((e.target as any).value))}>
              <option selected disabled value={NoNsId}>Target Namespace</option>
              {options(
                this.state.access.namespaceTree,
                ns => ns.name, ns => ns.nsId
              )}
            </select>
            <input type="submit" value="Clone"
              disabled={!this.state.cpyOp?.cfg.name || !this.state.cpyOp?.cfg?.nsId}
              onClick={() => this.cloneConfig()}
            />
          </div>
        )}
        {this.state.cfgList && (
          <table class="striped">
            {headers(["Name", "Created", "Actions"])}
            <tbody>
              {this.state.cfgList.page.items.map(cfg => row([
                cfg.name,
                <small>{utcYyyyMmDdHhMm(cfg.createUtcMs)}</small>,
                <div class="row justify-center align-center">
                  <div class="col auto">
                    <a href={uiConfigsNsIdCidFmt(this.props.nsId, cfg.cid)}>
                      <IcnTree height={30} />
                    </a>
                  </div>
                  <div class="col auto">
                    <a class="ptr"
                      onClick={() => this.setState({
                        ...this.state,
                        cpyOp: { cfg: { cid: cfg.cid, name: `${cfg.name}-copy`, nsId: cfg.nsId }, cfgClone: true }
                      })}>
                      <IcnCopy height={30} />
                    </a>
                  </div>
                </div>
              ]))}
            </tbody>
          </table>
        )}
      </div>
    )
  }
}

export default (props: OtConfigsProps) => <OtConfigs {...props} s={React.useContext(UiContext)} />
