import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Client {

    public static void main(String[] args) throws IOException {
        // 创建HttpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // Get请求
        HttpGet httpGet = new HttpGet("http://localhost:8088");
        // 执行请求
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        // 获取结果
        HttpEntity entity = httpResponse.getEntity();
        // 将结果转为String并打印
        System.out.println(EntityUtils.toString(entity));
        // close
        httpResponse.close();
        // close
        httpClient.close();
    }

}
