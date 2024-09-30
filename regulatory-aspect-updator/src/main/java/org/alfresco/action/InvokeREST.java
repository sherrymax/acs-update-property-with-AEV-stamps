package org.alfresco.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository. * ;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GlobalPropertiesHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.alfresco.service.namespace.RegexQNamePattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InvokeREST {

    private static Log logger = LogFactory.getLog(InvokeREST.class);
    private GlobalPropertiesHandler globalProperties;
    private ServiceRegistry serviceRegistry;

    public InvokeREST() {
        this.globalProperties = new GlobalPropertiesHandler();
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public ArrayList < String > callGET(ContentService contentService, String nodeId) {

        logger.debug(">>>>>> Node ID Inside callGET <<<<<<<< " + nodeId);

        ArrayList < String > stampSubjectList = new ArrayList < >();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        try {
            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            if (reader.exists()) {
                System.out.println(">>>>>> INSIDE callGET.reader.exists() <<<<<<<< " + nodeId);
                InputStream inputStream = reader.getContentInputStream();
                stampSubjectList = new QueryXMLAttributes().getSubjectFromStamp(inputStream);
                System.out.println(">>>>>> stampSubjectList <<<<<<<< " + String.join(",", stampSubjectList));

            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return stampSubjectList;
    }

    public ArrayList < String > getStampAssociations(NodeService nodeService, String nodeId) {
        ArrayList < String > stampNodeList = new ArrayList < >();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        List < ChildAssociationRef > childAssocsNodeRefList = nodeService.getChildAssocs(nodeRef);
        List < AssociationRef > assocsNodeRefList = nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

        for (AssociationRef item: assocsNodeRefList) {
            System.out.println(">>> >>> MYYYAAAAVOOO >>> item.getId() >>> >>> " + item.getTargetRef().getId());
            stampNodeList.add(item.getTargetRef().getId());
        }

        return stampNodeList;
    }

}