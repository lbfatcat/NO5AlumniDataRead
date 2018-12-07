
import java.io.*;

/**
 * @author liangbin
 * 从五中的校友录文件中按行读取数据，调用函数判别当前的内容，并写入目标数据文件当中；
 */
public class NO5AlumniDataRead {

	File configFile;
	File dataInfile, dataOutFile, logFile;
	ReadStatus rs; 
	
	String strGrade=new String(""), 
		   strType= new String(""), 
		   strClass= new String(""), 
		   strTeacher= new String(""), 
		   strStudent= new String("");
	
	public enum ReadStatus{
		S_GRADE,  		// 当前正在扫描年级信息；
		S_TYPE,   		// 当前正在扫描毕业类型信息：如 高中or 初中；
		S_CLASS,		// 当前正在扫描班级信息：如3班； 
		S_TEACHER,		// 当前正在扫描老师姓名信息； 
		S_STUDENT,		// 当前正在扫描学生姓名，并且是该班级的第一个学生姓名；
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
	
	private void resetSTR()
	{
		this.strGrade= this.strType= this.strClass= this.strTeacher= this.strStudent= "";
		return;
	}
	
	boolean isEmptyChar(char c)
	{
		return ( c==' ' || c==' ' || c=='\t' || c=='\n');
	}
	
	int findNextNonEmptyChar(int startIndex, String line)
	{
		if(line.length()==0 || startIndex<0 || startIndex> line.length()-1)
			return -1;
		
		boolean existNonEmptyChar= false;
		int i= startIndex;
		for(; i<line.length();i++)
			if(this.isEmptyChar(line.charAt(i))==false)
			{
				existNonEmptyChar= true;
				break;
			}
		
		if(existNonEmptyChar)
			return i;
		else
			return -1;
		
	}
	
	int findNextEmptyChar(int startIndex, String line)
	{
		if(line.length()==0 || startIndex<0 || startIndex> line.length()-1)
			return -1;
		
		boolean existEmptyChar= false;
		int i= startIndex;
		for(;i<line.length();i++)
			if(this.isEmptyChar(line.charAt(i))==true)
			{
				existEmptyChar= true;
				break;
			}
		
		if(existEmptyChar==true)
			return i;
		else
			return -1;
	}
	
	
	
	public NO5AlumniDataRead(String configureFilePath)
	{
		BufferedReader cfgReader=null;
		configFile= new File(configureFilePath);
		rs= ReadStatus.S_GRADE; // 默认从届别状态开始；
		
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
	
	void extractInfo(String line, int curLineNum, BufferedWriter bwData, BufferedWriter bwLog)
	{
		// write log on the receiving line;
		try {
			bwLog.write("LINE TO PROCESS: NO."+curLineNum+line+"\t");
			bwLog.newLine();
		}catch (IOException e){  
            System.out.println("Read or write Exceptioned");  
        }
		
		boolean stopIteration= false;
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
				rs= ReadStatus.S_TYPE; // 年级名称扫描完毕，接下来进入毕业类型扫描；
				break;
			case S_TYPE:
				if(line.charAt(i)=='初') strType="初中";
				else if(line.charAt(i)=='高') strType="高中";
			    i+=2; 
			    rs= ReadStatus.S_CLASS; // 毕业类型扫描完毕，接下来进入班级名称扫描；
			    break;
			case S_CLASS:
				strClass= strClass+line.charAt(i)+"班";
				i+=2;
				rs= ReadStatus.S_TEACHER; // 班级名称扫描完毕，接下来进入班主任姓名扫描；
				break;
			case S_TEACHER:
				int iTeacherStart= this.findNextNonEmptyChar(line.indexOf("班主任")+4, line);
				int iTeacherEnd= this.findNextEmptyChar(iTeacherStart, line);
				strTeacher= line.substring(iTeacherStart, iTeacherEnd);
				i= iTeacherEnd+1;
				rs= ReadStatus.S_STUDENT; // 班主任名字扫描完毕，接下来进入该班级学生姓名的扫描；
				break;
			case S_STUDENT:
				int ne= this.findNextNonEmptyChar(i,line);
				if(ne==-1) // this is an empty line;
				{
					stopIteration= true;
					break;
				}
				else
				{
					char nextChar=line.charAt(ne);
					if(nextChar=='2' || nextChar=='3') //  扫描遇到下一个班级了；当前这个行也是下一个班级的新行；要结束上一个班级的数据写入并更新状态结构；
					{
						rs= ReadStatus.S_GRADE;
						i= 0;
						this.resetSTR();
					}
					else // 当前还在扫描当前班级的学生姓名；
					{
						int e= this.findNextEmptyChar(i, line);
						if(e==-1) // the rest of the line is not empty;
						{
							this.strStudent= line.substring(i);
							stopIteration= true;
						}
						else
						{
							this.strStudent= line.substring(i,e);
							i= this.findNextNonEmptyChar(e, line);
						}
						try {
							bwData.write(this.strGrade + "," + this.strType + "," + this.strClass + "," + this.strTeacher + "," + this.strStudent);
							bwData.newLine();
							bwLog.write("Writing record:  "+this.strGrade + "," + this.strType + "," + this.strClass + "," + this.strTeacher + "," + this.strStudent);
							bwLog.newLine();
						}catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}
					break;
				}
			default :
				break;
			}
			if(stopIteration) break;
		}
		
		
		return;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NO5AlumniDataRead cfg_no5= new NO5AlumniDataRead("data/configure.txt");
		
		BufferedReader dataReader=null;
		BufferedWriter dataWriter=null;
		BufferedWriter logWriter=null;
		
		try {
			dataReader= new BufferedReader(new InputStreamReader(new FileInputStream(cfg_no5.dataInfile)));
			dataWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfg_no5.dataOutFile)));
			logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cfg_no5.logFile)));
			
			String line="";
			int lineCount=0;
			//cfg_no5.writeLog("Processing Start...", logWriter);
			logWriter.write("Processing Start...");
			logWriter.newLine();
			
			// 反复读取每一行，逐行提取校友相关信息；
			while((line= dataReader.readLine())!=null)
			{
				cfg_no5.extractInfo(line, lineCount++, dataWriter, logWriter);
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
