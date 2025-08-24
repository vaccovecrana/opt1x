import { box, boxError, headers, row } from "@ui/components/Ui"
import { IcnAdd, IcnEdit, IcnEditCfg } from "@ui/components/UiIcons"
import { apiV1NamespaceGet, apiV1NamespacePost, OtList, OtNamespace, OtNamespaceOp } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { utcYyyyMmDdHhMm, rpcUiHld, uiNamespacesIdFmt, uiNamespacesIdValuesFmt, uiNamespacesIdConfigFmt } from "@ui/util"
import { RenderableProps } from "preact"
import * as React from "preact/compat"

type OtNsProps = RenderableProps<{ s?: UiStore }>
type OtNsState = {
  nsList?: OtList<OtNamespace, string>
  nsEdit?: OtNamespaceOp
}

const NoNsId   = -1 // Special value for no namespace
const RootNsId = -2 // Special value for root namespace
const BlankNs: OtNamespaceOp = {
  namespace: { name: "" },
  keyNamespace: { writeAccess: true }
}

class OtNamespaces extends React.Component<OtNsProps, OtNsState> {

  componentDidMount(): void {
    this.loadNamespaces()
  }

  loadNamespaces() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceGet(25, this.state.nsList?.page?.nx1))
        .then(nsList => this.setState({ ...this.state, nsList })), d
    )
  }

  withEdit(name: string, pNsId: number) {
    const nsEdit = {...this.state.nsEdit,
      namespace: {...this.state.nsEdit.namespace},
      keyNamespace: {...this.state.nsEdit.keyNamespace}
    }
    if (name || name === "") {
      nsEdit.namespace.name = name
    }
    if (pNsId !== undefined) {
      nsEdit.namespace.pNsId = pNsId
    }
    this.setState({...this.state, nsEdit})
  }

  saveNamespace() {
    const { dispatch: d } = this.props.s
    if (this.state.nsEdit?.namespace?.pNsId === RootNsId) {
      this.state.nsEdit.namespace.pNsId = undefined
    }
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespacePost(this.state.nsEdit))
        .then(nsEdit => {
          if (nsEdit.error) {
            throw nsEdit
          } else if (nsEdit.namespace.nsId) {
            this.withEdit("", NoNsId)
            return this.loadNamespaces()
          } else {
            this.setState({...this.state, nsEdit})
          }
        }), d
    )
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>Namespaces</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, nsEdit: {...BlankNs}})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.nsEdit?.error && boxError(this.state.nsEdit.error)}
        {this.state.nsEdit?.validations?.length > 0 && boxError(
          this.state.nsEdit.validations.map(v => <div>{v.message}</div>)
        )}
        {this.state.nsEdit?.namespace?.nsId && box("Namespace created")}
        {this.state.nsEdit && (
          <div class="grid">
            <input placeholder="Name" value={this.state.nsEdit?.namespace?.name}
              onChange={e => this.withEdit((e.target as any).value, undefined)}
            />
            <select required value={this.state.nsEdit?.namespace?.pNsId || NoNsId}
              onChange={e => this.withEdit(undefined, parseInt((e.target as any).value))}>
              <option disabled value={NoNsId}>Parent Namespace</option>
              <option value={RootNsId}>Root Namespace</option>
              {this.state.nsList?.page?.items.map(ns => (
                <option value={ns.nsId}>{ns.name}</option>
              ))}
            </select>
            <input type="submit" value="Save"
              disabled={!this.state.nsEdit?.namespace?.name || !this.state.nsEdit?.namespace?.pNsId}
              onClick={() => this.saveNamespace()}
            />
          </div>
        )}
        {this.state.nsList && (
          <table class="striped">
            {headers(["Name", "Path", "Created", "Actions"])}
            <tbody>
              {this.state.nsList.page.items.map(ns => row([
                <a href={uiNamespacesIdFmt(ns.nsId)}>{ns.name}</a>,
                ns.path,
                utcYyyyMmDdHhMm(ns.createdAtUtcMs),
                <div class="row justify-center align-center">
                  <div class="col auto">
                    <a href={uiNamespacesIdValuesFmt(ns.nsId)}>
                      <IcnEdit height={30} />
                    </a>
                  </div>
                  <div class="col auto">
                    <a href={uiNamespacesIdConfigFmt(ns.nsId)}>
                      <IcnEditCfg height={30} />
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

export default (props: OtNsProps) => <OtNamespaces {...props} s={React.useContext(UiContext)} />
