package io.sustc.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.sustc.dto.AuthInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@Service
public class RecommenderService implements io.sustc.service.RecommenderService{

    @Override
    public List<String> recommendNextVideo(String bv) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recommendNextVideo'");
    }

    @Override
    public List<String> generalRecommendations(int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generalRecommendations'");
    }

    @Override
    public List<String> recommendVideosForUser(AuthInfo auth, int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recommendVideosForUser'");
    }

    @Override
    public List<Long> recommendFriends(AuthInfo auth, int pageSize, int pageNum) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'recommendFriends'");
    }
    
}
