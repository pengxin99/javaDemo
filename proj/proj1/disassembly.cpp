#include <stdio.h>

int b2cfile(char * in_path,char * out_path);

int main(int argc, char *argv[])
{
	// ���xݔ��·��	
	char path[] = "sample.txt" ;
	// �������·������������ڣ����Զ����� 
	char out_path[] = "test_out.txt" ;
	 
	b2cfile(path,out_path) ;
	
	return 0;
}

int b2cfile(char * path,char * out_path)
{
	FILE *fp ,*outfp ;	
//	char path[100] = in_path ;
	// �������·������������ڣ����Զ����� 
//	char out_path[100] = out_path ;
	// ÿ��ָ����ʱ�������
	char buffer[32] ;
	char space[2] ;
	 
	// ���ļ����Զ����ƶ� 
	if((fp = fopen(path,"rb")) == NULL)
	{
		printf("The file %s can not be opend!!",path) ;
		return 0;
	}
	// ���_ݔ���ļ� 
	if((outfp = fopen(out_path,"w")) == NULL)
	{
		printf("The file %s can not be opend!!",out_path) ;
	}
	
	while(fread(buffer,2,1,fp)!=NULL)
	{
		// �ж� '\n' 
		if(buffer[0] == '1' && buffer[1] == '0')
		{
			printf("%c%c",buffer[0],buffer[1]);
			continue ; 
		}
		// �Д� '\r' 
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
				fputc(buffer[i],outfp); //�����ǽ�����������д��out��ָ�ļ�
			}
		//	fputc('\n',outfp);
		}
	}
	fclose(fp) ;
	fclose(outfp) ;
	
	return 0;
}