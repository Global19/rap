/*******************************************************************************
 * Copyright (c) 2008, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rwt.internal.theme;

import static org.eclipse.rwt.internal.theme.ThemeTestUtil.setCustomTheme;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.rap.rwt.testfixture.Fixture;
import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.shellkit.ShellThemeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class ShellThemeAdapter_Test extends TestCase {

  private Display display;

  protected void setUp() {
    Fixture.setUp();
    Fixture.fakeNewRequest();
    display = new Display();
  }

  protected void tearDown() {
    Fixture.tearDown();
  }

  public void testPlainShell() throws IOException {
    Color yellow = display.getSystemColor( SWT.COLOR_YELLOW );
    Color blue = display.getSystemColor( SWT.COLOR_BLUE );
    Shell shell = new Shell( display, SWT.NONE );
    ShellThemeAdapter themeAdapter = getShellThemeAdapter( shell );

    setCustomTheme(   " * { color: blue; }"
                    + "Shell { border: 3px solid blue; background-color: yellow; }" );

    assertEquals( 3, themeAdapter.getBorderWidth( shell ) );
    assertEquals( blue, themeAdapter.getForeground( shell ) );
    assertEquals( yellow, themeAdapter.getBackground( shell ) );
  }

  public void testShellWithBorder() {
    Shell shell = new Shell( display, SWT.BORDER );
    ShellThemeAdapter themeAdapter = getShellThemeAdapter( shell );
    assertEquals( 1, themeAdapter.getBorderWidth( shell ) );
    shell.setMaximized( true );
    assertEquals( 0, themeAdapter.getBorderWidth( shell ) );
  }

  public void testTitleBarHeightFromCustomVariant() throws IOException {
    Shell shell = new Shell( display, SWT.TITLE );
    ShellThemeAdapter shellThemeAdapter = getShellThemeAdapter( shell );

    setCustomTheme( "Shell-Titlebar.special { height: 50px }" );
    shell.setData( WidgetUtil.CUSTOM_VARIANT, "special" );

    assertEquals( 50, shellThemeAdapter.getTitleBarHeight( shell ) );
  }

  public void testTitleBarMarginFromCustomVariant() throws IOException {
    Shell shell = new Shell( display, SWT.TITLE );
    ShellThemeAdapter shellThemeAdapter = getShellThemeAdapter( shell );

    setCustomTheme( "Shell-Titlebar.special { margin: 1px 2px 3px 4px }" );
    shell.setData( WidgetUtil.CUSTOM_VARIANT, "special" );

    assertEquals( new Rectangle( 4, 1, 6, 4 ), shellThemeAdapter.getTitleBarMargin( shell ) );
  }

  private static ShellThemeAdapter getShellThemeAdapter( Shell shell ) {
    return ( ShellThemeAdapter )shell.getAdapter( IThemeAdapter.class );
  }

}
