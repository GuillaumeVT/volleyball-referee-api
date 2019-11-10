package com.tonkar.volleyballreferee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.domain.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPageImpl<T> implements Slice<T>, Page<T>, Serializable {

    private       long    totalElements;
    private       int     number;
    private       int     size;
    private final List<T> content;

    public TestPageImpl() {
        content = new ArrayList<>();
    }

    @Override
    public int getTotalPages() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).getTotalPages();
    }

    @Override
    public long getTotalElements() {
        return totalElements;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getNumberOfElements() {
        return content.size();
    }

    @Override
    public List<T> getContent() {
        return content;
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
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Override
    public boolean isLast() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).isLast();
    }

    @Override
    public boolean hasNext() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).hasNext();
    }

    @Override
    public boolean hasPrevious() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).hasPrevious();
    }

    @Override
    public Pageable nextPageable() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).nextPageable();
    }

    @Override
    public Pageable previousPageable() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).previousPageable();
    }

    @Override
    public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return null;
    }

    @Override
    public Iterator<T> iterator() {
        return new PageImpl<>(getContent(), PageRequest.of(getNumber(), getSize()), getTotalElements()).iterator();
    }

}