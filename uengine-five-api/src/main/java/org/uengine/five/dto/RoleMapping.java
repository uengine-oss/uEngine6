package org.uengine.five.dto;

public class RoleMapping {
    String name;
    String[] resourceNames;
    String[] endpoints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getResourceNames() {
        return resourceNames;
    }

    public void setResourceNames(String[] resourceNames) {
        this.resourceNames = resourceNames;
    }

    public String[] getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }

    public org.uengine.kernel.RoleMapping toKernelRoleMapping() {
        org.uengine.kernel.RoleMapping kernelRoleMapping = org.uengine.kernel.RoleMapping.create();
        kernelRoleMapping.setName(name);
        for (int i = 0; i < resourceNames.length; i++) {
            kernelRoleMapping.setEndpoint(endpoints[i]);
            kernelRoleMapping.setResourceName(resourceNames[i]);
            kernelRoleMapping.moveToAdd();
        }

        return kernelRoleMapping;
    }
}
