# Clojure 开发约定

通过 `clj-nrepl-eval --discover-ports` 查找 nREPL，使用 `clj-nrepl-eval -p PORT` 求值，修改后使用 `:reload`。

本项目不是 Clojure Kit / Integrant 项目。运行入口为 `scripts/start`，Clerk 与 Quil 的完整重载入口为 `(user/reset)`。

遇到 `.clj`、`.cljs`、`.cljc`、`.edn` 的定界符错误时，立即运行 `clj-paren-repair PATH`，不得手工修复不匹配的括号。
