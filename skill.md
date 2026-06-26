---
name: lado-dev-rules
description: lado项目开发规范，作为多个Android仓库共享规范。涵盖布局、代码结构、数据类、Git、命名规范、IM管理、Figma资源等开发约定。
version: 1.0.0
---

# lado 项目开发规范

> 本文件为唯一维护主稿；其他历史副本不再保留。

## 客户端

### UI / 布局

- 布局文件中**不能有硬编码文字**，所有文字必须在 `strings.xml` 中定义，使用 `@string/` 引用
- 读取 Figma 文件时**不需要下载图片**，只提取资源信息（名称、节点 ID、下载链接）整理成表格写入 `dev/figma-assets-{日期}.md`，图片由用户自己手动下载并压缩
- 通用确认弹窗统一使用 `ConfirmQuitDialog`，不要新建类似弹窗
- **只有渐变色 + 圆角的背景，不创建 drawable 文件**，直接使用 State 系列自定义控件的属性实现：
  - `StateTextView` / `StateConstraintLayout` / `StateLinearLayout` 等
  - 通过 `app:uikit_startColor`、`app:uikit_endColor`、`app:uikit_gradientOrientation`、`app:uikit_radius` 等属性设置
  - 只有带描边、特殊形状（非矩形）、分层效果等复杂情况才需要单独建 drawable
- **纯色 + 圆角背景也优先使用 State 系列自定义控件**，不要顺手新建 shape drawable：
  - `StateTextView` 背景色优先使用 `app:uikit_normalBackgroundColor`
  - `StateConstraintLayout` / `StateLinearLayout` 等同理优先使用对应 normal background 属性
  - `UiKitTriangleView` 使用 `app:uikit_backgroundColor`，不要误用 StateView 的 normal background 属性
- **`res/drawable/` 目录默认只放 xml**；位图资源（`png` / `webp` / `jpg` / `9.png`）放到对应密度目录，如 `drawable-xxhdpi/`
- 如果必须在 drawable XML 中使用 `<gradient android:angle="...">`，**angle 必须是 45 的倍数**（如 `0 / 45 / 90 / 135 / 180 / 225 / 270 / 315`），禁止写成其他角度，避免部分机型崩溃
- Kotlin / Java 代码里需要把颜色字符串转成颜色值时，统一使用 `"#FF7537".toColorInt()` 这类写法，**不要使用** `Color.parseColor(...)`

### 分包样式改动约束

- 以后修改样式要考虑分包影响，除了pati能随意改，其他包改动都要考虑是否会影响到其他包。
- 涉及 `layout/drawable/color/styles/themes/dimens/strings` 或通用 UI 组件（如 `core/uikit`）时，先判断是否是共享资源。
- 若改动会影响非 `pati` 包线，默认不要直接改共享文件；优先用 `pati` 专属资源覆盖或 `pati` 模块内定制。
- 输出修改结果时，明确标注“本次改动是 `pati` 专属”还是“共享改动”，共享改动需说明影响范围。

### 富文本 / SpanUtils

- **需要富文本时，优先去 `SpanUtils` 找现成方法**，不要手动写 `SpannableStringBuilder` + `ImageSpan`
- 内联图标（金豆、钻石等）使用 `ConvertIconType` 中定义的占位符（如 `ConvertIconType.BEANS_ICON`），配合 `SpanUtils.convertText(context, ssb, text, placeholder, drawableRes, w, h)` 替换
- `strings.xml` 中的图标占位符写法：`%d(bean_icon)`，代码里用 `ConvertIconType.BEANS_ICON` 匹配
- 颜色高亮用 `SpanUtils.convertIndexColor` 或 `SpanUtils.convertTextColorRepeat`
- 加粗用 `SpanUtils.convertTextBoldRepeat`

### Dialog / 交互

- **所有弹窗必须通过 `Navigator.showDialog(fragment)` 打开**，不能直接调用 `fragment.show(fragmentManager, tag)`

### 接口定义

- **接口参数严格按照需求文档 / 用户说明**，不能自行推测或补充参数
- **已与后端约定的字段边界条件，禁止擅自放宽或改语义**。尤其是“服务端明确保证非空 / 必传 / 固定枚举”的字段，不能为了兜底、兼容或自认为更稳而改成更宽松的判断，除非用户明确要求
- `enter_bar.static_url` 属于事故级协议字段：只要有进场条，这个字段一定不为空。相关代码必须继续按这个协议做严格判断，**不要**改成 `static_url || dynamic_url` 一类的放宽条件

### 历史止血改动

- **涉及线程切换、post / delay、释放时序、detach / removeView 顺序的历史止血改动，禁止仅凭代码直觉直接回退或改成“更同步”“更直接”的写法**
- 如果用户明确说明某个历史改动已经在线上显著降低 ANR / Crash（例如 `ExoVideoView.release()` 中统一 `post destroy`），后续修改时必须默认“这个时序有业务背景”，先保持原策略，再结合线上数据和调用链确认，**不能**擅自改回主线程同步执行

### 字符串资源 / 新增位置

- **所有文案必须定义在 `strings.xml` 中**，代码里统一用 `AppUtil.getAppContext().getString(R.string.xxx)` 获取，不能直接写字面量字符串
- **新增 string 条目一律追加到 `strings.xml` 文件最末尾**，不要插入到中间
- **新增多语言（strings.xml 条目）、数据类字段、方法，一律追加到文件 / 类的最末尾**，不要插入到中间
- 原因：代码 review 时插入到中间的改动容易被忽略，追加到末尾改动更显眼
- **新增文案默认只需要补两份**：默认文案 `values/strings.xml` 和葡语 `values-pt/strings.xml`
- 除非用户明确要求，**不要主动把新增文案同步到其他多语言目录**
- 用户要求“搜品牌文案 / 搜 Lado/Pati / 整理文案残留”时，**只允许处理 `strings.xml`、分包资源覆盖、纯展示文案、纯注释**；**禁止**擅自修改 `Api`、请求 Header、网络拦截器、协议字段、`BuildConfig`、`applicationId`、scheme、配置常量、共享基础类等高风险代码
- 只要改动已经超出“文案替换”范畴（例如会影响请求协议、网络行为、路由、包名、构建配置），**必须先停下来向用户确认**，不能因为字符串里带 `Lado/Pati` 就直接全局替换

### Kotlin / 代码结构

- **禁止使用 `!!` 非空断言**，用 `?.` 安全调用、`?:` 默认值、`orEmpty()`、`?: return` 等方式替代
- **复杂逻辑不能写在 Fragment / Activity 中**，必须写在对应的 ViewModel 或 Presenter 中
- 接口必须在各模块的 `Api` 文件中注册，并在对应的 `Repository` 文件中实现
- **所有数据类必须继承 `BaseBean` 基类**（项目有代码混淆，不继承会出问题）
- **同一个文件不能定义两个并列的 `data class`**，关联的子类要么新建独立文件，要么作为主类的嵌套类（Kotlin 中直接在类内部声明，不加 `inner` 关键字，`inner class` 会持有外部类引用，`data class` 不应使用）
- **数据类新增参数一律追加到参数列表最末尾**，不要插入到已有参数中间，避免 review 时遗漏协议变更
- **新增数据类参数尽量补简短注释**，优先说明字段语义 / 使用场景，不写无信息量注释

### 命名规范

- 参数命名使用**驼峰式**（camelCase）
- **布局文件必须以所在模块名开头**，例如 `party_dialog_xxx.xml`、`party_item_xxx.xml`
- **ViewBinding 自动将布局 id 下划线转驼峰**：布局中 `android:id="@+id/tv_first_top_tips"` 在代码中访问时是 `binding.tvFirstTopTips`，写代码时必须用驼峰形式，不能用下划线

### IM 管理

- 所有私信 IM 消息类型统一在 `IMMsgManager` 中维护
- 新增 IM 类型需要：① `IMCustomMsgType.kt` 注册枚举值 ② `IMMsgManager.kt` 添加处理分支 ③ 新建对应 Event 类

### 组件库发布（`component`）

- `component` 仓库普通模块（如 `network` / `security` / `media`）发布参数默认读取根目录 `component/local.properties`
- `includeBuild` 的独立构建（如 `build-logic` / `build-logic-lado` / `apm-plugin` / `router-plugin`）应优先维护各自目录下的 `local.properties`
- 发布 `flavor-plugin` 时，版本与坐标以 `component/build-logic/local.properties` 为准；不要只改根目录 `local.properties`
- 若发布结果与预期版本不一致，优先检查是否存在命令行 `-P` 覆盖（`-P` 优先级高于 `local.properties`）

## Git

### 通用原则

- **不可以自动执行任何 git 操作**，`commit / push / merge` 等操作必须由用户自己完成
- 不要主动执行 `git add`、`git commit`、`git push`、`git merge` 等命令
- 若用户**明确授权**当前会话可由 AI 协助处理本地 Git 细节，则可在**不执行 `commit` / `push`** 的前提下，帮助完成 `git add`、冲突文件内容修复、冲突标记清理等操作；最终 `commit` 仍由用户本人执行
- **修改代码前必须先确认当前分支**；如果当前在 `qa` 分支，必须先提醒用户切换到正确的开发分支，再继续改代码
- **修改任何文件前，必须先看当前本地工作区状态和目标文件最新内容**（至少确认 `git status` / 目标文件内容 / 目标文件是否已有本地改动），**禁止**沿用上一轮记忆直接覆盖文件
- 如果目标文件或相邻文件已经有用户本地改动，后续修改必须**严格基于当前文件内容增量处理**；看不清是谁改的、意图不明确、或怀疑会覆盖用户修改时，**必须先停下来问用户**
- 对共享模块、公共类、协议层文件的修改要额外谨慎：**只要存在“可能影响多个包线 / 多个产品 / 公共请求协议”的风险，就不能直接改**

### 客户端分支约定（Android）

- **Android 线上分支是 `lado/dev`**
- 涉及 Android 发版、评估是否可上线、合并到发版分支时，默认以 `lado/dev` 作为线上 / 发版基线进行判断
- 不要默认把 Android 的 `master` 视为线上分支；如无用户特别说明，Android 相关 review、对比、cherry-pick 基线统一按 `lado/dev` 理解

### 服务端开发提测流程（`lado-go` 等 Go 服务）

- **分支约定：`master` 是线上分支，`qa` 是测试环境分支**
- 测试代码统一合并到 `qa`；`qa` push 成功后会自动部署测试环境
- **所有服务都必须从最新 `master` 拉功能分支开始开发**；开发完成后，再把功能分支内容同步到 `qa` 测试分支，`qa` push 成功后自动部署测试环境
- 标准流程：
  1. 先切到 `master` 分支，执行 `git pull`，同步最新代码
  2. 从最新 `master` 拉取新的功能分支
  3. 在功能分支开发，开发完成后 push 到远程
  4. 切换到 `qa` 分支，先 `git pull` 最新代码，再 merge 功能分支内容
  5. 在 `qa` 解决冲突后提交并 push；`qa` push 成功后自动部署测试环境
  6. 测试环境验证无异常后，切回功能分支做 code review；确认无异常后再提 `master` 的 merge request
- 如果在把功能分支同步到 `qa` 时遇到冲突，AI 可以负责**分析并解决冲突内容，整理到可 `commit` 的前一状态**；最终 `commit` 及后续 `push` 仍由用户本人执行
- **服务端开发默认不要直接从 `qa` 拉功能分支开始开发**，避免把其他测试代码带进本次功能分支
- **上线前必须确认 merge request 的目标分支是 `master`，不要误提到 `qa`**

## 服务端

### 本地仓库路径

- 用户本机的服务端本地默认根目录是 `/Users/cuican/lado-go/`
- 以后凡是排查 `lado-go` 相关服务端代码、接口实现、错误码、数据库读写链路，**默认先到这个目录下查找对应仓库**，不要每次重复向用户确认服务端代码路径
- 只有当目标仓库不在该目录、目录缺失，或用户明确给了新的路径时，才再次向用户确认

### 数据库操作

- **没有明确修改指令，只读不写**，查询结果只用于回答问题，不触发任何 `INSERT / UPDATE / DELETE`
- **settings 类配置表禁止直接 SQL 修改**，必须通过管理后台操作，直接改 DB 不会刷新缓存，且无变更记录
- 需要执行写操作时，必须先描述要做什么、影响哪些数据，等用户明确确认后再执行

### Proto / Protobuf

- **不要手动修改 `pb.go` 文件**，它是自动生成的
- 只改 `.proto` 文件，改完告知用户执行 `make pb` 重新生成

### Proto 文件修改流程（Go 后端）

#### 开发阶段（测试环境）

1. `lado-proto` 服务
   - 基于 `master` 创建功能分支
   - 修改 proto 文件
   - 执行 `make pb` 生成代码
   - 提交功能分支，合并到 `qa`

2. 依赖服务（`lado-live-server` / `lado-common-server` 等）
   - 基于 `master` 创建功能分支
   - 修改业务代码（如需要）
   - 执行 `go get gitlab.spaccez.com/lado/go/lado-proto@qa`
   - 执行 `go mod tidy`
   - 提交功能分支，合并到 `qa`，发布测试环境
   - ⚠️ **所有依赖 proto 的服务都要更新**，不只是业务逻辑服务。例如新增 IM 枚举时，`lado-common-server` 负责将 proto 数字枚举值转成字符串下发给客户端，如果它没有更新 proto 依赖，客户端会收到数字（如 `13006`）而非字符串（如 `BESTIE_DRAW_GUIDE_POP`），导致客户端无法匹配

3. 测试验证
   - 在测试环境完成服务端 / 客户端联调

#### 上线阶段（生产环境）

1. `lado-proto` 服务
   - 功能分支合并到 `master`

2. 依赖服务
   - 在功能分支执行 `go get gitlab.spaccez.com/lado/go/lado-proto@master`
   - 执行 `go mod tidy`
   - 提交依赖变更
   - 功能分支合并到 `master`，发布线上环境

## Review 规范

- 做 code review 时，**必须先重新对照本规范逐条检查**，不要只看代码“能不能跑”，还要看是否符合项目约定
- review 结论里，优先检查这几类高频问题：
  - 数据模型是否符合约定：继承 `BaseBean`、新增字段是否追加到末尾、关联子模型是否应该拆文件或收为主类嵌套类
  - 是否出现多余的 `@Keep`、额外混淆规则、无必要的 `consumer-rules.pro` / proguard 改动
  - 文案是否被错误加到多语言文件，是否存在“其实没必要抽 string 却被抽成资源”的情况
  - 布局是否真正按设计稿语义实现，尤其要区分“配置点位置”和“实时进度位置”，不要只看表面像不像
  - UI 规格是否与用户已确认的值一致，如字号、颜色、粗细、间距、对齐方式、已达成/未达成状态色
- review 时，**不要忽略用户已经明确确认过的口径**。一旦用户已经说死（例如字段语义、显示顺序、颜色值、字号、是否需要多语言），必须按该口径检查，不能再按自己的理解放过
- review 发现问题后，要明确说明：
  - 是功能逻辑问题、规范问题，还是仅样式/文案问题
  - 影响范围是什么
  - 是否会影响提测 / 验收
- 如果是自己或其他模型之前写的代码，也不能因为“是自己写的”就放松 review 标准；仍然要按规范和用户要求重新核对
