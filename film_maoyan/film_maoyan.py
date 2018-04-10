import requests
import re
import json
from multiprocessing import Pool
from requests.exceptions import RequestException

# 构造HTML下载器
headers = {'User-Agent':'Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36'}
def get_one_page(url):
    try:
        res = requests.get(url, headers=headers)
        
        if res.status_code == 200 :
            return res.text
        return None
    except RequestException:
        print("RequestException!!!")
        return None
    
# 构造HTML解析器
def parse_one_page(html):
    pattern = re.compile('<dd>.*?board-index.*?>(\d+)</i>.*?data-src="(.*?)".*?name"><a'
                         +'.*?>(.*?)</a>.*?star">(.*?)</p>.*?releasetime">(.*?)</p>'
                         +'.*?integer">(.*?)</i>.*?fraction">(.*?)</i>.*?</dd>',re.S)
    items = re.findall(pattern, html)
    for item in items:
        yield {
            'index': item[0],
            'image': item[1],
            'title': item[2],
            'actor': item[3].strip()[3:],
            'time': item[4].strip()[5:],
            'score': item[5] + item[6]
        }
    # yield 结果有返回值
    # 在函数中本来该return的地方用yield，如果用return，在第一轮循环就会跳出，
    # 结果文件只会有一部电影。如果用yield，函数返回的就是一个生成器，
    # 而生成器作为一种特殊的迭代器，可以用for——in方法，一次一次的把yield拿出来；
    
# 构造数据存储
def write_to_file(content):
    with open('result.txt','a', encoding='utf-8') as f:
        
        f.write(json.dumps(content, ensure_ascii=False) + '\n')
        f.close()
        
def main(offset):
    url_maoyan = 'http://maoyan.com/board/4?offset=' + str(offset)
    # url_douban = 'https://www.douban.com/doulist/240962/?start='+ str(offset) +'&sort=seq&sub_type='
    html = get_one_page(url_maoyan)
    for item in parse_one_page(html):
        print(item)
        # 大于9.0的做存储处理
        if(float(item['score']) >= 8.8):
            print("it is goog! ")
            write_to_file(item)
        
        
if __name__ == '__main__':
    p = Pool()
    p.map(main, [i*10 for i in range(1000)])
    # p.map(main, [i*25 for i in range(100)])