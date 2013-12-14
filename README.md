keyword
=======

关键词过滤、替换、高亮、统计算法.   
对1万字的文章、1千个关键词进行筛选，一秒内可执行2000次，平均耗时0.5毫秒。  
不依赖其它第三方工具库。  
参考了https://github.com/jmhsieh/aho-corasick。  
                                       
典型用法如下：                                      
  KeywordFilterBuilder builder = new KeywordFilter();
  
// 设置关键词   
 builder.setKeywords( Arrays.asList("心情", "哈哈", "关键词", "敏感词") );   
 
 // 设置跳过字符   
  builder.setSkipChars( Arrays.asList('*', '_',  ',',  '.',  '-') );   
  final KeywordFilter filter = builder.build();  
 
                                                 
  // 统计关键字出现次数                         
  filter.count( "老龙恼怒闹老农，老农恼怒闹老龙。", "老龙" );     // 2          
  filter.count( "老*龙恼怒闹老农，老农恼怒闹老_龙。", "老龙" );   // 2         
                                                 
  // 替换关键字                  
  filter.replace( "买彩票中奖了，哈哈", new ReplaceStrategy() {             
        public String replaceWith(String keywords) {                       
             return "呵呵";                                                     
        }                                                      
  }); 
  // return "买彩票中奖了，呵呵".  
                                                 
  // 判断是否包含关键词           
  filter.hasKeywords( "今天天气不错，心情也跟着好起来了" );      // true  
  filter.hasKeywords( "今天天气不错，心*情也跟着好起来了" );      // true

