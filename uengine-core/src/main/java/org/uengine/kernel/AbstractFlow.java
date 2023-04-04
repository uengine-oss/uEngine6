package org.uengine.kernel;

/**
 * Created by uengine on 2018. 3. 2..
 */
public abstract class AbstractFlow extends Relation implements java.io.Serializable {
    private static final long serialVersionUID = org.uengine.kernel.GlobalContext.SERIALIZATION_UID;

    private String sourceRef;

    public String getSourceRef() {
        return sourceRef;
    }

    public void setSourceRef(String source) {
        this.sourceRef = source;
    }

    private String targetRef;

    public String getTargetRef() {
        return targetRef;
    }

    public void setTargetRef(String target) {
        this.targetRef = target;
    }

    String tracingTag;

    public String getTracingTag() {
        return tracingTag;
    }

    public void setTracingTag(String tag) {
        tracingTag = tag;
    }
}