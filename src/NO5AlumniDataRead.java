
import java.io.*;

import ReadWriteChineseTxt.LineEmptyException;

/**
 * @author liangbin
 * 从五中的校友录文件中按行读取数据，调用函数判别当前的内容，并写入目标数据文件当中；
 */
public class NO5AlumniDataRead {

	File configFile;
	File dataInfile, dataOutFile, logFile;
	ReadStatus rs; 
	// BufferedWriter logWriter;
	int logLineCount=0;
	
	public enum ReadStatus{
		S_GRADE, S_TYPE, S_CLASS, S_TEACHER, S_STUDENT
	}
	
	class LineProcessException extends Exception
	{
		String ErrorMessage;
		public LineProcessException(String ErrMsg)
		{
			ErrorMessage= ErrMsg;
		}
		public String getMessage()
		{
			return this.ErrorMessage;
		}
	}
	
	
	public NO5AlumniDataRead(String configureFilePath)
	{
		BufferedReader cfgReader=null;
		configFile= new File(configureFilePath);
		rs= ReadStatus.S_CLASS; // 默认从届别状态开始；
		
		try {
			cfgReader= new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
		}catch (FileNotFoundException e) 
        {  
            System.out.println("configuration file is not found.");  
        }
		
		String firstLine="", secondLine="", thirdLine="";
		
		try {
			firstLine= cfgReader.readLine();
			if(firstLine==null) throw new LineProcessException("Error: first empty line read!");
		}catch(LineProcessException lee) {
			System.out.println(lee.getMessage());
		}catch (IOException e) 
        {  
            System.out.println("Read Exceptioned");  
        }
		
		this.dataInfile= new File(this.ExtractConfigureFileName(firstLine));
		
		try {
			secondLine= cfgReader.readLine();
			if(secondLine==null) throw new LineProcessException("Error: second empty line read!");
		}catch(LineProcessException lee) {
			System.out.println(lee.getMessage());
		}catch (IOException e) 
        {  
            System.out.println("Read Exceptioned");  
        }
		
		this.dataOutFile= new File(this.ExtractConfigureFileName(secondLine));
		
		try {
			thirdLine= cfgReader.readLine();
			if(thirdLine==null) throw new LineProcessException("Error: third empty line read!");
		}catch(LineProcessException lee) {
			System.out.println(lee.getMessage());
		}catch (IOException e) 
        {  
            System.out.println("Read Exceptioned");  
        }
		
		this.logFile= new File(this.ExtractConfigureFileName(thirdLine));
	}
	
	String ExtractConfigureFileName(String cfgLine)
	{
		String fileName="";
		fileName= cfgLine.substring(cfgLine.indexOf("=")+1);
		return fileName;
	}
	
	void writeLog(String logMsg,BufferedWriter bw)
	{
		try {
			bw.write(logMsg+this.logLineCount+"\n");
			this.logLineCount++;
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return;
	}
	
	void extractInfo(String line, int curLineNum, BufferedWriter bw)
	{
		// write log on the receiving line;
		try {
			bw.write("LINE TO PROCESS: NO."+curLineNum+line);
			bw.newLine();
		}catch (IOException e){  
            System.out.println("Read or write Exceptioned");  
        }
		
		String strGrade=new String(""), strType= new String(""), strClass= new String(""), strTeacher= new String(""), strStudent= new String("");
		for(int i=0; i<line.length();)
		{
			switch(rs)
			{
			case S_GRADE:
				try {
					if(Character.isDigit(line.charAt(i))==false) throw new LineProcessException("line exception");
				}catch(LineProcessException lpe) {
					lpe.printStackTrace();
				}
				strGrade= strGrade+line.substring(i, i+4)+"届";
				i+=5;
				rs= ReadStatus.S_TYPE;
				break;
			case S_TYPE:
				if(line.charAt(i)=='初') strType="初中";
				else if(line.charAt(i)=='高') strType="高中";
			    i+=2; 
			    rs= ReadStatus.S_CLASS;
			    break;
			case S_CLASS:
				strClass= strClass+line.charAt(i)+"班";
				i+=2;
				rs= ReadStatus.S_TEACHER;
				break;
			case S_TEACHER:
				rs= ReadStatus.S_STUDENT;
				break;
			case S_STUDENT:
				rs= ReadStatus.S_GRADE;
				break;
			default:
			}
		}
		
		
		return;
	}
	
	boolean isEmptyChar(char c)
	{
		return ( c==' ' || c==' ' || c=='\t' || c=='\n');
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NO5AlumniDataRead cfg_no5= new NO5AlumniDataRead("configure.txt");
		
		BufferedReader dataReader=null;
		BufferedWriter dataWriter=null;
		BufferedWriter logWriter=null;
		
		try {
			dataReader= new BufferedReader(new InputStreamReader(new FileInputStream(cfg_no5.dataInfile)));
			dataWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfg_no5.dataOutFile)));
			logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfg_no5.logFile)));
			
			String line="";
			int lineCount=0;
			cfg_no5.writeLog("Processing Start...", logWriter);
			
			// 反复读取每一行，逐行提取校友相关信息；
			while((line= dataReader.readLine())!=null)
			{
				cfg_no5.extractInfo(line, logWriter);
			}
			
		}catch (FileNotFoundException e){  
            System.out.println("file is not fond");  
        }catch (IOException e){  
            System.out.println("Read or write Exceptioned");  
        }finally {
        	if(null!= dataReader)
        	{
        		try {
        			dataReader.close();
        		}catch(IOException ioe) {
        			ioe.printStackTrace();
        		}
        	}
        	if(null!= dataWriter)
        	{
        		try {
        			dataWriter.close();
        		}catch(IOException ioe) {
        			ioe.printStackTrace();
        		}
        	}
        	if(null!= logWriter)
        	{
        		try {
        			logWriter.close();
        		}catch(IOException ioe) {
        			ioe.printStackTrace();
        		}
        	}
        }
		
		System.out.println("Data processed. Goodbye!");
		return;
	}

}
