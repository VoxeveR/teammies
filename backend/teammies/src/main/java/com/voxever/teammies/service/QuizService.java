package com.voxever.teammies.service;

import com.voxever.teammies.repository.QuizRepository;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    private QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }



    private String createQuiz(){

        return "";
    }

    private String addQuestion(){
        return "";
    }
}
