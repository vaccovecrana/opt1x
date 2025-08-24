import * as React from "preact/compat"

import { RenderableProps } from "preact"
import { UiContext, UiStore } from "@ui/store"

type OtLoginProps = RenderableProps<{ s?: UiStore, url?: string }>

class OtLogin extends React.Component<OtLoginProps> {
  render() {
    return (
      <div>
        <h4 class="mt16 mb16">Sign in</h4>
        <form action={this.props.url} method={"POST"}>
          <input placeholder="API key" required type="password" name="X-Opt1x-Key" />
          <button type="submit">Login</button>
        </form>
      </div>
    )
  }
}

export default (props: OtLoginProps) => <OtLogin {...props} s={React.useContext(UiContext)} />
