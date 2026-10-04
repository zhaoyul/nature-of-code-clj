# 代码本色 · Clojure / Clerk / Quil

完整的 2024 年版《The Nature of Code》中文 notebook 工程。复用你在 `RedCreationTech/org-epub-books` 完成的中文译文，覆盖 PDF 第 1–643 页：19 个部分、385 张配图，以及 99 个编号示例和 67 个随附练习/插图演示。

引言及第 0–11 章的全部 538 段教学代码已重写为 Clojure，并直接嵌入 `notebooks/*.clj`，作为 Clerk 可求值单元。每段包含具体的数据、运算或函数，能独立求值；绘图结果在 notebook 内交给真实 Quil 画布显示。编号示例与随附演示继续使用 Clojure/ClojureScript 共享模型，提供持续动画与交互参数。

完整中文正文、行内原书术语、公式及 387 处图注均保留。原 Org 和转换后的 Markdown 存档保留在 `resources/translations/the-nature-of-code/`；阅读页展示重写后的 Clojure 程序。书末索引的 12 段缩进词条是纯文本，不计入 538 段程序。浏览器运行的 JavaScript 文件由 ClojureScript 编译生成。

## 启动

需要 Java 17+ 和 Clojure CLI。无需 npm、Node 或独立 Processing 窗口。首次启动下载 Maven 依赖并编译浏览器程序。

```sh
./scripts/start
```

打开 [http://localhost:7779](http://localhost:7779)。同一进程提供 nREPL，默认端口为 `7878`。各章都有暂停/继续、单步、重置及参数滑块；画布离开可视区域时暂停。改变种群、网格、递归层数等初始参数后点击重置。点击画布后可使用键盘。

```sh
clj-nrepl-eval --discover-ports
clj-nrepl-eval -p 7878 "(require 'noc.engine :reload) (select-keys (noc.engine/run \"2.9\" 240 {:count 8 :G 0.5 :seed 2024}) [:id :frames])"
clj-nrepl-eval -p 7878 '(user/reset)'
```

`(user/reset)` 重载模型、重编译 Quil 并刷新 Clerk。也可从 Emacs/CIDER 连接 `localhost:7878`，直接求值、查看状态，修改代码后调用该入口。本项目使用普通函数与不可变数据，未引入 Integrant。

服务重启后，若 `clj-nrepl-eval` 仍持有旧会话而出现超时，可运行 `clj-nrepl-eval -p 7878 --reset-session "(+ 1 1)"` 更新 CLI 会话。

## 在 notebook 中实验

正文中的 Clojure 单元会直接执行，结果紧随代码显示。可编辑当前 `notebooks/*.clj` 中的表达式再求值，也可将单元复制到 nREPL；各单元自带所需的样本数据，不依赖前一个单元产生的状态。例如点积可直接计算：

```clojure
(require '[noc.math :as m])
(let [a [-3 5] b [4 2]]
  (m/dot a b))
;; => -2
```

`notebook/scene` 把有限的 Clojure 绘图命令输出为独立 Quil 静态画布；重新求值会更新绘图。`notebook/sketch` 则启动带暂停、单步、重置和参数控件的动画。

```clojure
(require '[noc.engine :as engine] '[noc.notebook :as notebook]
         '[noc.draw :as d])

;; 绘制当前表达式的结果。
(notebook/scene
 (d/scene (d/background) (d/line [60 120] [260 180])
          (d/circle [260 180] 16))
 {:width 640 :height 240})

;; 在当前 notebook 内启动独立画布。
(notebook/sketch "2.9" {:count 12 :G 0.7 :seed 2024})

;; 在 JVM 上运行确定的帧数，返回状态和绘图指令。
(engine/run "2.9" 240 {:count 8 :G 0.5})

;; 可编程输入：精确重现鼠标轨迹。
(engine/run "1.10" 100
            {:input-fn (fn [frame] {:mouse [(+ 100 frame) 120]})})
```

首页链接全部章节、书前材料、附录、图片来源与索引。`notebooks/laboratory.clj` 提供参数比较、固定随机种子和输入脚本示例。随附示例编号为 `extra-章号-序号` 或原练习号 `exercise-章号.序号`，每章底部与全书示例目录列出完整映射；`notebook/companion-id` 可按官方源目录定位它们。

## 验证与导出

```sh
clojure -M:test
./scripts/build
python3 -m http.server 8000 --directory public/build
```

导出文件位于 `public/build/index.html`，Quil 和 p5.js 等本地浏览器资源一并复制，中文配图嵌入文档。用 HTTP 服务打开；Clerk 的页面查看器、样式和字体仍从 CDN 加载，模型计算在本地执行。开发服务中的 [浏览器验证页](http://localhost:7779/js/check.html) 会在真实 Quil 画布中逐个测试全部示例并展示结果。

`clojure -M:test` 检查完整译文校验值、全部图片引用、166 个示例的有限步执行与有限数值，还检查向量/力、随机分布、动量、元胞自动机、分形、遗传操作、神经网络训练及物理约束。正文单元测试检查 538 段代码的完整覆盖、源码确实嵌入 notebook，以及每段在独立命名空间中使用实际项目函数与 Clerk viewer 求值；同时检查结果有界且没有非有限数值。

排版回归检查使用 Clerk 的实际 Markdown 解析器，验证公式、代码、图片各自独立，以及 387 处中文图注均为可见正文。重新转换 Org 译稿后运行 `python3 scripts/normalize_translation.py`，修正数学标记、行内代码并恢复图注；随后按下节命令重新生成正文布局和 notebook。仅修改阅读内容时，可执行 `(require 'noc.book :reload)` 后重新打开对应 notebook，无需重新编译 Quil。

已记录的验证结果保存在 [validation/summary.edn](validation/summary.edn)，静态构建日志位于 `validation/static-build.log`；修改后以本地测试和导出的最新输出为准。

## 工程结构

| 路径 | 内容 |
| --- | --- |
| `notebooks/` | 22 个 Clerk 阅读与实验入口 |
| `resources/clojure-book/{id}.edn` | 13 份逐段 Clojure 教学代码资源，共 538 段 |
| `resources/clojure-book/layout/` | 原文段落、代码位置、中文代码注释与来源哈希的布局映射 |
| `src/noc/lessons/` | 教学单元的辅助函数与样本数据 |
| `src/noc/chapters/ch00.cljc` 至 `ch11.cljc` | 99 个编号示例 |
| `src/noc/chapters/extras00.cljc` 至 `extras09.cljc` | 67 个随附演示 |
| `src/noc/math.cljc`、`learning.cljc` | 向量、确定性随机数、梯度噪声、MLP、反向传播与遗传操作 |
| `src/noc/engine.cljc`、`registry.cljc` | 统一初始化、步进、绘制与示例索引 |
| `src/noc/browser.cljs` | Quil 渲染与交互控件 |
| `resources/translations/the-nature-of-code/` | 原 Org 与 Markdown 译稿存档、配图、来源和校验值 |
| `resources/examples.edn` | 官方示例目录、来源网址及源代码提交 |

重新生成阅读入口需要 Python 3、Pandoc 和 Babashka：

```sh
python3 scripts/prepare_clojure_book.py
bb scripts/generate_notebooks.clj
```

`prepare_clojure_book.py` 定位 Markdown 中的教学代码并生成布局映射，保留代码之间的原文、中文注释和原代码哈希；它不修改译稿存档。`generate_notebooks.clj` 按原顺序交替写入正文阅读单元与真实 Clojure 表单，并插入对应动画示例。

修改正文教学代码时编辑 `resources/clojure-book/{id}.edn` 或其 `src/noc/lessons/` 辅助函数，再生成 notebook。修改动画算法时编辑 `.cljc` 文件；修改阅读布局时编辑生成脚本。直接编辑 notebook 适合即时实验，再次生成会覆盖这些临时改动。

## 移植说明与来源

物理章把 Matter.js 与 Toxiclibs.js 重写为 Clojure 的凸形 SAT 接触、刚体冲量、距离约束与 Verlet 弹簧求解器。它们保留每个示例要展示的物理行为，数值结果不会与外部库逐位相同。神经网络章使用实际前馈网络与反向传播，神经进化章对实际网络权重选择、交叉和突变。三维随附演示保留三维坐标，通过透视投影在 Quil 二维画布中显示；音乐 L 系统在点击后使用浏览器音频播放。

各算法与交互的对应关系、求解器近似和数值验证范围详见 [移植实现说明](docs/implementation-notes.md)。

原书：Daniel Shiffman, published by No Starch Press® Inc.，2024。官方 [正文](https://natureofcode.com/) 与 [代码源库](https://github.com/nature-of-code/noc-book-2)，移植参考提交 `03b9a7eea3b56be8dfaef324736cf56455a2c5e4`。中文原始资料来自你的 [已完成译文](https://github.com/RedCreationTech/org-epub-books/tree/main/books/completed/the-nature-of-code)，原始 Org SHA-256：`17a1f48e9d96631c5fb2d1ebeed629623d0bd71c979aa820bb45ef5a8f78ab18`。

原书版权、[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/) 声明及出版社保留的翻译权声明完整保留在书前内容。本工程增加了 Clojure 实现、浏览器交互、实验入口与验证代码；第三方依赖遵循各自许可。
