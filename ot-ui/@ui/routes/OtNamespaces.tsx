import { RenderableProps } from "preact"
import * as React from "preact/compat"

import { flagsOf, headers, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { apiV1NsGet, OtGroup, OtGroupNs, OtKeyAccess } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { rpcUiHld, uiConfigsNsIdFmt, uiNsNsIdFmt, uiNsNsIdValuesFmt } from "@ui/routes"
import { IcnBook, IcnTree } from "@ui/components/UiIcons"

type OtNsProps = RenderableProps<{ s?: UiStore }>
type OtNsState = { access?: OtKeyAccess }

class OtNamespaces extends React.Component<OtNsProps, OtNsState> {

  componentDidMount(): void {
    this.loadNamespaces()
  }

  loadNamespaces() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsGet())
        .then(access => this.setState({ ...this.state, access }))
      , d
    )
  }

  renderGrants(grants: OtGroupNs[], groups: OtGroup[]) {
    return grants.map(gns => {
      const group = groups.find(g => gns.gid === g.gid)
      return (
        <div>
          <code>{group.name} | {flagsOf(gns)}</code>
        </div>
      )
    })
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>Namespaces</h1></li></ul>
        </nav>
        {this.state.access && (
          <table class="striped">
            {headers(["Name", "Grant group/flags", "Created", "Actions"])}
            <tbody>
              {this.state.access.namespaces.map(ns => {
                const grants = this.state.access.groupNamespaces.filter(gns => (
                  gns.nsId === ns.nsId
                ))
                const canRw = grants.find(gns => gns.read || gns.write)
                const canManage = grants.find(gns => gns.manage)
                return row([
                  canManage
                    ? <a href={uiNsNsIdFmt(ns.nsId)}>{ns.name}</a>
                    : ns.name,
                  this.renderGrants(grants, this.state.access.groups),
                  <small>{utcYyyyMmDdHhMm(ns.createUtcMs)}</small>,
                  canRw && (
                    <div class="row justify-center">
                      <div class="col auto">
                        <a href={uiNsNsIdValuesFmt(ns.nsId)}>
                          <IcnBook height={28} />
                        </a>
                      </div>
                      <div class="col auto">
                        <a href={uiConfigsNsIdFmt(ns.nsId)}>
                          <IcnTree height={28} />
                        </a>
                      </div>
                    </div>
                  )
                ])
              })}
            </tbody>
          </table>
        )}
      </div>
    )
  }
}

export default (props: OtNsProps) => <OtNamespaces {...props} s={React.useContext(UiContext)} />
