package org.opencyc.constraintsolver;

import java.util.*;
import org.opencyc.cycobject.*;
import org.opencyc.api.*;

/**
 * Provides attribute and behavior for a horn clause assertion, used by the constraint solver
 * during backchaining inference.  Horn clauses are the result of OpenCyc's canonicalization of
 * implication assertions and consist of two parts, both of which are logical formulae.  The first
 * part is the antecedant and the second part is the consequent.  Backchaining inference employs
 * horn clauses when seeking to prove a logical formula. If the formula can be unified with the
 * consequent of a horn clause, then the search for a proof can be transformed into a search for
 * a proof of the logical formulae which constitute the horn clause antecedant.  This logical
 * inference step in a proof is named modus ponens.<p>
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
public class HornClause {

    /**
     * The antecedant part of the horn clause, as a list of conjuncts which are logical
     * expressions which all must be true for the consequent part of the horn clause to be
     * true.  Each antecedant element is represented as a <tt>Rule</tt> object.
     */
    ArrayList antecedantConjuncts;

    /**
     * The consequent part of the horn clause, as a single logical expression represented as a
     * <tt>Rule</tt> object.
     */
    Rule consequent;

    /**
     * The set of variables used in either the antecedantConjuncts or the consequent.
     */
    ArrayList variables;

    /**
     * Constructs a new (empty) <tt>HornClause</tt> object.
     */
    public HornClause() {
    }

    /**
     * Constructs a new <tt>HornClause</tt> object from the given antecedant conjuncts
     * and consequent.
     */
    public HornClause(ArrayList antecedantConjuncts, Rule consequent) {
        this.antecedantConjuncts = antecedantConjuncts;
        this.consequent = consequent;
        gatherVariables();
    }

    /**
     * Constructs a new <tt>HornClause</tt> object from the given cyclified string representation.
     *
     * @param hornClauseString the cyclified string representation of the horn clause
     */
    public HornClause(String hornClauseString) {
        CycList hornClause = CycAccess.current().makeCycList(hornClauseString);
        if (hornClause.size() != 3)
            throw new RuntimeException("HornClause list is not length 3 " + hornClauseString);
        CycConstant implies = (CycConstant) hornClause.first();
        if (! (implies.cyclify().equals("#$implies")))
            throw new RuntimeException("HornClause string not an implication " + hornClauseString);
        CycList antecedantCycList = (CycList) hornClause.second();
        CycList consequentCycList = (CycList) hornClause.third();
        antecedantConjuncts = new ArrayList();
        if (antecedantCycList.first().toString().equals("and")) {
            antecedantCycList = antecedantCycList.rest();
            for (int i = 0; i < antecedantCycList.size(); i++)
                antecedantConjuncts.add(new Rule((CycList) antecedantCycList.get(i)));
        }
        else {
            antecedantConjuncts.add(new Rule(antecedantCycList));
        }
        consequent = new Rule(consequentCycList);
        gatherVariables();
    }

    /**
     * Returns the antecedant conjuncts for this horn clause.
     *
     * @return the antecedant conjuncts for this horn clause
     */
    public ArrayList getAntecedantConjuncts() {
        return antecedantConjuncts;
    }

    /**
     * Returns the consequent for this horn clause.
     *
     * @return the consequent for this horn clause
     */
    public Rule getConsequent() {
        return consequent;
    }

    /**
     * Returns the variables for this horn clause.
     *
     * @return the variables for this horn clause
     */
    public ArrayList getVariables() {
        return variables;
    }

    /**
     * Gathers the variables used in the antecedant conjuncts and in the consequent of the
     * horn clause.
     */
    protected void gatherVariables() {
        variables = new ArrayList();
        variables.addAll(consequent.variables);
        for (int i = 0; i < antecedantConjuncts.size(); i++) {
            Rule antecedantConjunct = (Rule) antecedantConjuncts.get(i);
            for (int j = 0; j < antecedantConjunct.variables.size(); j++) {
                CycVariable variable = (CycVariable) antecedantConjunct.variables.get(j);
                if (! (variables.contains(variable)))
                    variables.add(variable);
            }
        }
    }

    /**
     * Renames variables that occur in the given list of other variables.
     *
     * @param otherVariables the other variables with whom this horn clause will have no
     * variables in common
     */
    public void renameVariables(ArrayList otherVariables, int verbosity) {
        if (verbosity > 3)
            System.out.println("ensuring that variables for \n" + this.cyclify() +
                               "are different from " + otherVariables);
        for (int i = 0; i < otherVariables.size(); i++) {
            CycVariable otherVariable = (CycVariable) otherVariables.get(i);
            if (variables.contains(otherVariable)) {
                CycVariable uniqueVariable = CycVariable.makeUniqueCycVariable(otherVariable);
                this.substituteVariable(otherVariable, uniqueVariable);
                if (verbosity > 3)
                    System.out.println("renamed " + otherVariable.cyclify() +
                                       " to " + uniqueVariable.cyclify());

            }
        }
    }


    /**
     * Substitutes an object for a variable.
     *
     * @param oldVariable the variable to replaced
     * @parma newObject the <tt>Object</tt> to be substituted for the variable
     */
    public void substituteVariable(CycVariable variable, Object newObject) {
        if (! (variables.contains(variable))) {
            throw new RuntimeException(variable + " is not a variable of \n" + this.cyclify());
        }
        variables.remove(variable);
        if (newObject instanceof CycVariable)
            variables.add(newObject);
        if (consequent.getVariables().contains(variable))
            consequent.substituteVariable(variable, newObject);
        for (int i = 0; i < antecedantConjuncts.size(); i++) {
            Rule antecedantConjunct = (Rule) antecedantConjuncts.get(i);
            if (antecedantConjunct.getVariables().contains(variable))
                antecedantConjunct.substituteVariable(variable, newObject);
        }
    }

    /**
     * Returns <tt>true</tt> if the object equals this object.
     *
     * @param object the object for comparison
     * @return <tt>boolean</tt> indicating equality of an object with this object.
     */
    public boolean equals(Object object) {
        if (! (object instanceof HornClause))
            return false;
        HornClause thatHornClause = (HornClause) object;
        return (this.consequent.equals(thatHornClause.getConsequent()) &&
                this.antecedantConjuncts.equals(thatHornClause.getAntecedantConjuncts()));
    }

    /**
     * Creates and returns a copy of this <tt>Rule</tt> suitable for mutation.
     *
     * @return a clone of this instance
     */
    public Object clone() {
        HornClause cloneHornClause = new HornClause();
        cloneHornClause.variables = (ArrayList) this.variables.clone();
        cloneHornClause.consequent = (Rule) this.consequent.clone();
        cloneHornClause.antecedantConjuncts = new ArrayList();
        for (int i = 0; i < this.antecedantConjuncts.size(); i++) {
            Rule antecedantConjunct = (Rule) this.antecedantConjuncts.get(i);
            cloneHornClause.antecedantConjuncts.add(antecedantConjunct.clone());
            }
        return cloneHornClause;
    }

    /**
     * Returns the string representation of the <tt>HornClause</tt>.
     *
     * @return the string representation of the <tt>HornClause</tt>
     */
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer("(implies");
        if (antecedantConjuncts.size() == 1) {
            stringBuffer.append("\n  ");
            stringBuffer.append(antecedantConjuncts.get(0).toString());
        }
        else {
            stringBuffer.append("\n  (and");
            for (int i = 0; i < antecedantConjuncts.size(); i++) {
                stringBuffer.append("\n    ");
                stringBuffer.append(antecedantConjuncts.get(i).toString());
            }
            stringBuffer.append(")");
        }
        stringBuffer.append("\n  ");
        stringBuffer.append(consequent.toString());
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

    /**
     * Returns the cyclified string representation of the <tt>HornClause</tt>.
     * Embedded constants are prefixed with ""#$".
     *
     * @return the cyclified string representation of the <tt>HornClause</tt>
     */
    protected String cyclify() {
        StringBuffer stringBuffer = new StringBuffer("(#$implies");
        if (antecedantConjuncts.size() == 1) {
            stringBuffer.append("\n  ");
            stringBuffer.append(((Rule) antecedantConjuncts.get(0)).cyclify());
        }
        else {
            stringBuffer.append("\n  (#$and");
            for (int i = 0; i < antecedantConjuncts.size(); i++) {
                stringBuffer.append("\n    ");
                stringBuffer.append(((Rule) antecedantConjuncts.get(i)).cyclify());
            }
            stringBuffer.append(")");
        }
        stringBuffer.append("\n  ");
        stringBuffer.append(consequent.cyclify());
        stringBuffer.append(")");
        return stringBuffer.toString();
    }






}