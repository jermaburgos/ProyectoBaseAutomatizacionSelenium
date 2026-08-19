package context;

public class ContextManager {

    private static final ThreadLocal<TestContext> context =
            ThreadLocal.withInitial(TestContext::new);

    public static TestContext getContext() {
        return context.get();
    }

    public static void removeContext() {
        context.remove();
    }
}
