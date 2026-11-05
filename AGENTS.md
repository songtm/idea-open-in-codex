# Open In Codex 项目记忆

Open In Codex 是 IntelliJ IDEA 插件，用于把当前项目或文件位置交给 Codex 桌面端。以下行为以源码和插件配置为准。

## 操作入口

- `Open File In Codex`（`Alt+Shift+O`）复制文件提示词，并尝试切换到 Codex 桌面端，供用户在当前聊天中粘贴。入口位于 Tools 菜单、编辑器右键菜单和项目树右键菜单。
- `Copy File Prompt` 复用同一套文件提示词逻辑，仅复制到剪贴板。入口位于编辑器和项目树右键菜单。
- `Open Project In Codex`（`Alt+Shift+P`）通过 `codex://new` 打开新聊天，传入项目路径和项目提示词。入口位于 Tools 菜单和项目树右键菜单。

## 源码导航

- Java 源码位于 `src/main/java/com/github/lkjian/ideaopenincodex`。`actions` 实现操作，`codex/CodexUrlLauncher.java` 构造并打开新聊天 URI，`i18n/CodexMessages.java` 从资源包读取提示词。
- 文件提示词格式为 `@{文件路径}#L{行号或范围}`。行号从 1 开始，优先使用选区，无选区时使用光标所在行。项目提示词格式为 `Project: {项目路径}`。中英文资源包目前使用相同模板。
- `src/main/resources/META-INF/plugin.xml` 注册菜单和快捷键。修改操作行为时，对照检查插件描述与中英文 README。

## 本地构建

- 在仓库根目录运行 `.scriptsuild-plugin.ps1`，脚本使用本机安装的 IntelliJ IDEA 编译。
- 当前版本为 `1.1.2`，构建产物为 `builddistributionsidea-open-in-codex-1.1.2.zip`。调整版本时核对 `build.gradle.kts`、`plugin.xml` 和构建脚本中的版本值。
- 在 IntelliJ IDEA 中通过 `Settings | Plugins | Install Plugin from Disk...` 安装 ZIP。
- 本机 Rider 2024.2 的插件 ZIP 路径为 `C:\Users\songtianming\AppData\Local\JetBrains\Rider2024.2\plugins\idea-open-in-codex.zip`。
