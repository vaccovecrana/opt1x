import typescript from "@rollup/plugin-typescript"
import nodeResolve from "@rollup/plugin-node-resolve"
import alias from "@rollup/plugin-alias"

const plugins = [
  alias({
    entries: [
      { find: "react", replacement: "preact/compat" },
      { find: "react-dom", replacement: "preact/compat" }
    ]
  }),
  nodeResolve(),
  typescript()
]

export default {
  input: "./@ui/index.tsx",
  output: {dir: "./build/ui", format: "esm", sourcemap: true},
  plugins
}
