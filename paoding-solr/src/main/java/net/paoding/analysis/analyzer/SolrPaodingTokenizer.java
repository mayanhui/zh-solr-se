package net.paoding.analysis.analyzer;

import java.io.IOException;
import java.io.Reader;

import net.paoding.analysis.analyzer.PaodingTokenizer;
import net.paoding.analysis.analyzer.TokenCollector;
import net.paoding.analysis.knife.Knife;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Solr 1.4 中使用对 PaodingTkenizer 的包装。
 * 
 */
public class SolrPaodingTokenizer extends Tokenizer {

	private PaodingTokenizer paodingTokenizer;
	
	private Knife knife;
	private TokenCollector tokenCollector;

	public SolrPaodingTokenizer(Reader input, Knife knife, TokenCollector tokenCollector) {
		paodingTokenizer = new PaodingTokenizer(input, knife, tokenCollector);
		this.input = input;
		this.knife = knife;
		this.tokenCollector = tokenCollector;
	}

	public Token next() throws IOException {
		return paodingTokenizer.next();
	}
	
	public void close() throws IOException {
		paodingTokenizer.close();
	}

	public void reset(Reader input) throws IOException {
		paodingTokenizer = new PaodingTokenizer(input, knife, tokenCollector);
		this.input = input;
	}
}