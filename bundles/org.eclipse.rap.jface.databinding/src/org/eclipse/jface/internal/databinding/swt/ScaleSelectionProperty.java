/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Scale;

/**
 * @since 3.3
 * 
 */
public class ScaleSelectionProperty extends WidgetIntValueProperty<Scale> {
	/**
	 * 
	 */
	public ScaleSelectionProperty() {
		super(SWT.Selection);
	}

	@Override
	protected int doGetIntValue(Scale source) {
		return source.getSelection();
	}

	@Override
	protected void doSetIntValue(Scale source, int value) {
		source.setSelection(value);
	}

	@Override
	public String toString() {
		return "Scale.selection <int>"; //$NON-NLS-1$
	}
}
