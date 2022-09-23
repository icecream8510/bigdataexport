package com.boco.eoms.base.poiutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class SendRequestUtil {
	public  static String doPost(String doUrl, Map<String, String> params) {
		String result = "";
		try {
			HttpClient client = new HttpClient();
	        PostMethod post = new PostMethod(doUrl);
	        
	        List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
	        for (String key : params.keySet()) {
	        	nameValuePairList.add(new NameValuePair(key,params.get(key)));
	        }
	        NameValuePair[] nameValuePairArray = new NameValuePair[nameValuePairList.size()];
	        nameValuePairList.toArray(nameValuePairArray);
	
	        post.setRequestBody(nameValuePairArray);
	        post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
	        int status = client.executeMethod(post);
	        result = post.getResponseBodyAsString() != null ? post.getResponseBodyAsString().trim() : "";
//	        System.out.println("result:"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
