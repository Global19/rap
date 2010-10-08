/*******************************************************************************
 * Copyright (c) 2008, 2010 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.rwt.internal.lifecycle;

import java.util.Locale;

public final class LifeCycleAdapterUtil {

  private LifeCycleAdapterUtil() {
    // prevent instatiation
  }

  /**
   * Inserts the package path segment <code>internal</code> at every possible
   * position in a given package name and appends class name + kit at the end.
   */
  public static String[] getKitPackageVariants( final String packageName,
                                                final String className )
  {
    String[] result;
    if( packageName == null || "".equals( packageName ) ) {
      StringBuffer buffer = new StringBuffer();
      buffer.append( "internal." );
      buffer.append( className.toLowerCase( Locale.ENGLISH ) );
      buffer.append( "kit" );
      result = new String[] { buffer.toString() };
    } else {
      String[] segments = packageName.split( "\\." );
      result = new String[ segments.length + 1 ];
      for( int i = 0; i < result.length; i++ ) {
        StringBuffer buffer = new StringBuffer();
        for( int j = 0; j < segments.length; j++ ) {
          if( j == i ) {
            buffer.append( "internal." );
          }
          buffer.append( segments[ j ] );
          if( j < segments.length - 1 ) {
            buffer.append( "." );
          }
        }
        if( i == segments.length ) {
          buffer.append( ".internal" );
        }
        buffer.append( "." );
        buffer.append( className.toLowerCase( Locale.ENGLISH ) );
        buffer.append( "kit" );
        result[ i ] = buffer.toString();
      }
    }
    return result;
  }

  /**
   * Returns the class name without package prefix for a given class.
   */
  public static String getSimpleClassName( final Class clazz ) {
    String className = clazz.getName();
    int idx = className.lastIndexOf( '.' );
    return className.substring( idx + 1 );
  }
}
