package org.socius.sociuswebbackend.services.impl;

import java.util.List;
import java.util.UUID;

import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.repositories.TeamRepository;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public List<TeamResponseDto> findAll() {
        List<TeamEntity> teams = teamRepository.findAll();
        return teams.stream()
                .map(teamMapper::entityToDto)
                .toList();
    }

    @Override
    public TeamResponseDto findById(UUID id) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + id));
        return teamMapper.entityToDto(team);
    }

    @Override
    public TeamResponseDto create(TeamRequestDto requestDto) {
        if (teamRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Team đã tồn tại");
        } else {
            TeamEntity team = teamMapper.requestDtoToEntity(requestDto);
            team = teamRepository.save(team);
            return teamMapper.entityToDto(team);
        }
    }

    @Override
    public TeamResponseDto update(UUID id, TeamRequestDto requestDto) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + id));

        if (!team.getName().equals(requestDto.getName()) &&
                teamRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Team với tên này đã tồn tại");
        }

        teamMapper.updateEntityFromDto(requestDto, team);
        team = teamRepository.save(team);
        return teamMapper.entityToDto(team);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy team với ID: " + id);
        }
        teamRepository.deleteById(id);
    }

}
