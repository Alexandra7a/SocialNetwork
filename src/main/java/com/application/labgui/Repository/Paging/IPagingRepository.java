package com.application.labgui.Repository.Paging;

import com.application.labgui.Domain.Entity;
import com.application.labgui.Repository.Repository;

public interface IPagingRepository<ID , E extends Entity<ID>> extends Repository<ID, E> {

    Page<E> findAll(Pageable pageable);
}
