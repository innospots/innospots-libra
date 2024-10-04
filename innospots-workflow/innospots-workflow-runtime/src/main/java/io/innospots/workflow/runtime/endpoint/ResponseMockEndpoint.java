package io.innospots.workflow.runtime.endpoint;

import cn.hutool.core.util.RandomUtil;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.response.*;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.datafaker.Faker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/4
 */
@Tag(name = "response mock")
@RestController
@RequestMapping(PathConstant.ROOT_PATH + "workflow/mock/response")
public class ResponseMockEndpoint {

    private RestClient restClient = RestClient.builder().build();

    private List<Map<String,Object>> history;

    @PostMapping("payload")
    WorkflowResponse<List<RespPayload>> cardResponse() {
        Faker faker = new Faker(Locale.CHINESE);
        faker.construction().materials();
        List<RespPayload> cl = new ArrayList<>();
        RespPayload c1 = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(2)
                .layout(CardLayout.GRID)
                .icon("/node/http-api.svg")
                .label("综合")
                .build();
        Map<String,Object> data = movie();
        c1.addRow(text("标题：" + data.get("mov_title"),
                data.get("mov_intro").toString(), data.get("mov_text").toString()));
        c1.addRow(text("标题：" + data.get("mov_title"),
                data.get("mov_intro").toString(), data.get("mov_text").toString()));
        c1.addRow(normal("自定义卡片：" + faker.appliance().brand(),
                "<br>"+faker.construction().materials()+"</br>", data.get("mov_text").toString()));
        c1.addRow(image("图片6",
                data.get("mov_pic").toString(), "图片描述"));
        c1.addRow(video("视频6", "https://sfile.chatglm.cn/testpath/video/cb850ab9-fe6b-5745-9eeb-2895d08eb9a3_0.mp4", "视频描述"));
        c1.addRow(markdown("markdown1", "# 标题\n ```核心标记```, **是加粗**", "简介内容"));
        c1.addRow(embed((String) data.get("mov_title"), "//player.bilibili.com/player.html?isOutside=true&aid=236534017&bvid=BV1xe411Z7oj&cid=1349699744&p=1", "嵌入卡片描述"));
        c1.addRow(file("文件1", "https://sfile.chatglm.cn/testpath/file/cb850ab9-fe6b-5745-9eeb-2895d08eb9a3_0.mp4", "文件描述"));
        cl.add(c1);

        //图片
        RespPayload images = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(3)
                .layout(CardLayout.GRID)
                .icon("/node/http-api.svg")
                .label("图片")
                .build();
        images.setRows(bingImages());
        cl.add(images);

        RespPayload videos = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(2)
                .layout(CardLayout.GRID)
                .icon("/node/http-api.svg")
                .label("视频")
                .build();
        videos.setRows(videoCards());
        cl.add(videos);

        RespPayload markdowns = RespPayload.builder()
                .align(CardAlign.CENTER)
                .column(1)
                .layout(CardLayout.PAGE)
                .icon("/node/http-api.svg")
                .label("markdown")
                .build();
        markdowns.setRows(markdowns());
        cl.add(markdowns);

        RespPayload files = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(2)
                .layout(CardLayout.GRID)
                .icon("/node/http-api.svg")
                .label("文件")
                .build();
        files.setRows(fileCards());
        cl.add(files);

        RespPayload chats = RespPayload.builder()
                .align(CardAlign.CENTER)
                .layout(CardLayout.CHAT)
                .icon("/node/http-api.svg")
                .label("聊天")
                .build();
        chats.setRows(chatCards());
        cl.add(chats);

        RespPayload tables = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(1)
                .layout(CardLayout.TABLE)
                .icon("/node/http-api.svg")
                .label("表格")
                .build();
        tables.setRows(tables());
        cl.add(tables);

        RespPayload embeds = RespPayload.builder()
                .align(CardAlign.LEFT)
                .column(2)
                .layout(CardLayout.GRID)
                .icon("/node/http-api.svg")
                .label("嵌入")
                .build();
        embeds.setRows(emBeds());
        cl.add(embeds);


        WorkflowResponse<List<RespPayload>> wf = new WorkflowResponse<>();
        wf.fillBody(cl);
        history.clear();
        history = null;
        return wf;
    }

    private List<ViewCard> emBeds(){
        List<ViewCard> cards = new ArrayList<>();
        cards.add(embed("嵌入链接展示1", "//player.bilibili.com/player.html?isOutside=true&aid=236534017&bvid=BV1xe411Z7oj&cid=1349699744&p=1", "嵌入卡片描述"));
        cards.add(embed("嵌入链接展示2", "//player.bilibili.com/player.html?isOutside=true&aid=113192923631355&bvid=BV1LjsvejEvj&cid=25988042137&p=1", "嵌入卡片描述"));
        return cards;
    }

    private List<ViewCard> tables(){
        List<ViewCard> cards = new ArrayList<>();
        Faker faker = new Faker(Locale.CHINA);

        PageBody pageBody = new PageBody();
        List<ParamField> cols = new ArrayList<>();
        cols.add(new ParamField("姓名", "name", FieldValueType.STRING));
        cols.add(new ParamField("年龄", "age", FieldValueType.INTEGER));
        cols.add(new ParamField("职业", "title", FieldValueType.INTEGER));
        cols.add(new ParamField("地址", "address", FieldValueType.STRING));
        cols.add(new ParamField("邮箱", "email", FieldValueType.STRING));
        cols.add(new ParamField("手机号", "phone", FieldValueType.STRING));
        cols.add(new ParamField("公司", "company", FieldValueType.STRING));
        cols.add(new ParamField("生日", "birthday", FieldValueType.DATE));
        cols.add(new ParamField("身份证号", "idNumber", FieldValueType.STRING));

        pageBody.setSchemaFields(cols);
        for (int i = 0; i < 10; i++) {
            Map<String,Object> item = new HashMap<>();
            item.put("name",faker.name().fullName());
            item.put("age",faker.number().numberBetween(18, 60));
            item.put("title",faker.job().title());
            item.put("address",faker.address().fullAddress());
            item.put("email",faker.internet().emailAddress());
            item.put("phone",faker.phoneNumber().cellPhone());
            item.put("company",faker.company().name());
            item.put("birthday",faker.date().birthday("yyyy-MM-dd"));
            item.put("idNumber",faker.idNumber().validEnZaSsn());
            pageBody.add(item);
        }

        pageBody.setPageSize(10L);
        pageBody.setTotal(10L);
        pageBody.setCurrent(1L);
        ViewCard vc = table("表格", pageBody, "表格描述");
        cards.add(vc);

        return cards;
    }

    private List<ViewCard> markdowns(){
        List<ViewCard> cards = new ArrayList<>();
        Faker faker = new Faker(Locale.CHINA);
        StringBuilder content = new StringBuilder();
        content.append("# 标题的内容");
        content.append("1. **基本信息**\n" +
                "   - 姓名：[你的名字]\n" +
                "   - 年龄：[你的年龄]\n" +
                "   - 出生地/现居地：[你的出生地或现在居住的城市]\n" +
                "\n" +
                "2. **教育背景**\n" +
                "   - 最高学历：[你的最高学历，例如“本科”、“硕士”等]\n" +
                "   - 毕业院校及专业：[你毕业的学校名称以及所学专业]\n" +
                "   - 相关课程/成就：[你在校期间学习的相关课程或者取得的重要成就]");
        cards.add(markdown("标题：markdown", content.toString(),faker.text().text(150)));

        return cards;
    }

    private List<ViewCard> videoCards() {
        List<ViewCard> cards = new ArrayList<>();
        cards.add(video("视频1", "https://sfile.chatglm.cn/testpath/video/1aa1515f-1193-58bf-83d7-3bd62b765f62_0.mp4", "视频描述"));
        cards.add(video("视频2", "https://sfile.chatglm.cn/testpath/video/61cf829a-0741-56d7-89b6-0c5ae4e51287_0.mp4", "视频描述"));
        cards.add(video("视频3", "https://sfile.chatglm.cn/testpath/video/cb850ab9-fe6b-5745-9eeb-2895d08eb9a3_0.mp4", "视频描述"));
        cards.add(video("视频4", "https://sfile.chatglm.cn/testpath/video/1aa1515f-1193-58bf-83d7-3bd62b765f62_0.mp4", "视频描述"));
        cards.add(video("视频5", "https://sfile.chatglm.cn/testpath/video/61cf829a-0741-56d7-89b6-0c5ae4e51287_0.mp4", "视频描述"));
        cards.add(video("视频6", "https://sfile.chatglm.cn/testpath/video/cb850ab9-fe6b-5745-9eeb-2895d08eb9a3_0.mp4", "视频描述"));
        return cards;
    }

    private List<ViewCard> bingImages(){
        List<Map<String,Object>> imList = images();
        List<ViewCard> cards = new ArrayList<>();
        for (Map<String, Object> map : imList) {
            ViewCard vc = image(String.valueOf(map.get("title")), String.valueOf(map.get("url")), String.valueOf(map.get("copyright")));
            vc.setPreviewUrl(String.valueOf(map.get("copyrightlink")));
            cards.add(vc);
        }

        return cards;
    }

    private List<ViewCard> imageCards() {
        List<ViewCard> cards = new ArrayList<>();
        cards.add(image("图片1", "https://www.ghumindiaghum.com/blog/wp-content/uploads/2020/12/dubaitour.jpg", "图片描述"));
        cards.add(image("图片2", "https://pic4.zhimg.com/v2-668f4a23ea5a3feaf7e879782f72f569_720w.jpg?source=172ae18b", "图片描述"));
        cards.add(image("图片3", "https://ts1.cn.mm.bing.net/th/id/R-C.bd761f3d24ed5d5b94bf9406bd0595c4?rik=CgCy16TQXKpWRQ&riu=http%3a%2f%2fpic.ntimg.cn%2f20130606%2f2531170_104007297000_2.jpg&ehk=dOJc%2fEY4AUn1YOZ4YxxE4m5eCr0Ix8CNRAvfbNNMLPU%3d&risl=&pid=ImgRaw&r=0", "图片描述"));
        cards.add(image("图片4", "https://www.ghumindiaghum.com/blog/wp-content/uploads/2020/12/dubaitour.jpg", "图片描述"));
        cards.add(image("图片5", "https://pic4.zhimg.com/v2-668f4a23ea5a3feaf7e879782f72f569_720w.jpg?source=172ae18b", "图片描述"));
        cards.add(image("图片6", "https://ts1.cn.mm.bing.net/th/id/R-C.bd761f3d24ed5d5b94bf9406bd0595c4?rik=CgCy16TQXKpWRQ&riu=http%3a%2f%2fpic.ntimg.cn%2f20130606%2f2531170_104007297000_2.jpg&ehk=dOJc%2fEY4AUn1YOZ4YxxE4m5eCr0Ix8CNRAvfbNNMLPU%3d&risl=&pid=ImgRaw&r=0", "图片描述"));
        return cards;
    }

    private List<ViewCard> fileCards(){
        List<ViewCard> cards = new ArrayList<>();
        cards.add(file("文件1", "https://mdpi-res.com/d_attachment/energies/energies-15-07288/article_deploy/energies-15-07288-v2.pdf?version=1665288575", "文件描述"));
        cards.add(file("文件2", "https://mdpi-res.com/d_attachment/energies/energies-15-07288/article_deploy/energies-15-07288-v2.pdf?version=1665288575", "文件描述"));
        cards.add(file("文件3", "https://mdpi-res.com/d_attachment/energies/energies-15-07288/article_deploy/energies-15-07288-v2.pdf?version=1665288575", "文件描述"));
        cards.add(file("文件4", "https://mdpi-res.com/d_attachment/energies/energies-15-07288/article_deploy/energies-15-07288-v2.pdf?version=1665288575", "文件描述"));

        return cards;
    }

    private List<ViewCard> chatCards(){
        List<Map<String,Object>> avatars = (List<Map<String, Object>>) avatars().get(0).get("list");
        Faker faker = new Faker(Locale.CHINA);
        history = history();
        Map<String,Object> h = history.get(0);
        List<ViewCard> cards = new ArrayList<>();
        int p =0;
        ViewCard vc = text("标题："+h.get("title"), (String) h.get("desc"), "聊天描述",faker.name().fullName(),faker.number().digit());
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        cards.add(vc);
        vc = text("标题:"+faker.appliance().brand(), faker.country().capital(), "聊天描述",faker.name().fullName(),faker.number().digit());
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        cards.add(vc);
        vc = text("标题:"+faker.appliance().brand(), faker.address().fullAddress(), "聊天描述",faker.name().fullName(),faker.number().digit());
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        cards.add(vc);
        vc = image("图片结果", "https://ts1.cn.mm.bing.net/th/id/R-C.bd761f3d24ed5d5b94bf9406bd0595c4?rik=CgCy16TQXKpWRQ&riu=http%3a%2f%2fpic.ntimg.cn%2f20130606%2f2531170_104007297000_2.jpg&ehk=dOJc%2fEY4AUn1YOZ4YxxE4m5eCr0Ix8CNRAvfbNNMLPU%3d&risl=&pid=ImgRaw&r=0", "图片描述");
        vc.setUserId(faker.number().digit());
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        cards.add(vc);

        vc = file("文件结果", "https://mdpi-res.com/d_attachment/energies/energies-15-07288/article_deploy/energies-15-07288-v2.pdf?version=1665288575", "文件描述");
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        vc.setUserId(faker.number().digit());
        cards.add(vc);
        vc = video("视频结果", "https://sfile.chatglm.cn/testpath/video/1aa1515f-1193-58bf-83d7-3bd62b765f62_0.mp4", "视频描述");
        vc.setIcon((String) avatars.get(p).get("icon"));
        vc.setUserName((String) avatars.get(p++).get("name"));
        vc.setUserId(faker.number().digit());
        cards.add(vc);

        return cards;
    }


    private ViewCard text(String title, String text, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.text)
                .description(description)
                .data(text)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }
    private ViewCard text(String title, String text, String description,String userName,String userId) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.text)
                .description(description)
                .data(text)
                .userId(userId)
                .userName(userName)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard markdown(String title, String text, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.markdown)
                .description(description)
                .data(text)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard image(String title, String src, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.image)
                .description(description)
                .src(src)
                .downloadUrl(src)
                .previewUrl(src)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard video(String title, String src, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.video)
                .description(description)
                .src(src)
                .previewUrl(src)
                .downloadUrl(src)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard chart(String title, Map<String, Object> data, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.chart)
                .description(description)
                .data(data)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard file(String title, String src, String description) {
        return file(title,src,null,description);
    }


    private ViewCard file(String title, String src,String icon, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.file)
                .description(description)
                .icon(icon)
                .src(src)
                .previewUrl(src)
                .downloadUrl(src)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard embed(String title, String src, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.embed)
                .description(description)
                .src(src)
                .previewUrl(src)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private ViewCard table(String title, PageBody pageBody, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.table)
                .description(description)
                .data(pageBody)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }


    private ViewCard normal(String title, Object data, String description) {
        return ViewCard.builder()
                .title(title)
                .viewType(CardViewType.normal)
                .description(description)
                .data(data)
                .viewId(RandomUtil.randomNumbers(16))
                .build();
    }

    private List<Map<String,Object>> history(){
        if(history!=null){
            return history;
        }
        history = (List<Map<String, Object>>) restClient.get().uri("https://api.oioweb.cn/api/common/history")
                .retrieve()
                .body(Map.class).get("result");
        return history;
    }

    private List<Map<String,Object>> images(){
        return (List<Map<String, Object>>) restClient.get().uri("https://api.oioweb.cn/api/bing")
                .retrieve()
                .body(Map.class).get("result");
    }

    private Map<String,Object> movie(){
        return (Map<String, Object>) restClient.get().uri("https://api.oioweb.cn/api/common/OneFilm")
                .retrieve()
                .body(Map.class).get("result");
    }

    private List<Map<String,Object>> avatars(){
        return (List<Map<String,Object>>) restClient.get().uri("https://api.oioweb.cn/api/picture/miyoushe_avatar")
                .retrieve()
                .body(Map.class).get("result");
    }


}
