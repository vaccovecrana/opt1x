import { headers, row, utcYyyyMmDdHhMm } from "@ui/components/Ui"
import { apiV1GroupGet, OtGroupRole, OtKeyAccess } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { RenderableProps } from "preact"
import * as React from "preact/compat"
import { rpcUiHld, uiGroupsIdFmt } from "@ui/routes"

type OtGroupsProps = RenderableProps<{ s?: UiStore }>
type OtGroupsState = { access?: OtKeyAccess }

class OtGroups extends React.Component<OtGroupsProps, OtGroupsState> {

  componentDidMount(): void {
    this.loadGroups()
  }

  loadGroups() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1GroupGet())
        .then(access => this.setState({...this.state, access})),
        d
    )
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>Groups</h1></li></ul>
        </nav>
        {this.state.access && (
          <table class="striped">
            {headers(["Name", "Role", "Created"])}
            <tbody>
              {this.state.access.groups.map(grp => {
                var {role} = this.state.access.keyGroups.find(kg => kg.gid === grp.gid)
                return row([
                  role === OtGroupRole.Admin
                    ? <a href={uiGroupsIdFmt(grp.gid)}>{grp.name}</a>
                    : grp.name,
                  role,
                  utcYyyyMmDdHhMm(grp.createUtcMs)
                ])
              })}
            </tbody>
          </table>
        )}
      </div>
    )
  }
}

export default (props: OtGroupsProps) => <OtGroups {...props} s={React.useContext(UiContext)} />
