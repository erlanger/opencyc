package org.opencyc.constraintsolver;

import java.util.*;
import java.io.*;
import java.net.*;
import org.opencyc.cycobject.*;
import org.opencyc.api.*;

/**
 * Provides argument type consistency checking for candidate backchaining rules, and also
 * provides additional constraint rules derived from argument type constraints on the
 * restricting constraint rules using the input constraint
 * rules as a starting point.  No additional rules are derived for domain populating rules
 * because their purpose is not to restrict permitted variable bindings, but to populate
 * the domains with values.<p>
 *
 * Argument type constraints are retrieved for candidate rules during backchaining.  For
 * variables mentioned in both the antecedant and consequent portions of the candidate
 * backchain rule, the argument type constraints on these variables can rule out
 * candidates for backchaining if associated collections are proven disjoint.
 *
 * The <tt>ArgumentTypeConstrainer</tt> provides the additional constraint rules as a preparation
 * step before beginning the forward checking search for permitted bindings.
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
public class ArgumentTypeConstrainer {

    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    protected int verbosity = 9;

    /**
     * Reference to the collection of additional argument type constraint rules.
     */
    ArrayList argumentTypeConstraintRules;

    /**
     * Reference to the parent <tt>ConstraintProblem</tt> object.
     */
    protected ConstraintProblem constraintProblem;

    /**
     * Constructs a new <tt>ArgumentTypeConstrainer</tt> object given the parent
     * <tt>ConstraintProblem</tt> object.
     *
     * @param constraintProblem the parent constraint problem
     */
    public ArgumentTypeConstrainer(ConstraintProblem constraintProblem) {
        this.constraintProblem = constraintProblem;
        argumentTypeConstraintRules = constraintProblem.argumentTypeConstraintRules;
    }

    /**
     * Retrieves the argument type constraint rules for the given rule.
     *
     * @param predicate the <tt>CycConstant</tt> which is the predicate of a simple rule
     * @param argPosition the argument position, indexed base 1, for which the argument
     * type constraint rules are sought
     * @param cycVariable the variable used to construct the returned rules
     * @return the argument type constraint rules for the given rule
     */
    public ArrayList retrieveArgumentTypeConstraintRules(Rule rule) throws IOException {
        ArrayList result = new ArrayList();
        CycConstant predicate = rule.getPredicate();
        if (verbosity > 3)
            System.out.println("Gathering type constraints for\n" + rule.cyclify());
        for (int i = 0; i < rule.getArguments().size(); i++) {
            Object argument = rule.getArguments().get(i);
            if (rule.getVariables().contains(argument)) {
                CycVariable cycVariable = (CycVariable) argument;
                int argPosition = i + 1;
                result.addAll(retrieveArgumentTypeConstraintRules(predicate,
                                                                  argPosition,
                                                                  cycVariable));
            }
        }
        return result;
    }

    /**
     * Retrieves the argument type constraint rules for the given predicate at the given
     * argument position, indexed base 1.
     *
     * @param predicate the <tt>CycConstant</tt> which is the predicate of a simple rule
     * @param argPosition the argument position, indexed base 1, for which the argument
     * type constraint rules are sought
     * @param cycVariable the variable used to construct the returned rules
     * @return the argument type constraint rules for the predicate
     */
    public ArrayList retrieveArgumentTypeConstraintRules(CycConstant predicate,
                                                            int argPosition,
                                                            CycVariable cycVariable) throws IOException {
        ArrayList result = new ArrayList();
        result.addAll(retrieveArgNIsas(predicate, argPosition, cycVariable));
        result.addAll(retrieveArgGenls(predicate, argPosition, cycVariable));
        return result;
    }

    /**
     * Retrieves the #$argNIsa constraint rules for the given predicate at the given
     * argument position, indexed base 1.
     *
     * @param predicate the <tt>CycConstant</tt> which is the predicate of a simple rule
     * @param argPosition the argument position, indexed base 1, for which the argument
     * type constraint rules are sought
     * @param cycVariable the variable used to construct the returned rules
     * @return the #$argNIsa constraint rules for the given predicate at the given
     * argument position, indexed base 1
     */
    public ArrayList retrieveArgNIsas(CycConstant predicate,
                                         int argPosition,
                                         CycVariable cycVariable) throws IOException {
        ArrayList result = new ArrayList();
        CycList isas = CycAccess.current().getArgNIsas(predicate, argPosition);
        for (int i = 0; i < isas.size(); i++) {
            CycConstant collection = (CycConstant) isas.get(i);
            String ruleString = "(#$isa " + cycVariable + " " + collection.cyclify() + ")";
            Rule rule = new Rule(ruleString);
            result.add(rule);
            if (verbosity > 3)
                System.out.println("  " + predicate.cyclify() +
                                   " has #$argNIsa constraint at arg position " + argPosition +
                                   "  \n" + rule.cyclify());
        }
        return result;
    }

    /**
     * Retrieves the #$argGenl constraint rules for the given predicate at the given
     * argument position, indexed base 1.
     *
     * @param predicate the <tt>CycConstant</tt> which is the predicate of a simple rule
     * @param argPosition the argument position, indexed base 1, for which the argument
     * type constraint rules are sought
     * @param cycVariable the variable used to construct the returned rules
     * @return the #$argNGenls constraint rules for the given predicate at the given
     * argument position, indexed base 1
     */
    public ArrayList retrieveArgGenls(CycConstant predicate,
                                      int argPosition,
                                      CycVariable cycVariable)
        throws IOException, UnknownHostException{
        ArrayList result = new ArrayList();
        CycList genls = CycAccess.current().getArgNGenls(predicate, argPosition);
        for (int i = 0; i < genls.size(); i++) {
            CycConstant collection = (CycConstant) genls.get(i);
            String ruleString = "(#$isa " + cycVariable + " " + collection.cyclify() + ")";
            Rule rule = new Rule(ruleString);
            result.add(rule);
            if (verbosity > 3)
                System.out.println("  " + predicate.cyclify() +
                                   " has #$argNGenl constraint at arg position " + argPosition +
                                   "  \n" + rule.cyclify());
        }
        return result;
    }

    /**
     * Returns <tt>true</tt> iff the given unary constraint rule is consistent with
     * the previously accepted unary constraints on the given variable.  Consistency is
     * determined by cases.  If the unary constraint is of the form<br>
     * <code>(#$isa variable collection)</code><br>
     * then the collection must not be disjoint from any other collections having the
     * variable as an element.  And in the case where the unary constraint is of the
     * form<br>
     * <code>(#$genls variable collection)</code><br>
     * then the collection must not be disjoint from any other collections have the
     * variable as a spec.
     *
     * @param unaryRule the unary constraint rule whose consistency is in question
     * @param cycVariable the variable to which the unary constraint is checked for consistency
     * @return <tt>true</tt> iff the given unary constraint rule is consistent with
     * the previously accepted unary constraints on the given variable
     */
    public boolean isUnaryRuleConsistent(Rule unaryRule, CycVariable cycVariable) {
        // Find the associated collections for the unary constraint rule under consideration.
        CycConstant consideringIsaCollection = null;
        CycConstant consideringGenlsCollection = null;
        CycConstant consideringPredicate = unaryRule.getPredicate();
        if (consideringPredicate.toString().equals("isa")) {
            consideringIsaCollection = (CycConstant) unaryRule.getArguments().third();
            if (verbosity > 3)
                System.out.println("considering isa collection " + consideringIsaCollection);
        }
        else if (consideringPredicate.toString().equals("genls")) {
            consideringGenlsCollection = (CycConstant) unaryRule.getArguments().third();
            if (verbosity > 3)
                System.out.println("considering genls collection " + consideringIsaCollection);
        }
        else {
            System.out.println("Unexpected predicate for argument constraint " + unaryRule);
            System.exit(1);
        }
        // Compare the considered collection(s) with those previously accepted for the
        // given variable.  Return false if any are disjoint.

        for (int i = 0; i < constraintProblem.constraintRules.size(); i++) {
            Rule rule = (Rule) constraintProblem.constraintRules.get(i);
            if ((rule.getArity() == 1) &&
                (rule.getVariables().contains(cycVariable)))
            if (verbosity > 3)
                System.out.println(cycVariable + " has applicable unary rule \n" + rule);
            CycConstant predicate = rule.getPredicate();
            if (predicate.toString().equals("isa")) {
                CycConstant isaCollection = (CycConstant) rule.getArguments().third();
                if (areDisjointCollections(consideringIsaCollection, isaCollection)) {
                    if (verbosity > 3)
                        System.out.println(consideringIsaCollection +
                                           " is disjoint from " + isaCollection);
                    return false;
                }
            }
            else if (predicate.toString().equals("genls")) {
                CycConstant genlsCollection = (CycConstant) rule.getArguments().third();
                if (areDisjointCollections(consideringGenlsCollection, genlsCollection)) {
                    if (verbosity > 3)
                        System.out.println(consideringGenlsCollection +
                                           " is disjoint from " + genlsCollection);
                    return false;
                }
            }
            else {
                System.out.println("Unexpected rule " + rule);
                System.exit(1);
            }
        }
        return true;
    }

    //TODO replace with CycAccess method
    protected boolean areDisjointCollections(CycConstant collection1, CycConstant collection2) {
        return false;
    }





    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     *
     * @param verbosity 0 --> quiet ... 9 -> maximum diagnostic input
     */
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

}