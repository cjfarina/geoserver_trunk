package org.geoserver.gss.wfsbridge;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.opengis.wfs.TransactionType;

import org.geoserver.gss.impl.GSS;
import org.geoserver.wfs.TransactionEvent;

public class GSSTransactionListenerTest extends TestCase {

    private GSSTransactionListener listener;

    private GSS mockGssFacade;

    protected void setUp() throws Exception {
        mockGssFacade = mock(GSS.class);
        listener = new GSSTransactionListener(mockGssFacade);
    }

    public void testBeforeTransaction() {
        Map<Object, Object> extendedProperties = new HashMap<Object, Object>();
        TransactionType request = mock(TransactionType.class);
        when(request.getExtendedProperties()).thenReturn(extendedProperties);
        listener.beforeTransaction(request);
        assertTrue(extendedProperties.get(GSSTransactionListener.GSS_TRANSACTION_UUID) instanceof String);
    }

    public void testDataStoreChange() {
        TransactionEvent event = mock(TransactionEvent.class);
        listener.dataStoreChange(event);
        fail("Not yet implemented");
    }

    public void testBeforeCommit() {
        fail("Not yet implemented");
    }

    public void testAfterTransaction() {
        fail("Not yet implemented");
    }

}
