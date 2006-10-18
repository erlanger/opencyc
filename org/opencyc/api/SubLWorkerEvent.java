/* $Id$
 */

package org.opencyc.api;

// INTERNAL IMPORTS
import org.opencyc.cycobject.CycList;
import org.opencyc.cycobject.CycFort;
import org.opencyc.api.CycApiException;
import org.opencyc.util.StringUtils;

// EXTERNAL IMPORTS
import java.io.IOException;
import java.util.*;

/**
 * <P>SubLWorkerEvent is designed to represent the state of an
 * event that can be generated by SubLWorkers and passed to the
 * SubLWorkerListener interface.
 *
 * <p>Copyright 2004 Cycorp, Inc., license is open source GNU LGPL.
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
 *
 * @author tbrussea
 * @date March 25, 2004, 2:01 PM
 * @version $Id$
 */
public class SubLWorkerEvent extends EventObject {

  //// Constructors
  

  /** Creates a new instance of SubLWorkerEvent, this constructor is
   * appropriate only for creating starting events.
   * @param source the SubLWorker that generates the event
   * @param workerId the client-unique id associated with this SubLWorker
   */
  public SubLWorkerEvent(SubLWorker source, Integer workerId) {
    super(source);
    this.workerId = workerId;
    this.eventType = STARTING_EVENT_TYPE;
  }

  /** Creates a new instance of SubLWorkerEvent, this constructor is
   * appropriate only for creating data available events.
   * @param source the SubLWorker that generates the event
   * @param newData the work (or subset if incrementally collecting results),
   * that has been produced by the SubLWorker.
   * @param percentComplete percentage complete, or negative if the 
   * SubL code does not maintain this information.
   */
  public SubLWorkerEvent(SubLWorker source, Object newData, float percentComplete) {
    super(source);
    this.newData = newData;
    this.percentComplete = percentComplete;
    this.eventType = DATA_AVAILABLE_EVENT_TYPE;
  }
  
  /** Creates a new instance of SubLWorkerEvent, this constructor is 
   * appropriate only for creating termination events.
   * @param source the SubLWorker that generates the event
   * @param status why this SubLWorker terminated
   * @param e exception thrown while doing work (or null if none thrown)
   */
  public SubLWorkerEvent(SubLWorker source, 
      SubLWorkerStatus status, Exception e) {
    super(source);
    this.e = e;
    this.status = status;
    this.eventType = TERMINATION_EVENT_TYPE;
  }
  
  //// Public Area
  
  /** Returns the worker that generated this event.
   * @return the worker that generated this event
   */  
  public SubLWorker getWorker() { 
    return (SubLWorker)getSource();
  }
  
  /** Returns the type of event.
   * @return the type of event
   */  
  public SubLWorkerEventType getEventType() {
    return eventType;
  }

  /** Returns any exceptions thrown while processing the work.
   * This value is only set for TERMINATION_EVENT_TYPE events.
   * @return any exceptions thrown while processing the work
   */  
  public Exception getException() {
    return e;
  }

  /** Returns the worker status appropriate for this event.
   * This value is only set for TERMINATION_EVENT_TYPE events.
   * @return the worker status appropriate for this event
   */  
  public SubLWorkerStatus getStatus() {
    return status;
  }

  /** Returns the percentage complete, or negative if the 
   * SubL code does not maintain this information.
   * This value is only set for DATA_AVAILABLE_EVENT_TYPE events.
   * @return percentage complete, or negative if the 
   * SubL code does not maintain this information
   */  
  public float getPercentComplete() {
    return percentComplete;
  }

  /** Returns the work (or subset if incrementally collecting results),
   * that has been produced by the SubLWorker.
   * This value is only set for DATA_AVAILABLE_EVENT_TYPE events.
   * @return the work (or subset if incrementally collecting results),
   * that has been produced by the SubLWorker.
   */  
  public Object getWork() {
    return newData;
  }

  /** Returns the client-unique id for the communication with the Cyc server.
   * This value is only set for STARTING_EVENT_TYPE events.
   * @return the client-unique id for the communication with the Cyc server
   */  
  public Integer getId() {
    return workerId;
  }

  /**
   * Returns a string representation of the SubLWorker.
   * @return a string representation of the SubLWorker
   */  
  public String toString() {
    return toString(2);
  }
  
  /**
   * Returns a string representation of the SubLWorker.
   * @param indentLength the number of spaces to preceed each line of 
   * output String
   * @return a string representation of the SubLWorker
   */
  public String toString(int indentLength) {
    StringBuffer nlBuff = new StringBuffer();
    nlBuff.append(System.getProperty("line.separator"));
    for (int i = 1; i < indentLength; i++) { nlBuff.append(" "); }
    String nl = nlBuff.toString();
    String sp = " ";
    StringBuffer buf = new StringBuffer(sp + this.getClass().getName());
    buf.append(":").
      append(nl).append("Event type: ").append(getEventType().getName()).
      append(nl).append("SubLWorker: ").append(getWorker().toString(indentLength + 2));
    if (getEventType() == STARTING_EVENT_TYPE) {
      buf.append(nl).append("Worker id: ").append(getId());
    } else if (getEventType() == DATA_AVAILABLE_EVENT_TYPE) {
      buf.append(nl).append("Percent complete: ").append(getPercentComplete()).
        append(nl).append("Latest results: ").append(getWork());
    } else {
      buf.append(nl).append("Status: ").append(getStatus()).
        append(nl).append("Exception: ").append(StringUtils.
        getStringForException(getException()));
    }
    return buf.toString();
  }

  //// Protected Area
  
  //// Private Area
  
  //// Internal Rep
  
  private SubLWorkerStatus status;
  private Exception e = null;
  private Object newData = null;
  private Integer workerId;
  private SubLWorkerEventType eventType;
  private float percentComplete;

  public static class SubLWorkerEventType {
    
    private String name;
    
    /** Returns the name of the event type
     * @return the name of the event type
     */    
    public String getName() { return name; }
    
    /** Returns the name of the event type
     * @return the name of the event type
     */    
    public String toString() { return getName(); }
    
    /** Constructor.
     * @param name the name of the event type
     */    
    private SubLWorkerEventType(String name) { this.name = name; }
  }

  /** Indicates that this is a start event. */
  public static final SubLWorkerEventType STARTING_EVENT_TYPE =
    new SubLWorkerEventType("Starting");

  /** Indicates that this is a data available event. */
  public static final SubLWorkerEventType DATA_AVAILABLE_EVENT_TYPE =
    new SubLWorkerEventType("Data available");

  /** Indicates that this is a termination event. */
  public static final SubLWorkerEventType TERMINATION_EVENT_TYPE =
    new SubLWorkerEventType("Terminated");

}
