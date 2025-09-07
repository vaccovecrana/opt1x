// TODO - move this code into its own component library

import * as React from "react"
import { useState } from "react"

import { IcnTreeAdd, IcnTreeArr, IcnTreeArrItem, IcnTreeDel, IcnTreeNubClose, IcnTreeNubOpen, IcnTreeObj } from "@ui/components/UiIcons"

export enum NodeType { Num = "number", Str = "string", Boo = "boolean", Obj = "object", Arr = "array" }

export interface NodeAdapter<K> {
  getType:            (node: K) => NodeType
  getEntries:         (node: K) => [string, K][]
  initialValue:       (parent: K, parentType: NodeType, key: string, childType: NodeType) => K
  addChild:           (parent: K, parentType: NodeType, key: string, child: K, childType: NodeType) => K
  setChild:           (parent: K, parentType: NodeType, key: string, child: K, childType: NodeType) => K
  deleteChild:        (parent: K, parentType: NodeType, key: string) => K
  renameChild:        (parent: K, parentType: NodeType, oldKey: string, newKey: string) => K
  initializeAux:      (node: K) => void        // Mutates node to set ID (generate if missing) and open (true if missing)
  copyAux:            (from: K, to: K) => void // Mutates 'to' to copy ID/open from 'from'
  getId:              (node: K) => string
  getOpen:            (node: K) => boolean
  setOpen:            (node: K, open: boolean) => void
  renderEditor:       (value: K, onChange: (newValue: K) => void, onCancel: () => void, type: NodeType) => React.JSX.Element
  renderDisplay:      (value: K, type: NodeType) => React.JSX.Element
  renderAddPrimitive: (onSubmit: (newVal: K) => void, onCancel: () => void) => React.JSX.Element
}

const nubShape = (isArrayItem: boolean, isNestable: boolean, expanded: boolean, onClick: () => void) => {
  if (isNestable) {
    return (
      <span class="ptr nub" onClick={onClick}>
        {expanded ? <IcnTreeNubClose height={24} /> : <IcnTreeNubOpen height={24} />}
      </span>
    )
  }
  if (isArrayItem) {
    return (
      <span class="nub" onClick={isNestable ? onClick : undefined}>
        <IcnTreeArrItem height={24} />
      </span>
    )
  }
  return <span />
}

interface TreeEditorProps<K> {
  value: K
  onChange: (newValue: K) => void
  adapter: NodeAdapter<K>
}

const ValueView = <K, >({ adapter, value, onChange }: { adapter: NodeAdapter<K>; value: K; onChange: (newVal: K) => void }) => {
  const [editing, setEditing] = useState(false)
  const type = adapter.getType(value)
  return editing ? (
    adapter.renderEditor(
      value,
      (newVal) => {
        onChange(newVal)
        setEditing(false)
      },
      () => setEditing(false),
      type
    )
  ) : (
    <span class="ptr" onClick={() => setEditing(true)}>
      {adapter.renderDisplay(value, type)}
    </span>
  )
}

interface TreeNodeProps<K> {
  entryKey: string
  entryValue: K
  isArrayItem: boolean
  handleKeyChange: (newKey: string) => void
  handleDelete: () => void
  subOnChange: (newVal: K) => void
  adapter: NodeAdapter<K>
}

const TreeNode = <K, >({
  entryKey, entryValue, isArrayItem,
  handleKeyChange, handleDelete,
  subOnChange, adapter
}: TreeNodeProps<K>) => {
  const isNestable = adapter.getType(entryValue) === NodeType.Obj || adapter.getType(entryValue) === NodeType.Arr
  const [expanded, setExpanded] = useState(adapter.getOpen(entryValue))
  const handleToggle = () => {
    const newExpanded = !expanded
    setExpanded(newExpanded)
    adapter.setOpen(entryValue, newExpanded)
  }
  return (
    <li class="node">
      <div class="nodeInput">
        {nubShape(isArrayItem, isNestable, expanded, handleToggle)}
        {!isArrayItem && (
          <input defaultValue={entryKey} onBlur={(e) => handleKeyChange((e.target as any).value.trim())} />
        )}
        {!isNestable && (
          <ValueView adapter={adapter} value={entryValue} onChange={subOnChange} />
        )}
        <span class="ptr" onClick={handleDelete}>
          <IcnTreeDel height={24} />
        </span>
      </div>
      {isNestable && expanded && (
        <TreeEditor value={entryValue} onChange={subOnChange} adapter={adapter} />
      )}
    </li>
  )
}

const track = <K, >(value: K, adapter: NodeAdapter<K>): K => {
  adapter.initializeAux(value)
  const type = adapter.getType(value)
  if (type === NodeType.Obj || type === NodeType.Arr) {
    adapter.getEntries(value).forEach(([, child]) => track(child, adapter))
  }
  return value
}

export const TreeEditor = <K, >({ value, onChange, adapter }: TreeEditorProps<K>) => {
  const val0 = track(value, adapter)
  const type = adapter.getType(val0)
  const isArray = type === NodeType.Arr
  const isNestable = type === NodeType.Obj || isArray

  if (!isNestable) {
    return <ValueView adapter={adapter} value={val0} onChange={onChange} />
  }

  const entries = adapter.getEntries(val0)
  const [addingPrimitive, setAddingPrimitive] = useState(false)

  const getNewKeyIdx = () => entries.length + 1

  const handleAdd = (parent: K, childType: NodeType, child?: K) => {
    const parentType = adapter.getType(parent)
    const isParentArray = parentType === NodeType.Arr
    const newKeyIdx = getNewKeyIdx()
    const keyToUse = isParentArray ? newKeyIdx.toString() : `key${newKeyIdx}`
    const initVal = child ? child : adapter.initialValue(parent, parentType, keyToUse, childType)
    const newVal = track(initVal, adapter)
    let newParent = adapter.addChild(parent, parentType, keyToUse, newVal, childType)
    adapter.copyAux(parent, newParent)
    onChange(newParent)
  }

  const handleDelete = (parent: K, key: string) => {
    let newParent = adapter.deleteChild(parent, adapter.getType(parent), key)
    adapter.copyAux(parent, newParent)
    onChange(newParent)
  }

  const handleKeyChange = (parent: K, oldKey: string, newKey: string) => {
    if (newKey === oldKey || !newKey) return
    let newParent = adapter.renameChild(parent, adapter.getType(parent), oldKey, newKey)
    adapter.copyAux(parent, newParent)
    onChange(newParent)
  }

  const handleValueChange = (parent: K, key: string, newVal: K) => {
    const parentType = adapter.getType(parent)
    const childType = adapter.getType(newVal)
    const updatedValue = track(newVal, adapter)
    let newParent = adapter.setChild(parent, parentType, key, updatedValue, childType)
    adapter.copyAux(parent, newParent)
    onChange(newParent)
  }

  return (
    <ul class="uiTree">
      {entries.map(([key, val]: [string, K]) => (
        <TreeNode
          key={adapter.getId(val)} isArrayItem={isArray}
          entryKey={key} entryValue={val}
          handleKeyChange={(newKey) => handleKeyChange(val0, key, newKey)}
          handleDelete={() => handleDelete(val0, key)}
          subOnChange={(newVal) => handleValueChange(val0, key, newVal)}
          adapter={adapter}
        />
      ))}
      <li class="mini-toolbar">
        <div>
          {!addingPrimitive ? (
            <div>
              <span class="ptr" onClick={() => handleAdd(val0, NodeType.Arr)}><IcnTreeArr height={24} /></span>
              <span class="ptr" onClick={() => handleAdd(val0, NodeType.Obj)}><IcnTreeObj height={24} /></span>
              {adapter.renderAddPrimitive && <span class="ptr" onClick={() => setAddingPrimitive(true)}><IcnTreeAdd height={24} /></span>}
            </div>
          ) : (
            adapter.renderAddPrimitive(
              (newVal) => {
                handleAdd(val0, adapter.getType(newVal), newVal)
                setAddingPrimitive(false)
              },
              () => setAddingPrimitive(false)
            )
          )}
        </div>
      </li>
    </ul>
  )
}
