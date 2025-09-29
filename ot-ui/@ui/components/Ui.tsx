import * as React from "preact/compat"

import UiIdenticon from "@ui/components/UiIdenticon"
import { OtApiKey, OtGroupNs, OtList, OtResult } from "@ui/rpc"
import { uiRoot } from "@ui/routes"

export const utcYyyyMmDdHhMm = (timestamp: number): string => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

export const avatar = (k: OtApiKey, withLabel: boolean) => {
  return (
    <li>
      <a href={uiRoot} class="secondary">
        <UiIdenticon seed={k.kid.toString()} className="br16" />
        {withLabel && (
          <small class="ml8 small">
            {k.name}
          </small>
        )}
      </a>
    </li>
  )
}

export const headers = (values: any[]) => {
  return (
    <thead>
      {values.map(h => (
        <th>{h}</th>
      ))}
    </thead>
  )
}

export const row = (cells: any[]) => {
  return (
    <tr>
      {cells.map(c => (
        <td>{c}</td>
      ))}
    </tr>
  )
}

export const options = <T,>(values: T[], labelFn?: (v: T) => string, valFn?: (v: T) => any) => {
  return values.map(v => {
    let label = labelFn ? labelFn(v) : v.toString()
    let value: any = undefined
    if (valFn) {
      value = valFn(v)
    }
    return (<option value={value}>{label}</option>)
  })
}

export const boxHero = (lines: string[]) => (
  <article class="card info hero p24">
    {lines.map(text => <div>{text}</div>)}
  </article>
)

export const boxOk = (value: any) => (
  <article class="success">
    {value}
  </article>
)

export const boxError = (value: any) => (
  <article class="error">
    {value}
  </article>
)

export const boxValidations = (result: OtResult) => (
  <article class="info">
    <ul>
      {result.validations?.map(v => (
        <li>{v.message}</li>
      ))}
    </ul>
  </article>
)

export const isOk = (result: OtResult) => {
  const ok = !result?.error && result?.validations?.length === 0
  return ok
}

export const boxResult = (result: OtResult, okMsg?: any) => {
  var ok = isOk(result)
  return [
    result?.error && boxError(result.error),
    result?.validations?.length > 0 && boxValidations(result),
    ok && okMsg && boxOk(okMsg)
  ]
}

export const appendPage = <T, K>(p0: OtList<T, K>, p1: OtList<T, K>) => {
  if (!p0 || p0.page.nx1 === p1.page.nx1) {
    return p1
  }
  p0 = {...p0}
  p0.page.items = p0.page.items.concat(p1.page.items)
  p0.page.size = p0.page.size + p1.page.size
  p0.page.nx1 = p1.page.nx1
  p0.error = p1.error
  p0.validations = p1.validations
  return p0
}

export const flagsOf = (gns: OtGroupNs): string => {
  const r = gns.read   ? "r" : "-"
  const w = gns.write  ? "w" : "-"
  const m = gns.manage ? "m" : "-"
  return `${r}${w}${m}`
}

export const valTruncate = (st: string): string => {
  if (st && st.length > 16) {
    return `${st.substring(0, 15)}...`
  }
  return st
}
