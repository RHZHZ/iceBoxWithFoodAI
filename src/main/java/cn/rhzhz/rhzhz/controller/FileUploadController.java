package cn.rhzhz.controller;

import cn.rhzhz.DTO.Result;


import cn.rhzhz.utils.TencentOssUnilts;
import cn.rhzhz.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Autowired
    private TencentOssUnilts tencentOssUnilts;


    // 使用示例
//    File targetFile = new File(uploadDir + File.separator + newName);

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        //取文件后缀
        String fileType = originalFileName.substring(originalFileName.lastIndexOf("."));
        String newName = UUID.randomUUID() + fileType;
        //获取用户ID
        Map<String,Object> map = ThreadLocalUtil.get();
        int id = (int) map.get("id");
        //获取时间
        LocalDate time = LocalDate.now();
        // 创建目标目录
        String uploadPath = "/用户"+id+"/"+time +newName;
// 1.本地存储
//
//
//        File targetDir = new File(uploadPath);
//        if (!targetDir.exists()) {
//            targetDir.mkdirs();
//        }
//
//        // 保存文件
//        File targetFile = new File(targetDir, newName);
//        file.transferTo(targetFile);
//        2.OSS存储
        String url = "";
        try {
            long contentLength = file.getSize();
            url = tencentOssUnilts.fileUpload(uploadPath, file.getInputStream(), contentLength);
        } catch (IOException e) {
            return Result.error("OSS服务错误");
        }
        if(url==null)return Result.error("oss服务错误");
        return Result.success(url);
    }


}
