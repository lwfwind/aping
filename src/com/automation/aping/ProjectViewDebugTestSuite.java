package com.automation.aping;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;

/**
 * The type Run testcase.
 */
public class ProjectViewDebugTestSuite extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        debugTestNGInIDEA(project, e);
    }

    //Defining action’s visibility
    @Override
    public void update(final AnActionEvent e) {
        e.getPresentation().setVisible(false);
        final Project project = e.getData(CommonDataKeys.PROJECT);
        PsiFile psiXmlFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiXmlFile == null)
            return;
        VirtualFile virtualXmlFile = psiXmlFile.getVirtualFile();
        if (!virtualXmlFile.getName().endsWith(".xml")) {
            return;
        }
        String content = (String) FileDocumentManager.getInstance().getDocument(virtualXmlFile).getText();
        e.getPresentation().setVisible((project != null && content.contains("TestSuite") && content.contains("TestCase")));
        String suiteName = virtualXmlFile.getName().substring(0, virtualXmlFile.getName().indexOf("."));
        e.getPresentation().setText("Debug " + suiteName);
    }

    private void debugInIDEA(Project project, AnActionEvent e) {
        ApplicationConfiguration applicationConfiguration = Util.getApplicationConfiguration(project, e, "project_view");
        try {
            Executor debugExecutorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
            final ProgramRunner runner = RunnerRegistry.getInstance().getRunner(DefaultDebugExecutor.EXECUTOR_ID,
                    applicationConfiguration);
            ExecutionEnvironmentBuilder executionEnvironmentBuilder = new ExecutionEnvironmentBuilder(project,
                    debugExecutorInstance).runner(runner).runProfile(applicationConfiguration);
            ExecutionEnvironment build = executionEnvironmentBuilder.build();
            runner.execute(build);
        } catch (ExecutionException ex) {
            Messages.showMessageDialog(project, "error", "error", Messages.getErrorIcon());
        }
    }

    public void debugTestNGInIDEA(Project project, AnActionEvent e) {
        TestNGConfiguration applicationConfiguration = Util.getTestNGConfiguration(project, e, "project_view");
        try {
            Executor debugExecutorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
            final ProgramRunner runner = RunnerRegistry.getInstance().getRunner(DefaultDebugExecutor.EXECUTOR_ID,
                    applicationConfiguration);
            ExecutionEnvironmentBuilder executionEnvironmentBuilder = new ExecutionEnvironmentBuilder(project,
                    debugExecutorInstance).runner(runner).runProfile(applicationConfiguration);
            ExecutionEnvironment build = executionEnvironmentBuilder.build();
            runner.execute(build);
        } catch (ExecutionException ex) {
            Messages.showMessageDialog(project, "error", "error", Messages.getErrorIcon());
        }
    }

}
