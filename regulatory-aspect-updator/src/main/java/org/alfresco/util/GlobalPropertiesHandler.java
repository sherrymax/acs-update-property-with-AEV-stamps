package org.alfresco.util;

import org.alfresco.behaviour.RegulatoryAspectUpdatorBehaviour;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GlobalPropertiesHandler {
    private static Log logger = LogFactory.getLog(GlobalPropertiesHandler.class);
    private static String boeingNamespace;
    private static String boeingAspectName;
    private static String regulatoryAspectListPropertyName;

    public String getBoeingNamespace() {
        return this.boeingNamespace;
    }
    public void setBoeingNamespace(String ns) {
        this.boeingNamespace = ns;
    }

    public String getBoeingAspectName() {
        return this.boeingAspectName;
    }
    public void setBoeingAspectName(String aspectName) {
        this.boeingAspectName = aspectName;
    }

    public String getRegulatoryAspectListPropertyName() {
        return this.regulatoryAspectListPropertyName;
    }
    public void setRegulatoryAspectListPropertyName(String propName) {
        this.regulatoryAspectListPropertyName = propName;
    }

}