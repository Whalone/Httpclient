import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchWeibo {
    private String getHTML(String url) throws URISyntaxException, ClientProtocolException,
            IOException {

        //在这里设置了用户Cookie策略，屏蔽掉cookierejected的报错。
        CookieSpecProvider cookieSpecProvider = new CookieSpecProvider() {
            @Override
            public CookieSpec create(HttpContext httpContext) {
                return new BrowserCompatSpec(){
                    public void validate(Cookie cookie, CookieOrigin origin)
                            throws MalformedCookieException{

                    }
                };
            }
        };
        Registry<CookieSpecProvider> r = RegistryBuilder
                .<CookieSpecProvider> create()
                .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
                .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
                .register("cookie", cookieSpecProvider)
                .build();

        /*设置socket超时socketTimeout和连接超时connectTimeout，这很关键，
         如果不设置的话，当网络不好的情况下，某次请求没有及时得到响应，程序可能会卡死。
        但是设置连接超时，超时之后再自动重连就可以避免这个问题。*/
        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec("cookie")
                .setSocketTimeout(5000) //设置socket超时时间
                .setConnectTimeout(5000)  //设置connect超时时间
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieSpecRegistry(r)
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        String html = "html获取失败";//用于验证是否渠道正常的html
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);
            html = EntityUtils.toString(response.getEntity());
        }catch (IOException e){
            e.printStackTrace();
        }
        return html;
    }

    private boolean isExistHTML(String html) throws InterruptedException{
        boolean isExist = false;
        //没有找到相关的微博呢，换个关键词试试吧！（html页面上的信息）
        Pattern pNoResult = Pattern.compile("\\\\u6ca1\\\\u6709\\\\u627e\\\\u5230\\\\u76f8"
                + "\\\\u5173\\\\u7684\\\\u5fae\\\\u535a\\\\u5462\\\\uff0c\\\\u6362\\\\u4e2a"
                + "\\\\u5173\\\\u952e\\\\u8bcd\\\\u8bd5\\\\u5427\\\\uff01");
        Matcher mNoResult = pNoResult.matcher(html);
        if(!mNoResult.find()){
            isExist = true;
        }
        return isExist;
    }

    private void writeWeibo2txt(String html,String savePath) throws IOException{
        File htmltxt = new File(savePath); //新建一个txt文件用于存放爬取的结果信息
        FileWriter fw = new FileWriter(htmltxt);
        BufferedWriter bw = new BufferedWriter(fw);

        /*Pattern p = Pattern.compile("\"id\":\\s\"\\d{19}\",(\\n*?)|(\\s*?)\"content\":\\s\".*?\",(\\n*?)|(\\s*?)\"prettyTime\":\\s\".*?\"");
        Matcher m = p.matcher(html);*/
        bw.write(html);
        bw.close();

    }

    public static void main(String[] args) throws IOException,URISyntaxException,InterruptedException{
        SearchWeibo crawler = new SearchWeibo();
        String serachword = "iPad";//搜素关键字"iPad"的微博HTML页面
        String html = crawler.getHTML("http://s.weibo.com/weibo/"+serachword);
        String savePath = "e:/weibo/html.txt";
        if(html!="htmlh获取失败"){
            if(crawler.isExistHTML(html)){
                System.out.println(html);
                System.out.println("11111111111111111111111111");
                crawler.writeWeibo2txt(html,savePath);
            }
        }
    }
}
