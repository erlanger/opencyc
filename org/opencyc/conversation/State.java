package org.opencyc.conversation;

import java.util.*;

/**
 * Contains the attributes and behavior of a chat conversation Finite
 * State Machine State.<p>
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
public class State implements Comparable {

    /**
     * state identifier within the conversation
     */
    protected String stateId;

    /**
     * performative --> Arc
     */
    protected HashMap arcs = new HashMap();

    /**
     * Dictionary of attribute and object values.
     */
    protected HashMap attributes = new HashMap();

    /**
     * Constructs a new State object.
     */
    public State(String stateId) {
        this.stateId = stateId;
    }

    /**
     * Returns the state id.
     *
     * @return the state id
     */
    public String getStateId () {
        return stateId;
    }

    /**
     * Returns the collection of the arcs from this state.
     *
     * @return the collection of the arcs from this state
     */
    public Collection getArcs () {
        return arcs.values();
    }

    /**
     * Records the arc to take when the its performative is observed.
     *
     * @param arc the FSM arc which specifies a transition-to state and an action
     */
    public void addArc (Arc arc) {
         addArc(arc.getPerformative(), arc);
    }

    /**
     * Records the arc to take when the given performative is observed.
     *
     * @param performative the given performative
     * @param arc the FSM arc which specifies a transition-to state and an action
     */
    public void addArc (Performative performative,
                                 Arc arc) {
         arcs.put(performative, arc);
    }

    /**
     * Returns the arc to take for the given performative.
     *
     * @param performative the given performative
     * @return the FSM arc which specifies a transition-to state and an action
     */
    public Arc getArc (Performative performative) {
        return (Arc) arcs.get(performative);
    }

    /**
     * Sets the value for the given attribute.
     *
     * @param attribute the key object
     * @param value the value object
     */
    public void set (String attribute, Object value) {
        attributes.put(attribute, value);
    }

    /**
     * Returns the value for the given attribute.
     *
     * @param attribute the key object
     * @retrun the value for the given attribute
     */
    public Object get (Object attribute) {
        return attributes.get(attribute);
    }
    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object.
     *
     * @param object the reference object with which to compare.
     * @return a negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object
     */
     public int compareTo (Object object) {
        if (! (object instanceof State))
            throw new ClassCastException("Must be a State object");
        return this.stateId.compareTo(((State) object).stateId);
     }

    /**
     * Returns <tt>true</tt> iff some object equals this object
     *
     * @param object the <tt>Object</tt> for equality comparison
     * @return equals <tt>boolean</tt> value indicating equality or non-equality.
     */
    public boolean equals(Object object) {
        if (! (object instanceof State))
            return false;
        return ((State) object).stateId.equals(stateId) &&
               ((State) object).arcs.equals(arcs);
    }

    /**
     * Returns the string representation of the <tt>State</tt>
     *
     * @return the representation of the <tt>State</tt> as a <tt>String</tt>
     */
    public String toString() {
        return stateId;
    }

}