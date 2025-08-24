import * as React from "react"
import { useState, useMemo } from "react"

interface UiSearchProps<K> {
  items:        K[]
  getLabel:     (item: K) => string
  getSearchKey: (item: K) => string
  onSelect:     (item: K) => void
  getCategory?: (item: K) => string | undefined
}

function UiSearch<K>({ items, getLabel, getSearchKey, onSelect, getCategory }: UiSearchProps<K>) {
  const [query, setQuery] = useState("")
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

  const grouped = useMemo(() => {
    const groups: { [key: string]: K[] } = {}
    filtered.forEach((item) => {
      const cat = getCategory ? getCategory(item) || "Uncategorized" : ""
      if (!groups[cat]) groups[cat] = []
      groups[cat].push(item)
    })
    return groups
  }, [filtered, getCategory])

  return (
    <div class="uiSearch">
      <input type="text" value={query}
        onChange={(e) => setQuery((e.target as any).value)}
        placeholder="Search..."
      />
      {Object.keys(grouped).length > 0 && (
        <div class="dropDown">
          {Object.entries(grouped).map(([cat, groupItems]) => (
            <div key={cat}>
              {cat && <h3 class="category">{cat}</h3>}
              {groupItems.map((item, idx) => (
                <div class="result" key={idx} onClick={() => onSelect(item)}>
                  {getLabel(item)}
                </div>
              ))}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default UiSearch
