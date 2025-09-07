import * as React from "react"
import { useState, useMemo, useEffect } from "react"

interface UiSearchProps<K> {
  items:        K[]
  getLabel:     (item: K) => string
  getSearchKey: (item: K) => string
  getCategory?: (item: K) => string | undefined
  onSelect:     (item: K) => void
  onCancel:     () => void
}

function UiSearch<K>({ items, getLabel, getSearchKey, onSelect, onCancel, getCategory }: UiSearchProps<K>) {
  const [query, setQuery] = useState("")
  const [highlightedIndex, setHighlightedIndex] = useState(-1)

  const filtered = useMemo(
    () => {
      if (query.length < 3) return [];
      const lower = query.toLowerCase();
      return items.filter((item) =>
        getSearchKey(item).toLowerCase().includes(lower)
      )
    },
    [query, items, getSearchKey]
  )

  const { grouped, flatItems } = useMemo(
    () => {
      const grouped: { [key: string]: K[] } = {}
      filtered.forEach((item) => {
        const cat = getCategory ? getCategory(item) || "Uncategorized" : ""
        if (!grouped[cat]) grouped[cat] = []
        grouped[cat].push(item)
      })
      const flatItems = Object.entries(grouped).flatMap(([_, items]) => items)
      return { grouped, flatItems }
    },
    [filtered, getCategory]
  )

  const itemToIndex = useMemo(
    () => new Map(flatItems.map((item, i) => [item, i])),
    [flatItems]
  )

  useEffect(() => {
    setHighlightedIndex(-1)
  }, [query])

  return (
    <div class="uiSearch">
      <input type="text" value={query}
        onChange={(e) => setQuery((e.target as any).value)}
        onBlur={() => {
          setQuery("")
          onCancel()
        }}
        onKeyDown={(e) => {
          if (e.key === "ArrowDown") {
            e.preventDefault()
            setHighlightedIndex(prev => prev === -1 ? 0 : Math.min(prev + 1, flatItems.length - 1))
          } else if (e.key === "ArrowUp") {
            e.preventDefault()
            setHighlightedIndex(prev => prev === -1 ? flatItems.length - 1 : Math.max(prev - 1, 0))
          } else if (e.key === "Enter" && highlightedIndex >= 0) {
            e.preventDefault()
            const selected = flatItems[highlightedIndex]
            onSelect(selected)
            setQuery("")
            setHighlightedIndex(-1)
          }
        }}
        placeholder="Search..."
      />
      {Object.keys(grouped).length > 0 && (
        <div class="dropDown">
          {Object.entries(grouped).map(([cat, groupItems]) => (
            <div key={cat}>
              {cat && <h3 class="category">{cat}</h3>}
              {groupItems.map((item, localIdx) => {
                const idx = itemToIndex.get(item)!
                return (
                  <div
                    class={`result ${idx === highlightedIndex ? "highlighted" : ""}`}
                    key={localIdx}
                    onClick={() => onSelect(item)}
                    onMouseEnter={() => setHighlightedIndex(idx)}
                  >
                    {getLabel(item)}
                  </div>
                )
              })}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default UiSearch