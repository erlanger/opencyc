package org.opencyc.elf.message;

//// Internal Imports
import org.opencyc.elf.bg.planner.Schedule;

import org.opencyc.elf.bg.taskframe.TaskCommand;

//// External Imports

/** Provides the container for the schedule job message, that is sent from the job assigner to
 * the scheduler.  If the schedule is not included, then the scheduler must determine its
 * own schedule using the task command.
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
public class ScheduleJobMsg extends GenericMsg {
  
  //// Constructors
  
  /** Creates a new instance of ScheduleJobMsg. */
  public ScheduleJobMsg() {
  }
  
  //// Public Area
  
  /** Returns a string representation of this object.
   * 
   * @return a string representation of this object
   */
  public String toString() {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("[ScheduleJobMsg ");
    if (taskCommand != null)
      stringBuffer.append(taskCommand.toString());
    if (schedule != null)
      stringBuffer.append(schedule.toString());
    stringBuffer.append("]");
    return stringBuffer.toString();
  }

  /** Gets the commanded task for scheduling in case the schedule is not explicitly given
   *
   * @return the commanded task for scheduling in case the schedule is not explicitly given
   */
  public TaskCommand getTaskCommand () {
    return taskCommand;
  }

  /** Sets the commanded task for scheduling in case the schedule is not explicitly given
   *
   * @param taskCommand the commanded task for scheduling in case the schedule is not explicitly given
   */
  public void setTaskCommand (TaskCommand taskCommand) {
    this.taskCommand = taskCommand;
  }

  /** Gets the schedule
   *
   * @return the schedule
   */
  public Schedule getSchedule () {
    return schedule;
  }

  /** Sets the schedule
   *
   * @param schedule the schedule
   */
  public void setSchedule (Schedule schedule) {
    this.schedule = schedule;
  }

  //// Protected Area
  
  //// Private Area
  
  //// Internal Rep
  
  /** the schedule */
  protected Schedule schedule;
  
  /** the commanded task for scheduling in case the schedule is not explicitly given */
  protected TaskCommand taskCommand;
  
  //// Main
  
  
}
