import * as React from "preact/compat"
import { useEffect, useState } from "preact/hooks";

const UiVersion = () => {
  const [version, setVersion] = useState("");
  useEffect(() => {
    fetch("/version")
      .then(response => response.text())
      .then(data => { setVersion(data) })
  }, [])
  return (
    <div class="version">
      <small>
        <code>{version}</code>
      </small>
    </div>
  );
};

export default UiVersion;