import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class Test {
	public static void main(String [] args) throws Exception, FileNotFoundException{
		String path = "D://app//m2//";
		String fileName = "088_" + System.currentTimeMillis() + "_001.xml";
		File file = new File(path+fileName);
		
		OutputStreamWriter  out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        // Ð´ÎÄ¼þ
        out.write("sssdf");
        out.flush();
        System.out.println("ddddddddd");
		
	}
}
