import { lockUi, UiDispatch, usrError } from "@ui/store"

export const uiRoot                = "/"
export const uiInit                = "/init"
export const uiUnseal              = "/unseal"
export const uiLogin               = "/login"
export const uiApiKeys             = "/keys"
export const uiNamespaces          = "/namespaces"
export const uiNamespacesId        = "/namespaces/:nsId"
export const uiNamespacesIdValues  = "/namespaces/:nsId/values"
export const uiNamespacesIdConfig   = "/namespaces/:nsId/config"
export const uiNamespacesIdConfigId = "/namespaces/:nsId/config/:cid"

export const uiNamespacesIdFmt         = (nsId: number) => `/namespaces/${nsId}`
export const uiNamespacesIdValuesFmt   = (nsId: number) => `/namespaces/${nsId}/values`
export const uiNamespacesIdConfigFmt   = (nsId: number) => `/namespaces/${nsId}/config`
export const uiNamespacesIdConfigIdFmt = (nsId: number, cid: number) => `/namespaces/${nsId}/config/${cid}`

export const utcYyyyMmDdHhMm = (timestamp: number): string => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

export const rpcUiHld = <T>(fn: Promise<T>, d: UiDispatch) => {
  return fn.catch((error) => {
    if (error.response && error.response.redirected) {
      window.location.href = uiLogin // Redirect to login on unauthorized access
    }
    return usrError(error, d)
  }).then(() => lockUi(false, d))
}
