/*
 * SuprSetr is Copyright 2010-2017 by Jeremy Brooks
 *
 * This file is part of SuprSetr.
 *
 * SuprSetr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuprSetr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuprSetr.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.jeremybrooks.suprsetr;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.Toolkit;

/**
 * @author Jeremy Brooks
 */
public class DocumentSizeFilter extends DocumentFilter {
	private int maxCharacters;

	public DocumentSizeFilter(int maxChars) {
		maxCharacters = maxChars;
	}

	public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
		//This rejects the entire insertion if it would make
		//the contents too long. Another option would be
		//to truncate the inserted string so the contents
		//would be exactly maxCharacters in length.
		if ((fb.getDocument().getLength() + str.length()) <= maxCharacters) {
			super.insertString(fb, offs, str, a);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}

	public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
		//This rejects the entire replacement if it would make
		//the contents too long. Another option would be
		//to truncate the replacement string so the contents
		//would be exactly maxCharacters in length.
		if ((fb.getDocument().getLength() + str.length() - length) <= maxCharacters) {
			super.replace(fb, offs, length, str, a);
		} else {
			Toolkit.getDefaultToolkit().beep();
		}
	}
}
