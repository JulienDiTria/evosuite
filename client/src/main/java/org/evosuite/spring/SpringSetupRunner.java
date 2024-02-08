package org.evosuite.spring;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class SpringSetupRunner extends SpringJUnit4ClassRunner {

    public Statement statement;
    public FrameworkMethod frameworkMethod;
    public Object testInstance;

    /**
     * Construct a new {@code SpringSetupRunner} and initialize a
     * {@link org.springframework.test.context.TestContextManager TestContextManager}
     * to provide Spring testing functionality to standard JUnit 4 tests.
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public SpringSetupRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    /**
     * Perform the same logic as
     * {@link SpringJUnit4ClassRunner#runChild(FrameworkMethod, RunNotifier)},
     * except the statement is not executed and only saved for later use.
     */
    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        Description description = describeChild(frameworkMethod);
        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        }
        else {
            try {
                statement = methodBlock(frameworkMethod);
            }
            catch (Throwable ex) {
                statement = new Fail(ex);
            }
            // THIS IS THE EXECUTION OF THE STATEMENT WHICH WE DON'T WANT
//            runLeaf(statement, description, notifier);
        }
    }

    /**
     * Intercept the methodBlock to save the framework method and test instance
     * then call the super methodBlock {@link SpringJUnit4ClassRunner#methodBlock(FrameworkMethod)} .
     */
    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        try {
            testInstance = new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        }
        catch (Throwable ex) {
            return new Fail(ex);
        }
        this.frameworkMethod = frameworkMethod;

        return super.methodBlock(frameworkMethod);
    }

    /**
     * Get the test context manager
     */
    public TestContextManager getTestContextManagerPublic() {
        return super.getTestContextManager();
    }

}
