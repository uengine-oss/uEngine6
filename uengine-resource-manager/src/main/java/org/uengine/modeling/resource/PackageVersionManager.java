package org.uengine.modeling.resource;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jjy on 2016. 1. 15..
 */
public class PackageVersionManager extends SimpleVersionManager{



    @Override
    public String versionDirectoryOf(Version version) {

        if(version!=null)
            return getAppName() + VERSION_DIR + (getModuleName()!=null ? "/" + getModuleName() : "") + "/" + version.getMajor() + "." + version.getMinor();
        else
            return getAppName() + VERSION_DIR + (getModuleName()!=null ? "/" + getModuleName() : "");

    }

    @Override
    public String getLogicalPath(String resourcePath) {

        {
            String appName_versions = versionDirectoryOf(null);

            if (resourcePath.startsWith(appName_versions)) {
                String withoutPrefixPath = resourcePath.substring(appName_versions.length()+1);
                String[] parts = withoutPrefixPath.split("/");

                String logicalPath = "";
                if(parts.length > 2){
                    for(int i=0; i<parts.length; i++){ //join the path elements without the version and module name
                        if(i!=1)
                            logicalPath = logicalPath + (i>0 ? "/":"") + parts[i];
                    }
                }

                return logicalPath;
            }

            String prefixCandidate2 = getAppName() + "/../" + appName_versions;

            if (resourcePath.startsWith(prefixCandidate2)) {
                String withoutPrefixPath = resourcePath.substring(prefixCandidate2.length()+1);
                String[] parts = withoutPrefixPath.split("/");

                String logicalPath = "";
                String sep = "";
                if(parts.length > 2){
                    if(getModuleName()==null){
                        setModuleName(parts[0]);
                    }

                    for(int i=2; i<parts.length; i++){ //join the path elements without the version and module name
                        logicalPath = logicalPath + sep + parts[i];
                        sep = "/";
                    }
                }

                return logicalPath;
            }


        }


        {
            String prefixCandidate = getAppName() + "/";

            if (resourcePath.startsWith(prefixCandidate)) {

                String withoutPrefixPath = resourcePath.substring(prefixCandidate.length());
                String[] parts = withoutPrefixPath.split("/");

                String logicalPath = "";
                String sep = "";

                if(parts.length > 1){
                    if(getModuleName()==null){
                        setModuleName(parts[0]);
                    }

                    for(int i=1; i<parts.length; i++){ //join the path elements without the version and module name
                        logicalPath = logicalPath + sep + parts[i];
                        sep = "/";
                    }

                    return logicalPath;
                }

                if(getModuleName()!=null) return ""; //return without module name

                return withoutPrefixPath; // in case of default module (no module resource)
            }
        }

        return super.getLogicalPath(resourcePath);
    }

    @Override
    public void load(String appName, String moduleName) throws Exception {
        if(moduleName==null) throw new Exception("Select a folder(module) for version management");

        super.load(appName, moduleName);
    }
}
