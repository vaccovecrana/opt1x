import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { apiV1UnsealPost, OtUnsealOp } from "@ui/rpc"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { uiRoot } from "@ui/routes"

type OtUnsealProps = RenderableProps<{ s?: UiStore }>
type OtUnsealState = {
  result: OtUnsealOp
  key: string
}

class OtUnseal extends React.Component<OtUnsealProps, OtUnsealState> {

  unseal() {
    const { dispatch: d } = this.props.s
    lockUi(true, d)
      .then(() => apiV1UnsealPost(this.state.key))
      .then(result => this.setState({ result, key: "" }))
      .then(() => lockUi(false, d))
  }

  componentDidUpdate(): void {
    if (this.state.result?.ready) {
      window.location.href = uiRoot
    }
  }

  render() {
    const { result, key } = this.state
    return (
      <div>
        <h4 class="mt16 mb16">Enter each portion of the unseal key, one at a time.</h4>
        <form>
          <input
            placeholder="Unseal key" type="password" value={key} required
            onChange={e => this.setState({ ...this.state, key: (e.target as any).value })}
          />
          <div class="grid">
            <div>
              <button type="button" onClick={() => this.unseal()}>Unseal</button>
            </div>
            <div>
              {result && (
                <div class="txr">
                  {result.error && <div>{result.error}</div>}
                  <div>Loaded {result.loadedKeys} keys.</div>
                  {result.ready && <div>Unseal complete.</div>}
                </div>
              )}
            </div>
          </div>
        </form>
      </div>
    )
  }

}

export default (props: OtUnsealProps) => <OtUnseal s={React.useContext(UiContext)} />
