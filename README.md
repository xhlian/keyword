keyword
=======

关键字过滤、替换、高亮、统计算法

学习AC自动机算法写的代码。
不依赖其它第三方工具库。
参考了https://github.com/jmhsieh/aho-corasick。
                                       
典型用法如下：                                      
  KeywordFilterBuilder builder = new KeywordFilte
  // 设置关键字                                       
  builder.setKeywords(Arrays.asList("心情", "哈哈"));
  // 设置跳过字符                                      
  builder.setSkipChars(Arrays.asList('*', ' ')); 
  final KeywordFilter filter = builder.build();  
                                                 
  // 统计关键字出现次数, 下例中返回2.                          
  filter.count("老龙恼怒闹老农，老农恼怒闹老龙。", "老龙");        
  filter.count("老*龙恼怒闹老农，老农恼怒闹老 龙。", "老龙");      
                                                 
  // 替换关键字, 下例中返回"买彩票中奖了，呵呵".                    
  filter.replace("买彩票中奖了，哈哈", new ReplaceStrategy
  	public String replaceWith(String keywords) {
 		return "呵呵";                            
 	}                                           
  });                                            
                                                 
  // 判断是否包含关键字, 下例中返回true, 因为包含"心情".             
  filter.hasKeywords("今天天气不错，心情也跟着好起来了")         
  filter.hasKeywords("今天天气不错，心*情也跟着好起来了")        

