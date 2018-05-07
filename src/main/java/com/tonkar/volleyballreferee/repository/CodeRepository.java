package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Code;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CodeRepository extends MongoRepository<Code,String> {

    Code findByDate(long date);

    Code findByCode(int code);

    long deleteByDate(long date);

    long deleteByDateLessThan(long date);
}
