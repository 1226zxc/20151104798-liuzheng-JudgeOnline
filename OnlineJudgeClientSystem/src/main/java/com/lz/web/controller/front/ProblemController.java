package com.lz.web.controller.front;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.lz.util.BeanMapperUtil;
import com.lz.web.bean.ResponseMap;
import com.lz.web.constant.WebConstant;
import com.lz.web.controller.PageController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.lz.constant.ConstantParameter;
import com.lz.web.dto.ProblemAnswerDTO;
import com.lz.web.exception.ServiceLogicException;
import com.lz.web.po.Problem;
import com.lz.web.po.User;
import com.lz.web.service.front.AnswerSubmitService;
import com.lz.web.service.front.ProblemService;
import com.lz.web.service.page.PageResult;
import com.lz.web.service.page.PageService;
import com.lz.web.vo.request.ProblemAnswerVO;
import com.lz.web.vo.request.ProblemSearchByDifficultyVO;
import com.lz.web.vo.request.ProblemSearchByNameVO;
import com.lz.web.vo.request.ProblemSearchByTypeVO;
import com.lz.web.vo.request.ProblemSearchByValueVO;
import com.lz.web.vo.response.ProblemResponse;

@Controller
@RequestMapping("/ProblemController")
public class ProblemController extends PageController<Problem, Problem, ProblemResponse> {
    @Autowired
    private ProblemService problemService;
    @Autowired
    private AnswerSubmitService answerSubmitService;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMap find(@RequestParam("id") Integer id) {
        ResponseMap responseMap = new ResponseMap().buildSucessResponse();
        responseMap.append("problem", problemService.getProblemById(id));
        return responseMap;
    }

    /**
     * 默认进行题目名字模糊查找
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/searchByName", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<Problem> searchByName(@Valid ProblemSearchByNameVO vo) {
        return problemService.getPageByLikeName(vo.getPageShowCount(), vo.getWantPageNumber(), vo.getProblemName());
    }

    @RequestMapping(value = "/searchByType", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<Problem> searchByType(@Valid ProblemSearchByTypeVO vo) {
        Problem condition = new Problem();
        condition.setIsPublish(true);
        condition.setProblemTypeId(vo.getProblemTypeId());
        return getPageService().getPage(vo.getPageShowCount(), vo.getWantPageNumber(), condition);
    }

    @RequestMapping(value = "/search/{id}", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<Problem> search(@PathVariable Integer id) {
        Problem condition = new Problem();
        condition.setProblemId(id);
        condition.setIsPublish(true);
        return problemService.getPage(1, 1, condition);
    }

    @RequestMapping(value = "/searchByValue", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<Problem> searchByValue(@Valid ProblemSearchByValueVO vo) {
        Problem condition = new Problem();
        condition.setIsPublish(true);
        condition.setProblemValue(vo.getProblemValue());
        return getPageService().getPage(vo.getPageShowCount(), vo.getWantPageNumber(), condition);
    }

    @RequestMapping(value = "/searchByDifficulty", method = RequestMethod.GET)
    @ResponseBody
    public PageResult<Problem> searchByDifficulty(@Valid ProblemSearchByDifficultyVO vo) {
        Problem condition = new Problem();
        condition.setIsPublish(true);
        condition.setProblemDifficulty(vo.getProblemDifficulty());
        return getPageService().getPage(vo.getPageShowCount(), vo.getWantPageNumber(), condition);
    }

    /**
     * 处理提交的代码
     *
     * @param session
     * @param problemAnswerVO
     * @return
     */
    @RequestMapping(value = "/submitAnswer", method = RequestMethod.POST)
    @ResponseBody
    public ResponseMap submitAnswer(HttpSession session, @Valid ProblemAnswerVO problemAnswerVO) {
        Long nextSubmitTime = (Long) session.getAttribute(ConstantParameter.NEXT_SUBMIT_RECORD_TIME);

        // 如果为空，就表明是第一次提交
        if (nextSubmitTime != null) {
            if (nextSubmitTime > System.currentTimeMillis()) {
                throw new ServiceLogicException("请" + TimeUnit.MILLISECONDS.toSeconds(
                        nextSubmitTime - System.currentTimeMillis()) + "秒后再提交代码");
            }
        }

        User user = (User) session.getAttribute(WebConstant.USER_SESSION_ATTRIBUTE_NAME);
        ProblemAnswerDTO dto = BeanMapperUtil.map(problemAnswerVO, ProblemAnswerDTO.class);
        // 设置提交的用户
        dto.setUser(user);
        answerSubmitService.submitAnswer(dto);
        // 5秒后才能允许再一次提交代码
        session.setAttribute(ConstantParameter.NEXT_SUBMIT_RECORD_TIME,
                System.currentTimeMillis() + ConstantParameter.SUBMIT_RECORD_GAP);
        return new ResponseMap().buildSucessResponse();
    }

    @Override
    public PageService<Problem, Problem> getPageService() {
        return problemService;
    }

    @Override
    public Class<ProblemResponse> returnVoClass() {
        return ProblemResponse.class;
    }
}
