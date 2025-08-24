import { box, boxError, headers, row } from "@ui/components/Ui"
import { IcnAdd, IcnTree } from "@ui/components/UiIcons"
import { apiV1NamespaceIdConfigGet, apiV1NamespaceIdConfigPost, OtConfig, OtConfigOp, OtList } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { utcYyyyMmDdHhMm, rpcUiHld, uiNamespacesIdConfigIdFmt } from "@ui/util"
import { RenderableProps } from "preact"
import * as React from "preact/compat"

type OtConfigsProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtConfigsState = {
  cfgList?: OtList<OtConfig, string>
  cfgOp?: OtConfigOp
}

class OtConfigs extends React.Component<OtConfigsProps, OtConfigsState> {

  componentDidMount(): void {
    this.loadConfigs()
  }

  loadConfigs() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceIdConfigGet(this.props.nsId, 25, this.state.cfgList?.page?.nx1))
        .then(cfgList => this.setState({ ...this.state, cfgList })),
      d
    )
  }

  withEdit(name: string) {
    const cfgEdit = {...this.state.cfgOp, cfg: {...this.state.cfgOp?.cfg}}
    if (name !== undefined) {
      cfgEdit.cfg.name = name
    }
    this.setState({...this.state, cfgOp: cfgEdit})
  }

  saveConfig() {
    const { dispatch: d } = this.props.s
    this.state.cfgOp.cfg.nsId = this.props.nsId
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceIdConfigPost(this.props.nsId, this.state.cfgOp))
        .then(cfgOp => {
          if (cfgOp.error) {
            throw cfgOp
          } else if (cfgOp.cfg.cid) {
            this.setState({...this.state, cfgOp}, () => this.withEdit(""))
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
          <ul><li><h1>Configurations</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, cfgOp: {...{ cfg: { name: "" } }}})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.cfgOp?.error && boxError(this.state.cfgOp.error)}
        {this.state.cfgOp?.validations?.length > 0 && boxError(
          this.state.cfgOp.validations.map(v => <div>{v.message}</div>)
        )}
        {this.state.cfgOp?.cfg?.cid && box("Configuration created")}
        {this.state.cfgOp && (
          <div class="grid">
            <input placeholder="Name" value={this.state.cfgOp?.cfg?.name}
              onChange={e => this.withEdit((e.target as any).value)} />
            <input type="submit" value="Save"
              disabled={!this.state.cfgOp?.cfg?.name}
              onClick={() => this.saveConfig()}
            />
          </div>
        )}
        {this.state.cfgList && (
          <table class="striped">
            {headers(["Name", "Created", "Actions"])}
            <tbody>
              {this.state.cfgList.page.items.map(cfg => row([
                cfg.name,
                utcYyyyMmDdHhMm(cfg.createdAtUtcMs),
                <div class="row justify-center align-center">
                  <div class="col auto">
                    <a href={uiNamespacesIdConfigIdFmt(this.props.nsId, cfg.cid)}>
                      <IcnTree height={30} />
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
