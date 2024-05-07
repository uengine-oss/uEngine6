package org.uengine.kernel;

public class FieldDescriptor {
    // {
	// 	"_type":"org.uengine.model.FieldDescriptor",
	// 	"name":"id",
	// 	"className":"Long",
	// 	"nameCamelCase":"id",
	// 	"namePascalCase":"Id",
	// 	"isKey":true
    // }
    String name;
    String className;
    Boolean isKey;

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getIsKey() {
        return isKey;
    }

    public void setIsKey(Boolean isKey) {
        this.isKey = isKey;
    }
}
