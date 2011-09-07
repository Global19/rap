/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.combokit;

import java.io.IOException;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.internal.protocol.ClientObjectFactory;
import org.eclipse.rwt.internal.protocol.IClientObject;
import org.eclipse.rwt.internal.util.EncodingUtil;
import org.eclipse.rwt.internal.util.NumberFormatUtil;
import org.eclipse.rwt.lifecycle.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Widget;

/**
 * Life cycle adapter for Combo widgets.
 */
public class ComboLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Combo";
  private static final String[] DEFAUT_ITEMS = new String[ 0 ];
  private static final Integer DEFAULT_SELECTION = new Integer( -1 );
  private static final Point DEFAULT_TEXT_SELECTION = new Point( 0, 0 );
  private static final Integer DEFAULT_TEXT_LIMIT = new Integer( Combo.LIMIT );
  private static final Integer DEFAULT_VISIBLE_ITEM_COUNT = new Integer( 5 );

  // Must be in sync with appearance "list-item"
  private static final int LIST_ITEM_PADDING = 3;

  // Constants for JS functions names
  private static final String JS_FUNC_SELECT = "select";
  private static final String JS_FUNC_SET_SELECTION_TEXT = "setTextSelection";

  // Property names for preserve-value facility
  static final String PROP_ITEMS = "items";
  static final String PROP_TEXT = "text";
  static final String PROP_SELECTION = "selection";
  static final String PROP_TEXT_SELECTION = "textSelection";
  static final String PROP_TEXT_LIMIT = "textLimit";
  static final String PROP_LIST_VISIBLE = "listVisible";
  static final String PROP_EDITABLE = "editable";
  static final String PROP_VERIFY_MODIFY_LISTENER = "verifyModifyListener";
  static final String PROP_VISIBLE_ITEM_COUNT = "visibleItemCount";
  static final String PROP_ITEM_HEIGHT = "itemHeight";

  public void preserveValues( Widget widget ) {
    Combo combo = ( Combo )widget;
    ControlLCAUtil.preserveValues( combo );
    IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
    String[] items = combo.getItems();
    adapter.preserve( PROP_ITEMS, items );
    Integer selection = new Integer( combo.getSelectionIndex() );
    adapter.preserve( PROP_SELECTION, selection );
    adapter.preserve( PROP_TEXT_SELECTION, combo.getSelection() );
    adapter.preserve( PROP_TEXT_LIMIT, new Integer( combo.getTextLimit() ) );
    adapter.preserve( PROP_VISIBLE_ITEM_COUNT, new Integer( combo.getVisibleItemCount() ) );
    adapter.preserve( PROP_ITEM_HEIGHT, new Integer( getItemHeight( combo ) ) );
    adapter.preserve( PROP_TEXT, combo.getText() );
    adapter.preserve( Props.SELECTION_LISTENERS,
                      Boolean.valueOf( SelectionEvent.hasListener( combo ) ) );
    adapter.preserve( PROP_LIST_VISIBLE, new Boolean( combo.getListVisible() ) );
    adapter.preserve( PROP_EDITABLE, Boolean.valueOf( isEditable( combo ) ) );
    boolean hasVerifyListener = VerifyEvent.hasListener( combo );
    boolean hasModifyListener = ModifyEvent.hasListener( combo );
    boolean hasListener = hasVerifyListener || hasModifyListener;
    adapter.preserve( PROP_VERIFY_MODIFY_LISTENER, Boolean.valueOf( hasListener ) );
    WidgetLCAUtil.preserveCustomVariant( combo );
  }

  public void readData( Widget widget ) {
    Combo combo = ( Combo )widget;
    String value = WidgetLCAUtil.readPropertyValue( widget, "selectedItem" );
    if( value != null ) {
      combo.select( NumberFormatUtil.parseInt( value ) );
    }
    String listVisible = WidgetLCAUtil.readPropertyValue( combo, "listVisible" );
    if( listVisible != null ) {
      combo.setListVisible( Boolean.valueOf( listVisible ).booleanValue() );
    }
    readTextAndSelection( combo );
    ControlLCAUtil.processSelection( combo, null, true );
    ControlLCAUtil.processMouseEvents( combo );
    ControlLCAUtil.processKeyEvents( combo );
    ControlLCAUtil.processMenuDetect( combo );
    WidgetLCAUtil.processHelp( combo );
  }

  public void renderInitialization( Widget widget ) throws IOException {
    Combo combo = ( Combo )widget;
    IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
    clientObject.create( TYPE );
    clientObject.setProperty( "parent", WidgetUtil.getId( combo.getParent() ) );
    clientObject.setProperty( "style", WidgetLCAUtil.getStyles( combo ) );
  }

  public void renderChanges( Widget widget ) throws IOException {
    Combo combo = ( Combo )widget;
    ControlLCAUtil.renderChanges( combo );
    WidgetLCAUtil.renderCustomVariant( combo );
    renderItemHeight( combo );
    renderVisibleItemCount( combo );
    renderItems( combo );
    renderListVisible( combo );
    writeSelection( combo );
    renderEditable( combo );
    writeText( combo );
    writeTextSelection( combo );
    writeTextLimit( combo );
    writeVerifyAndModifyListener( combo );
    writeSelectionListener( combo );
  }

  public void renderDispose( Widget widget ) throws IOException {
    ClientObjectFactory.getForWidget( widget ).destroy();
  }

  ///////////////////////////////////////
  // Helping methods to read client state

  private static void readTextAndSelection( final Combo combo ) {
    final Point selection = readSelection( combo );
    final String value = WidgetLCAUtil.readPropertyValue( combo, "text" );
    if( value != null ) {
      if( VerifyEvent.hasListener( combo ) ) {
        // setText needs to be executed in a ProcessAcction runnable as it may
        // fire a VerifyEvent whose fields (text and doit) need to be evaluated
        // before actually setting the new value
        ProcessActionRunner.add( new Runnable() {
          public void run() {
            combo.setText( value );
            // since text is set in process action, preserved values have to be
            // replaced
            IWidgetAdapter adapter = WidgetUtil.getAdapter( combo );
            adapter.preserve( PROP_TEXT, value );
            if( selection != null ) {
              combo.setSelection( selection );
              adapter.preserve( PROP_TEXT_SELECTION, selection );
            }
         }
        } );
      } else {
        combo.setText( value );
        if( selection != null ) {
          combo.setSelection( selection );
        }
      }
    } else if( selection != null ) {
      combo.setSelection( selection );
    }
  }

  private static Point readSelection( Combo combo ) {
    Point result = null;
    String selStart = WidgetLCAUtil.readPropertyValue( combo, "selectionStart" );
    String selLength = WidgetLCAUtil.readPropertyValue( combo, "selectionLength" );
    if( selStart != null || selLength != null ) {
      result = new Point( 0, 0 );
      if( selStart != null ) {
        result.x = NumberFormatUtil.parseInt( selStart );
      }
      if( selLength != null ) {
        result.y = result.x + NumberFormatUtil.parseInt( selLength );
      }
    }
    return result;
  }

  //////////////////////////////////////////////
  // Helping methods to write changed properties

  private static void renderItemHeight( Combo combo ) {
    Integer newValue = new Integer( getItemHeight( combo ) );
    if( WidgetLCAUtil.hasChanged( combo, PROP_ITEM_HEIGHT, newValue ) ) {
      IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
      clientObject.setProperty( PROP_ITEM_HEIGHT, newValue );
    }
  }

  private static void renderVisibleItemCount( Combo combo ) {
    Integer newValue = new Integer( combo.getVisibleItemCount() );
    Integer defValue = DEFAULT_VISIBLE_ITEM_COUNT;
    if( WidgetLCAUtil.hasChanged( combo, PROP_VISIBLE_ITEM_COUNT, newValue, defValue ) ) {
      IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
      clientObject.setProperty( PROP_VISIBLE_ITEM_COUNT, newValue );
    }
  }

  private static void renderItems( Combo combo ) {
    String[] items = combo.getItems();
    if( WidgetLCAUtil.hasChanged( combo, PROP_ITEMS, items, DEFAUT_ITEMS ) ) {
      IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
      clientObject.setProperty( PROP_ITEMS, items );
    }
  }

  private static void renderListVisible( Combo combo ) {
    Boolean newValue = Boolean.valueOf( combo.getListVisible() );
    if( WidgetLCAUtil.hasChanged( combo, PROP_LIST_VISIBLE, newValue, Boolean.FALSE ) ) {
      IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
      clientObject.setProperty( PROP_LIST_VISIBLE, newValue );
    }
  }

  private static void writeSelection( Combo combo ) throws IOException {
    Integer newValue = new Integer( combo.getSelectionIndex() );
    Integer defValue = DEFAULT_SELECTION;
    boolean selectionChanged
      = WidgetLCAUtil.hasChanged( combo, PROP_SELECTION, newValue, defValue );
    // The 'textChanged' statement covers the following use case:
    // combo.add( "a" );  combo.select( 0 );
    // -- in a subsequent request --
    // combo.removeAll();  combo.add( "b" );  combo.select( 0 );
    // When only examining selectionIndex, a change cannot be determined
    boolean textChanged
      = !isEditable( combo ) && WidgetLCAUtil.hasChanged( combo, PROP_TEXT, combo.getText(), "" );
    if( selectionChanged || textChanged ) {
      JSWriter writer = JSWriter.getWriterFor( combo );
      writer.call( JS_FUNC_SELECT, new Object[] { newValue } );
    }
  }

  private static void renderEditable( Combo combo ) {
    Boolean newValue = Boolean.valueOf( isEditable( combo ) );
    if( WidgetLCAUtil.hasChanged( combo, PROP_EDITABLE, newValue, Boolean.TRUE ) ) {
      IClientObject clientObject = ClientObjectFactory.getForWidget( combo );
      clientObject.setProperty( PROP_EDITABLE, newValue );
    }
  }

  private static void writeTextSelection( Combo combo ) throws IOException {
    Point newValue = combo.getSelection();
    Point defValue = DEFAULT_TEXT_SELECTION;
    Integer start = new Integer( newValue.x );
    Integer end = new Integer( newValue.y );
    Integer count = new Integer( end.intValue() - start.intValue() );
    // TODO [rh] could be optimized: when text was changed and selection is 0,0
    //      there is no need to write JavaScript since the client resets the
    //      selection as well when the new text is set.
    if( WidgetLCAUtil.hasChanged( combo, PROP_TEXT_SELECTION, newValue, defValue ) ) {
      // [rh] Workaround for bug 252462: Changing selection on a hidden text
      // widget causes exception in FF
      if( combo.isVisible() ) {
        JSWriter writer = JSWriter.getWriterFor( combo );
        writer.call( JS_FUNC_SET_SELECTION_TEXT, new Object[] { start, count } );
      }
    }
  }

  private static void writeTextLimit( Combo combo ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( combo );
    Integer newValue = new Integer( combo.getTextLimit() );
    Integer defValue = DEFAULT_TEXT_LIMIT;
    if( WidgetLCAUtil.hasChanged( combo, PROP_TEXT_LIMIT, newValue, defValue ) ) {
      if( newValue.intValue() == Combo.LIMIT ) {
        newValue = null;
      }
      writer.set( "textLimit", newValue );
    }
  }

  private static void writeText( Combo combo ) throws IOException {
    if( isEditable( combo ) ) {
      String newValue = combo.getText();
      JSWriter writer = JSWriter.getWriterFor( combo );
      if( WidgetLCAUtil.hasChanged( combo, PROP_TEXT, newValue, "" ) ) {
        String value = EncodingUtil.removeNonDisplayableChars( newValue );
        writer.set( "value", value );
      }
    }
  }

  private static void writeSelectionListener( Combo combo ) throws IOException {
    boolean hasListener = SelectionEvent.hasListener( combo );
    Boolean newValue = Boolean.valueOf( hasListener );
    String prop = Props.SELECTION_LISTENERS;
    if( WidgetLCAUtil.hasChanged( combo, prop, newValue, Boolean.FALSE ) ) {
      JSWriter writer = JSWriter.getWriterFor( combo );
      writer.set( "hasSelectionListener", newValue );
    }
  }

  private static void writeVerifyAndModifyListener( Combo combo ) throws IOException {
    boolean hasVerifyListener = VerifyEvent.hasListener( combo );
    boolean hasModifyListener = ModifyEvent.hasListener( combo );
    boolean hasListener = hasVerifyListener || hasModifyListener;
    Boolean newValue = Boolean.valueOf( hasListener );
    String prop = PROP_VERIFY_MODIFY_LISTENER;
    if( WidgetLCAUtil.hasChanged( combo, prop, newValue, Boolean.FALSE ) ) {
      JSWriter writer = JSWriter.getWriterFor( combo );
      writer.set( "hasVerifyModifyListener", newValue );
    }
  }

  //////////////////
  // Helping methods

  private static boolean isEditable( Combo combo ) {
    return ( ( combo.getStyle() & SWT.READ_ONLY ) == 0 );
  }

  static int getItemHeight( Combo combo ) {
    int charHeight = Graphics.getCharHeight( combo.getFont() );
    int padding = 2 * LIST_ITEM_PADDING;
    return charHeight + padding;
  }
}
