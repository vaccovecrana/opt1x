import { lockUi, Opt1xKey, UiDispatch, usrError } from "@ui/store"

export const uiRoot                 = "/"
export const uiInit                 = "/init"
export const uiUnseal               = "/unseal"
export const uiLogin                = "/login"
export const uiApiKeys              = "/keys"
export const uiGroups               = "/groups"
export const uiGroupsId             = "/groups/:gid"
export const uiConfigs              = "/configs"
export const uiConfigsNsId          = "/configs/:nsId"
export const uiConfigsNsIdCid       = "/configs/:nsId/:cid"
export const uiNamespaces           = "/namespaces"
export const uiNamespacesId         = "/namespaces/:nsId"
export const uiValuesNsId           = "/values/:nsId"

export const uiGroupsIdFmt       = (gid: number)  => `/groups/${gid}`
export const uiNamespacesIdFmt   = (nsId: number) => `/namespaces/${nsId}`
export const uiValuesNsIdFmt     = (nsId: number) => `/values/${nsId}`
export const uiConfigsNsIdFmt    = (nsId: number) => `/configs/${nsId}`
export const uiConfigsNsIdCidFmt = (nsId: number, cid: number) => `/configs/${nsId}/${cid}`

export const rpcUiHld = <T>(fn: Promise<T>, d: UiDispatch) => {
  return fn.catch((error) => {
    if (error.response && error.response.redirected) {
      window.location.href = uiLogin // Redirect to login on unauthorized access
    }
    return usrError(error, d)
  }).then(() => lockUi(false, d))
}

export const logout = () => {
  const o1xCook = `${Opt1xKey}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
  document.cookie = o1xCook
  document.location = uiRoot
}
