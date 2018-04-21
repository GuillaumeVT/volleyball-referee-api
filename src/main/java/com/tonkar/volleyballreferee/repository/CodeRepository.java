package com.tonkar.volleyballreferee.repository;

import com.tonkar.volleyballreferee.model.Code;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CodeRepository extends MongoRepository<Code,String> {

    Code findCodeByDate(long date);

    Code findCodeByCode(int code);

    long deleteCodeByDate(long date);

    long deleteCodeByDateLessThan(long date);
}
