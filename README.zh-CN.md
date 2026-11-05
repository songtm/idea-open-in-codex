# Open In Codex

这是一个 IntelliJ IDEA 插件，用于将当前项目或文件位置交给 Codex 桌面端。

## 功能

- `Open File In Codex` 将文件路径和行号复制到剪贴板。在 Windows 上，插件会尝试切换到已打开的 Codex 窗口，等待快捷键松开后发送 Ctrl+V。没有选区时使用光标所在行。
- `Copy File Prompt`：把与文件打开功能相同的当前文件路径和行号范围提示词复制到剪贴板，但不打开 Codex；可从编辑器和项目视图右键菜单使用。
- `Open Project In Codex`：打开新的 Codex 会话，并把当前项目路径作为 Codex 工作目录，同时附带当前项目路径提示词。

默认快捷键：

- `Alt+Shift+O`：在 Codex 中打开当前文件位置
- `Alt+Shift+P`：在 Codex 中打开当前项目

## 安装

推荐使用安装脚本直接安装到当前 IDEA 用户插件目录：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\install-plugin.ps1
```

安装完成后重启 IntelliJ IDEA，然后在 `Settings | Plugins | Installed` 中搜索 `Open In Codex`。

也可以手动安装构建产物：

```text
build\distributions\idea-open-in-codex-1.1.2.zip
```

在 IDEA 中进入 `Settings | Plugins`，点击齿轮图标，选择 `Install Plugin from Disk...`，然后选择上面的 zip 文件。

## 本地构建

仓库内提供了 PowerShell 构建脚本，会使用本机已安装的 IntelliJ IDEA JBR 编译插件：

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-plugin.ps1
```

构建完成后会生成：

```text
build\distributions\idea-open-in-codex-1.1.2.zip
```

## 使用方式

打开任意项目后，可以通过以下入口使用：

- 顶部菜单：`Tools | Open File In Codex`
- 编辑器右键菜单：`Open File In Codex`
- 编辑器右键菜单：`Copy File Prompt`
- 项目视图右键菜单：`Open File In Codex`
- 项目视图右键菜单：`Copy File Prompt`
- 快捷键：`Alt+Shift+O`

项目入口：

- 顶部菜单：`Tools | Open Project In Codex`
- 项目视图右键菜单：`Open Project In Codex`
- 快捷键：`Alt+Shift+P`

项目入口生成的提示词格式：

```text
请查看当前项目：
项目：D:\path\to\project
```

## Codex URL

插件会打开类似下面的协议地址：

```text
codex://new?path=<项目路径>&prompt=<提示词>
```

文件入口生成的提示词格式：

```text
@D:/path/to/File.java#L24-27
```

Windows 下会通过 PowerShell 调用 `Start-Process` 打开 Codex 桌面端协议。
