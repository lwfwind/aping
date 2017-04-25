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
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * The type Run testcase.
 */
public class EditorDebugTestCase extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        debugInIDEA(project, e);
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
            e.getPresentation().setText("Debug " + testSuiteName);
        } else {
            e.getPresentation().setText("Debug " + testCaseName);
        }
    }

    public void debugInIDEA(Project project, AnActionEvent e) {
        ApplicationConfiguration applicationConfiguration = Util.getApplicationConfiguration(project, e, "editor");
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
