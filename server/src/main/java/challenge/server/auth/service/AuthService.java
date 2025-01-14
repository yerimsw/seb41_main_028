package challenge.server.auth.service;

import challenge.server.auth.entity.Auth;
import challenge.server.auth.repository.AuthRepository;
import challenge.server.challenge.entity.Challenge;
import challenge.server.challenge.repository.ChallengeRepository;
import challenge.server.challenge.service.ChallengeService;
import challenge.server.exception.BusinessLogicException;
import challenge.server.exception.ExceptionCode;
import challenge.server.file.service.FileUploadService;
import challenge.server.review.entity.Review;
import challenge.server.utils.CustomBeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static challenge.server.challenge.entity.Challenge.Status.SUCCESS;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final AuthRepository authRepository;
    private final ChallengeService challengeService;
    private final CustomBeanUtils<Auth> beanUtils;
    private final FileUploadService fileUploadService;

    @Transactional
    public Auth createAuth(Auth auth, Challenge challenge) {
        auth.setChallenge(challenge);
        challenge.updatePostedAt(LocalDateTime.now());

        // 챌린지 성공 여부 확인
        // TODO: 인증성 발급 방법 논의 필요
        if (challenge.successCheck()) {
            challenge.changeStatus(SUCCESS);
        }

        return authRepository.save(auth);
    }

    @Transactional
    public Auth updateAuth(Auth auth) {
        Auth findAuth = findVerifiedAuth(auth.getAuthId());
        fileUploadService.delete(findAuth.getAuthImageUrl());

        return beanUtils.copyNonNullProperties(auth, findAuth);
    }

    public Auth findAuth(Long authId) {
        return findVerifiedAuth(authId);
    }

    public List<Auth> findAllByChallenge(Long lastAuthId, Long challengeId, int size) {
        return authRepository.findAllByChallengeChallengeId(lastAuthId, challengeId, size);
    }

    public List<Auth> findAllByHabit(Long lastAuthId, Long habitId, int size) {
        return authRepository.findAllByChallengeHabitHabitId(lastAuthId, habitId, size);
    }

    @Transactional
    public void deleteAuth(Long authId) {
        Auth auth = findVerifiedAuth(authId);
        fileUploadService.delete(auth.getAuthImageUrl());
        authRepository.deleteById(authId);
    }

    private Auth findVerifiedAuth(Long authId) {
        return authRepository.findById(authId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.AUTH_NOT_FOUND));
    }
}