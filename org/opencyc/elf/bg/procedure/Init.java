/* $Id$
 *
 * Copyright (c) 2003 Cycorp, Inc.  All rights reserved.
 * This software is the proprietary information of Cycorp, Inc.
 * Use is subject to license terms.
 */

package org.opencyc.elf.bg.procedure;

//// Internal Imports

//// External Imports

import java.util.ArrayList;

/**
 * <P>Init is designed to...
 *
 * <P>Copyright (c) 2003 Cycorp, Inc.  All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 *
 * @author reed
 * @date August 11, 2003, 4:24 PM
 * @version $Id$
 */
public class Init extends org.opencyc.elf.bg.procedure.Procedure {
  
  //// Constructors
  
  /** Creates a new instance of Init. */
  /**
   * Creates a new instance of Init.
   * @param namespace the procedure namespace
   * @param name the procedure name
   * @param parameterTypes the types of the procedure parameters
   * @param outputType the type of the procedure output
   */
  public Init (String namespace, String name, ArrayList parameterTypes, Class outputType) {
    super(namespace, name, parameterTypes, outputType);
  }
  
  public Object execute(java.util.ArrayList inputs) {
    //TODO
    return null;
  }
  
  //// Public Area
  
  //// Protected Area
  
  //// Private Area
  
  //// Internal Rep
  
  //// Main
  
}
