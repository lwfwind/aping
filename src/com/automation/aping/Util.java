package com.automation.aping;

import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;
import com.theoryinpractice.testng.configuration.TestNGConfigurationType;
import com.theoryinpractice.testng.model.TestData;
import com.theoryinpractice.testng.model.TestType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static TestNGConfiguration getTestNGConfiguration(Project project, AnActionEvent e, String type) {
        TestNGConfiguration ac = new TestNGConfiguration("aping", project, TestNGConfigurationType.getInstance().getConfigurationFactories()[0]);
        Module module = Util.getModule(project, e);
        if (module != null) {
            if (type.equalsIgnoreCase("editor")) {
                final Editor editor = e.getData(CommonDataKeys.EDITOR);
                String testCaseName = Util.getTestCaseName(editor);
                VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
                if (virtualFile != null) {
                    String fileName = virtualFile.getName();
                    String testSuiteName = fileName.substring(0, fileName.indexOf("."));
                    //ac.setClassConfiguration(findPsiClass(project, "com.qa.framework.plugin.TestNGEntry"));
                    TestData testData = ac.getPersistantData();
                    testData.SUITE_NAME = project.getBasePath() + File.separator + module.getName() + File.separator + "testng-xml" + File.separator + "TestNGEntry.xml";
                    testData.TEST_OBJECT = TestType.SUITE.getType();
                    ac.setVMParameters(" -DtestSuiteName=" + testSuiteName + " -DtestCaseName=" + testCaseName);
                }
            } else if (type.equalsIgnoreCase("project_view")) {
                PsiFile psiXmlFile = e.getData(LangDataKeys.PSI_FILE);
                if (psiXmlFile != null) {
                    VirtualFile virtualXmlFile = psiXmlFile.getVirtualFile();
                    String testSuiteName = virtualXmlFile.getName().substring(0, virtualXmlFile.getName().indexOf("."));
                    //ac.setClassConfiguration(findPsiClass(project, "com.qa.framework.plugin.TestNGEntry"));
                    TestData testData = ac.getPersistantData();
                    testData.SUITE_NAME = project.getBasePath() + File.separator + module.getName() + File.separator + "testng-xml" + File.separator + "TestNGEntry.xml";
                    testData.TEST_OBJECT = TestType.SUITE.getType();
                    ac.setVMParameters(" -DtestSuiteName=" + testSuiteName + " -DtestCaseName=" + "null");
                } else {
                    PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
                    if (psiElement != null && psiElement instanceof PsiDirectory) {
                        //ac.setClassConfiguration(findPsiClass(project, "com.qa.framework.factory.ExecutorFactory"));
                        TestData testData = ac.getPersistantData();
                        testData.SUITE_NAME = project.getBasePath() + File.separator + module.getName() + File.separator + "testng-xml" + File.separator + "FactoryRun.xml";
                        testData.TEST_OBJECT = TestType.SUITE.getType();
                        String xmlPath = ((PsiDirectory) psiElement).getVirtualFile().getPath();
                        ac.setVMParameters(" -DxmlPath=" + xmlPath);
                    }
                }

            }
            ac.setModule(module);
            ac.setWorkingDirectory(project.getBasePath() + File.separator + module.getName());
        }
        return ac;
    }

    public static ApplicationConfiguration getApplicationConfiguration(Project project, AnActionEvent e, String type) {
        ApplicationConfiguration ac = new ApplicationConfiguration("aping", project, ApplicationConfigurationType.getInstance());
        ac.setMainClassName("com.qa.framework.plugin.Entry");
        if (type.equalsIgnoreCase("editor")) {
            final Editor editor = e.getData(CommonDataKeys.EDITOR);
            String testCaseName = Util.getTestCaseName(editor);
            VirtualFile virtualFile = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
            if (virtualFile != null) {
                String fileName = virtualFile.getName();
                String testSuiteName = fileName.substring(0, fileName.indexOf("."));
                ac.setProgramParameters("-testSuiteName " + testSuiteName + " -testCaseName " + testCaseName);
            }
        } else if (type.equalsIgnoreCase("project_view")) {
            PsiFile psiXmlFile = e.getData(LangDataKeys.PSI_FILE);
            if (psiXmlFile != null) {
                VirtualFile virtualXmlFile = psiXmlFile.getVirtualFile();
                String testSuiteName = virtualXmlFile.getName().substring(0, virtualXmlFile.getName().indexOf("."));
                ac.setProgramParameters("-testSuiteName " + testSuiteName + " -testCaseName " + "null");
            }
        }
        Module module = getModule(project, e);
        if (module != null) {
            ac.setModule(module);
            ac.setWorkingDirectory(project.getBasePath() + File.separator + module.getName());
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
        if (fileByUrl != null) {
            return projectFileIndex.getModuleForFile(fileByUrl);
        } else {
            return null;
        }
    }

    public static String getTestCaseName(final Editor editor) {
        Document document = editor.getDocument();
        int offset = editor.getCaretModel().getOffset();
        int caretLineNumber = document.getLineNumber(offset);
        String lineText = document.getText().substring(document.getLineStartOffset(caretLineNumber), document.getLineEndOffset(caretLineNumber));
        String testCaseName = "";
        if (lineText.contains("TestSuite ") || lineText.contains("<?xml version")) {
            testCaseName = "null";
        } else if (lineText.contains("TestCase ")) {
            testCaseName = getBetweenString(lineText, "name=\"", "\"");
        } else {
            List<String> testCaseList = new ArrayList<String>();
            int lineNumber = 0;
            for (String line : document.getText().split("\n")) {
                lineNumber = lineNumber + 1;
                if (lineNumber > caretLineNumber) {
                    break;
                }
                if (line.contains("TestCase ")) {
                    testCaseName = getBetweenString(line, "name=\"", "\"");
                    testCaseList.add(testCaseName);
                }
            }
            if (testCaseList.size() > 0) {
                testCaseName = testCaseList.get(testCaseList.size() - 1);
            } else {
                testCaseName = "null";
            }
        }
        return testCaseName;
    }

    public static String getBetweenString(String origiString, String beforeStr, String afterStr) {
        String afterSearchStr = origiString.substring(origiString.indexOf(beforeStr) + beforeStr.length());
        return afterSearchStr.substring(0, afterSearchStr.indexOf(afterStr));
    }

    private static PsiClass findPsiClass(final Project project, final String className) {
        final PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        assert psiClass != null;
        return psiClass;
    }

    public static void updateEditorVisibility(final AnActionEvent e, String runOrDebug) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        Document document = editor.getDocument();
        //Set visibility only in case of existing project and editor
        e.getPresentation().setVisible((project != null && document.getText().contains("TestSuite") && document.getText().contains("TestCase")));
        String testCaseName = Util.getTestCaseName(editor);
        if (testCaseName.equals("")) {
            String testSuiteName = document.toString().substring(document.toString().lastIndexOf("/") + 1, document.toString().lastIndexOf("."));
            e.getPresentation().setText(runOrDebug + " " + testSuiteName);
        } else {
            e.getPresentation().setText(runOrDebug + " " + testCaseName);
        }
    }

    public static void updateProjectViewVisibility(final AnActionEvent e, String runOrDebug) {
        e.getPresentation().setVisible(false);
        final Project project = e.getData(CommonDataKeys.PROJECT);
        PsiFile psiXmlFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiXmlFile != null) {
            VirtualFile virtualXmlFile = psiXmlFile.getVirtualFile();
            if (virtualXmlFile == null) {
                return;
            }
            if (!virtualXmlFile.getName().endsWith(".xml")) {
                return;
            }
            String content = (String) FileDocumentManager.getInstance().getDocument(virtualXmlFile).getText();
            e.getPresentation().setVisible((project != null && content.contains("TestSuite") && content.contains("TestCase")));
            String suiteName = virtualXmlFile.getName().substring(0, virtualXmlFile.getName().indexOf("."));
            e.getPresentation().setText(runOrDebug + " " + suiteName);
        } else {
            PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
            if (psiElement == null || !(psiElement instanceof PsiDirectory)) {
                return;
            }
            List<PsiFile> psiFileList = new ArrayList<>();
            getSubFiles((PsiDirectory) psiElement, psiFileList);
            boolean isContainValidXml = false;
            for (PsiFile psiFile : psiFileList) {
                VirtualFile virtualXmlFile = psiFile.getVirtualFile();
                if (virtualXmlFile.getName().endsWith(".xml")) {
                    String content = (String) FileDocumentManager.getInstance().getDocument(virtualXmlFile).getText();
                    if (content.contains("TestSuite") && content.contains("TestCase")) {
                        isContainValidXml = true;
                        break;
                    }
                }
            }
            if (isContainValidXml) {
                e.getPresentation().setVisible(true);
                e.getPresentation().setText(runOrDebug + " in " + ((PsiDirectory) psiElement).getName());
            }
        }
    }

    private static void getSubFiles(PsiDirectory psiDirectory, List<PsiFile> psiFileList) {
        PsiFile[] psiFileArray = psiDirectory.getFiles();
        if (psiFileArray.length > 0) {
            for (PsiFile psiFile : psiFileArray) {
                psiFileList.add(psiFile);
            }
        }
        PsiDirectory[] psiDirectories = psiDirectory.getSubdirectories();
        if (psiDirectories.length > 0) {
            for (PsiDirectory psiSubDirectory : psiDirectories) {
                getSubFiles(psiSubDirectory, psiFileList);
            }
        }
    }
}
