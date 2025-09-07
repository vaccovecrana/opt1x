import { boxResult, headers, options, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { apiV1GroupIdDelete, apiV1GroupIdGet, apiV1GroupPost, OtAdminOp, OtGroupRole, OtKeyAccess } from "@ui/rpc"
import { lockUi, Opt1x, UiContext, UiStore } from "@ui/store"
import { RenderableProps } from "preact"
import * as React from "preact/compat"
import { rpcUiHld, uiGroupsIdFmt } from "@ui/routes"
import { IcnAdd, IcnBind, IcnDelete } from "@ui/components/UiIcons"

type OtGroupProps = RenderableProps<{ s?: UiStore, gid?: number }>
type OtGroupState = { access?: OtKeyAccess, adminOp?: OtAdminOp }

const NoId = -1
const NoRole = ""

const BlankBindOp = (): OtAdminOp => ({
  keyGroupBind: { kid: NoId, gid: NoId },
})

const BlankGroupOp = (): OtAdminOp => ({
  group: { name:  undefined }
})

class OtGroups extends React.Component<OtGroupProps, OtGroupState> {

  componentDidMount(): void {
    this.loadGroups()
  }

  componentDidUpdate(previousProps: Readonly<OtGroupProps>): void {
    if (previousProps.gid !== this.props.gid) {
      this.setState({...this.state, adminOp: undefined})
      this.loadGroups()
    }
  }

  loadGroups() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1GroupIdGet(this.props.gid))
        .then(access => this.setState({...this.state, access}))
      , d
    )
  }

  groupEdit(name: string) {
    const group = {...this.state.adminOp.group}
    if (name != undefined) {
      group.name = name
    }
    this.setState({...this.state, adminOp: {...this.state.adminOp, group}})
  }

  bindEdit(kid: number, gid: number, role: OtGroupRole) {
    const keyGroupBind = {...this.state.adminOp.keyGroupBind}
    if (kid !== undefined) {
      keyGroupBind.kid = kid
    }
    if (gid !== undefined) {
      keyGroupBind.gid = gid
    }
    if (role !== undefined) {
      keyGroupBind.role = role
    }
    this.setState({...this.state, adminOp: {...this.state.adminOp, keyGroupBind}})
  }

  saveOp() {
    const { dispatch: d } = this.props.s
    if (this.state.adminOp.group) {
      this.state.adminOp.group.pGid = this.props.gid
    } else if (this.state.adminOp.keyGroupBind.kid) {
      const grp = this.state.access.groups.find(grp0 => grp0.gid === this.state.adminOp.keyGroupBind.gid)
      this.state.adminOp.group = grp
    }
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1GroupPost(this.state.adminOp))
        .then(adminOp => {
          if (adminOp.keyGroupBind?.id) {
            adminOp.group = undefined
            adminOp.keyGroupBind.gid = NoId
            adminOp.keyGroupBind.kid = NoId
            adminOp.keyGroupBind.role = undefined
            this.setState({...this.state, adminOp})
            return this.loadGroups()
          }
          if (adminOp.group?.gid) {
            adminOp.group.name = ""
            this.setState({...this.state, adminOp})
            return this.loadGroups()
          }
          this.setState({...this.state, adminOp})
        })
      , d
    )
  }

  deleteGroup() {
    if (confirm("This is a destructive operation. All key/namespace assignments will be deleted. Proceed?")) {
      const { dispatch: d } = this.props.s
        rpcUiHld(
          lockUi(true, d)
            .then(() => apiV1GroupIdDelete(this.props.gid))
            .then(adminOp => {
              if (adminOp.validations?.length === 0 && ! adminOp.error) {
                window.location.href = uiGroupsIdFmt(adminOp.group.pGid)
              } else {
                this.setState({...this.state, adminOp})
              }
            })
          , d
        )
    }
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.access?.group?.path}</h1></li></ul>
          <ul>
            <li>
              {this.state.access?.groups?.length > 0 && (
                <a class="ptr" onClick={() => this.setState({...this.state, adminOp: BlankBindOp()})}>
                  <IcnBind height={32} />
                </a>
              )}
              <a class="ptr ml8" onClick={() => this.setState({...this.state, adminOp: BlankGroupOp()})}>
                <IcnAdd height={32} />
              </a>
              {this.state.access?.group?.name !== Opt1x && (
                <a class="ptr ml8" onClick={() => this.deleteGroup()}>
                  <IcnDelete height={32} />
                </a>
              )}
            </li>
          </ul>
        </nav>
        {this.state.adminOp && boxResult(
          this.state.adminOp,
          this.state.adminOp.keyGroupBind?.id
            ? <div>Key bound</div>
            : <div>Group created</div>
        )}
        {this.state.adminOp?.keyGroupBind && (
          <div class="grid">
            { /* TODO replace with search-as-you-type */ }
            <select
              required value={this.state.adminOp.keyGroupBind.kid}
              onChange={e => this.bindEdit(parseInt((e.target as any).value), undefined, undefined)}>
              <option selected disabled value={NoId}>Key</option>
              {options(this.state.access.keys, k => k.name, k => k.kid)}
            </select>
            <select
              required value={this.state.adminOp.keyGroupBind.gid}
              onChange={e => this.bindEdit(undefined, parseInt((e.target as any).value), undefined)}>
              <option selected disabled value={NoId}>Group</option>
              {options(this.state.access.groups, g => g.name, g => g.gid)}
            </select>
            <select
              required value={this.state.adminOp.keyGroupBind.role || NoRole}
              onChange={e => this.bindEdit(undefined, undefined, (e.target as any).value)}>
              <option selected disabled value={NoRole}>Role</option>
              {options([OtGroupRole.Member, OtGroupRole.Admin])}
            </select>
            <input type="submit" value="Bind"
              disabled={!this.state.adminOp.keyGroupBind.kid || !this.state.adminOp.keyGroupBind.gid || !this.state.adminOp.keyGroupBind.role}
              onClick={() => this.saveOp()} />
          </div>
        )}
        {this.state.adminOp?.group && (
          <div class="grid">
            <input placeholder="Group Name" value={this.state.adminOp.group.name || ""}
              onChange={e => this.groupEdit((e.target as any).value)} />
            <input type="submit" value="Save"
              disabled={!this.state.adminOp.group.name}
              onClick={() => this.saveOp()} />
          </div>
        )}
        {this.state.access?.groups?.length > 0 && (
          <table class="striped">
            {headers(["Name", "Created"])}
            <tbody>
              {this.state.access.groups.map(grp => row([
                <a href={uiGroupsIdFmt(grp.gid)}>{grp.name}</a>,
                utcYyyyMmDdHhMm(grp.createUtcMs)
              ]))}
            </tbody>
          </table>
        )}
        {this.state.access?.keyGroups?.length > 0 && [
          <h3>Key Access</h3>,
          <table class="striped">
            {headers(["Key", "Role", "Granted"])}
            <tbody>
              {this.state.access.keyGroups.map(kg => {
                let key = this.state.access.keys.find(k => k.kid === kg.kid)
                if (!key) {
                  key = this.props.s.state.apiKey
                }
                return row([
                  key?.name || "?",
                  kg.role,
                  utcYyyyMmDdHhMm(kg.grantUtcMs)
                ])
              })}
            </tbody>
          </table>
        ]}
      </div>
    )
  }
}

export default (props: OtGroupProps) => <OtGroups {...props} s={React.useContext(UiContext)} />
