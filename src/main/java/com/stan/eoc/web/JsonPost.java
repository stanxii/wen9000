package com.stan.eoc.web;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonPost {
	
	public static JSONObject post(String url, JSONObject json) {
		// String targethost=cbatip;
		// int targetport=80;
		HttpClient client = new DefaultHttpClient();

		// ((AbstractHttpClient)
		// client).getCredentialsProvider().setCredentials(
		// new AuthScope(targethost, targetport),
		// new UsernamePasswordCredentials("support", "support"));

		HttpPost post = new HttpPost(url);
		JSONObject response = new JSONObject();
		try {
			StringEntity s = new StringEntity(json.toString());

			s.setContentEncoding("UTF-8");
			s.setContentType("text/json");
			post.setEntity(s);
			HttpResponse res = client.execute(post);

			// System.out.println(((HttpResponse) res).getStatusLine());

			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = res.getEntity();

				// String charset = EntityUtils.getContentCharSet(entity);

				// EntityUtils.consume(entity);
				// System.out.println("----------------------------------Content->>>>>"
				// +EntityUtils.toString(res.getEntity()));
				response = (JSONObject) JSONValue.parse(EntityUtils
						.toString(res.getEntity()));
				System.out
						.println("----------------------------------response->>>>>"
								+ response.toJSONString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("status", "1");
			// throw new RuntimeException(e);
		} finally {
			client.getConnectionManager().shutdown();
		}

		return response;
	}
}
