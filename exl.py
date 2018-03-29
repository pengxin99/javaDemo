# coding=utf-8

import xlrd
import sys
print(sys.getdefaultencoding())		# 查看系统默认的编码版本


# 读excel
file_name = '333.xls'			
excel = xlrd.open_workbook(file_name)	# 打开Excel文件

table = excel.sheets()[0]				# 读取表，可能有多个表，这里 excel.sheets() 返回的是list
nrows = table.nrows						# 统计行数
ncols = table.ncols						# 统计列数

for i in xrange(0,nrows):				# 按行打印每一行的内容
	rowValues= table.row_values(i)
	for item in rowValues:
		# print(item)
		pass

# 将内容重新组织，写入txt
txtName = "sqlcoding.txt"
f=file(txtName, "a+")
count = 0
for i in xrange(1,nrows):
	rowValues = table.row_values(i)
	# print(type(rowValues))
	ID = int(rowValues[1])
	name = rowValues[2]
	# print(name)
	time = "2018-03-30 08:00:00"
	count += 1
	sql = "INSERT INTO user (id, name, type, createTime) VALUES ('"+ str(ID) + "', '" + name + "', 'jijifenzi', '2018-03-30 08:00:00');\n"
	# print(type(sql))
	# print(sql)

	# f.write(sql)

f.close()
print("END", count)