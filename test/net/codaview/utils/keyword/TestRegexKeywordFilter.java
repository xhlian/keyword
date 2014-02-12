package net.codaview.utils.keyword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRegexKeywordFilter {
	private List<String> keywords;

	private String text;

	@Before
	public void setUp() throws IOException {
		text = read("文章-12150字");
		keywords = getKeywords("敏感词库-1063字");
	}

	@Test
	public void testHasKeyword() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心情"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		Assert.assertTrue("包含关键字", filter.hasKeywords("天气真好!心情也好!"));
		Assert.assertTrue("包含关键字", filter.hasKeywords("天气真好!心*情也好!"));
		Assert.assertTrue("包含关键字", filter.hasKeywords("天气真好!心**情也好!"));
		Assert.assertTrue("包含关键字", filter.hasKeywords("天气真好!心 情也好!"));
		Assert.assertFalse("不包含关键字", filter.hasKeywords("天气真好!情也好!"));
		Assert.assertFalse("不包含关键字", filter.hasKeywords("好天气!真心是好天气!"));
		Assert.assertFalse("不包含关键字", filter.hasKeywords("天气真好!我们出门去玩吧。"));
		Assert.assertFalse("不包含关键字", filter.hasKeywords("天气真好!心_情也好!"));
	}

	@Test
	public void testSimpleReplace() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心情"));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keywords) {
				return "*";
			}
		};

		Assert.assertEquals("大家的*都很好", filter.replace("大家的心情都很好", ss));
		Assert.assertEquals("大家的心都很好", filter.replace("大家的心都很好", ss));
	}
	
	@Test
	public void testSpecialLetter() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心(情"));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keywords) {
				return "*";
			}
		};

		Assert.assertEquals("大家的*都很好", filter.replace("大家的心(情都很好", ss));
	}


	@Test
	public void testSamePrefixReplace() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心事", "心事重", "心事重重"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keywords) {
				return "*";
			}
		};

		Assert.assertEquals("毛人凤正*地在地毯上来回走着",
				filter.replace("毛人凤正心事重重地在地毯上来回走着", ss));
	}

	@Test
	public void testSamePrefixReplaceWithSkipChar() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心事", "心事重", "心事重重", "事重"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keywords) {
				return "*";
			}
		};

		Assert.assertEquals("毛人凤正*地在地毯上来回走着",
				filter.replace("毛人凤正心*事重重地在地毯上来回走着", ss));
	}

	@Test
	public void testReplace() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心情"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keywords) {
				return "文明用语";
			}
		};
		Assert.assertEquals("天气真好!文明用语也好!", filter.replace("天气真好!心情也好!", ss));
		Assert.assertEquals("文明用语", filter.replace("心情", ss));
		Assert.assertEquals("文明用语", filter.replace("心*情", ss));
		Assert.assertEquals("文明用语", filter.replace("心 情", ss));
		Assert.assertEquals("我的文明用语不好", filter.replace("我的心 情不好", ss));
	}

	@Test
	public void testHighLight() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心情"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		final ReplaceStrategy ss = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keyword) {
				return "<b>" + keyword + "</b>";
			}
		};
		Assert.assertEquals("我的<b>心情</b>不好", filter.replace("我的心情不好", ss));
	}

	@Test
	public void testCount() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.compile();

		Assert.assertEquals(2, filter.count("心情好,好心情", "心情"));

		String text2 = "无论你来自哪里，有什么兴趣爱好，都能在这里找到和你一样特别的人。";
		Assert.assertEquals(1, filter.count(text2, "无论"));
		Assert.assertEquals(2, filter.count(text2, "你"));
		Assert.assertEquals(2, filter.count(text2, "里"));
		Assert.assertEquals(1, filter.count(text2, "特别的人"));

		String text = "老龙恼怒闹老农，老农恼怒闹老龙。农怒龙恼农更怒，龙恼农怒龙怕农 。";
		Assert.assertEquals(2, filter.count(text, "老龙"));
		Assert.assertEquals(5, filter.count(text, "龙"));
		Assert.assertEquals(2, filter.count(text, "恼怒"));
	}

	@Test
	public void testCountWithSkipChar() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();

		Assert.assertEquals(2, filter.count("心*情好,好心情", "心情"));
		Assert.assertEquals(2, filter.count("心*情好,好心 情", "心情"));
		Assert.assertEquals(1, filter.count("心*情好,好心 情", "心 情"));

		Assert.assertEquals(0, filter.count("心*情好,好心 情", "心X情"));
		Assert.assertEquals(0, filter.count("心*情好,好心 情", "心   情"));

		Assert.assertEquals(1, filter.count("老*龙恼怒闹老农", "老龙"));
		Assert.assertEquals(1, filter.count("老**龙恼怒闹老农", "老龙"));
		Assert.assertEquals(1, filter.count("老       龙恼怒闹老农", "老龙"));
		Assert.assertEquals(1, filter.count("老   *  龙恼怒闹老农", "老龙"));
		Assert.assertEquals(1, filter.count("老龙恼怒闹老农", "恼怒"));
	}

	@Test
	public void testCountWithSamePrefix() {
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(Arrays.asList("心事", "心事重", "心事重重", "事重"));
		filter.addSkipChar(Arrays.asList('*', ' '));
		filter.compile();
		
		String text2 = "毛人凤正心事重重地在地毯上来回走着。";
		Assert.assertEquals(1, filter.count(text2, "心事重重"));
		Assert.assertEquals(1, filter.count(text2, "心事重"));
		Assert.assertEquals(1, filter.count(text2, "心事"));
		Assert.assertEquals(0, filter.count(text2, "毛毯"));
	}

	@Test
	public void testLargeText() throws IOException {
		// 临时创建一个唯一的关键词
		final String uniqueWordHead = "关键词2" + System.currentTimeMillis();
		keywords.add(0, uniqueWordHead);
		final String uniqueWordTail = "关键词1" + System.currentTimeMillis();
		keywords.add(uniqueWordTail);
		// 构建过滤器
		RegexKeywordFilter filter = new RegexKeywordFilter();
		filter.add(keywords);
		filter.addSkipChar(Arrays.asList('*', ' ', '_', '-', '，'));
		filter.compile();
		
		Assert.assertTrue(keywords.size() > 1000);
		Assert.assertTrue(text.length() > 10000);
		Assert.assertTrue("之前不包含临时创建的关键词", text.indexOf(uniqueWordTail) == -1);

		// 替换
		final ReplaceStrategy hightlightStrategy = new ReplaceStrategy() {
			@Override
			public String replaceWith(String keyword) {
				return "<b>" + keyword + "</b>";
			}
		};

		String text = uniqueWordHead + this.text + uniqueWordTail;
		String result = filter.replace(text, hightlightStrategy);

		Assert.assertTrue("替换成功",
				result.indexOf("<b>" + uniqueWordHead + "</b>") > -1);
		Assert.assertTrue("替换成功",
				result.indexOf("<b>" + uniqueWordTail + "</b>") > -1);
	}

	private List<String> getKeywords(String fileName) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		List<String> keywords = new LinkedList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			keywords.add(line);
		}
		return keywords;
	}

	private String read(String fileName) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

}
