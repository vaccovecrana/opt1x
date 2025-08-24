import * as React from "react"

import { IcnSave } from "@ui/components/UiIcons"
import { lockUi, UiContext, UiStore } from "@ui/store"
import { RenderableProps } from "preact"
import { apiV1NamespaceIdConfigIdGet, apiV1NamespaceIdConfigIdNodePost, apiV1ValueGet, OtConfigOp, OtNodeType, OtValueOp } from "@ui/rpc"
import { rpcUiHld } from "@ui/util"
import { NodeAdapter, NodeType, TreeEditor } from "@ui/tree-editor/UiTree"
import { readTree, OtVarV, capFirst, writeTree, boxError } from "@ui/components/Ui"
import UiSearch from "@ui/components/UiSearch"

type OtConfigProps = RenderableProps<{ s?: UiStore, nsId?: number, cid?: number }>
type OtConfigState = { valOp?: OtValueOp, cfgOp?: OtConfigOp, root?: OtVarV }

let idCounter = -1
let maxIdx = -1

class OtConfig extends React.Component<OtConfigProps, OtConfigState> implements NodeAdapter<OtVarV> {

  componentDidMount() {
    this.loadConfig()
  }

  loadConfig() {
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => Promise.all([
          apiV1ValueGet(),
          apiV1NamespaceIdConfigIdGet(this.props.nsId, this.props.cid, true)
        ]))
        .then(([valOp, cfgOp]) => {
          const [root, mxIdx] = readTree(cfgOp)
          maxIdx = mxIdx
          this.setState({...this.state, valOp, cfgOp, root})
        })
      , d
    )
  }

  saveConfig() {
    this.state.cfgOp.vars = writeTree(this.state.root)
    const { dispatch: d } = this.props.s
    rpcUiHld(
      lockUi(true, d)
        .then(() => apiV1NamespaceIdConfigIdNodePost(this.props.nsId, this.props.cid, this.state.cfgOp))
        .then(cfgOp => {
          if (!cfgOp.error) {
            const [root, mxIdx] = readTree(cfgOp)
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
    return this.newVal(capFirst(childType) as OtNodeType, key)
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
    return <input type="text" disabled value={value.val.encrypted ? "***" : value.val.value} />
  }

  renderAddPrimitive(
    onSubmit: (newVal: OtVarV, selectedKey?: string) => void,
    onCancel?: () => void, suggestedKey?: string
  ) {
    if (this.state.valOp) {
      return (
        <UiSearch items={this.state.valOp.values}
          getLabel={val => `${val.name} - ${val.encrypted ? "***" : val.value}`}
          getSearchKey={val => val.name}
          getCategory={val => this.state.valOp.namespaces.find(ns => ns.nsId === val.nsId).name}
          onSelect={val => {
            const newVal = this.newVal(OtNodeType.Value, val.name)
            newVal.val = val
            newVal.node.vid = val.vid
            onSubmit(newVal)
          }}
        />
      )
    }
    return undefined
  }

  renderEditor(value: OtVarV, onChange: (newValue: OtVarV) => void, type: NodeType) {
    return this.renderAddPrimitive(onChange)
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
        {this.state.cfgOp?.error && boxError(this.state.cfgOp.error)}
        {this.state.cfgOp?.validations?.length > 0 && boxError(
          this.state.cfgOp.validations.map(v => <div>{v.message}</div>)
        )}
        <div>
          {this.state.root && (
            <TreeEditor
              adapter={this} value={this.state.root}
              onChange={root => this.setState({...this.state, root})} />
          )}
        </div>
      </div>
    )
  }

}

export default (props: OtConfigProps) => <OtConfig {...props} s={React.useContext(UiContext)} />
