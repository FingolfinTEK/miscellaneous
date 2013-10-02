package com.fingy.pelephone.scrape.util;

import com.fingy.io.IOHelper;
import com.fingy.scrape.util.HttpClientParserUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.TruncatedChunkException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Map;

public class HttpPostHelper {

    public static String postDataToUrlWithCookies(String searchUrl, String data, Map<String, String> cookies) throws IOException {
        HttpPost post = new HttpPost(searchUrl);
        post.setHeader("User-Agent", HttpClientParserUtil.USER_AGENT);
        post.setEntity(new StringEntity(data, ContentType.create("application/json", "UTF-8")));

        DefaultHttpClient client = HttpClientParserUtil.getClient();
        HttpClientParserUtil.addCookies(client, cookies);
        HttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();
        final byte[] content = IOHelper.readContent(entity.getContent(), TruncatedChunkException.class);
        return new String(content, "windows-1255");
    }

}
