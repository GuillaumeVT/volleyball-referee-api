package com.tonkar.volleyballreferee.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties({ "sort", "pageable" })
public class PageDto<T> implements Page<T> {
    private List<T> content;

    private long    totalElements;
    private int     totalPages;
    private int     number;
    private int     size;
    private int     numberOfElements;
    private boolean first;
    private boolean last;

    public PageDto(List<T> content, Pageable pageable, long total) {
        this.content = content;
        this.number = pageable.isPaged() ? pageable.getPageNumber() : 0;
        this.size = pageable.isPaged() ? pageable.getPageSize() : this.content.size();
        this.totalElements = total;
        this.totalPages = this.size == 0 ? 1 : (int) Math.ceil(this.totalElements / (double) this.size);
        this.numberOfElements = this.content.size();
        this.first = !hasPrevious();
        this.last = !hasNext();
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public Sort getSort() {
        return Sort.unsorted();
    }

    @Override
    public boolean hasNext() {
        return number + 1 < totalPages;
    }

    @Override
    public boolean hasPrevious() {
        return number > 0;
    }

    @Override
    public Pageable nextPageable() {
        return hasNext() ? PageRequest.of(number + 1, size) : Pageable.unpaged();
    }

    @Override
    public Pageable previousPageable() {
        return hasPrevious() ? PageRequest.of(number - 1, size) : Pageable.unpaged();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return new PageDto<>(this.stream().map(converter).collect(Collectors.toList()), PageRequest.of(number, size), totalElements);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }
}
