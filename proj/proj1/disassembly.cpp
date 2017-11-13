#include <stdio.h>

int b2cfile(char * in_path,char * out_path);

int main(int argc, char *argv[])
{
	// 定義輸入路徑	
	char path[] = "sample.txt" ;
	// 定义输出路径，如果不存在，会自动生成 
	char out_path[] = "test_out.txt" ;
	 
	b2cfile(path,out_path) ;
	
	return 0;
}

int b2cfile(char * path,char * out_path)
{
	FILE *fp ,*outfp ;	
//	char path[100] = in_path ;
	// 定义输出路径，如果不存在，会自动生成 
//	char out_path[100] = out_path ;
	// 每条指令暂时存放数组
	char buffer[32] ;
	char space[2] ;
	 
	// 打开文件，以二进制读 
	if((fp = fopen(path,"rb")) == NULL)
	{
		printf("The file %s can not be opend!!",path) ;
		return 0;
	}
	// 打開輸出文件 
	if((outfp = fopen(out_path,"w")) == NULL)
	{
		printf("The file %s can not be opend!!",out_path) ;
	}
	
	while(fread(buffer,2,1,fp)!=NULL)
	{
		// 判断 '\n' 
		if(buffer[0] == '1' && buffer[1] == '0')
		{
			printf("%c%c",buffer[0],buffer[1]);
			continue ; 
		}
		// 判斷 '\r' 
		else if(buffer[0] == '1' && buffer[1] == '3')
		{
			printf("%c%c",buffer[0],buffer[1]);
			fread(space,2,1,fp) ;
			continue ;
		}
		else{
			fread(buffer+2,30,1,fp) ;
			//printf("%s",buffer) ;
			//fwrite(buffer,32,1,outfp) ;
			for(int i = 0 ;i<32 ;i++)
			{
				printf("%c",buffer[i]);
				fputc(buffer[i],outfp); //若不是结束符，将它写入out所指文件
			}
		//	fputc('\n',outfp);
		}
	}
	fclose(fp) ;
	fclose(outfp) ;
	
	return 0;
}