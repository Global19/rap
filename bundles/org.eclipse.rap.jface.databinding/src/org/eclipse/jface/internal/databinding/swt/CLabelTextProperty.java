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

import org.eclipse.swt.custom.CLabel;

/**
 * @since 3.3
 * 
 */
public class CLabelTextProperty extends WidgetStringValueProperty<CLabel> {
	@Override
	protected String doGetStringValue(CLabel source) {
		return source.getText();
	}

	@Override
	protected void doSetStringValue(CLabel source, String value) {
		source.setText(value == null ? "" : value); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return "CLabel.text <String>"; //$NON-NLS-1$
	}
}
