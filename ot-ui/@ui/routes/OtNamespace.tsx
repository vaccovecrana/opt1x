import { box, boxError, headers, row } from "@ui/components/Ui"
import { IcnAdd } from "@ui/components/UiIcons"
import { apiV1KeyGet, apiV1NamespaceIdGet, apiV1NamespaceKeyGet, apiV1NamespaceKeyPost, OtApiKey, OtAssignmentList, OtList, OtNamespace, OtNamespaceOp } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { utcYyyyMmDdHhMm, rpcUiHld } from "@ui/util"
import { RenderableProps } from "preact"
import * as React from "preact/compat"

type OtNamespaceVProps = RenderableProps<{ s?: UiStore, nsId?: number }>
type OtNamespaceVState = {
  apiKeys?: OtList<OtApiKey, string>
  nskPage?: OtAssignmentList
  nsOp?: OtNamespaceOp
  ns?: OtNamespace
}

const NoKid = -1

const BlankOp = (): OtNamespaceOp => ({
  keyNamespace: { writeAccess: false },
  namespace: {}
})

class OtNamespaceV extends React.Component<OtNamespaceVProps, OtNamespaceVState> {

  componentDidMount(): void {
    this.loadAssignments()
  }

  loadAssignments() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => Promise.all([
          apiV1KeyGet(50, this.state.apiKeys?.page?.nx1),
          apiV1NamespaceKeyGet(50, this.props.nsId, this.state.nskPage?.page?.nx1),
          apiV1NamespaceIdGet(this.props.nsId)
        ]))
        .then(([apiKeys, nskPage, nsPage]) => this.setState({
          ...this.state, apiKeys, nskPage,
          ns: nsPage.page.items[0]
        })),
        d
    )
  }

  saveAssignment() {
    const { dispatch: d } = this.props.s
    this.state.nsOp.namespace.nsId = this.props.nsId
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceKeyPost(this.state.nsOp))
        .then(nsOp => {
          if (nsOp.error) {
            throw nsOp
          } else if (nsOp.keyNamespace.id) {
            this.setState({...this.state, nsOp}, () => this.withEdit(NoKid, false))
            return this.loadAssignments()
          } else {
            this.setState({...this.state, nsOp})
          }
        }),
      d
    )
  }

  withEdit(kid: number, writeAccess: boolean) {
    var nsOp = {...this.state.nsOp}
    if (kid !== undefined) {
      nsOp.keyNamespace.kid = kid
    }
    if (writeAccess !== undefined) {
      nsOp.keyNamespace.writeAccess = writeAccess
    }
    this.setState({...this.state, nsOp})
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.ns?.name}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.setState({...this.state, nsOp: BlankOp()})}>
                <IcnAdd height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.nsOp?.error && boxError(this.state.nsOp.error)}
        {this.state.nsOp?.validations?.length > 0 && boxError(
          this.state.nsOp.validations.map(v => <div>{v.message}</div>)
        )}
        {this.state.nsOp?.keyNamespace?.id && box("API key assigned")}
        {this.state.nsOp && this.state.apiKeys && (
          <div class="grid">
            <fieldset>
              <select required value={this.state.nsOp?.keyNamespace?.kid || NoKid}
                onChange={e => this.withEdit(parseInt((e.target as any).value), undefined)}>
                <option disabled value={NoKid}>Key Assignment</option>
                {this.state.apiKeys.page.items.map(key => (
                  <option value={key.kid}>{key.name}</option>
                ))}
              </select>
              <input type="checkbox" id="wa"
                checked={this.state.nsOp?.keyNamespace?.writeAccess}
                onChange={e => this.withEdit(undefined, (e.target as any).checked)}
              />
              <label htmlFor="wa">Write access</label>
            </fieldset>
            <input type="submit" value="Save"
              disabled={this.state.nsOp?.keyNamespace?.kid === undefined || this.state.nsOp?.keyNamespace?.kid === NoKid}
              onClick={() => this.saveAssignment()}
            />
          </div>
        )}
        {this.state.nskPage && (
          <table class="striped">
            {headers(["Key", "Write access", "Date"])}
            <tbody>
              {this.state.nskPage.page.items.map(kns => row([
                this.state.nskPage.apiKeys.find(ak => ak.kid === kns.kid).name,
                kns.writeAccess ? "Yes" : "No",
                utcYyyyMmDdHhMm(kns.grantUtcMs)
              ]))}
            </tbody>
          </table>
        )}
      </div>
    )
  }
}

export default (props: OtNamespaceVProps) => <OtNamespaceV {...props} s={React.useContext(UiContext)} />
