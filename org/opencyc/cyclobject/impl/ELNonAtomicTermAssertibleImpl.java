package org.opencyc.cyclobject.impl;

import java.util.ArrayList;

import org.opencyc.cyclobject.CycLNonAtomicTermAssertible;
import org.opencyc.cyclobject.el.ELExpressionAssertible;
import org.opencyc.cyclobject.el.ELNonAtomicTermAssertible;

/*****************************************************************************
 * KB comment for #$ELNonAtomicTerm-Assertible as of 2002/05/07:<p>
 *
 * The collection of syntactically and semantically well-formed non-atomic terms
 * in the EL language.  These sentences meet the criteria necessary to be part
 * of an assertion into the Cyc KB, after being converted into HL form by the
 * #$CycCanonicalizer.  Instances of this collection are not themselves
 * assertible.  Just because a non-atomic term is assertible does not require it
 * to be used in an assertion.  Each instance of this collection involves a
 * #$Function-Denotational applied to some number of arguments, as permitted by
 * the arity of the logical relation.  For a thorough discussion of what
 * constitutes a well-formed CycL formula, see the Cyc documentation.<p>
 *
 * @version $Id$
 * @author Tony Brusseau, Steve Reed
 *
 * <p>Copyright 2001 Cycorp, Inc., license is open source GNU LGPL.
 * <p><a href="http://www.opencyc.org/license.txt">the license</a>
 * <p><a href="http://www.opencyc.org">www.opencyc.org</a>
 * <p><a href="http://sf.net/projects/opencyc">OpenCyc at SourceForge</a>
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
 *****************************************************************************/
public class ELNonAtomicTermAssertibleImpl
  extends ELNonAtomicTermAskableImpl
  implements ELNonAtomicTermAssertible, ELExpressionAssertible,
             CycLNonAtomicTermAssertible {

  public boolean isEL() { return true; }
  
  protected ELNonAtomicTermAssertibleImpl(ArrayList rep) {
    super(rep);
  }

  public static ELNonAtomicTermAssertibleImpl 
    createELNonAtomicTermAssertible(ArrayList rep) { 
    try {
      return (ELNonAtomicTermAssertibleImpl)createFormula(rep, 
        Class.forName("ELNonAtomicTermAssertibleImpl"));
    } catch (Exception e) { e.printStackTrace(); } //can't happen
    return null; //will never get here
  }
}
