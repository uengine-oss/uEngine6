package org.uengine.modeling;

import java.io.Serializable;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author jyj
 */
public class ElementView implements Serializable, Cloneable {

    @Override
    public Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    private static final long serialVersionUID = 1234L;

    public final static String WHERE_ICONIC = "iconic";
    public final static String WHERE_CANVAS = "canvas";
    public final static String WHERE_DETAIL = "detail";

    String id;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String parent;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    String shapeId;

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    double x;
        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }


    double y;
        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }


    double width;
        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }


    double height;
        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }


    String fromEdge;

    public String getFromEdge() {
        return fromEdge;
    }

    public void setFromEdge(String fromEdge) {
        this.fromEdge = fromEdge;
    }

    String toEdge;

    public String getToEdge() {
        return toEdge;
    }

    public void setToEdge(String toEdge) {
        this.toEdge = toEdge;
    }

    public void addToEdge(String toEdge) {
        if (getToEdge() == null || "".equals(getToEdge())) {
            this.toEdge = toEdge;
        } else {
            this.toEdge = getToEdge() + "," + toEdge;
        }
    }

    String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    String style;

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @JsonIgnore
    IElement element;

    public IElement getElement() {
        return element;
    }

    public void setElement(IElement element) {
        if(element!=null && element.getName()!=null)
            setLabel(element.getName());

        this.element = element;
    }

    
    public ElementView() {
    }

    public ElementView(IElement element) {
        this();
        this.setElement(element);
    }

    String instStatus;

    public String getInstStatus() {
        return instStatus;
    }

    public void setInstStatus(String instStatus) {
        this.instStatus = instStatus;
    }

    String backgroundColor;

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    String viewType;

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    int propertyDialogHeight;

    public int getPropertyDialogHeight() {
        return propertyDialogHeight;
    }

    public void setPropertyDialogHeight(int propertyDialogHeight) {
        this.propertyDialogHeight = propertyDialogHeight;
    }

    int propertyDialogWidth;

    public int getPropertyDialogWidth() {
        return propertyDialogWidth;
    }

    public void setPropertyDialogWidth(int propertyDialogWidth) {
        this.propertyDialogWidth = propertyDialogWidth;
    }


    /**
     * convert main object from RelationView to IRelation
     *
     * @return IRelation which have this class as relationView
     */
    public IElement asElement() {
        IElement element = getElement();
        setElement(null);
        element.setElementView(this);
        return element;
    }

    boolean changed;
        public boolean getChanged() {
            return changed;
        }
        public void setChanged(boolean changed) {
            this.changed = changed;
        }

    boolean byDrop;
        public boolean getByDrop() {
            return byDrop;
        }
        public void setByDrop(boolean byDrop) {
            this.byDrop = byDrop;
        }

    private String value;
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    private String from;
        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

    private String to;
        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

    String geom;

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }
}