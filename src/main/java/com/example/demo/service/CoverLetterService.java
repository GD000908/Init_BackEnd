package com.example.demo.service;

import com.example.demo.dto.CoverLetterDto;
import com.example.demo.dto.CoverLetterQuestionDto;
import com.example.demo.entity.CoverLetter;
import com.example.demo.entity.CoverLetterQuestion;
import com.example.demo.repository.CoverLetterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;

    @Transactional
    public Long save(CoverLetterDto dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        CoverLetter entity = new CoverLetter();
        entity.setUserId(dto.getUserId());
        entity.setTitle(dto.getTitle());
        entity.setIsDraft(dto.getIsDraft());
        entity.setCompany(dto.getCompany());
        entity.setPosition(dto.getPosition());

        List<CoverLetterQuestion> questions = dto.getQuestions().stream()
                .map(q -> {
                    CoverLetterQuestion question = new CoverLetterQuestion();
                    question.setTitle(q.getTitle());
                    question.setContent(q.getContent());
                    question.setWordLimit(q.getWordLimit());
                    question.setCoverLetter(entity);
                    return question;
                }).collect(Collectors.toList());

        entity.setQuestions(questions);

        CoverLetter saved = coverLetterRepository.save(entity);
        return saved.getId();
    }

    @Transactional
    public CoverLetterDto getById(Long id) {
        CoverLetter entity = coverLetterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 자기소개서가 없습니다. id=" + id));

        return CoverLetterDto.fromEntity(entity);
    }

    public List<CoverLetterDto> getByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }

        List<CoverLetter> coverLetters = coverLetterRepository.findByUserId(userId);
        return coverLetters.stream()
                .map(CoverLetterDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, CoverLetterDto dto) {
        CoverLetter entity = coverLetterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 자기소개서가 없습니다. id=" + id));

        entity.setTitle(dto.getTitle());
        entity.setIsDraft(dto.getIsDraft());
        entity.setCompany(dto.getCompany());
        entity.setPosition(dto.getPosition());

        entity.getQuestions().clear();

        List<CoverLetterQuestion> updatedQuestions = dto.getQuestions().stream()
                .map(q -> {
                    CoverLetterQuestion question = new CoverLetterQuestion();
                    question.setTitle(q.getTitle());
                    question.setContent(q.getContent());
                    question.setWordLimit(q.getWordLimit());
                    question.setDisplayOrder(q.getDisplayOrder());
                    question.setCoverLetter(entity);
                    return question;
                }).collect(Collectors.toList());

        entity.getQuestions().addAll(updatedQuestions);
    }
}
