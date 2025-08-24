import * as React from "preact/compat"
import { Context, createContext } from "preact"
import { OtApiKey } from "@ui/rpc"

const Opt1xKey = "X-Opt1x-Key"

export interface UiState {
  uiLocked: boolean
  lastMessage: any
  apiKey: OtApiKey
}

export type UiDispatch = (action: UiAction) => void

export interface UiStore {
  state: UiState
  dispatch: UiDispatch
}

export type UiAction =
  | {type: "lockUi", payload: boolean}
  | {type: "usrMsg", payload: string}
  | {type: "usrMsgClear"}

export const hit = (act: UiAction, d: UiDispatch): Promise<void> => {
  d(act)
  return Promise.resolve()
}

export const lockUi = (locked: boolean, d: UiDispatch) => hit({type: "lockUi", payload: locked}, d)
export const usrInfo = (payload: string, d: UiDispatch) => hit({type: "usrMsg", payload}, d)
export const usrError = (payload: string, d: UiDispatch) => hit({type: "usrMsg", payload}, d)
export const usrMsgClear = (d: UiDispatch) => hit({type: "usrMsgClear"}, d)

export const UiReducer: React.Reducer<UiState, UiAction> = (state0: UiState, action: UiAction): UiState => {
  switch (action.type) {
    case "usrMsg": return {...state0, lastMessage: action.payload}
    case "usrMsgClear": return {...state0, lastMessage: undefined}
    case "lockUi": return {...state0, uiLocked: action.payload}
  }
}

const decodeJWT = (token: string) => {
  const base64UrlDecode = (str: string) => {
    return decodeURIComponent(atob(str).split("").map((c) => {
      return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(""))
  }
  const [_0, payload, _2] = token.split(".")
  if (!payload) {
    throw new Error("Invalid token")
  }
  const decodedPayload = base64UrlDecode(payload.replace(/-/g, "+").replace(/_/g, "/"))
  return JSON.parse(decodedPayload)
}

const getCookie = (name: string) => {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(";").shift()
}

const loadApiKey = (): OtApiKey => {
  const token = getCookie(Opt1xKey)
  if (token) {
    try {
      return JSON.parse(decodeJWT(token)[Opt1xKey]) as OtApiKey
    } catch (error) {
      console.error('Invalid token', error)
    }
  }
  return undefined
}

export const initialState: UiState = {
  lastMessage: undefined,
  uiLocked: false,
  apiKey: loadApiKey()
}

export const UiContext: Context<UiStore> = createContext({
  state: initialState, dispatch: () => {}
})
