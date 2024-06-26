package org.uengine.processmanager;

import org.uengine.kernel.TransactionListener;

import java.util.List;

/**
 * Created by uengine on 2018. 11. 16..
 */
public interface TransactionContext {
    void addTransactionListener(TransactionListener tl);

    List getTransactionListeners();

    Object getSharedContext(String contextKey);

    void setSharedContext(String contextKey, Object value);

    void commit() throws Exception;

    void rollback() throws Exception;

    void releaseResources() throws Exception;

    void addDebugInfo(String s);
}
