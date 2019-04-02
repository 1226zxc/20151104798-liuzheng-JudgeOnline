package com.lz.web.controller.front;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.servlet.http.HttpSession;

import com.lz.web.bean.ResponseMap;
import com.lz.web.constant.WebConstant;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.web.po.SubmitRecord;
import com.lz.web.po.User;
import com.lz.web.service.front.SubmitRecordService;
import com.lz.web.service.page.PageResult;
import com.lz.web.vo.request.SubmitRecordVO;

@Controller
@RequestMapping("/SubmitRecordController")
public class SubmitRecordController {
    @Autowired
    private SubmitRecordService submitRecordService;

    @RequestMapping(value = "/downloadFile", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadFile(@RequestParam("filePath") String filePath, @RequestParam("fileType") String fileType) {
        File file = submitRecordService.decodeToFileByFilePath(filePath);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentDispositionFormData("attachment", "in".equals(fileType) ? "input.txt" : "output.txt");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        try {
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<SubmitRecord> list(@RequestParam("pageShowCount") int pageShowCount, @RequestParam("wantPageNumber") int wantPageNumber,
            HttpSession session) {
        User user = (User) session.getAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME);
        return submitRecordService.getPage(pageShowCount, wantPageNumber, user);
    }

    @RequestMapping(value = "/submitDetail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap showSubmitCode(@RequestParam(value = "submitId") BigInteger submitId, @RequestParam(value = "tableName") String tableName) {
        SubmitRecordVO submitDetails = submitRecordService.getSubmitDetails(submitId, tableName);
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("submitDetails", submitDetails);
        return responseMap;
    }

    @RequestMapping(value = "/downloadFileByPath", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadFileByPath(@RequestParam("filePath") String filePath, @RequestParam("fileType") String fileType) {
        File file = submitRecordService.decodeToFileByFilePath(filePath);
        HttpHeaders headers = new HttpHeaders();

        headers.setContentDispositionFormData("attachment", "in".equals(fileType) ? "input.txt" : "output.txt");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        try {
            return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
