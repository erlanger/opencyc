package org.opencyc.elf.bg.executor;

//// Internal Imports
import org.opencyc.elf.BufferedNodeComponent;
import org.opencyc.elf.Node;
import org.opencyc.elf.NodeComponent;

import org.opencyc.elf.bg.BehaviorGeneration;

import org.opencyc.elf.bg.planner.Schedule;
import org.opencyc.elf.bg.planner.Scheduler;

import org.opencyc.elf.bg.taskframe.Action;
import org.opencyc.elf.bg.taskframe.TaskCommand;

import org.opencyc.elf.message.ExecuteScheduleMsg;
import org.opencyc.elf.message.ExecutorStatusMsg;
import org.opencyc.elf.message.GenericMsg;

//// External Imports
import java.util.ArrayList;
import java.util.List;

import EDU.oswego.cs.dl.util.concurrent.Puttable;
import EDU.oswego.cs.dl.util.concurrent.Takable;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

/** Provides the Executor for ELF BehaviorGeneration.
 * 
 * @version $Id$
 * @author Stephen L. Reed  
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
public class Executor extends BufferedNodeComponent {
  
  //// Constructors
  
  /** Creates a new instance of Executor with the given
   * input and output channels.
   *
   * @param node the containing ELF node
   * @param executorChannel the takable channel from which messages are input from the
   * associated scheduler
   */
  public Executor (Node node,
                   Takable executorChannel) {
    setNode(node);
    this.executorChannel = executorChannel;         
  }

  //// Public Area

  /** Initializes this executor and begins consuming schedules.
   *
   * @param schedulerChannel the puttable channel to which messages are output to the
   * associated scheduler
   */
  public void initialize(Puttable schedulerChannel) {
    getLogger().info("Initializing Executor");
    consumer = new Consumer(executorChannel,
                            schedulerChannel,
                            this);
    consumerExecutor = new ThreadedExecutor();
    try {
      consumerExecutor.execute(consumer);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
 
  /** Returns a string representation of this object.
   * 
   * @return a string representation of this object
   */
  public String toString() {
    return "Executor for " + node.getName();
  }
  
  /** Gets the schedule to execute
   * 
   * @return the schedule to execute
   */
  public Schedule getScheduleToExecute() {
    return scheduleToExecute;
  }

  /** Sets the schedule to execute
   * 
   * @param scheduleToExecute the schedule to execute
   */
  public void setScheduleToExecute(Schedule scheduleToExecute) {
    this.scheduleToExecute = scheduleToExecute;
  }

  /** Gets the behavior generation instance
   * 
   * @return the behavior generation instance
   */
  public BehaviorGeneration getBehaviorGeneration() {
    return behaviorGeneration;
  }

  /** Sets the behavior generation instance
   * 
   * @param behaviorGeneration the behavior generation instance
   */
  public void setBehaviorGeneration(BehaviorGeneration behaviorGeneration) {
    this.behaviorGeneration = behaviorGeneration;
  }

  /** Gets the scheduler whose plans this executor executes
   * 
   * @return the scheduler whose plans this executor executes
   */
  public Scheduler getScheduler() {
    return scheduler;
  }

  /** Sets the scheduler whose plans this executor executes
   * 
   * @param scheduler the scheduler whose plans this executor executes
   */
  public void setSchedulerr(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  //// Protected Area
  
  /** Thread which processes the input message channel. */
  protected class Consumer implements Runnable {
    
    /** the takable channel from which messages are input */
    protected final Takable executorChannel;
    
    /** the puttable channel to which messages are output to the scheduler */
    protected final Puttable schedulerChannel;
    
    /** the reference to this node component as a message sender */
    protected NodeComponent sender;
          
    /** the node's controlled resources */
    protected List controlledResources;
    
    /** Creates a new instance of Consumer.
     *
     * @param executorChannel the takable channel from which messages are input
     * @param schedulerChannel the puttable channel to which messages are output to the
     * scheduler
     * @param sender the reference to this node component as a message sender
     */
    protected Consumer (Takable executorChannel,
                        Puttable schedulerChannel,
                        NodeComponent sender) { 
      getLogger().info("Creating Executor.Consumer");
      this.executorChannel = executorChannel;
      this.schedulerChannel = schedulerChannel;
      this.sender = sender;
    }

    /** Reads messages from the input queue and processes them. */
    public void run () {
      try {
        while (true) { 
          dispatchMsg((GenericMsg) executorChannel.take()); 
        }
      }
      catch (InterruptedException ex) {}
    }
     
    /** Dispatches the given input channel message by type.
     *
     * @param genericMsg the given input channel message
     */
    void dispatchMsg (GenericMsg genericMsg) {
      if (genericMsg instanceof ExecuteScheduleMsg)
        processExecutorScheduleMsg((ExecuteScheduleMsg) genericMsg);
      else
        throw new RuntimeException("Unhandled message " + genericMsg);
    }
    
    /** Processes the execute schedule message. 
     * 
     * @param executeSceduleMsg the execute schedule message
     */
    protected void processExecutorScheduleMsg(ExecuteScheduleMsg executeSceduleMsg) {
    }
  }
  
  /** Interruptable thread which executes the input schedule. */
  protected class ScheduleExecutor implements Runnable {
    
    /** Constructs a new ScheduleExecutor object */
    ScheduleExecutor() {
    }
    
    /** Executes the input schedule. */
    public void run() {
    }
    
  }
  
  /** Receives the update schedule message from ? */
  protected void receiveUpdateSchedule () {
    // TODO
    // receive via channel from ?
    // TaskCommnd taskCommand
    // Schedule schedule
  }

  /** Receives the execute schedule message from plan selector. 
   * (scheduler should be the intermediary)
   */
  protected void receiveExecuteSchedule () {
    // TODO
    // receive via channel from ?
    // TaskCommnd taskCommand
    // Schedule schedule
  }
  
  /** Sends the do subtask message to behavior generation, for subsequent forwarding to
   * the next highest level node
   */
  protected void doSubTask () {
    // TODO
    // send via channel to ?
    // ArrayList controlledResources
    // TaskCommnd taskCommand
  }
  
  /** Sends the executor status to its scheduler. */
  protected void sendExecutorStatus () {
    // TODO
    // send via channel to ?
    // ArrayList controlledResources
    // TaskCommnd taskCommand
    // Schedule schedule
    // Status status
    // send receiveExecutorStatus(taskCommand, schedule, status) to (its) scheduler
  }
    
  public Puttable getChannel() {
    return (Puttable) executorChannel;
  }
  
  //// Private Area
  
  //// Internal Rep
  
  /** the takable channel from which messages are input */
  protected Takable executorChannel;

  /** the thread which processes the input channel of messages */
  protected Consumer consumer;

  /** the consumer thread executor */
  protected EDU.oswego.cs.dl.util.concurrent.Executor consumerExecutor;
  
  /** the executor for this scheduler */
  protected org.opencyc.elf.bg.executor.Executor executor;
  
  /** the schedule to execute */
  protected Schedule scheduleToExecute;

  /** the behavior generation instance which owns this executor */
  protected BehaviorGeneration behaviorGeneration;

  /** the scheduler whose plans this executor executes */
  protected Scheduler scheduler;
}