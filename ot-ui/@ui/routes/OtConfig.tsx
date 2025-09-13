import * as React from "react"
import { RenderableProps } from "preact"

import { IcnSave } from "@ui/components/UiIcons"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { apiV1CfgCidGet, apiV1CfgCidPost, apiV1ValGet, OtConfigOp, OtNodeType, OtValueOp, OtVar } from "@ui/rpc"
import { NodeAdapter, NodeType, TreeEditor } from "@ui/tree-editor/UiTree"
import { rpcUiHld } from "@ui/routes"
import { boxResult } from "@ui/components/Ui"

import UiSearch from "@ui/components/UiSearch"

export interface OtVarV extends OtVar {
  children?: Map<string, OtVarV>
  open?: boolean
}

type OtConfigProps = RenderableProps<{ s?: UiStore, nsId?: number, cid?: number }>
type OtConfigState = {
  valOp?: OtValueOp
  cfgOp?: OtConfigOp
  root?: OtVarV
}

let idCounter = -1
let maxIdx = -1

class OtConfig extends React.Component<OtConfigProps, OtConfigState> implements NodeAdapter<OtVarV> {

  componentDidMount() {
    this.loadConfig()
  }

  readTree(op: OtConfigOp): [OtVarV, number] {
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

  writeTreeTail(root: OtVarV, idx: Map<number, OtVar>) {
    idx.set(root.node.nid, root)
    root.children?.forEach(child => this.writeTreeTail(child, idx))
  }

  writeTree(root: OtVarV): OtVar[] {
    var nodes: Map<number, OtVar> = new Map()
    this.writeTreeTail(root, nodes)
    return [...nodes.values()]
  }

  capFirst(str: string): string {
    if (str.length === 0) {
      return ""
    }
    return str.charAt(0).toUpperCase() + str.slice(1)
  }

  loadConfig() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => Promise.all([
          apiV1ValGet(),
          apiV1CfgCidGet(this.props.cid, true)
        ]))
        .then(([valOp, cfgOp]) => {
          const [root, mxIdx] = this.readTree(cfgOp)
          maxIdx = mxIdx
          this.setState({...this.state, valOp, cfgOp, root})
        })
      , d
    )
  }

  saveConfig() {
    this.state.cfgOp.vars = this.writeTree(this.state.root)
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1CfgCidPost(this.props.cid, this.state.cfgOp))
        .then(cfgOp => {
          if (!cfgOp.error) {
            const [root, mxIdx] = this.readTree(cfgOp)
            maxIdx = mxIdx
            this.setState({...this.state, cfgOp, root})
          } else {
            this.setState({...this.state, cfgOp})
          }
        })
      , d
    )
  }

  getType (node: OtVarV) {
    return node.node.type.toLowerCase() as NodeType
  }

  getEntries(node: OtVarV) {
    if (node.children) {
      return [...node.children.entries()]
        .sort((e0, e1) => e0[1].node.itemIdx - e1[1].node.itemIdx)
    }
    return []
  }

  newVal(type: OtNodeType, label: string) {
    var out: OtVarV = {
      children: new Map(),
      node: { type, label, cid: this.props.cid }
    }
    return out
  }

  initialValue(parent: OtVarV, parentType: NodeType, key: string, childType: NodeType) {
    return this.newVal(this.capFirst(childType) as OtNodeType, key)
  }

  addChild(parent: OtVarV, parentType: NodeType, key: string, child: OtVarV, childType: NodeType) {
    const out = {...parent}
    if (!out.children) {
      out.children = new Map()
    }
    out.children.set(key, child)
    child.node.pNid = parent.node.nid
    return out
  }

  setChild(parent: OtVarV, parentType: NodeType, key: string, child: OtVarV, childType: NodeType) {
    const out = {...parent}
    out.children.set(key, child)
    child.node.pNid = parent.node.nid
    return out
  }

  deleteChild(parent: OtVarV, parentType: NodeType, key: string) {
    const out = {...parent}
    out.children.delete(key)
    return out
  }

  renameChild(parent: OtVarV, parentType: NodeType, oldKey: string, newKey: string) {
    const out = {...parent}
    const c0 = out.children.get(oldKey)
    out.children.delete(oldKey)
    out.children.set(newKey, c0)
    c0.node.label = newKey
    return out
  }

  initializeAux(node: OtVarV) {
    if (node.open === undefined) {
      node.open = node.node.type === OtNodeType.Object || node.node.type === OtNodeType.Array
    }
    if (node.node?.nid === undefined) {
      idCounter = idCounter + 1
      maxIdx = maxIdx + 1
      node.node.nid = idCounter
      node.node.itemIdx = maxIdx
    }
  }

  copyAux(from: OtVarV, to: OtVarV) {
    to.node.nid = from.node.nid
    to.open = from.open
  }

  getId(node: OtVarV) {
    return node.node.nid.toString()
  }

  getOpen(node: OtVarV) {
    return node.open
  }

  setOpen(node: OtVarV, open: boolean) {
    node.open = open
  }

  renderDisplay(value: OtVarV, type: NodeType) {
    return <input type="text" disabled value={value.val.encrypted ? "***" : value.val.val} />
  }

  renderAddPrimitive(
    onSubmit: (newVal: OtVarV, selectedKey?: string) => void,
    onCancel: () => void
  ) {
    if (this.state.valOp) {
      return (
        <UiSearch items={this.state.valOp.values}
          getLabel={val => `${val.name} - ${val.encrypted ? "***" : val.val}`}
          getSearchKey={val => val.name}
          getCategory={val => this.state.valOp.namespaces.find(ns => ns.nsId === val.nsId).name}
          onSelect={val => {
            const newVal = this.newVal(OtNodeType.Value, val.name)
            newVal.val = val
            newVal.node.vid = val.vid
            onSubmit(newVal)
          }}
          onCancel={onCancel}
        />
      )
    }
    return undefined
  }

  renderEditor(value: OtVarV, onChange: (newValue: OtVarV) => void, onCancel: () => void, type: NodeType) {
    return this.renderAddPrimitive(onChange, onCancel)
  }

  render() {
    return (
      <div>
        <nav>
          <ul><li><h1>{this.state.cfgOp?.cfg?.name}</h1></li></ul>
          <ul>
            <li>
              <a class="ptr" onClick={() => this.saveConfig()}>
                <IcnSave height={32} />
              </a>
            </li>
          </ul>
        </nav>
        {this.state.cfgOp && boxResult(this.state.cfgOp)}
        <div>
          {this.state.root && (
            <TreeEditor
              adapter={this} value={this.state.root}
              onChange={root => this.setState({...this.state, root})}
            />
          )}
        </div>
      </div>
    )
  }

}

export default (props: OtConfigProps) => <OtConfig {...props} s={React.useContext(UiContext)} />
