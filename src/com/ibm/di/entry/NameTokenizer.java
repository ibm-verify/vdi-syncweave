/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.util.ListIterator;

/**
 * Tokenizer used for reading TDI attribute names.
 * 
 */
public class NameTokenizer implements Cloneable {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This constant determines the before-start position's value.
	 */
	private static final int BEFORE_START = -1;

	/**
	 * This constant determines the after-end position's value
	 */
	private int AFTER_END = BEFORE_START;

	/**
	 * The attribute name.
	 */
	private String name;

	/**
	 * The current position.
	 */
	private int position = BEFORE_START;

	/**
	 * The used escape character.
	 */
	private char escapeChar = '\\';

	/**
	 * Sets the attribute name to be parsed.
	 * 
	 * @param name
	 *            attribute name.
	 */
	public void setName(String name) {
		this.setName(name, BEFORE_START);
	}

	/**
	 * Sets the attribute name to be parsed.
	 * 
	 * @param name
	 *            attribute name.
	 * @param start
	 *            whether the tokenizer should be position at the name start or
	 *            at its end.
	 */
	public void setName(String name, boolean start) {
		if (start) {
			this.setName(name, BEFORE_START);
		} else {
			this.setName(name, Integer.MAX_VALUE);
		}
	}

	/**
	 * Sets the attribute name to be parsed.
	 * 
	 * @param name
	 *            attribute name.
	 * @param position
	 *            starting position.
	 */
	public void setName(String name, int position) {
		if (name == null) {
			throw new IllegalArgumentException("The name cannot be null");
		}
		AFTER_END = name.length();

		if (position < BEFORE_START) {
			position = BEFORE_START;
		}
		if (position > AFTER_END) {
			position = AFTER_END;
		}
		this.name = name;
		this.position = position;
	}

	/**
	 * Changes the used escape character.
	 * 
	 * @param escapeChar
	 *            escape character.
	 */
	public void setEscapeChar(char escapeChar) {
		this.escapeChar = escapeChar;
	}

	/**
	 * Returns the current tokenizer position.
	 * 
	 * @return position.
	 */
	public int getCurrentPosition() {
		return position;
	}

	/**
	 * Returns the position of the next separator.
	 * 
	 * @param separator
	 *            the separator.
	 * @return the separator's position.
	 */
	public int getNextSeparatorPosition(char separator) {
		if (position >= AFTER_END) {
			return AFTER_END;
		}

		if (separator != 0) {
			while ((position = name.indexOf(separator, ++position)) > 0) {
				if (name.charAt(position - 1) != escapeChar) {
					// found it
					break;
				} else {
					int current = position - 2;
					while (current >= 0 && name.charAt(current) == escapeChar) {
						--current;
					}

					if ((position - 1 - current) % 2 == 0) {
						// found it
						break;
					}
				}
			}
			if (position == -1) {
				position = AFTER_END;
			}
		} else {
			position = AFTER_END;
		}
		return position;
	}

	/**
	 * Returns the string from the current position to the next separator. All
	 * escaped separators in the resulting string will be replaced by the
	 * separator.
	 * 
	 * @param separator
	 *            the separator.
	 * @return the resolved string from the current position to the next
	 *         separator.
	 */
	public String getNextToken(char separator) {
		if (position == AFTER_END) {
			return null;
		}
		int startIndex = position + 1;
		getNextSeparatorPosition(separator);
		String result = name.substring(startIndex, position);
		result = resolve(result, separator);
		return result;
	}

	/**
	 * Returns the position of the previous separator.
	 * 
	 * @param separator
	 *            the separator.
	 * @return the separator's position.
	 */
	public int getPreviousSeparatorPosition(char separator) {
		if (position <= BEFORE_START) {
			return BEFORE_START;
		}

		if (separator != 0) {
			while ((position = name.lastIndexOf(separator, --position)) > 0) {
				if (name.charAt(position - 1) != escapeChar) {
					// found it
					break;
				} else {
					int current = position - 2;
					while (current >= 0 && name.charAt(current) == escapeChar) {
						--current;
					}

					if ((position - 1 - current) % 2 == 0) {
						// found it
						break;
					}
					position = current;
				}
			}
			if (position == -1) {
				position = BEFORE_START;
			}
		} else {
			position = BEFORE_START;
		}

		return position;
	}

	/**
	 * Returns the string from the current position to the previous separator.
	 * All escaped separators in the resulting string will be replaced by the
	 * separator.
	 * 
	 * @param separator
	 *            the separator.
	 * @return the resolved string from the current position to the previous
	 *         separator.
	 */
	public String getPreviousToken(char separator) {
		if (position == BEFORE_START) {
			return null;
		}
		int endIndex = position;
		getPreviousSeparatorPosition(separator);
		String result = name.substring(position + 1, endIndex);
		result = resolve(result, separator);
		return result;
	}

	/**
	 * Replaces the escaped occurrences of the provided separator with only the
	 * separator.
	 * 
	 * @param escapedString
	 *            the escaped string.
	 * @param separator
	 *            the separator.
	 * @return the resolved string.
	 */
	private String resolve(String escapedString, char separator) {
		return escapedString.replace(String.valueOf(new char[] { escapeChar, separator }), String.valueOf(separator));
	}

	/**
	 * Get the index of the before-start position.
	 * 
	 * @return the before-start position's index.
	 */
	public final int getBeforeStartIndex() {
		return BEFORE_START;
	}

	/**
	 * Get the index of the after-end position.
	 * 
	 * @return the after-end position's index.
	 */
	public final int getAfterEndIndex() {
		return AFTER_END;
	}

	/**
	 * Creates a {@link ListIterator} for easier work with the Tokenizer..
	 * 
	 * @param separator
	 *            the separator to be used by the Iterator.
	 * @return the {@link ListIterator}.
	 */
	public ListIterator<String> listIterator(char separator) {
		return new NamedIterator(this, separator);
	}

	/**
	 * {@inheritDoc}
	 */
	protected NameTokenizer clone() {
		NameTokenizer newTokenizer = new NameTokenizer();
		newTokenizer.setName(name, position);
		newTokenizer.setEscapeChar(escapeChar);
		return newTokenizer;
	}

	/**
	 * An Iterator interface used for simpler traversing of the
	 * {@link NameTokenizer} structure.
	 */
	private static class NamedIterator implements ListIterator<String> {

		/**
		 * The wrapped {@link NameTokenizer}.
		 */
		private NameTokenizer tokenizer;

		/**
		 * The separator used by the iterator.
		 */
		private char separator;

		/**
		 * Constructor. The iterator clones the provided Tokenizer, so that it
		 * is independent from its new changes.
		 * 
		 * @param tokenizer
		 *            the Tokenizer, which will be exposed through this
		 *            iterator.
		 * @param separator
		 *            the separator to be used during the iteration.
		 */
		public NamedIterator(NameTokenizer tokenizer, char separator) {
			this.tokenizer = tokenizer.clone();
			this.separator = separator;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasNext() {
			return tokenizer.getCurrentPosition() < tokenizer.getAfterEndIndex();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean hasPrevious() {
			return tokenizer.getCurrentPosition() > tokenizer.getBeforeStartIndex();
		}

		/**
		 * {@inheritDoc}
		 */
		public String next() {
			return tokenizer.getNextToken(separator);
		}

		/**
		 * {@inheritDoc}
		 */
		public String previous() {
			return tokenizer.getPreviousToken(separator);
		}

		/**
		 * {@inheritDoc}
		 */
		public int nextIndex() {
			return tokenizer.getNextSeparatorPosition(separator);
		}

		/**
		 * {@inheritDoc}
		 */
		public int previousIndex() {
			return tokenizer.getPreviousSeparatorPosition(separator);
		}

		/**
		 * This method is <b>NOT</b> supported. Throws
		 * {@link UnsupportedOperationException}.
		 */
		public void add(String object) {
			throw new UnsupportedOperationException();
		}

		/**
		 * This method is <b>NOT</b> supported. Throws
		 * {@link UnsupportedOperationException}.
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * This method is <b>NOT</b> supported. Throws
		 * {@link UnsupportedOperationException}.
		 */
		public void set(String object) {
			throw new UnsupportedOperationException();
		}

	}

}
