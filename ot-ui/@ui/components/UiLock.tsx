import * as React from "preact/compat"
import { RenderableProps } from "preact"

import { useContext } from "preact/hooks"
import { UiContext, usrMsgClear } from "@ui/store"

const resolveMessage = (error: any): string => {
  if (typeof error === "string") {
    return error
  } else if (error && typeof error.error === "string") {
    return error.error
  } else if (error && typeof error.message === "string") {
    return error.message
  }
  return "An unknown error occurred"
}

const UiLock = (props: RenderableProps<{}>) => {
  const {dispatch: d, state} = useContext(UiContext)
  return (
    <div>
      {props.children}
      {state.uiLocked && <div class="uiLock" />}
      {state.lastMessage && (
        <dialog open>
          <article>
            <header>
              <button
                aria-label="Close" {...{rel: "prev"}}
                onClick={() => usrMsgClear(d)}
              />
              <div>ℹ️</div>
            </header>
            <p class="txc mt16">
              {resolveMessage(state.lastMessage)}
            </p>
          </article>
        </dialog>
      )}
    </div>
  )
}

export default UiLock
