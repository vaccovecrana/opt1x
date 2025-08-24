import * as React from "preact/compat"
import { useState } from "preact/compat"

interface UtProps {
  children: React.ReactNode
  label: string
}

const UiToggle: React.FC<UtProps> = ({ children, label }) => {
  const [isVisible, setIsVisible] = useState<boolean>(false)
  const toggleVisibility = () => {
    setIsVisible(!isVisible)
  }
  return (
    <div>
      <a href="#" onClick={(e) => {
        e.preventDefault()
        toggleVisibility()
      }}>
        {label} ({isVisible ? "Hide" : "Show"})
      </a>
      {isVisible && <div>{children}</div>}
    </div>
  )
}

export default UiToggle