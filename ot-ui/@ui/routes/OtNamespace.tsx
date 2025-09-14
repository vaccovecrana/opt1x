import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { boxResult, flagsOf, headers, options, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { apiV1NsNsIdGet, apiV1NsPost, OtAdminOp, OtKeyAccess } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { rpcUiHld, uiConfigsNsIdFmt, uiNsNsIdFmt, uiNsNsIdValuesFmt } from "@ui/routes"
import { IcnAdd, IcnBind, IcnBook, IcnTree } from "@ui/components/UiIcons"

type OtNsProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtNsState = { access?: OtKeyAccess, adminOp?: OtAdminOp }

const NoId = -1

const BlankBindOp = (): OtAdminOp => ({
  groupNs: {
    gid: NoId, nsId: undefined,
    read: false, write: false, manage: false
  }
})

const BlankNsOp = (): OtAdminOp => ({
  ns: { name: undefined }
})

class OtNamespace extends React.Component<OtNsProps, OtNsState> {

  componentDidMount(): void {
    this.loadNamespaces()
  }

  componentDidUpdate(previousProps: Readonly<OtNsProps>): void {
    if (previousProps.nsId !== this.props.nsId) {
      this.setState({...this.state, adminOp: undefined})
      this.loadNamespaces()
    }
  }

  loadNamespaces() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsNsIdGet(this.props.nsId))
        .then(access => this.setState({...this.state, access}))
      , d
    )
  }

  bindEdit(gid: number, nsId: number, read?: boolean, write?: boolean, manage?: boolean) {
    const groupNs = {...this.state.adminOp.groupNs}
    if (gid !== undefined) {
      groupNs.gid = gid
    }
    if (nsId !== undefined) {
      groupNs.nsId = nsId
    }
    if (read !== undefined) {
      groupNs.read = read
    }
    if (write !== undefined) {
      groupNs.write = write
    }
    if (manage !== undefined) {
      groupNs.manage = manage
    }
    this.setState({...this.state, adminOp: {...this.state.adminOp, groupNs}})
  }

  nsEdit(name: string) {
    const ns = {...this.state.adminOp.ns}
    if (name !== undefined) {
      ns.name = name
    }
    this.setState({...this.state, adminOp: {...this.state.adminOp, ns}})
  }

  saveOp() {
    const { dispatch: d } = this.props.s
    if (this.state.adminOp.ns) {
      this.state.adminOp.ns.pNsId = this.props.nsId
    } else if (this.state.adminOp.groupNs.gid) {
      const grp = this.state.access.groupTree.find(grp => grp.gid === this.state.adminOp.groupNs.gid)
      const ns = this.state.access.namespaces.find(ns0 => ns0.nsId === this.state.adminOp.groupNs.nsId)
      this.state.adminOp.group = grp
      this.state.adminOp.ns = ns
    }
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NsPost(this.state.adminOp))
        .then(adminOp => {
          if (adminOp.groupNs?.id) {
            adminOp.ns = undefined
            adminOp.groupNs.gid = NoId
            adminOp.groupNs.nsId = NoId
            adminOp.groupNs.read = false
            adminOp.groupNs.write = false
            adminOp.groupNs.manage = false
            this.setState({...this.state, adminOp})
            return this.loadNamespaces()
          }
          if (adminOp.ns?.nsId) {
            adminOp.ns.name = ""
            this.setState({...this.state, adminOp})
            return this.loadNamespaces()
          }
          this.setState({...this.state, adminOp})
        })
      , d
    )
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.access?.namespace?.path}</h1></li></ul>
          <ul>
            <li>
              {this.state.access?.namespaces?.length > 0 && (
                <a class="ptr" onClick={() => this.setState({...this.state, adminOp: BlankBindOp()})}>
                  <IcnBind height={32} />
                </a>
              )}
              <a class="ptr ml8" onClick={() => this.setState({...this.state, adminOp: BlankNsOp()})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.adminOp && boxResult(
          this.state.adminOp,
          this.state.adminOp.groupNs?.id
            ? <div>Namespace bound</div>
            : <div>Namespace created</div>
        )}
        {this.state.adminOp?.groupNs && this.state.access && (
          <div class="grid">
            <select
              required value={this.state.adminOp.groupNs.gid}
              onChange={e => this.bindEdit(parseInt((e.target as any).value), undefined)}>
              <option selected disabled value={NoId}>Group</option>
              {options(this.state.access.groupTree, g => g.name, g => g.gid)}
            </select>
            <select
              required value={this.state.adminOp.groupNs.nsId}
              onChange={e => this.bindEdit(undefined, parseInt((e.target as any).value))}>
              <option selected disabled value={NoId}>Namespace</option>
              {options(this.state.access.namespaces, ns => ns.name, ns => ns.nsId)}
            </select>
            <fieldset>
              <input type="checkbox" id="read"
                checked={this.state.adminOp.groupNs.read}
                onChange={e => this.bindEdit(undefined, undefined, (e.target as any).checked, undefined, undefined)} />
              <label htmlFor="read">R</label>
              <input type="checkbox" id="write"
                checked={this.state.adminOp.groupNs.write}
                onChange={e => this.bindEdit(undefined, undefined, undefined, (e.target as any).checked, undefined)} />
              <label htmlFor="write">W</label>
              <input type="checkbox" id="manage"
                checked={this.state.adminOp.groupNs.manage}
                onChange={e => this.bindEdit(undefined, undefined, undefined, undefined, (e.target as any).checked)} />
              <label htmlFor="manage">M</label>
            </fieldset>
            <input type="submit" value="Bind"
              disabled={!this.state.adminOp.groupNs.gid || !this.state.adminOp.groupNs.nsId}
              onClick={() => this.saveOp()} />
          </div>
        )}
        {this.state.adminOp?.ns && (
          <div class="grid">
            <input placeholder="Namespace Name" value={this.state.adminOp.ns.name || ""}
              onChange={e => this.nsEdit((e.target as any).value)} />
            <input type="submit" value="Save"
              disabled={!this.state.adminOp.ns.name}
              onClick={() => this.saveOp()} />
          </div>
        )}
        {this.state.access?.namespaces?.length > 0 && [
          <h3>Sub-groups</h3>,
          <table class="striped">
            {headers(["Name", "Created", "Actions"])}
            <tbody>
              {this.state.access.namespaces.map(ns => {
                const rwg = this.state.access.groupNamespaces.find(gns => gns.read || gns.write)
                return row([
                  <a href={uiNsNsIdFmt(ns.nsId)}>{ns.name}</a>,
                  <small>{utcYyyyMmDdHhMm(ns.createUtcMs)}</small>,
                  rwg && (
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
        ]}
        {this.state.access?.groupNamespaces?.length > 0 && [
          <h3>Group Access</h3>,
          <table class="striped">
            {headers(["Group", "Permissions", "Granted"])}
            <tbody>
              {this.state.access.groupNamespaces.map(gns => {
                const grp = this.state.access.groups.find(grp => grp.gid === gns.gid)
                return row([
                  grp?.name,
                  <code>{flagsOf(gns)}</code>,
                  utcYyyyMmDdHhMm(gns.grantUtcMs)
                ])
              })}
            </tbody>
          </table>
        ]}
      </div>
    )
  }
}

export default (props: OtNsProps) => <OtNamespace {...props} s={React.useContext(UiContext)} />
