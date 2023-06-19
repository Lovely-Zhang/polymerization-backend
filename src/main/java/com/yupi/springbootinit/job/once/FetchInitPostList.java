package com.yupi.springbootinit.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.springbootinit.esdao.PostEsDao;
import com.yupi.springbootinit.model.entity.Post;
import com.yupi.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 获得初始化帖子列表成功
 *
 * @author zhf
 */
// 取消注释后每次启动springboot项目的时候会 执行一次run方法
//@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;

    @Override
    public void run(String... args) {
        // 1.获取数据
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"reviewStatus\":1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result2 = HttpRequest.post(url)
                .body(json)
                .execute()
                .body();
        System.out.println(result2);

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
                    if (b) {
                        log.info("获得初始化帖子列表成功， 条目 = {}", list.size());
                    } else {
                        log.error("获取初始化帖子列表失败");
                    }
                }
            }
        }
    }
}
