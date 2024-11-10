package com.obiscr.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LatexFormatAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent) {
        Project project = actionEvent.getProject();
        if (project == null) {
            return;
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        if (!selectionModel.hasSelection()) {
            return;
        }

        String selectedText = ReadAction.compute(selectionModel::getSelectedText);
        if (selectedText == null || selectedText.isEmpty()) {
            return;
        }
        String[] lines = selectedText.split("\n");

        String formattedString = formattedLatexString(lines);

        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();

        WriteCommandAction.runWriteCommandAction(project, () -> {
            editor.getDocument().replaceString(start, end, formattedString);
        });
    }

    private String formattedLatexString(String[] lines) {
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(formatLine(line)).append("\n");
        }
        return result.toString().trim();
    }

    private String formatLine(String line) {
        StringBuilder formattedLine = new StringBuilder();
        int start = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (isChineseCharOrPunctuation(c)) {
                if (start != -1) {
                    String formula = line.substring(start, i);
                    formattedLine.append(formatFormula(formula));
                    start = -1;
                }
                formattedLine.append(c);
            } else if (start == -1) {
                start = i;
            }
        }

        if (start != -1) {
            String formula = line.substring(start);
            formattedLine.append(formatFormula(formula));
        }

        return formattedLine.toString();
    }

    private boolean isChineseCharOrPunctuation(char c) {
        return String.valueOf(c).matches("[\\u4e00-\\u9fa5，。、；：？！【】]");
    }

    private String formatFormula(String formula) {
        formula = formula.trim();

        formula = formula.replace('（', '(')
                .replace('）', ')')
                .replaceAll("’", "^{\\prime}");

        StringBuilder result = new StringBuilder();
        for (char c : formula.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("\\vec{").append(c).append("}");
            } else {
                result.append(c);
            }
        }

        return " $" + result + "$ ";
    }

}
