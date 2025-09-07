import * as React from "preact/compat"

import { NodeAdapter, NodeType } from "./UiTree"

let idCounter = 0

const objProp = (value: any) => ({
  value,
  enumerable: false,
  writable: true,
  configurable: true
})

export class ObjAdapter implements NodeAdapter<any> {

  getType(node: any): NodeType {
    if (Array.isArray(node)) {
      return NodeType.Arr
    }
    const type = typeof node
    return type as NodeType
  }

  getEntries(node: any) {
    const entries = Array.isArray(node)
      ? node.map((item, index) => [index.toString(), item])
      : Object.entries(node)
    return entries as [string, any][]
  }

  initialValue(parent: any, parentType: NodeType, key: string, childType: NodeType) {
    switch (childType) {
      case NodeType.Obj: return {}
      case NodeType.Arr: return []
      case NodeType.Num: return 0
      case NodeType.Boo: return false
    }
    return ""
  }

  addChild(parent: any, parentType: NodeType, key: string, child: any, childType: NodeType) {
    const out = parentType === NodeType.Obj ? {...parent} : [...parent]
    if (parentType === NodeType.Obj) {
      out[key] = child
    } else if (parentType === NodeType.Arr) {
      out.push(child)
    }
    return out
  }

  setChild(parent: any, parentType: NodeType, key: string, child: any) {
    const out = parentType === NodeType.Obj ? {...parent} : [...parent]
    out[key] = child
    return out
  }

  deleteChild(parent: any, parentType: NodeType, key: string) {
    const out = parentType === NodeType.Obj ? {...parent} : [...parent]
    if (parentType === NodeType.Arr) {
      out.splice(parseInt(key), 1)
    } else {
      delete out[key]
    }
    return out
  }

  renameChild(parent: any, parentType: NodeType, oldKey: string, newKey: string) {
    let out = parentType === NodeType.Obj ? {...parent} : [...parent]
    const val = parent[oldKey]
    out = this.deleteChild(out, parentType, oldKey)
    out = this.setChild(out, parentType, newKey, val)
    return out
  }

  initializeAux(node: any): void {
    if (typeof node === "object" && node !== null) {
      if (node._id === undefined) {
        Object.defineProperty(node, "_id", objProp(`id${idCounter++}`))
      }
      if (node._open === undefined) {
        Object.defineProperty(node, "_open", objProp(true))
      }
    }
  }

  copyAux(from: any, to: any) {
    ["_id", "_open"].forEach(prop => {
      if (from.hasOwnProperty(prop)) {
        const desc = Object.getOwnPropertyDescriptor(from, prop)!
        Object.defineProperty(to, prop, desc)
      }
    })
  }

  getId(node: any): string {
    if (typeof node === "object") {
      return node._id
    }
    return node
  }

  getOpen(node: any): boolean {
    return node._open
  }

  setOpen(node: any, open: boolean) {
    node._open = open
  }

  renderEditor(value: any, onChange: (newValue: any) => void, onCancel: () => void, type: NodeType) {
    switch (type) {
      case NodeType.Str: return <input type="text" value={value} onBlur={(e) => onChange((e.target as any).value)} />
      case NodeType.Num: return <input type="number" value={value} onBlur={(e) => onChange(Number((e.target as any).value))} />
      case NodeType.Boo: return <input type="checkbox" checked={value} onChange={(e) => onChange((e.target as any).checked)} />
    }
    const strValue = JSON.stringify(value)
    return (
      <textarea
        rows={1}
        value={strValue}
        onChange={(e) => {
          try {
            onChange(JSON.parse((e.target as any).value))
          } catch {
            onChange(value)
          }
        }}
      />
    )
  }

  renderDisplay(value: any, type: NodeType) {
    return <input type="text" disabled value={value} />
  }

  renderAddPrimitive(
    onSubmit: (newVal: any, selectedKey?: string) => void,
    onCancel: () => void
  ) {
    const [selectedType, setSelectedType] = React.useState<NodeType>(NodeType.Str);
    const [inputValue, setInputValue] = React.useState("");
    const handleSubmit = () => {
      let newVal: any;
      if (selectedType === NodeType.Num) {
        newVal = parseFloat(inputValue)
      } else {
        newVal = inputValue
      }
      onSubmit(newVal)
    }
    return (
      <div>
        <select value={selectedType} onChange={(e) => setSelectedType((e.target as any).value as NodeType)}>
          <option value={NodeType.Num}>Number</option>
          <option value={NodeType.Str}>String</option>
        </select>
        <input type="text" value={inputValue} onChange={(e) => setInputValue((e.target as any).value)} />
        <button onClick={handleSubmit}>Save</button>
        <button onClick={onCancel}>Cancel</button>
      </div>
    )
  }

}
