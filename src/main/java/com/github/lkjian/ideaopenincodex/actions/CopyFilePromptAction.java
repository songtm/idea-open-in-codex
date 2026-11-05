package com.github.lkjian.ideaopenincodex.actions;

import com.github.lkjian.ideaopenincodex.i18n.CodexMessages;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.datatransfer.StringSelection;

/**
 * 将当前文件路径和行号范围组成的 Codex 提示词复制到系统剪贴板。
 */
public final class CopyFilePromptAction extends AnAction {

    /**
     * 未打开编辑器时用于提示 Codex 的默认行号范围。
     */
    private static final int SINGLE_LINE_RANGE_SIZE = 1;

    /**
     * 指定菜单可用状态在后台线程计算。
     */
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * 生成与打开文件功能一致的提示词并复制到系统剪贴板，不启动 Codex。
     */
    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null || file.isDirectory()) {
            return;
        }

        copyPrompt(file, event.getData(CommonDataKeys.EDITOR));
    }

    static void copyPrompt(VirtualFile file, Editor editor) {
        LineRange lineRange = resolveLineRange(editor);
        String prompt = CodexMessages.filePrompt(file.getPath(), lineRange.toDisplayText());

        // 仅写入剪贴板，避免触发 Codex 桌面端协议。
        CopyPasteManager.getInstance().setContents(new StringSelection(prompt));
    }

    /**
     * 仅在当前上下文包含非目录文件和所属项目时显示复制入口。
     */
    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabledAndVisible(project != null && file != null && !file.isDirectory());
    }

    /**
     * 根据编辑器选区或光标位置计算需要写入提示词的行号范围。
     *
     * @param editor 当前文件编辑器；从项目文件树调用时可能为空
     * @return 从 1 开始计数的文件行号范围
     */
    private static LineRange resolveLineRange(Editor editor) {
        if (editor == null) {
            return new LineRange(1, SINGLE_LINE_RANGE_SIZE);
        }

        // 保持与打开文件功能相同的选区结束行计算规则。
        if (editor.getSelectionModel().hasSelection()) {
            LogicalPosition start = editor.offsetToLogicalPosition(editor.getSelectionModel().getSelectionStart());
            LogicalPosition end = editor.offsetToLogicalPosition(editor.getSelectionModel().getSelectionEnd());
            int endLine = end.column == 0 && end.line > start.line ? end.line : end.line + 1;
            return new LineRange(start.line + 1, Math.max(start.line + 1, endLine));
        }

        int line = editor.getCaretModel().getLogicalPosition().line + 1;
        return new LineRange(line, line);
    }

    /**
     * 表示用于 Codex 文件提示词的起止行号。
     *
     * @param startLine 起始行号，包含边界
     * @param endLine 结束行号，包含边界
     */
    private record LineRange(int startLine, int endLine) {

        /**
         * 将行号范围转换为单行或起止行展示文本。
         */
        String toDisplayText() {
            if (startLine == endLine) {
                return String.valueOf(startLine);
            }
            return startLine + "-" + endLine;
        }
    }
}
