package com.px.miniapp.controller;

import com.px.common.R;
import com.px.service.miniapp.QuestionAnswerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 车辆表 前端控制器
 * </p>
 *
 * @author 品讯科技
 * @since 2023-10-26
 */
@RestController
@RequestMapping("/qa")
public class QuestionAnswerController {

    private final QuestionAnswerService questionAnswerService;

    public QuestionAnswerController(QuestionAnswerService questionAnswerService) {
        this.questionAnswerService = questionAnswerService;
    }

    @GetMapping()
    R<?> listQA() {
        return R.success(questionAnswerService.list());
    }
}
