package reggie.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reggie.pojo.R;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String path;

    /**
     * 件上传，file名字要与请求发送的文件名字对应
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //收到的file会为一个临时文件，在系统盘临时文件里，请求结束就会删除，需要转存到指定位置
        //path在yml里定义好路径，以后要改路径只用该配置文件，OriginalFilename是图片的原名
//        file.transferTo(new File(path+file.getOriginalFilename()));
        //截取文件的后缀
        String originalFilename = file.getOriginalFilename();
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
        //客户上传的文件很可能重名造成文件覆盖，用UUID生成随机文件名
        String s = UUID.randomUUID().toString()+substring;
        //在转存之前先判断目录是否存在，不造成文件丢失
        File file1 = new File(path);
        if (!file1.exists()){
            file1.mkdirs();
        }
        //转存
        file.transferTo(new File(path+s));
        //随机生成的文件名字需要给页面返回，因为页面需要下载此图片显示
        return R.success(s);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        //创建输入流并指定文件
        FileInputStream fileInputStream=new FileInputStream(new File(path+name));
        //用HttpServletResponse获取servlet的输出流
        ServletOutputStream outputStream=response.getOutputStream();
        //指定输出流的文件格式
        response.setContentType("/image/jpeg");
        int len=0;
        byte[] bytes=new byte[1024];
        while ((len=fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        fileInputStream.close();
        outputStream.close();
    }


}
