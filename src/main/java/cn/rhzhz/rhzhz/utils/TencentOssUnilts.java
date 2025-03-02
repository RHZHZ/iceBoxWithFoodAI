package cn.rhzhz.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Policy;
import com.tencent.cloud.Response;
import com.tencent.cloud.Statement;
import com.tencent.cloud.cos.util.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TencentOssUnilts {

    @Value("${tencent.oss.SecretId}")
    private String SECRET_ID;

    @Value("${tencent.oss.appid}")
    private String APPID;

    @Value("${tencent.oss.SecretKey}")
    private String SECRET_KEY;

    @Value("${tencent.oss.bucket}")
    private String BUCKET_NAME;

    @Value("${tencent.oss.region}")
    private String REGION;

    @Value("${tencent.oss.URL}")
    private String URL;

    private static final Logger logger = LoggerFactory.getLogger(TencentOssUnilts.class);


    private Map<String,String> getTempKey(){

        TreeMap<String, Object> config = new TreeMap<String, Object>();
        Map<String,String> map = new HashMap<>();
        try {
            // 替换为您的云 api 密钥 SecretId
            config.put("secretId", SECRET_ID);
            // 替换为您的云 api 密钥 SecretKey
            config.put("secretKey", SECRET_KEY);
            logger.info("id:{},KEY{}",SECRET_ID,SECRET_KEY);
            // 初始化 policy
            Policy policy = new Policy();

            // 设置域名:
            // 如果您使用了腾讯云 cvm，可以设置内部域名
            //config.put("host", "sts.internal.tencentcloudapi.com");

            // 临时密钥有效时长，单位是秒，默认 1800 秒，目前主账号最长 2 小时（即 7200 秒），子账号最长 36 小时（即 129600）秒
            config.put("durationSeconds", 1800);
            // 换成您的 bucket
            config.put("bucket", BUCKET_NAME);
            // 换成 bucket 所在地区
            config.put("region", REGION);

            // 开始构建一条 statement
            Statement statement = new Statement();
            // 声明设置的结果是允许操作
            statement.setEffect("allow");
            /**
             * 密钥的权限列表。必须在这里指定本次临时密钥所需要的权限。
             * 权限列表请参见 https://cloud.tencent.com/document/product/436/31923
             * 规则为 {project}:{interfaceName}
             * project : 产品缩写  cos相关授权为值为cos,数据万象(数据处理)相关授权值为ci
             * 授权所有接口用*表示，例如 cos:*,ci:*
             * 添加一批操作权限 :
             */
            statement.addActions(new String[]{
                    "cos:PutObject",
                    // 表单上传、小程序上传
                    "cos:PostObject",
                    // 分块上传
                    "cos:InitiateMultipartUpload",
                    "cos:ListMultipartUploads",
                    "cos:ListParts",
                    "cos:UploadPart",
                    "cos:CompleteMultipartUpload",
                    // 处理相关接口一般为数据万象产品 权限中以ci开头
                    // 创建媒体处理任务
                    "ci:CreateMediaJobs",
                    // 文件压缩
                    "ci:CreateFileProcessJobs"
            });

            /**
             * 这里改成允许的路径前缀，可以根据自己网站的用户登录态判断允许上传的具体路径
             * 资源表达式规则分对象存储(cos)和数据万象(ci)两种
             * 数据处理、审核相关接口需要授予ci资源权限
             *  cos : qcs::cos:{region}:uid/{appid}:{bucket}/{path}
             *  ci  : qcs::ci:{region}:uid/{appid}:bucket/{bucket}/{path}
             * 列举几种典型的{path}授权场景：
             * 1、允许访问所有对象："*"
             * 2、允许访问指定的对象："a/a1.txt", "b/b1.txt"
             * 3、允许访问指定前缀的对象："a*", "a/*", "b/*"
             *  如果填写了“*”，将允许用户访问所有资源；除非业务需要，否则请按照最小权限原则授予用户相应的访问权限范围。
             *
             * 示例：授权examplebucket-1250000000 bucket目录下的所有资源给cos和ci 授权两条Resource
             */
            statement.addResources(new String[]{
                    // 使用配置中的BUCKET_NAME和APPID变量，避免硬编码
                    "qcs::cos:" + REGION + ":uid/" + APPID + ":" + BUCKET_NAME + "/*",
                    "qcs::ci:" + REGION + ":uid/" + APPID + ":bucket/" + BUCKET_NAME + "/*"
            });
            // 把一条 statement 添加到 policy
            // 可以添加多条
            policy.addStatement(statement);
            // 将 Policy 示例转化成 String，可以使用任何 json 转化方式，这里是本 SDK 自带的推荐方式
            config.put("policy", Jackson.toJsonPrettyString(policy));

            Response response = CosStsClient.getCredential(config);
            map.put("tmpSecretId",response.credentials.tmpSecretId);
            map.put("tmpSecretKey",response.credentials.tmpSecretKey);
            map.put("sessionToken",response.credentials.sessionToken);
            return map;
        } catch (Exception e) {
            logger.error("获取临时密钥失败: {}", e.getMessage());
            throw new RuntimeException("临时密钥生成失败，请检查配置和权限");
        }
    }

    public String fileUpload(String fileName, InputStream filePath, long contentLength){
        //获取临时令牌
        Map <String,String> map = getTempKey();
        //FILE URL
        String fileUrl = "";

        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例
        // https://cloud.tencent.com/document/product/436/65935#045eef71-40e8-4d68-9ae1-811aeab606d2
        TransferManager transferManager = createTransferManager(map);
        //文件名
        String key =fileName;
        ObjectMetadata objectMetadata = new ObjectMetadata();
        //上传的 InputStream 未指定内容长度，会导致SDK无法优化传输并可能触发服务端拒绝。
        // 上传的流如果能够获取准确的流长度，则推荐一定填写 content-length
        // 如果确实没办法获取到，则下面这行可以省略，但同时高级接口也没办法使用分块上传了
        // 注意：流式上传不支持并发分块上传
        objectMetadata.setContentLength(contentLength);  // 设置内容长度
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, key, filePath, objectMetadata);

        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回 UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
            // 成功：putobjectResult 会返回文件的 etag
            String etag = uploadResult.getETag();
            //https://icebox-foodai-1251531439.cos.ap-chongqing.myqcloud.com/text/text.txt
            //拼接地址
            fileUrl = URL  + fileName;
            logger.info("文件上传结果：{}",etag);
            System.out.println(etag);
            System.out.println(uploadResult.getCiUploadResult());
        } catch (CosServiceException e) {
            //失败，抛出 CosServiceException
            logger.error("服务端错误: 状态码={}, 错误码={}, 请求ID={}, 详情={}",
            e.getStatusCode(), e.getErrorCode(), e.getRequestId(), e.getMessage());
            return null;
        } catch (CosClientException e) {
            //失败，抛出 CosClientException
            logger.error("客户端错误: {}", e.getMessage(), e);
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 确定本进程不再使用 transferManager 实例之后，关闭即可
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);
        return fileUrl;

    }

    // 创建 TransferManager 实例，这个实例用来后续调用高级接口
    TransferManager createTransferManager(Map<String,String>map) {
        // 创建一个 COSClient 实例，这是访问 COS 服务的基础实例。
        // 详细代码参见本页: 简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient(map.get("tmpSecretId"),map.get("tmpSecretKey"),map.get("sessionToken"));

        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);

        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);

        return transferManager;
    }

    // 创建 COSClient 实例，这个实例用来后续调用请求
    COSClient createCOSClient(String tmpSecretId,String tmpSecretKey ,String sessionToken) {
        // 创建 COSClient 实例，这个实例用来后续调用请求

        COSCredentials cred = new BasicSessionCredentials(tmpSecretId, tmpSecretKey, sessionToken);

        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION 请参见 https://cloud.tencent.com/document/product/436/6224
        clientConfig.setRegion(new Region(REGION));

        // 以下的设置，是可选的：

        // 设置 socket 读取超时，默认 30s
        // clientConfig.setSocketTimeout(30*1000);
        // 设置建立连接超时，默认 30s
        // clientConfig.setConnectionTimeout(30*1000);

        // 如果需要的话，设置 http 代理，ip 以及 port
        // clientConfig.setHttpProxyIp("httpProxyIp");
        // clientConfig.setHttpProxyPort(80);

        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);

    }

    void shutdownTransferManager(TransferManager transferManager) {
        // 指定参数为 true, 则同时会关闭 transferManager 内部的 COSClient 实例。
        // 指定参数为 false, 则不会关闭 transferManager 内部的 COSClient 实例。
        transferManager.shutdownNow(true);
    }
}
