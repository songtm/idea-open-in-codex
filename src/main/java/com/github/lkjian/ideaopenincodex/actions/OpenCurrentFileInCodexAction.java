package com.github.lkjian.ideaopenincodex.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class OpenCurrentFileInCodexAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(OpenCurrentFileInCodexAction.class);

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null || file.isDirectory()) {
            return;
        }

        CopyFilePromptAction.copyPrompt(file, event.getData(CommonDataKeys.EDITOR));
        if (!SystemInfo.isWindows) {
            showPasteInstructions(project);
            return;
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                String script = """
                        $ErrorActionPreference = 'Stop'
                        $windows = @(Get-Process -Name Codex,ChatGPT -ErrorAction SilentlyContinue |
                            Where-Object { $_.MainWindowHandle -ne 0 })
                        if ($windows.Count -ne 1) { throw "Expected one Codex window, found $($windows.Count)." }
                        $shell = New-Object -ComObject WScript.Shell
                        if (-not $shell.AppActivate($windows[0].Id)) { throw 'Could not activate Codex.' }
                        Add-Type @'
                        using System;
                        using System.Runtime.InteropServices;
                        public static class CodexKeyboard {
                            [DllImport("user32.dll")] public static extern IntPtr GetForegroundWindow();
                            [DllImport("user32.dll")] public static extern short GetAsyncKeyState(int key);
                            [DllImport("user32.dll")] public static extern void keybd_event(byte key, byte scan, uint flags, UIntPtr extra);
                        }
                        '@
                        $deadline = [DateTime]::UtcNow.AddSeconds(2)
                        do {
                            Start-Sleep -Milliseconds 40
                            $foreground = [CodexKeyboard]::GetForegroundWindow() -eq $windows[0].MainWindowHandle
                            $heldKeys = @(0x10, 0x11, 0x12, 0x4F, 0x56, 0x5B, 0x5C | Where-Object {
                                [CodexKeyboard]::GetAsyncKeyState($_) -lt 0
                            })
                        } until (($foreground -and $heldKeys.Count -eq 0) -or [DateTime]::UtcNow -ge $deadline)
                        if (-not $foreground -or $heldKeys.Count -ne 0 -or
                            [CodexKeyboard]::GetForegroundWindow() -ne $windows[0].MainWindowHandle) {
                            throw 'Codex is not in the foreground or a shortcut key is still held.'
                        }
                        $extra = [UIntPtr]::Zero
                        [CodexKeyboard]::keybd_event(0x11, 0, 0, $extra)
                        try {
                            [CodexKeyboard]::keybd_event(0x56, 0, 0, $extra)
                            [CodexKeyboard]::keybd_event(0x56, 0, 2, $extra)
                        } finally {
                            [CodexKeyboard]::keybd_event(0x11, 0, 2, $extra)
                        }
                        """;
                String encodedScript = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
                Process process = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
                        "-EncodedCommand", encodedScript
                ).redirectErrorStream(true).start();
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (process.waitFor() == 0) {
                    return;
                }
                LOG.warn("Could not paste into Codex: " + output.strip());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException ex) {
                LOG.warn("Could not run the Codex paste command", ex);
            }

            ApplicationManager.getApplication().invokeLater(() -> {
                if (!project.isDisposed()) {
                    showPasteInstructions(project);
                }
            });
        });
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabledAndVisible(project != null && file != null && !file.isDirectory());
    }

    private static void showPasteInstructions(Project project) {
        Messages.showInfoMessage(project,
                "File prompt copied. Switch to your current Codex chat and paste it.",
                "Open In Codex");
    }
}
