package org.opencyc.api;

import  java.io.*;
import  javax.naming.TimeLimitExceededException;
import  fipaos.ont.fipa.*;
import  fipaos.ont.fipa.fipaman.*;
import  fipaos.util.*;
import  org.opencyc.util.*;
import  org.opencyc.cycobject.*;
import  org.opencyc.cycagent.*;
import  org.opencyc.cycagent.coabs.*;
import  org.opencyc.cycagent.fipaos.*;

/**
 * Provides remote access a binary connection and an ascii connection to the OpenCyc server.
 *
 * @version $Id$
 * @author Stephen L. Reed
 *
 * <p>Copyright 2001 Cycorp, Inc., license is open source GNU LGPL.
 * <p><a href="http://www.opencyc.org/license.txt">the license</a>
 * <p><a href="http://www.opencyc.org">www.opencyc.org</a>
 * <p><a href="http://www.sourceforge.net/projects/opencyc">OpenCyc at SourceForge</a>
 * <p>
 * THIS SOFTWARE AND KNOWLEDGE BASE CONTENT ARE PROVIDED ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OPENCYC
 * ORGANIZATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE AND KNOWLEDGE
 * BASE CONTENT, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class RemoteCycConnection implements CycConnectionInterface {

    /**
     * name of the local agent
     */
    protected String myAgentName;

    /**
     * name of the cyc proxy agent
     */
    protected String cycProxyAgentName;

    /**
     * Indicates the CoABS agent community.
     */
    public static final int COABS_AGENT_COMMUNTITY = 1;

    /**
     * Indicates the FIPA-OS agent community.
     */
    public static final int FIPA_OS_AGENT_COMMUNTITY = 2;

    /**
     * agent community to which the cyc proxy agent belongs
     */
    protected int agentCommunity;

    /**
     * No api trace.
     */
    public static final int API_TRACE_NONE = 0;

    /**
     * Message-level api trace.
     */
    public static final int API_TRACE_MESSAGES = 1;

    /**
     * Detailed api trace.
     */
    public static final int API_TRACE_DETAILED = 2;

    /**
     * Parameter that, when true, causes a trace of the messages to and from the server.
     */
    protected int trace = API_TRACE_NONE;

    /**
     * the interface for interacting with an agent community such as CoABS or FIPA-OS
     */
    AgentCommunityAdapter agentCommunityAdapter;

    /**
     * Constructs a new RemoteCycConnection object to the given CycProxyAgent in the given
     * agent community.
     *
     * @param myAgentName the name of the local agent
     * @param cycProxyAgentName the name of the cyc proxy agent
     * @param agentCommunity the agent community to which the cyc proxy agent belongs
     */
    public RemoteCycConnection(String myAgentName,
                               String cycProxyAgentName,
                               int agentCommunity) throws IOException {
        this.myAgentName = myAgentName;
        this.cycProxyAgentName = cycProxyAgentName;
        this.agentCommunity = agentCommunity;
        if (agentCommunity == COABS_AGENT_COMMUNTITY)
            agentCommunityAdapter = new CoAbsCommunityAdapter(myAgentName);
        else if (agentCommunity == FIPA_OS_AGENT_COMMUNTITY)
            agentCommunityAdapter = new FipaOsCommunityAdapter(myAgentName);
        else
            throw new IOException("Invalid agent community " + agentCommunity);
    }

    /**
     * Send a message to Cyc and return the <tt>Boolean</tt> true as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element.  If an error occurs the first element is <tt>Boolean</tt>
     * false and the second element is the error message string.
     *
     * @param message the api command
     * @return an array of two objects, the first is an Integer response code, and the second is the
     * response object or error string.
     */
    public Object[] converse (Object message) throws IOException, CycApiException {
        if (trace > API_TRACE_NONE) {
            if (message instanceof String)
                System.out.println(message + " --> cyc");
            else if (message instanceof CycList)
                System.out.println(((CycList) message).cyclify());
            else
                throw new CycApiException("Invalid message class " + message);
            System.out.print("cyc --> ");
        }
        Object [] response = {null, null};

        ACL acl = new ACL();
        acl.setPerformative(FIPACONSTANTS.REQUEST);
        AgentID senderAid = new AgentID();
        senderAid.setName(myAgentName);
        acl.setSenderAID(senderAid);
        AgentID receiverAid = new AgentID();
        receiverAid.setName(cycProxyAgentName);
        acl.addReceiverAID(receiverAid);
        CycList apiRequest = null;
        String apiRequestXml;
        try {
            if (message instanceof String)
                apiRequest = CycAccess.sharedCycAccessInstance.makeCycList((String) message);
            else
                apiRequest = (CycList) message;
            apiRequestXml = apiRequest.toXMLString();
        }
        catch (Exception e) {
            Log.current.errorPrintln(e.getMessage());
            Log.current.printStackTrace(e);
            return response;
        }
        acl.setContentObject(apiRequestXml, ACL.BYTELENGTH_ENCODING);
        acl.setLanguage(FIPACONSTANTS.XML);
        acl.setOntology("cyc-api");
        acl.setReplyWith(agentCommunityAdapter.nextMessageId());

        try {
            ACL replyAcl = agentCommunityAdapter.converseMessage(acl, this.agentCommunityAdapter.WAIT_FOREVER);
        }
        catch (TimeLimitExceededException e) {
            Log.current.errorPrintln(e.getMessage());
            Log.current.printStackTrace(e);
            return response;
        }

        if (trace > API_TRACE_NONE)
            if (response[1] instanceof CycList)
                System.out.println(response[0] + " " + ((CycList) response[1]).cyclify());
            else
                System.out.println(response[0] + " " + response[1]);

        return response;
    }

    /**
     * Send a message to Cyc and return the response code as the first
     * element of an object array, and the cyc response Symbolic Expression as
     * the second element, spending no less time than the specified timer allows
     * but throwing a <code>TimeOutException</code> at the first opportunity
     * where that time limit is exceeded.
     * If an error occurs the second element is the error message string.
     *
     * @param message the api command which must be a String or a CycList
     * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
     * @return an array of two objects, the first is an Integer response code, and the second is the
     * response object or error string.
     */
    public Object[] converse (Object message, Timer timeout)
        throws IOException, TimeOutException, CycApiException {
        Object [] response = {null, null};

        return response;
    }

    /**
     * Close the api sockets and streams.
     */
    public void close () {
    }

    /**
     * Returns the trace value.
     *
     * @return the trace value
     */
    public int getTrace() {
        return trace;
    }

    /**
     * Sets the trace value.
     * @param trace the trace value
     */
    public void setTrace(int trace) {
        this.trace = trace;
    }

    /**
     * Turns on the diagnostic trace of socket messages.
     */
    public void traceOn() {
        trace = API_TRACE_MESSAGES;
    }

    /**
     * Turns on the detailed diagnostic trace of socket messages.
     */
    public void traceOnDetailed() {
        trace = API_TRACE_DETAILED;
    }

    /**
     * Turns off the diagnostic trace of socket messages.
     */
    public void traceOff() {
        trace = API_TRACE_NONE;
    }

    /**
     * Returns connection information, suitable for diagnostics.
     *
     * @return connection information, suitable for diagnostics
     */
    public String connectionInfo () {
        return "cyc proxy agent " + cycProxyAgentName +
               ", agent community " + agentCommunityName();
    }

    /**
     * Returns the agent community name.
     *
     * @return the agent community name
     */
    public String agentCommunityName () {
        if (agentCommunity == this.COABS_AGENT_COMMUNTITY)
            return "CoABS";
        else if (agentCommunity == this.FIPA_OS_AGENT_COMMUNTITY)
            return "FIPA-OS";
        else
            throw new RuntimeException("Invalid agent community");
    }

}