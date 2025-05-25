package org.socius.sociuswebbackend.services.impl;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.LoginHistoryMapper;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.LoginHistoryEntity;
import org.socius.sociuswebbackend.repositories.LoginHistoryRepository;
import org.socius.sociuswebbackend.services.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    final private LoginHistoryRepository loginHistoryRepository;
    final private LoginHistoryMapper loginHistoryMapper;

    @Override
    public LoginHistoryResponseDto createLoginHistory(LoginHistoryRequestDto requestDto) {
        LoginHistoryEntity entity = loginHistoryMapper.requestDtoToEntity(requestDto);
        entity = loginHistoryRepository.save(entity);
        return loginHistoryMapper.entityToDto(entity);
    }

    @Override
    public List<LoginHistoryResponseDto> getLoginHistoryByUserId(UUID userId) {
        List<LoginHistoryEntity> loginHistories = loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId);
        return loginHistoryMapper.entitiesToDtos(loginHistories);
    }
}
