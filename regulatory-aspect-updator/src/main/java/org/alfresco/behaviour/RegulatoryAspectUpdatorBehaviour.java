package org.alfresco.behaviour;

import org.alfresco.action.Orchestrator;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.GlobalPropertiesHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegulatoryAspectUpdatorBehaviour implements NodeServicePolicies.OnUpdatePropertiesPolicy {

    private static final Log logger = LogFactory.getLog(RegulatoryAspectUpdatorBehaviour.class);
    public ArrayList<String> stampSubjectList;
    public String docNodeId = "";
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private final GlobalPropertiesHandler globalProperties = new GlobalPropertiesHandler();
    
    //  FETCHING VALUES FROM alfresco-global.properties - START
    @Value("${boeing.namespace}")
    private String NAMESPACE_BOEING;
    @Value("${boeing.aspect.name}")
    private String ASPECT_BOEING_ONEPPPM;
    @Value("${boeing.regulatory-aspect-list.property}")
    private String PROP_REGULATORY_ASPECT_LIST;
    //  FETCHING VALUES FROM alfresco-global.properties - END

    public void init() {

        System.out.println("*** **** **** START of INIT() method >>> >>> >>> ");

        GlobalPropertiesHandler globalPropertiesHandler = new GlobalPropertiesHandler();
        globalPropertiesHandler.setBoeingNamespace(this.NAMESPACE_BOEING);
        globalPropertiesHandler.setBoeingAspectName(this.ASPECT_BOEING_ONEPPPM);
        globalPropertiesHandler.setRegulatoryAspectListPropertyName(this.PROP_REGULATORY_ASPECT_LIST);

        System.out.println("*** **** **** NODE SERVICE >> " + this.nodeService);

        //On Property Update
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "onUpdateProperties", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

        System.out.println("*** **** **** END of INIT() method >>> >>> >>> ");
    }

    @Override
    public void onUpdateProperties(final NodeRef nodeRef, Map<QName, Serializable> beforeValues, Map<QName, Serializable> afterValues) {

        if (nodeService.exists(nodeRef)) {
            try {
                String tsgNameSpace = "http://www.tsgrp.com/model/openannotate/1.0";
                QName QN_PROP_TSG_IS_ANNOTATED = QName.createQName(tsgNameSpace, "isAnnotated");
                Boolean isAnnotated = (Boolean) nodeService.getProperty(nodeRef, QN_PROP_TSG_IS_ANNOTATED);
                System.out.println("*** isAnnotated >>> " + nodeService.getProperty(nodeRef, QN_PROP_TSG_IS_ANNOTATED));
            } catch (Exception ex) {
                System.out.println("*** **** EXCEPTION **** ****");
                System.out.println(ex);
            }
        }


        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
            private String docNodeId;

            public Object doWork() throws Exception {

                if (nodeService.exists(nodeRef)) {
                    try {
                        String nodeId = nodeRef.getId();
                        System.out.println("CURRENT NODE ID : " + nodeId);

                        /*
                        String fileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString();
                        String fileType = nodeService.getProperty(nodeRef, ContentModel.TYPE_CONTENT).toString();
                        String mimeType = fileType.split("mimetype=")[1].split("size")[0];
                        mimeType = mimeType.replace("|","");

                        System.out.println("FILE NAME : "+fileName);
                        System.out.println("FILE TYPE : "+fileType);
                        System.out.println("MIME TYPE : "+mimeType);

                        Boolean isWriting = fileName.trim().indexOf(".docx") != -1;
                        Boolean isContentTypeOpenAnnotate = mimeType.trim().indexOf("application/vnd.adobe.xfdf") != -1;
                        */

                        Boolean isWriting = isWritingDoc(nodeRef);
                        Boolean isContentTypeOpenAnnotate = isContentTypeOpenAnnotateDoc(nodeRef);

                        if (isWriting || isContentTypeOpenAnnotate) {

                            System.out.println("Invoking new Orchestrator().executeCalls() ");

                            //if content type is Open Annotate, then node is Association.
                            ArrayList<String> stampSubjectList = new Orchestrator().executeCalls(contentService, nodeService, nodeRef, nodeId, isContentTypeOpenAnnotate);

                            System.out.println("stampSubjectList.size() = " + stampSubjectList.size());

                            if (stampSubjectList.size() > 0) {
                                System.out.println("DocNodeId >> " + nodeId + " >> stamp subject >> " + String.join(",", stampSubjectList));
                                new RegulatoryAspectUpdatorBehaviour().applyWebPublishedAspect(nodeService, nodeId, stampSubjectList);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public Boolean isWritingDoc(NodeRef nodeRef) {
        String fileName = nodeService.getProperty(nodeRef, ContentModel.PROP_NAME).toString().trim();
        return ((fileName.indexOf(".docx") != -1) && ((fileName.indexOf("POL") != -1) || (fileName.indexOf("PRO") != -1) || (fileName.indexOf("BPI") != -1)));
    }

    public Boolean isContentTypeOpenAnnotateDoc(NodeRef nodeRef) {
        String fileType = nodeService.getProperty(nodeRef, ContentModel.TYPE_CONTENT).toString();
        String mimeType = fileType.split("mimetype=")[1].split("size")[0];
        mimeType = mimeType.replace("|", "");

        return (mimeType.trim().indexOf("application/vnd.adobe.xfdf") != -1);

    }

    public void applyWebPublishedAspect(NodeService nodeService, String nodeId, ArrayList<String> stampSubjectList) {

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        NodeRef sourceNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);

        List<AssociationRef> sourceAssociationNodeRefList = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);

        for (AssociationRef item : sourceAssociationNodeRefList) {
            sourceNodeRef = item.getSourceRef();
            System.out.println(">>> >>> item.getId() >>> >>> " + item.getId());
            System.out.println(">>> >>> item.getSourceRef().getId() >>> >>> " + item.getSourceRef().getId());
        }

        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>();
        String nameSpace = globalProperties.getBoeingNamespace();

        QName QN_ASPECT_BOEING_ONEPPPM = QName.createQName(nameSpace, globalProperties.getBoeingAspectName());
        QName QN_PROP_REGULATORY_ASPECT_LIST = QName.createQName(nameSpace, globalProperties.getRegulatoryAspectListPropertyName());

        aspectProperties.put(QN_PROP_REGULATORY_ASPECT_LIST, String.join(",", stampSubjectList)); //Comma Separated Reference Values
        nodeService.addAspect(sourceNodeRef, QN_ASPECT_BOEING_ONEPPPM, aspectProperties);

        System.out.println("ASPECT SAVED SUCCESSFULLY TO NODE ID >>> " + sourceNodeRef.getId() + " >>> " + String.join(",", stampSubjectList));
    }

    public NodeService getNodeService() {
        return this.nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

}
