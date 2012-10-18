package net.paoding.analysis.analyzer;

import java.io.Reader;
import java.util.Map;

import net.paoding.analysis.analyzer.TokenCollector;
import net.paoding.analysis.analyzer.impl.MaxWordLengthTokenCollector;
import net.paoding.analysis.analyzer.impl.MostWordsTokenCollector;
import net.paoding.analysis.knife.PaodingMaker;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.solr.analysis.BaseTokenizerFactory;

/**
 * 中文切词 对庖丁切词的封装
 */
public class ChineseTokenizerFactory extends BaseTokenizerFactory {
	/**
	 * 最多切分 默认模式
	 */
	public static final String MOST_WORDS_MODE = "most-words";
	/**
	 * 按最大切分
	 */
	public static final String MAX_WORD_LENGTH_MODE = "max-word-length";
	private String mode = null;

	public void setMode(String mode) {
		if (mode == null || MOST_WORDS_MODE.equalsIgnoreCase(mode)
				|| "default".equalsIgnoreCase(mode)) {
			this.mode = MOST_WORDS_MODE;
		} else if (MAX_WORD_LENGTH_MODE.equalsIgnoreCase(mode)) {
			this.mode = MAX_WORD_LENGTH_MODE;
		} else {
			throw new IllegalArgumentException("不合法的分析器Mode参数设置:" + mode);
		}
	}

	@Override
	public void init(Map<String, String> args) {
		super.init(args);
		setMode((String) args.get("mode"));
	}

	private TokenCollector createTokenCollector() {
		if (MOST_WORDS_MODE.equals(mode))
			return new MostWordsTokenCollector();
		if (MAX_WORD_LENGTH_MODE.equals(mode))
			return new MaxWordLengthTokenCollector();
		throw new Error("never happened");
	}

	@Override
	public Tokenizer create(Reader input) {
		return new SolrPaodingTokenizer(input, PaodingMaker.make(),
				createTokenCollector());
	}
}