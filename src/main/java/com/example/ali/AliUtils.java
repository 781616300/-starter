package com.example.ali;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.green.model.v20180509.ImageSyncScanRequest;
import com.aliyuncs.green.model.v20180509.TextScanRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @Author: xgs
 * Description:
 * @Date: 2022/07/25/17:24
 */

public class AliUtils {

    public final static Logger LOGGER = LoggerFactory.getLogger(AliUtils.class);

    private AliyunResource aliyunResource;

    public AliUtils(AliyunResource aliyunResource) {
        this.aliyunResource = aliyunResource;
    }

    /**
     * @Author xgs
     * @Date 2022/7/26 11:26
     * @Description 图片审核：检测图片是否存在暴力、色情、二维码等
    */
    public boolean reviewImage(String imgUrl) throws Exception {
        LOGGER.info("reviewImage,入参：{}",imgUrl);
        IClientProfile profile = DefaultProfile
                .getProfile("cn-shanghai",aliyunResource.getAccessKeyID(), aliyunResource.getAccessKeySecret());
        DefaultProfile
                .addEndpoint("cn-shanghai", "cn-shanghai", "Green", "green.cn-shanghai.aliyuncs.com");
        IAcsClient client = new DefaultAcsClient(profile);

        ImageSyncScanRequest imageSyncScanRequest = new ImageSyncScanRequest();
        // 指定api返回格式
        imageSyncScanRequest.setAcceptFormat(FormatType.JSON);
        // 指定请求方法
        imageSyncScanRequest.setMethod(MethodType.POST);
        imageSyncScanRequest.setEncoding("utf-8");
        //支持http和https
        imageSyncScanRequest.setProtocol(ProtocolType.HTTP);


        JSONObject httpBody = new JSONObject();
        /**
         * 设置要检测的场景, 计费是按照该处传递的场景进行
         * 一次请求中可以同时检测多张图片，每张图片可以同时检测多个风险场景，计费按照场景计算
         * 例如：检测2张图片，场景传递porn、terrorism，计费会按照2张图片鉴黄，2张图片暴恐检测计算
         * porn: porn表示色情场景检测
         * logo: 商标
         * 其他详见官方文档
         */
        httpBody.put("scenes", aliyunResource.getImgScenarios());

        /**
         * 设置待检测图片， 一张图片一个task
         * 多张图片同时检测时，处理的时间由最后一个处理完的图片决定
         * 通常情况下批量检测的平均rt比单张检测的要长, 一次批量提交的图片数越多，rt被拉长的概率越高
         * 这里以单张图片检测作为示例, 如果是批量图片检测，请自行构建多个task
         */
        JSONObject task = new JSONObject();
        task.put("dataId", UUID.randomUUID().toString());

        //设置图片链接
        task.put("url", imgUrl);
        task.put("time", new Date());
        httpBody.put("tasks", Arrays.asList(task));

        imageSyncScanRequest.setHttpContent(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(httpBody.toJSONString()),
                "UTF-8", FormatType.JSON);

        /**
         * 请设置超时时间, 服务端全链路处理超时时间为10秒，请做相应设置
         * 如果您设置的ReadTimeout小于服务端处理的时间，程序中会获得一个read timeout异常
         */
        imageSyncScanRequest.setConnectTimeout(3000);
        imageSyncScanRequest.setReadTimeout(10000);
        HttpResponse httpResponse = null;
        try {
            httpResponse = client.doAction(imageSyncScanRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //服务端接收到请求，并完成处理返回的结果
        if (httpResponse != null && httpResponse.isSuccess()) {
            JSONObject scrResponse = JSON.parseObject(org.apache.commons.codec.binary.StringUtils.newStringUtf8(httpResponse.getHttpContent()));
            System.out.println(JSON.toJSONString(scrResponse, true));
            int requestCode = scrResponse.getIntValue("code");
            //每一张图片的检测结果
            JSONArray taskResults = scrResponse.getJSONArray("data");
            if (200 == requestCode) {
                for (Object taskResult : taskResults) {
                    //单张图片的处理结果
                    int taskCode = ((JSONObject) taskResult).getIntValue("code");
                    //图片要检测的场景的处理结果, 如果是多个场景，则会有每个场景的结果
                    JSONArray sceneResults = ((JSONObject) taskResult).getJSONArray("results");
                    if (200 == taskCode) {
//                        Object sceneResult = sceneResults.get(0);
                        Boolean result=true;
                        for (Object sceneResult : sceneResults) {
                            String scene = ((JSONObject) sceneResult).getString("scene");
                            String suggestion = ((JSONObject) sceneResult).getString("suggestion");
                            if (!suggestion.equalsIgnoreCase("pass")){
                                result=false;
                            }
                        }
                        return result;
                    } else {
                        //单张图片处理失败, 原因视具体的情况详细分析
                        LOGGER.info("task process fail. task response:" + JSON.toJSONString(taskResult));
                        return false;
                    }
                }
            } else {
                /**
                 * 表明请求整体处理失败，原因视具体的情况详细分析
                 */
                LOGGER.error("the whole image scan request failed. response:" + JSON.toJSONString(scrResponse));
                return false;
            }
        }
        return false;
    }

    public Boolean reviewTextContent(String content) {
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai",
                aliyunResource.getAccessKeyID(),
                aliyunResource.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        TextScanRequest textScanRequest = new TextScanRequest();
        textScanRequest.setAcceptFormat(FormatType.JSON); // 指定api返回格式
        textScanRequest.setHttpContentType(FormatType.JSON);
        textScanRequest.setMethod(com.aliyuncs.http.MethodType.POST); // 指定请求方法
        textScanRequest.setEncoding("UTF-8");
        textScanRequest.setRegionId("cn-shanghai");
        List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
        Map<String, Object> task1 = new LinkedHashMap<String, Object>();
        task1.put("dataId", UUID.randomUUID().toString());
        /**
         * 待检测的文本，长度不超过10000个字符
         */
//        抵制毒品交易
//          尼玛
        task1.put("content", content);
        tasks.add(task1);
        JSONObject data = new JSONObject();

        /**
         * 检测场景，文本垃圾检测传递：antispam
         **/
        data.put("scenes", aliyunResource.getTextScenarios());
        data.put("tasks", tasks);
        System.out.println(JSON.toJSONString(data, true));

        try {
            textScanRequest.setHttpContent(data.toJSONString().getBytes("UTF-8"), "UTF-8", FormatType.JSON);
            // 请务必设置超时时间
            textScanRequest.setConnectTimeout(3000);
            textScanRequest.setReadTimeout(6000);

            HttpResponse httpResponse = client.doAction(textScanRequest);
            if(httpResponse.isSuccess()){
                JSONObject scrResponse = JSON.parseObject(new String(httpResponse.getHttpContent(), "UTF-8"));
                System.out.println(JSON.toJSONString(scrResponse, true));
                if (200 == scrResponse.getInteger("code")) {
                    JSONArray taskResults = scrResponse.getJSONArray("data");
                    for (Object taskResult : taskResults) {
                        if(200 == ((JSONObject)taskResult).getInteger("code")){
                            JSONArray sceneResults = ((JSONObject)taskResult).getJSONArray("results");
//                            JSONObject sceneResult = (JSONObject)sceneResults.get(0);
                            Boolean result=true;
                            for (Object sceneResult : sceneResults) {
                                JSONObject sceneResultJson = (JSONObject) sceneResult;
                                String scene = sceneResultJson.getString("scene");
                                String suggestion = sceneResultJson.getString("suggestion");
                                //根据scene和suggetion做相关处理
                                //suggestion == pass 未命中垃圾  suggestion == block 命中了垃圾，可以通过label字段查看命中的垃圾分类
                                if (!Objects.equals("pass",suggestion)){
                                    result=false;
                                }


                            //                            suggestion=pass：文本正常，文章状态改为发布通过
                            //                            review：需要人工审核，需要在后台管理系统中进行人工审核（很多自媒体平台都会采用机审+人工审的方式）
                            //                            block：文本违规，可以直接删除或者做限制处理，审核不通过
                            }
                            return result;
                        }else{
                            LOGGER.info("task process fail:" + ((JSONObject)taskResult).getInteger("code"));
                            return false;
                        }
                    }
                } else {
                    LOGGER.info("detect not success. code:" + scrResponse.getInteger("code"));
                    return false;
                }
            }else{
                LOGGER.error("response not success. status:" + httpResponse.getStatus());
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }


}
