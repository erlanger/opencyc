package org.opencyc.elf.bg.state;

import java.util.*;

import junit.framework.*;


/**
 * Provides a suite of JUnit test cases for the org.opencyc.elf.bg.state
 * package.
 * 
 * <p></p>
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
public class UnitTest extends TestCase {
  /**
   * Main method in case tracing is prefered over running JUnit.
   * @param args DOCUMENT ME!
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  /**
   * Construct a new UnitTest object.
   * 
   * @param name the test case name.
   */
  public UnitTest(String name) {
    super(name);
  }

  /**
   * Runs the unit tests
   * @return DOCUMENT ME!
   */
  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    testSuite.addTest(new UnitTest("testSituation"));

    return testSuite;
  }

  /**
   * Tests Situation object behavior.
   */
  public void testSituation() {
    System.out.println("\n*** testSituation ***");

    Situation situation1 = new Situation();
    Object stateVariable1 = new Object();
    Object attribute1 = new Object();
    Assert.assertNull(situation1.getState().getStateValue(stateVariable1));
    situation1.getState().setStateValue(stateVariable1, "abc");
    Assert.assertEquals("abc", situation1.getState().getStateValue(stateVariable1));

    Situation situation2 = new Situation(situation1);
    Assert.assertEquals(situation1, situation2);

    Object stateVariable2 = new Object();
    situation2.getState().setStateValue(stateVariable2, "def");
    Assert.assertTrue(!situation1.equals(situation2));
    System.out.println(situation2.toString());

    String context = "context";
    situation2 = new Situation(situation1);
    situation1.getState().setContext(context);
    Assert.assertEquals(context, situation1.getState().getContext());
    Assert.assertTrue(!situation1.equals(situation2));
    situation2.getState().setContext(context);
    Assert.assertTrue(situation1.equals(situation2));
    situation2.getState().setContext("context2");
    Assert.assertTrue(!situation1.equals(situation2));

    int iteratorCount = 0;
    Object iterator1 = situation1.getState().stateVariables();
    Assert.assertTrue(iterator1 instanceof Iterator);

    Iterator iterator2 = situation1.getState().stateVariables();

    while (iterator2.hasNext()) {
      Object stateVariable = iterator2.next();
      iteratorCount++;
      Assert.assertEquals(stateVariable1, stateVariable);
    }

    Assert.assertEquals(1, iteratorCount);
    Assert.assertTrue(situation1.getState().isStateVariable(stateVariable1));

    System.out.println("*** testSituation OK ***");
  }
}