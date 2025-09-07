import * as React from "preact/compat"

import { apiV1InitGet, OtInitOp } from "@ui/rpc"
import { RenderableProps } from "preact"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { uiUnseal } from "@ui/routes"

type OtInitProps = RenderableProps<{ s?: UiStore }>
type OtInitState = {
  result: OtInitOp
}

class OtInit extends React.Component<OtInitProps, OtInitState> {

  componentDidMount(): void {
    const { dispatch: d } = this.props.s
    lockUi(true, d)
      .then(() => apiV1InitGet())
      .then(result => this.setState({ result }))
      .then(() => lockUi(false, d))
  }

  render() {
    const { result } = this.state
    return result ? (
      <div>
        {result.rootApiKey && (
          <div>
            <h4 class="mt16 mb16">Opt1x initialized. Copy and save the root key and Unseal keys below.</h4>
            <pre class="p16">{JSON.stringify(result, null, 2)}</pre>
          </div>
        )}
        <div class="txc">
          <div>{result.error}</div>
          <a href={uiUnseal} onClick={() => window.location.href = uiUnseal}>
            Continue to unseal
          </a>
        </div>
      </div>
    ) : <div />
  }

}

export default (props: OtInitProps) => <OtInit s={React.useContext(UiContext)} />
