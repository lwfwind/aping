package com.automation.aping;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;
import com.theoryinpractice.testng.configuration.TestNGConfigurationType;

import java.io.File;

/**
 * The type Run testcase.
 */
public class EditorRunTestCase extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        runTestNGTestInIDEA(project, e);
    }

    //Defining action’s visibility
    @Override
    public void update(final AnActionEvent e) {
        //Get required data keys
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
            e.getPresentation().setText("Run " + testSuiteName);
        } else {
            e.getPresentation().setText("Run " + testCaseName);
        }
    }

    public void runInIDEA(Project project, AnActionEvent e) {
        ApplicationConfiguration applicationConfiguration = Util.getApplicationConfiguration(project, e, "editor");
        try {

            Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();
            final ProgramRunner runner = RunnerRegistry.getInstance().getRunner(DefaultRunExecutor.EXECUTOR_ID,
                    applicationConfiguration);
            ExecutionEnvironmentBuilder executionEnvironmentBuilder = new ExecutionEnvironmentBuilder(project,
                    runExecutorInstance).runner(runner).runProfile(applicationConfiguration);
            ExecutionEnvironment build = executionEnvironmentBuilder.build();
            runner.execute(build);
        } catch (ExecutionException ex) {
            Messages.showMessageDialog(project, "error", "error", Messages.getErrorIcon());
        }
    }

    private void runTestNGTestInIDEA(Project project, AnActionEvent e) {
        TestNGConfiguration applicationConfiguration = Util.getTestNGConfiguration(project, e, "editor");
        try {

            Executor runExecutorInstance = DefaultRunExecutor.getRunExecutorInstance();
            final ProgramRunner runner = RunnerRegistry.getInstance().getRunner(DefaultRunExecutor.EXECUTOR_ID,
                    applicationConfiguration);
            ExecutionEnvironmentBuilder executionEnvironmentBuilder = new ExecutionEnvironmentBuilder(project,
                    runExecutorInstance).runner(runner).runProfile(applicationConfiguration);
            ExecutionEnvironment build = executionEnvironmentBuilder.build();
            runner.execute(build);
        } catch (ExecutionException ex) {
            Messages.showMessageDialog(project, "error", "error", Messages.getErrorIcon());
        }
    }
}
