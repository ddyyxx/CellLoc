package tong.map.preprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.defcons.MyCons;

/**
 * �����OpenStreetMap���ص�ԭʼ���ݣ�����ȡ���������Ϊtext�ļ�
 * @author Administrator
 *
 */
public class OpenStreetMap {
    static String s = null;
	//ʵ���Լ��Ľ�����ʽ
	static class MySAXHandler extends DefaultHandler {

		FileOutputStream fosPoint;
		FileOutputStream fosArc;
		OutputStreamWriter oswPoint;
		OutputStreamWriter oswArc;
		BufferedWriter bwPoint;
		BufferedWriter bwArc;
	    //��ʼ����ʱ����
	    public void startDocument() throws SAXException {
			// ����Ϣ
			File pointFile = new File(MyCons.MongoDataDir+"MapPre\\Point.txt");
			// ����Ϣ
			File arcFile = new File(MyCons.MongoDataDir+"MapPre\\Arc.txt");
			try {
				fosPoint = new FileOutputStream(pointFile);
				fosArc = new FileOutputStream(arcFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			oswPoint = new OutputStreamWriter(fosPoint);
			oswArc = new OutputStreamWriter(fosArc);
			bwPoint = new BufferedWriter(oswPoint);
			bwArc = new BufferedWriter(oswArc);
	        System.out.println("��ʼ�����ĵ���");
	    }

	    //��ɽ���ʱ����
	    public void endDocument() throws SAXException {
	       try {
			bwPoint.close();
			   oswPoint.close();
			   fosPoint.close();
			   bwArc.close();
			   oswArc.close();
			   fosArc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        System.out.println("�ĵ�������ɣ�");
	    }

	    /**
	     * ��ʼһ��Ԫ�صĽ���
	     * @param uri
	     * @param localName
	     * @param qName ��ǩ��
	     * @param attributes ����
	     * @throws SAXException
	     */
	    public void startElement(String uri, String localName, String qName,
	            Attributes attributes) throws SAXException {
	        if ((attributes != null)&&attributes.getLength() > 0) {
	        	if(qName.equals("node")){
	        		StringBuilder sb = new StringBuilder();
	        		sb.append(attributes.getValue("id")+"      ");
	        		sb.append(attributes.getValue("lat")+"      ");
	        		sb.append(attributes.getValue("lon"));
	                try {
						bwPoint.write(sb.toString()+"\r\n");
						bwPoint.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                System.out.println(sb.toString());
	        	}else if(qName.equals("way")){
	            	s = attributes.getValue("id")+"      "
	            			+attributes.getValue("version")+"      ";
	        	}else if(qName.equals("nd")){
	        		if(s==null)
	        			return;
	        		try {
						bwArc.write(s+attributes.getValue("ref")+"      \r\n");
						bwArc.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		System.out.println(s+attributes.getValue("ref")+"      ");
	        	}else if(qName.equals("tag")){
	        		if(s==null)
	        			return;
	        		try {
						bwArc.write(s+"      "+attributes.getValue("k")+"\r\n");
						bwArc.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		System.out.println(s+"      "+attributes.getValue("k"));
	        	}else if(qName.equals("relation")){
	        		if(s!=null)
	        			s = null;
	        	}
	        }
	    }

	    /**
	     * ����һ��Ԫ�صĽ���,����������ǩʱ���ô˷��� ͨ���ڴ˷����Ա�ǩȡֵ������
	     * @param uri
	     * @param localName
	     * @param qName
	     * @throws SAXException
	     */
	    public void endElement(String uri, String localName, String qName)
	            throws SAXException {
	    	
	    }

	    //�÷����ǻ��Ԫ�ؼ��text�ı����ݣ�����ͨ��new String(ch, start, length)�����
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
//	        System.out.print(new String(ch, start, length));
	    }
	}
		 
	public static void main(String[] args) {
		SAXParserFactory saxfac = SAXParserFactory.newInstance();
		try {
			SAXParser saxparser = saxfac.newSAXParser();
			InputStream is = new FileInputStream(MyCons.NewMapDir);
			MySAXHandler handler = new MySAXHandler();
			saxparser.parse(is, handler);;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("���!");
   }
}
