package org.opencyc.uml.interpreter;

import java.io.*;
import java.util.*;
import org.opencyc.api.*;
import org.opencyc.cycobject.*;
import org.opencyc.util.*;

/**
 * Provides a pool of context frames for use by state machine interpreter
 * threads.  The pool is used to reduce the creation and destruction of
 * temporary Cyc context microtheories.
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

public class ContextStackPool {

    /**
     * The quiet verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    public static final int QUIET_VERBOSITY = 0;

    /**
     * The default verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    public static final int DEFAULT_VERBOSITY = 3;

    /**
     * Sets verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    protected int verbosity = DEFAULT_VERBOSITY;

    /**
     * the global temporary interpreter workspace
     */
    protected CycFort temporaryWorkspaceMt;

    /**
     * the cyc access instance
     */
    protected CycAccess cycAccess;

    /**
     * the list of microtheories which form the evaluation context
     * stack for the main evaluation thread
     */
    protected ArrayList contextFrames = new ArrayList();

    /**
     * the default number of frames in the state machine evaluation context stack
     */
    public static final int DEFAULT_CONTEXT_FRAMES_COUNT = 10;

    /**
     * the number of frames in the state machine evaluation context stack
     */
    protected int contextFramesCount = DEFAULT_CONTEXT_FRAMES_COUNT;

    /**
     * stack of available context frames
     */
    protected Stack freeContextFrames = new Stack();

    /**
     * #$umlStateMachineInterpetationContextStackParent
     */
    protected CycConstant umlStateMachineInterpetationContextStackParent;

    /**
     * Constructs a new ContextStackPool object.
     */
    public ContextStackPool() {
    }

    /**
     * Constructs a new ContextStackPool object given the
     * cyc server connection, temporary workspace and verbosity.
     *
     * @param cycAccess the Cyc server connection
     * @param temporaryWorkspaceMt the temporary workspace microtheory in which
     * the frame context linking assertions are stored
     * @param verbosity the verbosity of this object
     */
    public ContextStackPool(CycAccess cycAccess,
                            CycFort temporaryWorkspaceMt,
                            int verbosity)
        throws IOException, CycApiException, ExpressionEvaluationException {
        this.cycAccess = cycAccess;
        this.temporaryWorkspaceMt = temporaryWorkspaceMt;
        this.verbosity = verbosity;
        initialize();
    }

    /**
     * Initializes the context stack pool object.
     */
    public void initialize ()
        throws IOException, CycApiException, ExpressionEvaluationException {
        umlStateMachineInterpetationContextStackParent =
            cycAccess.getKnownConstantByName("umlStateMachineInterpetationContextStackParent");
    }

    /**
     * Creates the context evaluation stack whose elements contain state variables
     * for either a state machine or procedure.
     */
    public void createContextStack ()
        throws IOException, CycApiException, ExpressionEvaluationException {
        String startName = "UMLStateMachineContextFrame";
        for (int i = 0; i < this.contextFramesCount; i++) {
            CycConstant contextFrame = cycAccess.makeUniqueCycConstant(startName);
            if (verbosity > 2)
                Log.current.println(contextFrame.cyclify() + " created");
            contextFrames.add(contextFrame);
            freeContextFrames.push(contextFrame);
            CycList sentence = new CycList();
            sentence.add(cycAccess.isa);
            sentence.add(contextFrame);
            sentence.add(cycAccess.getKnownConstantByName("UMLStateMachineContextFrame"));
            cycAccess.assertWithBookkeepingAndWithoutTranscript(sentence, cycAccess.baseKB);
        }

    }

    /**
     * Destroys the context evaluation stack whose elements contain state variables
     * for either a state machine or procedure.
     */
    public void destroyContextStack ()
        throws IOException, CycApiException, ExpressionEvaluationException {
        Iterator iter = contextFrames.iterator();
        while (iter.hasNext()) {
            CycConstant contextFrame = (CycConstant) iter.next();
            cycAccess.killWithoutTranscript(contextFrame);
            if (verbosity > 2)
                Log.current.println(contextFrame.cyclify() + " killed");
        }
        contextFrames = null;
        freeContextFrames = null;
    }

    /**
     * Allocates a context frame from the free list, given its parent context
     * frame
     *
     * @param parentContextFrame the parent context frame in the calling
     * state machine interpretation thread
     * @param definitionMt the state machine definition mt
     * @return the allocated context frame
     */
    public CycConstant allocateContextFrame (CycConstant parentContextFrame,
                                             CycConstant definitionMt)
        throws IOException, CycApiException, ExpressionEvaluationException {
        if (freeContextFrames.empty())
            throw new RuntimeException("Context frames exhausted");
        CycConstant contextFrame = allocateContextFrame();
        CycList sentence = new CycList();
        sentence.add(cycAccess.getKnownConstantByName("umlStateMachineInterpetationContextStackParent"));
        sentence.add(contextFrame);
        sentence.add(parentContextFrame);
        cycAccess.assertWithBookkeepingAndWithoutTranscript(sentence, temporaryWorkspaceMt);
        sentence = new CycList();
        sentence.add(cycAccess.genlMt);
        sentence.add(contextFrame);
        sentence.add(definitionMt);
        cycAccess.assertWithBookkeepingAndWithoutTranscript(sentence, cycAccess.baseKB);
        if (verbosity > 2)
            Log.current.println(contextFrame.cyclify() +
                                "\n allocated with parent " + parentContextFrame +
                                "\n with definitionMt " + definitionMt);
        return contextFrame;
    }

    /**
     * Allocates a parentless context frame from the free list.
     *
     * @return the allocated context frame
     */
    public synchronized CycConstant allocateContextFrame () {
        if (freeContextFrames.empty())
            throw new RuntimeException("Context frames exhausted");
        return (CycConstant) freeContextFrames.pop();
    }

    /**
     * Deallocates a context frame by unasserting all of its contained assertions and
     * then returning it to the free list.
     *
     * @param contextFrame the deallocated context frame
     */
    public synchronized void deallocateContextFrame (CycConstant contextFrame)
        throws IOException, CycApiException, ExpressionEvaluationException {
        cycAccess.unassertMtContentsWithoutTranscript(contextFrame);
        freeContextFrames.push(contextFrame);
        cycAccess.unassertMatchingAssertionsWithoutTranscript(umlStateMachineInterpetationContextStackParent,
                                                              contextFrame,
                                                              temporaryWorkspaceMt);
        if (verbosity > 2)
            Log.current.println(contextFrame.cyclify() + " deallocated ");
    }

    /**
     * Returns the parent context of the given allocated context or null if none.
     *
     * @param contextFrame the given allocated context
     * @return the parent context of the given allocated context or null if none
     */
    public CycConstant getParentContextFrame (CycConstant contextFrame)
        throws IOException, CycApiException, ExpressionEvaluationException {
        return (CycConstant) cycAccess.getArg2(umlStateMachineInterpetationContextStackParent,
                                               contextFrame,
                                               temporaryWorkspaceMt);
    }


    /**
     * Gets the  verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.  0 --> quiet ... 9 -> maximum diagnostic input.
     *
     * @return  the  verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input
     */
    public int getVerbosity () {
        return verbosity;
    }

    /**
     * Sets verbosity of this object's output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     *
     * @param verbosity 0 --> quiet ... 9 -> maximum diagnostic input
     */
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }


    /**
     * Gets the number of frames in the state machine evaluation context stack
     *
     * @return the number of frames in the state machine evaluation context stack
     */
    public int getContextFramesCount () {
        return contextFramesCount;
    }

    /**
     * Sets the number of frames in the state machine evaluation context stack
     *
     * @param contextFramesCount
     */
    public void setContextFramesCount (int contextFramesCount) {
        this.contextFramesCount = contextFramesCount;
    }

    /**
     * Gets the global temporary interpreter workspace
     *
     * @return the global temporary interpreter workspace
     */
    public CycFort getTemporaryWorkspaceMt () {
        return temporaryWorkspaceMt;
    }

    /**
     * Sets the global temporary interpreter workspace
     *
     * @param temporaryWorkspaceMt
     */
    public void setTemporaryWorkspaceMt (CycFort temporaryWorkspaceMt) {
        this.temporaryWorkspaceMt = temporaryWorkspaceMt;
    }

    /**
     * Gets the cyc access instance
     *
     * @return the cyc access instance
     */
    public CycAccess getCycAccess () {
        return cycAccess;
    }

    /**
     * Sets the cyc access instance
     *
     * @param cycAccess the cyc access instance
     */
    public void setCycAccess (CycAccess cycAccess) {
        this.cycAccess = cycAccess;
    }

}