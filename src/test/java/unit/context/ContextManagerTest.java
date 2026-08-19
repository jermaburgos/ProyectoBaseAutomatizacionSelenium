package unit.context;

import context.ContextManager;
import context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ContextManagerTest {

    @Test
    public void getContext_debeRetornarLaMismaInstanciaEnElMismoHilo() {
        TestContext first = ContextManager.getContext();
        TestContext second = ContextManager.getContext();

        Assert.assertSame(first, second);
    }

    @Test
    public void removeContext_debeCrearUnaNuevaInstancia() {
        TestContext first = ContextManager.getContext();

        ContextManager.removeContext();

        TestContext second = ContextManager.getContext();

        Assert.assertNotSame(first, second);
    }
}
