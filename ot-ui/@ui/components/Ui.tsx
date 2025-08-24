import * as React from "preact/compat"

import { uiRoot } from "@ui/util"
import UiIdenticon from "@ui/components/UiIdenticon"
import { OtApiKey, OtConfigOp, OtNodeType, OtVar } from "@ui/rpc"

export const avatar = (k: OtApiKey, withLabel: boolean) => {
  return (
    <li>
      <a href={uiRoot} class="secondary">
        <UiIdenticon seed={k.kid.toString()} className="br16" />
        {withLabel && (
          <small class="ml8 small">
            {k.name}
            <code class="ml8">{k.role}</code>
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

export const options = (values: any[]) => {
  return values.map(v => (
    <option>{v}</option>
  ))
}

export const box = (value: any) => {
  return (
    <article>
      {value}
    </article>
  )
}

export const boxError = (value: any) => {
  return (
    <article class="error">
      {value}
    </article>
  )
}

export interface OtVarV extends OtVar {
  children?: Map<string, OtVarV>
  open?: boolean
}

export const readTree = (op: OtConfigOp): [OtVarV, number] => {
  let maxIdx: number = -1
  const { vars } = op
  const nodeIdx: Map<number, OtVarV> = new Map()
  for (const node of vars) {
    const nv: OtVarV = node as OtVarV
    nodeIdx.set(nv.node.nid, nv)
    if (nv.node.itemIdx && nv.node.itemIdx > maxIdx) {
      maxIdx = nv.node.itemIdx
    }
  }
  let root: OtVarV
  for (const v of nodeIdx.values()) {
    const parent = nodeIdx.get(v.node.pNid)
    if (parent) {
      if (!parent.children) {
        parent.children = new Map()
      }
      parent.children.set(v.node.label, v)
    } else {
      root = v
    }
  }
  root = root || {
    children: new Map(),
    node: {
      nid: -1, type: OtNodeType.Object,
      label: "root", itemIdx: -1
    }
  }
  return [root, maxIdx]
}

const writeTreeTail = (root: OtVarV, idx: Map<number, OtVar>) => {
  idx.set(root.node.nid, root)
  root.children?.forEach(child => writeTreeTail(child, idx))
}

export const writeTree = (root: OtVarV): OtVar[] => {
  var nodes: Map<number, OtVar> = new Map()
  writeTreeTail(root, nodes)
  return [...nodes.values()]
}

export const capFirst = (str: string): string => {
  if (str.length === 0) {
    return ""
  }
  return str.charAt(0).toUpperCase() + str.slice(1)
}
