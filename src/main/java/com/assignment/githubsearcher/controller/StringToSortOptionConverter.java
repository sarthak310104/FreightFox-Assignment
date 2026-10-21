package com.assignment.githubsearcher.controller;

import com.assignment.githubsearcher.dto.SortOption;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSortOptionConverter implements Converter<String, SortOption> {

    @Override
    public SortOption convert(String source) { return SortOption.fromValue(source.trim()); }
}