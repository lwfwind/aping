package com.automation.aping;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.theoryinpractice.testng.configuration.TestNGConfiguration;

/**
 * The type Run testcase.
 */
public class ProjectViewRunTestSuite extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        runTestNGInIDEA(project, e);
    }

    //Defining action’s visibility
    @Override
    public void update(final AnActionEvent e) {
        Util.updateProjectViewVisibility(e, "Run");
    }

    private void runInIDEA(Project project, AnActionEvent e) {
        ApplicationConfiguration applicationConfiguration = Util.getApplicationConfiguration(project, e, "project_view");
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

    private void runTestNGInIDEA(Project project, AnActionEvent e) {
        TestNGConfiguration applicationConfiguration = Util.getTestNGConfiguration(project, e, "project_view");
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
