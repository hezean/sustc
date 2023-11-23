package io.sustc.command;

import io.sustc.service.RecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent
@ConditionalOnBean(RecommenderService.class)
public class RecommenderCommand {

    @Autowired
    private RecommenderService recommenderService;

    @ShellMethod("rec general")
    public List<String> generalRecommendations(
            @ShellOption(defaultValue = "1") Integer pageSize,
            @ShellOption(defaultValue = "10") Integer pageNum
    ) {
        return recommenderService.generalRecommendations(pageSize, pageNum);
    }

    @ShellMethod("rec user")
    public List<String> recommendVideosForUser(
            Long mid,
            @ShellOption(defaultValue = "1") Integer pageSize,
            @ShellOption(defaultValue = "10") Integer pageNum
    ) {
        return recommenderService.recommendVideosForUser(mid, pageSize, pageNum);
    }

    @ShellMethod("rec video")
    public List<String> recommendNextVideo(String bv) {
        return recommenderService.recommendNextVideo(bv);
    }

    @ShellMethod("rec friends")
    public List<Long> recommendFriends(
            Long mid,
            @ShellOption(defaultValue = "1") Integer pageSize,
            @ShellOption(defaultValue = "10") Integer pageNum
    ) {
        return recommenderService.recommendFriends(mid, pageSize, pageNum);
    }
}
