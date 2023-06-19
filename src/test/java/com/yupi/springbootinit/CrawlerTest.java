package com.yupi.springbootinit;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest {

    @Autowired
    PostService postService;

    @Test
    void testFetchPicture() throws IOException {
        int first = 1;
        String url = String.format("https://cn.bing.com/images/search?q=奥特曼图片&go=搜索&qs=ds&form=QBIR&first=%s", first);
        // 拿到页面代码数据
        Document doc = Jsoup.connect(url).get();
        // 获取到具体值
//        Elements elements = doc.select(".dgControl.waterfall.hover");
        Elements elements = doc.select(".iuscp.isv");
//        Elements elements = doc.select(".iuscp.varhvarh.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取到图片所在的json字符串, 再转化为对象取出来具体数据
            String m = element.select(".iusc").get(0).attr("m");
            Map map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");
            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");
            Picture picture = new Picture();
            picture.setTitle(murl);
            picture.setUrl(title);
            pictures.add(picture);
        }
        System.out.println("pictures = " + pictures);
    }

    @Test
    void testFetchPassage() {
        // 1.获取数据
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result2 = HttpRequest.post(url)
                .body(json)
                .execute()
                .body();

        // 2.json 转对象
        Map<String, Object> map = JSONUtil.toBean(result2, Map.class);
        System.out.println("map = " + map);
        Integer code = (Integer) map.get("code");
        List<Post> list = new ArrayList<>();
        if (code == 0) {
            JSONObject data = (JSONObject) map.get("data");
            if (data != null) {
                JSONArray records = (JSONArray) data.get("records");
                if (records != null) {
                    records.forEach(item -> {
                        JSONObject tempRecords = (JSONObject) item;
                        Post post = new Post();
                        // 获取到每项内容
                        post.setTitle(tempRecords.getStr("title"));
                        post.setContent(tempRecords.getStr("content"));
                        JSONArray tags = (JSONArray) tempRecords.get("tags");
                        post.setTags(JSONUtil.toJsonStr(tags));
                        post.setUserId(1L);
                        post.setCreateTime(new Date());
                        post.setUpdateTime(new Date());
                        list.add(post);
                    });
                    // 3.数据入库
                    boolean b = postService.saveBatch(list);
                    Assertions.assertTrue(b);
                }
            }
        }
    }


}
