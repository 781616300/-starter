# 自定义spring-boot-starter

#### 介绍
自定义starter，注入AliUtils，配置好id、秘钥、检测场景。可直接使用阿里云内容检测、图片检测功能


#### 使用说明

1.clone项目,install打成jar包

2.pom文件引入jar包

```
  <dependency>
            <groupId>com.example</groupId>
            <artifactId>ali-spring-boot-starter</artifactId>
            <version>0.0.1-SNAPSHOT</version>
  </dependency>
```
3.配置key、秘钥、检测场景

![输入图片说明](https://images.gitee.com/uploads/images/2022/0726/114647_a7e1ede4_10259873.png "屏幕截图.png")

4.使用

```
    @Autowired
    private AliUtils aliUtils;

    @RequestMapping("/ali")
    public String ali() throws Exception {
        boolean b = aliUtils.reviewImage("https://campus-server.oss-cn-hangzhou.aliyuncs.com/system/18.png");
        if (b){
            return "图片审核通过";
        }
        return "图片审核失败，图片存在暴力";
    }

```


# -starter
