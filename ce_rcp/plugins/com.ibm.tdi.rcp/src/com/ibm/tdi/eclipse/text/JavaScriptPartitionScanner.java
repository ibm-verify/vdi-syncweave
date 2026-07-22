/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

public class JavaScriptPartitionScanner extends RuleBasedPartitionScanner {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static final String JAVASCRIPT_COMMENT = "js.comment";
	public static final String JAVASCRIPT_MULTILINE_COMMENT = "js.multiline.comment";
	public static final String JAVASCRIPT_CONST_STRING = "js.const.string";
	public static final String JAVASCRIPT_CONST_KEYWORD = "js.const.keyword";
	// If it is not a keyword, call it an identifier
	public static final Token JAVASCRIPT_IDENTIFIER = new Token("js.identifier");

	// -- JavaScript Keywords we highlight
	private static String[] keywords = new String[] { "break", "case",
			"comment", "continue", "default", "delete", "do", "else", "export",
			"for", "function", "if", "in", "import", "label", "new", "return",
			"switch", "this", "typeof", "var", "void", "while", "with" };

	public JavaScriptPartitionScanner() {
		ArrayList<IRule> rules = new ArrayList<IRule>();

		IToken comment = new Token(JAVASCRIPT_COMMENT);
		IToken mcomment = new Token(JAVASCRIPT_MULTILINE_COMMENT);
		IToken strconst = new Token(JAVASCRIPT_CONST_STRING);

		rules.add(new MultiLineRule("/*", "*/", mcomment, (char) 0, true));
		rules.add(new EndOfLineRule("//", comment));

		// Add rule for strings and character constants.
		rules.add(new SingleLineRule("\"", "\"", strconst, '\\'));
		rules.add(new SingleLineRule("'", "'", strconst, '\\'));

		// Add rule for javascript keywords
		rules.add(new KeywordRule());

		setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
	}

	/**
	 * The rule based part scanner above won't take
	 * 
	 * @author stadheim
	 * 
	 */
	private static class KeywordRule implements IPredicateRule, IWordDetector {

		private WordRule kw;
		private IToken keyword = new Token(JAVASCRIPT_CONST_KEYWORD);

		public KeywordRule() {
			kw = new WordRule(this, JAVASCRIPT_IDENTIFIER);
			for (String str : keywords)
				kw.addWord(str, keyword);

		}

		public IToken evaluate(ICharacterScanner scanner, boolean resume) {
			return kw.evaluate(scanner);
		}

		public IToken getSuccessToken() {
			return keyword;
		}

		public IToken evaluate(ICharacterScanner scanner) {
			return evaluate(scanner, false);
		}

		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}
	}

}
