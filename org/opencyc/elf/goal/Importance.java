package org.opencyc.elf.goal;

//// Internal Imports

//// External Imports

/**
 * Provides the Importance container for the Elementary Loop Functioning
 * (ELF).
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
public class Importance {
  
  //// Constructors
  
  /** Constructs a new Importance object. */
  public Importance() {
  }

  /**
   * Constructs a new Importance object given the importance.
   *
   * @param importance the importance
   */
  public Importance(float importance) {
    setImportance(importance);
  }

  //// Public Area
  
  /** most important */
  public static final float MOST = 1.0f;
  
  /** more important */
  public static final float MORE = .75f;
  
  /** neutrally important */
  public static final float NEUTRAL = .5f;
  
  /** less important */
  public static final float LESS = .75f;
  
  /** least important */
  public static final float LEAST = 0.0f;
  
  /**
   * Gets the importance
   * 
   * @return the importance
   */
  public float getImportance() {
    return importance;
  }

  /**
   * Sets the importance
   * 
   * @param importance the importance
   * @throws IllegalArgumentException when the importance is not in the range [0.0 ... +1.0]
   */
  public void setImportance(float importance) {
    if ((importance < 0.0) || (importance > 1.0))
      throw new IllegalArgumentException(importance + " is not in the range [0.0 ... +1.0]");
    this.importance = importance;
  }
  
  //// Protected Area
  
  /** Importances range from 0.0 for least important, to +1 for most important. */
  protected float importance;

  //// Private Area
  
  //// Internal Rep
  
}