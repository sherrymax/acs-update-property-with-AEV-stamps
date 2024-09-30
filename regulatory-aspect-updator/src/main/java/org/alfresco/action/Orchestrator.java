package org.alfresco.action;

import java.util. * ;

import org.alfresco.behaviour.RegulatoryAspectUpdatorBehaviour;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.cmr.repository. * ;
import org.alfresco.util.GlobalPropertiesHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public class Orchestrator {

    private static Log logger = LogFactory.getLog(Orchestrator.class);
    private InvokeREST invoker = new InvokeREST();
    private ArrayList < String > stampSubjectList = new ArrayList < >();
    private GlobalPropertiesHandler globalProperties = new GlobalPropertiesHandler();
    private NodeService nodeService;
    
    public Orchestrator() {
        RegulatoryAspectUpdatorBehaviour behaviour = new RegulatoryAspectUpdatorBehaviour();
        this.nodeService = behaviour.getNodeService();
    }

    public ArrayList < String > executeCalls(ContentService contentService, NodeService nodeService, final NodeRef nodeRef, String nodeId, Boolean isAlreadyStampAssociation) {
        ArrayList < String > stampNodeIdList = new ArrayList < String > ();

        logger.debug("NODE ID Inside executeCalls() >>> " + nodeId);

        if (isAlreadyStampAssociation == true) {
            logger.debug(" Node ID " + nodeId + " is already a Stamp ASSOCIATION");
            stampNodeIdList.add(nodeId);
        } else {
            logger.debug(" Node ID " + nodeId + " is NOT A Stamp ASSOCIATION");
            stampNodeIdList = invoker.getStampAssociations(nodeService, nodeId);
        }
        String stampNodeId = "";

        logger.debug(" Stamp Nodes List Size = " + stampNodeIdList.size());
        logger.debug(" Invoking FOR LOOP to Get Content of Stamp ASSOCIATION ");
        if (stampNodeIdList.size() > 0) {
            for (var i = 0; i < stampNodeIdList.toArray().length; i++) {
                stampNodeId = (String) stampNodeIdList.toArray()[i];
                logger.debug(" Iteration # " + i + " to Get Content of Stamp ASSOCIATION >>> StampNodeId = " + stampNodeId);
                this.stampSubjectList = new InvokeREST().callGET(contentService, stampNodeId);
            }
        }

        return this.stampSubjectList;

    }

}