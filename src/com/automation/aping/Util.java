package com.automation.aping;


import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static ApplicationConfiguration getApplicationConfiguration(Project project, AnActionEvent e) {
        ApplicationConfiguration ac = new ApplicationConfiguration("aping", project, ApplicationConfigurationType.getInstance());
        ac.setMainClassName("com.qa.framework.plugin.Entry");
        Module module = getModule(project, e);
        if (module != null) {
            ac.setModule(module);
            ac.setWorkingDirectory(project.getBasePath() + File.separator + module.getName());
        }
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        String testCaseName = Util.getTestCaseName(editor);
        VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (virtualFile != null) {
            String fileName = virtualFile.getName();
            String testSuiteName = fileName.substring(0, fileName.indexOf("."));
            ac.setProgramParameters("-testSuiteName " + testSuiteName + " -testCaseName " + testCaseName);
        }

        return ac;
    }

    public static Module getModule(Project project, final AnActionEvent e) {
        final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (virtualFile == null) {
            // backward compatibility
            return ModuleManager.getInstance(project).getModules()[0];
        }
        String fullPath = virtualFile.getPath();
        VirtualFile fileByUrl = VirtualFileManager.getInstance().findFileByUrl("file://" + fullPath);
        if(fileByUrl != null) {
            return projectFileIndex.getModuleForFile(fileByUrl);
        }
        else
        {
            return null;
        }
    }

    public static String getTestCaseName(final Editor editor) {
        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        int caretLineNumber = document.getLineNumber(offset);
        String lineText = document.getText().substring(document.getLineStartOffset(caretLineNumber), document.getLineEndOffset(caretLineNumber));
        String testCaseName = "";
        if (lineText.contains("DataConfig ") || lineText.contains("<?xml version")) {
            testCaseName = "";
        } else if (lineText.contains("TestData ")) {
            testCaseName = getBetweenString(lineText, "name=\"", "\"");
        } else {
            List<String> testCaseList = new ArrayList<String>();
            int lineNumber = 0;
            for (String line : document.getText().split("\n")) {
                lineNumber = lineNumber + 1;
                if (lineNumber > caretLineNumber) {
                    break;
                }
                if (line.contains("TestData ")) {
                    testCaseName = getBetweenString(line, "name=\"", "\"");
                    testCaseList.add(testCaseName);
                }
            }
            if (testCaseList.size() > 0) {
                testCaseName = testCaseList.get(testCaseList.size() - 1);
            } else {
                testCaseName = "";
            }
        }
        return testCaseName;
    }

    public static String getBetweenString(String origiString, String beforeStr, String afterStr) {
        String afterSearchStr = origiString.substring(origiString.indexOf(beforeStr) + beforeStr.length());
        return afterSearchStr.substring(0, afterSearchStr.indexOf(afterStr));
    }
}